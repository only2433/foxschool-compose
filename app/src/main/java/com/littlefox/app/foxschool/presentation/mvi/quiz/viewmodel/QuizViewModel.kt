package com.littlefox.app.foxschool.presentation.mvi.quiz.viewmodel

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.QuizApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.enumerate.Grade
import com.littlefox.app.foxschool.enumerate.QuizAnswerViewType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorQuizImageNotHaveData
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorRequestData
import com.littlefox.app.foxschool.`object`.data.quiz.ExamplePictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPhonicsTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizStudyRecordData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTypeData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.`object`.result.quiz.QuizInformationResult
import com.littlefox.app.foxschool.`object`.result.quiz.QuizItemResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizAction
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizEvent
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizSideEffect
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizState
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel.Companion
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(private val apiViewModel : QuizApiViewModel): BaseMVIViewModel<QuizState, QuizEvent, SideEffect>(
    QuizState()
)
{
    companion object
    {
        // 퀴즈 효과음 PATH
        private const val MEDIA_EXCELLENT_PATH : String                 = "mp3/quiz_excellent.mp3"
        private const val MEDIA_VERYGOOD_PATH : String                  = "mp3/quiz_verygood.mp3"
        private const val MEDIA_GOODS_PATH : String                     = "mp3/quiz_good.mp3"
        private const val MEDIA_POOL_PATH : String                      = "mp3/quiz_tryagain.mp3"
        private const val MEDIA_CORRECT_PATH : String                   = "mp3/quiz_correct.mp3"
        private const val MEDIA_INCORRECT_PATH : String                 = "mp3/quiz_incorrect.mp3"

        private val EXAMPLE_INDEX_SOUND_LIST = arrayOf(
            "https://cdn.littlefox.co.kr/contents/quiz/data/a.mp3",
            "https://cdn.littlefox.co.kr/contents/quiz/data/b.mp3",
            "https://cdn.littlefox.co.kr/contents/quiz/data/c.mp3",
            "https://cdn.littlefox.co.kr/contents/quiz/data/d.mp3"
        )

        private val EXAMPLE_EXCEPTION_INDEX_SOUND_LIST = arrayOf(
            "http://cdn.littlefox.co.kr/contents/quiz/data/a.mp3",
            "http://cdn.littlefox.co.kr/contents/quiz/data/b.mp3",
            "http://cdn.littlefox.co.kr/contents/quiz/data/c.mp3",
            "http://cdn.littlefox.co.kr/contents/quiz/data/d.mp3"
        )

        private const val QUIZ_INTRO : Int                              = 0
        private const val QUIZ_IMAGE_WIDTH : Int                        = 479
        private const val QUIZ_IMAGE_HEIGHT : Int                       = 361

        private const val PLAY_INIT : Int                               = 0
        private const val PLAY_REPLAY : Int                             = 1
    }

    private lateinit var mContext : Context

    private var mQuizPassagePlayer : MediaPlayer? = null
    private var mQuizEffectPlayer : MediaPlayer? = null

    private var mQuizRequestObject : QuizStudyRecordData? = null
    private lateinit var mQuizUserSelectObjectList : ArrayList<QuizUserInteractionData>
    private var mQuizInformationResult : QuizInformationResult? = null
    private var mQuizItemResultList : ArrayList<QuizItemResult>? = null

    private var mTextInformationList : ArrayList<QuizTextData> = ArrayList<QuizTextData>()
    private var mPhonicsTextInformationList : ArrayList<QuizPhonicsTextData> = ArrayList<QuizPhonicsTextData>()

    private lateinit var mQuizIntentParamsObject : QuizIntentParamsObject

    private var mQuizPlayingCount : Int     = -1
    private var mCorrectAnswerCount : Int   = 0

    /**
     * 현재 퀴즈페이지의 정보 (Index, Type)
     */
    private var mCurrentQuizPageIndex : Int = -1
    private var mCurrentQuizType : String   = ""

    /**
     * 퀴즈 제한시간
     */
    private var mQuizLimitTime : Int = 0
    private var mAudioAttributes : AudioAttributes? = null
    private var mMainExampleSoundIndex : Int = 0
    private var mQuizPlayTimerJob: Job? = null
    private var mCurrentPageMaxCount = 0

    override fun init(context : Context)
    {
        mContext = context
        mQuizIntentParamsObject = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_QUIZ_PARAMS)!!
        mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()

        mQuizUserSelectObjectList = ArrayList()
        onHandleApiObserver()
        requestQuizInformationAsync()
    }

    override fun resume()
    {
        Log.f("")
        if(mCurrentQuizPageIndex != -1 &&
            mCurrentQuizPageIndex != mQuizPlayingCount)
        {
            enableTimer(true)
        }
    }

    override fun pause()
    {
        Log.f("")
        if(mCurrentQuizPageIndex != -1 &&
            mCurrentQuizPageIndex != mQuizPlayingCount)
        {
            enableTimer(false)
        }

        stopEffectPlay()
        stopMediaPlay()
    }

    override fun destroy()
    {
        Log.f("")
        releaseMediaPlay()
        releaseEffectPlay()
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect{ data ->
                    data?.let {
                        if(data.first == RequestCode.CODE_QUIZ_RECORD_SAVE)
                        {
                            postSideEffect(
                                SideEffect.EnableLoading(isLoading = data.second)
                            )
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.quizInformationData.collect{ data ->
                    data?.let {
                        mQuizInformationResult = data

                        postEvent(
                            QuizEvent.SetTitle(
                                mQuizInformationResult!!.getTitle(),
                                mQuizInformationResult!!.getSubTitle()
                            )
                        )

                        mCurrentQuizType = mQuizInformationResult!!.getType()
                        mQuizItemResultList = mQuizInformationResult!!.getQuestionItemInformationList()
                        mQuizPlayingCount = mQuizInformationResult!!.getQuizCount()

                        Log.f("Content Quiz Id : " + mQuizInformationResult!!.getContentsId() + ", type : " + mCurrentQuizType)

                        if(mCurrentQuizType == Common.QUIZ_CODE_TEXT && mQuizItemResultList!![0].getSoundUrl() != "")
                        {
                            mCurrentQuizType = Common.QUIZ_CODE_SOUND_TEXT
                        }
                        when(mCurrentQuizType)
                        {
                            Common.QUIZ_CODE_PICTURE ->
                            {
                                requestDownloadFileAsync()
                                Log.f("Image Question - " + mQuizInformationResult!!.getTitle())
                            }
                            Common.QUIZ_CODE_TEXT ->
                            {
                                makeTextQuestion(PLAY_INIT)
                                Log.f("Text Question - " + mQuizInformationResult!!.getTitle())
                            }
                            Common.QUIZ_CODE_PHONICS_SOUND_TEXT ->
                            {
                                makePhonicsTextQuestion(PLAY_INIT)
                                Log.f("Main Sound Question - " + mQuizInformationResult!!.getTitle())
                            }
                            Common.QUIZ_CODE_SOUND_TEXT ->
                            {
                                makeTextQuestion(PLAY_INIT)
                                Log.f("Main Example Text Question - " + mQuizInformationResult!!.getTitle())
                            }
                        }

                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.downloadQuizResource.collect{ data ->
                    data?.let {
                        viewModelScope.launch(Dispatchers.Main) {
                            makePictureQuestion(PLAY_INIT)
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.quizSaveRecordData.collect{ data ->
                    data?.let {
                        postSideEffect(
                            QuizSideEffect.ShowWarningMessageDialog(
                                mContext.resources.getString(R.string.message_quiz_save_record_success)
                            )
                        )
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.errorReport.collect{ data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        if(result.isDuplicateLogin)
                        {
                            //중복 로그인 시 재시작
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initAutoIntroSequence()
                            }
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initScene()
                            }
                        }
                        else
                        {
                            if(code == RequestCode.CODE_QUIZ_INFORMATION ||
                                code == RequestCode.CODE_DOWNLOAD_QUIZ_RESOURCE )
                            {
                                Log.f("FAIL code : $code")
                                postSideEffect(
                                    SideEffect.ShowToast(result.message)
                                )
                                viewModelScope.launch(Dispatchers.Main) {
                                    withContext(Dispatchers.IO){
                                        delay(Common.DURATION_LONG)
                                    }
                                    (mContext as Activity).finish()
                                }
                                if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                                {
                                    val data = ErrorRequestData(
                                        CrashlyticsHelper.ERROR_CODE_QUIZ_REQUEST,
                                        mQuizIntentParamsObject!!.getContentID(),
                                        result.status,
                                        result.message,
                                        Exception()
                                    )
                                    CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
                                }
                            }
                            else if(code == RequestCode.CODE_QUIZ_RECORD_SAVE)
                            {
                                Log.f("FAIL ASYNC_CODE_QUIZ_SAVE_RECORD")
                                postEvent(
                                    QuizEvent.EnableSaveButton(true),
                                )
                                postSideEffect(
                                    QuizSideEffect.ShowWarningMessageDialog(
                                        result.message
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onHandleAction(action : Action)
    {
       when(action)
       {
           is QuizAction.PageSelected ->
           {
               onQuizPageSelected();
           }
           is QuizAction.ClickNextQuiz ->
           {
               onGoNext();
           }
           is QuizAction.ClickSaveStudyInformation ->
           {
               onSaveStudyInformation();
           }
           is QuizAction.ClickReplay ->
           {
               onGoReplay();
           }
           is QuizAction.ClickQuizPlaySound ->
           {
               onPlaySound();
           }
           is QuizAction.SelectUserAnswer ->
           {
               onChoiceItem(action.data);
           }
       }
    }

    override suspend fun reduceState(current : QuizState, event : QuizEvent) : QuizState
    {
        return when(event)
        {
            is QuizEvent.LoadingComplete ->
            {
                current.copy(
                    isLoadingComplete = event.isComplete
                )
            }
            is QuizEvent.SetTitle ->
            {
                current.copy(
                    title = event.title
                )
            }
            is QuizEvent.SetQuizPlayData ->
            {
                current.copy(
                    quizPlayData = event.data
                )
            }
            is QuizEvent.EnableTaskBox ->
            {
                current.copy(
                    showTaskBox = event.isShow
                )
            }
            is QuizEvent.SetViewPageCount ->
            {
                current.copy(
                    viewPageCount = event.size
                )
            }
            is QuizEvent.NotifyAnswerViewType ->
            {
                current.copy(
                    answerViewType = event.type
                )
            }
            is QuizEvent.UpdateCorrectAnswerText ->
            {
                current.copy(
                    answerCorrectText = event.answerText
                )
            }
            is QuizEvent.SetCurrentPage ->
            {
                current.copy(
                    currentPage = event.page
                )
            }
            is QuizEvent.SetResultData ->
            {
                current.copy(
                    resultData = event.data
                )
            }
            is QuizEvent.UpdatePlayTime ->
            {
                current.copy(
                    playTime = event.time
                )
            }
            is QuizEvent.ForceChangePage ->
            {
                current.copy(
                    forceChangePage = event.page
                )
            }
            is QuizEvent.EnableSaveButton ->
            {
                current.copy(
                    enableSaveButton = event.isEnable
                )
            }
        }
    }

    private fun requestQuizInformationAsync()
    {
        Log.f("mContentID : ${mQuizIntentParamsObject.getContentID()}")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_QUIZ_INFORMATION,
            mQuizIntentParamsObject.getContentID()
        )
    }

    private fun requestDownloadFileAsync()
    {
        val downloadUrlList = ArrayList<String>()
        val fileSavePathList = ArrayList<String>()
        downloadUrlList.add(mQuizInformationResult!!.getCorrectImageUrl())
        fileSavePathList.add(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getCorrectImageFileName())
        if(mQuizInformationResult!!.getInCorrectImageUrl() != "")
        {
            downloadUrlList.add(mQuizInformationResult!!.getInCorrectImageUrl())
            fileSavePathList.add(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getInCorrectImageFileName())
            Log.f("InCorrect URL : " + mQuizInformationResult!!.getInCorrectImageUrl())
        }
        Log.f("Correct URL : " + mQuizInformationResult!!.getCorrectImageUrl())

        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_DOWNLOAD_QUIZ_RESOURCE,
            downloadUrlList,
            fileSavePathList
        )
    }


    private fun requestQuizSaveRecord()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_QUIZ_RECORD_SAVE,
            mQuizRequestObject,
            mQuizIntentParamsObject.getHomeworkNumber()
        )
    }

    /** ========== 사운드(효과음) 재생 ========== */
    /**
     * 효과음 재생 (정오답, 퀴즈결과)
     */
    private fun playEffectSound(type : String)
    {
        Log.f("type : $type")
        val afd : AssetFileDescriptor

        if(mQuizEffectPlayer != null)
        {
            mQuizEffectPlayer!!.reset()
        }
        else
        {
            mQuizEffectPlayer = MediaPlayer()
        }

        try
        {
            afd = mContext.assets.openFd(type)
            mQuizEffectPlayer!!.run {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }
            afd.close()
        } catch(e : IOException)
        {
            e.printStackTrace()
        }
    }

    /**
     * 퀴즈 결과 효과음 재생 (맞춘 개수에 따라)
     */
    private fun playResultByQuizCorrect()
    {
        when(CommonUtils.getInstance(mContext).getMyGrade(mQuizPlayingCount, mCorrectAnswerCount))
        {
            Grade.EXCELLENT -> playEffectSound(MEDIA_EXCELLENT_PATH)
            Grade.VERYGOOD -> playEffectSound(MEDIA_VERYGOOD_PATH)
            Grade.GOODS -> playEffectSound(MEDIA_GOODS_PATH)
            Grade.POOL -> playEffectSound(MEDIA_POOL_PATH)
        }
    }

    /**
     * 미디어(효과음) 플레이어 정지
     */
    private fun stopEffectPlay()
    {
        mQuizEffectPlayer?.stop()
    }

    /**
     * 미디어(효과음) 플레이어 제거
     */
    private fun releaseEffectPlay()
    {
        mQuizEffectPlayer?.stop()
        mQuizEffectPlayer?.release()
        mQuizEffectPlayer = null
    }
    /** ========== 사운드(효과음) 재생 end ========== */

    /** ========== 사운드(퀴즈) 재생 ========== */
    /**
     * 미디어(퀴즈) 재생
     */
    private fun playMediaPlay(uri : String)
    {
        Log.f("uri : $uri")
        if(mQuizPassagePlayer != null)
        {
            mQuizPassagePlayer!!.reset()
        }
        else
        {
            mQuizPassagePlayer = MediaPlayer()
        }

        try
        {
            mQuizPassagePlayer!!.run {
                setDataSource(mContext, Uri.parse(uri))
                setAudioAttributes(mAudioAttributes)
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }

        } catch(e : java.lang.Exception)
        {
            Log.f("Exception : " + e.message)
            e.printStackTrace()
        }
    }

    /**
     * 미디어(퀴즈) 재생
     */
    private fun playMediaPlay(uriList : ArrayList<String>)
    {
        Log.f("")
        if(mQuizPassagePlayer != null)
        {
            mQuizPassagePlayer!!.reset()
        }
        else
        {
            mQuizPassagePlayer = MediaPlayer()
        }

        try
        {
            Log.f(
                "play Index : " + mMainExampleSoundIndex + ", url : " + uriList[mMainExampleSoundIndex] + ", size : " + uriList.size
            )
            mQuizPassagePlayer!!.run {
                setDataSource(mContext, Uri.parse(uriList[mMainExampleSoundIndex]))
                setAudioAttributes(mAudioAttributes)
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }

        }
        catch(e : java.lang.Exception)
        {
            Log.f("Exception : " + e.message)
            e.printStackTrace()
        }

        mQuizPassagePlayer?.setOnCompletionListener {
            mMainExampleSoundIndex++
            if(mMainExampleSoundIndex < uriList.size)
            {
                playMediaPlay(uriList)
            }
        }
    }

    /**
     * 미디어(퀴즈) 플레이어 정지
     */
    private fun stopMediaPlay()
    {
        mQuizPassagePlayer?.stop()
    }

    /**
     * 미디어(퀴즈) 플레이어 제거
     */
    private fun releaseMediaPlay()
    {
        mQuizPassagePlayer?.stop()
        mQuizPassagePlayer?.release()
        mQuizPassagePlayer = null
    }
    /** ========== 사운드(퀴즈) 재생 end ========== */


    /** ========== 퀴즈 데이터 생성 ========== */
    /**
     * PlayFragment에 전달할 이미지 문제를 만드는 메소드. 각각의 Play Fragment는 하나의 문제로 간주한다.
     * @param type PLAY_INIT : 퀴즈 시작(맨처음) , PLAY_REPLAY : 퀴즈 재시작
     */
    private fun makePictureQuestion(type : Int)
    {
        Log.f("type : $type")
        val maxQuestionCount = mQuizInformationResult!!.getQuizCount()
        var correctQuestionIndex = -1
        var randImageIndex = -1
        var mQuizPictureData : QuizPictureData
        val mPictureQuizList : ArrayList<QuizPictureData> = ArrayList<QuizPictureData>()
        mQuizItemResultList?.shuffle(Random(System.currentTimeMillis()))
        val mCorrectImageList = ArrayList<Bitmap>()
        try
        {
            var correctBitmap =
                BitmapFactory.decodeFile(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getCorrectImageFileName())
            for(i in mQuizItemResultList!!.indices)
            {
                var itemBitmap : Bitmap = Bitmap.createBitmap(
                    correctBitmap,
                    0,
                    i * QUIZ_IMAGE_HEIGHT,
                    QUIZ_IMAGE_WIDTH,
                    QUIZ_IMAGE_HEIGHT
                )
                mCorrectImageList.add(itemBitmap)
            }
            correctBitmap.recycle()
        } catch(e : java.lang.Exception)
        {
            if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
            {
                val data = ErrorQuizImageNotHaveData(
                    mQuizInformationResult!!.getContentsId(),
                    java.lang.String.valueOf(mQuizInformationResult!!.getQuizId()),
                    mQuizInformationResult!!.getTitle(),
                    mQuizInformationResult!!.getSubTitle(),
                    mQuizInformationResult!!.getCorrectImageUrl(),
                    "",
                    e
                )
                CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
            }
        }

        if(mQuizInformationResult!!.getInCorrectImageUrl() == "")
        {
            Log.f("maxQuestionCount : $maxQuestionCount")
            for(i in 0 until maxQuestionCount)
            {
                correctQuestionIndex = mQuizItemResultList!![i].getCorrectIndex()
                randImageIndex = CommonUtils.getInstance(mContext).getRandomNumber(maxQuestionCount, correctQuestionIndex)
                mQuizPictureData = QuizPictureData(
                    i,
                    mQuizItemResultList!![i].getTitle(),
                    ExamplePictureData(correctQuestionIndex, mCorrectImageList[correctQuestionIndex]),
                    ExamplePictureData(randImageIndex, mCorrectImageList[randImageIndex])
                )
                mQuizPictureData.setRecordQuizValue(correctQuestionIndex, randImageIndex)
                mQuizPictureData.shuffle()
                mPictureQuizList.add(mQuizPictureData)

                Log.f("Position : " + i + "CorrectIndex : " + correctQuestionIndex + ", randImageIndex : " + randImageIndex)
                Log.f("title : " + mQuizItemResultList!![i].getTitle())
            }
        }
        else
        {
            try
            {
                var incorrectBitmap =
                    BitmapFactory.decodeFile(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getInCorrectImageFileName())

                for(i in 0 until maxQuestionCount)
                {
                    correctQuestionIndex = mQuizItemResultList!![i].getCorrectIndex()

                    var incorrectPieceBitmap : Bitmap = Bitmap.createBitmap(
                        incorrectBitmap,
                        0,
                        correctQuestionIndex * QUIZ_IMAGE_HEIGHT,
                        QUIZ_IMAGE_WIDTH,
                        QUIZ_IMAGE_HEIGHT
                    )
                    mQuizPictureData = QuizPictureData(
                        i,
                        mQuizItemResultList!![i].getTitle(),
                        ExamplePictureData(
                            correctQuestionIndex,
                            mCorrectImageList[correctQuestionIndex]
                        ),
                        ExamplePictureData(correctQuestionIndex, incorrectPieceBitmap)
                    )
                    mQuizPictureData.setRecordQuizValue(correctQuestionIndex, correctQuestionIndex)
                    mQuizPictureData.shuffle()
                    mPictureQuizList.add(mQuizPictureData)
                    Log.f("Position : " + i + "CorrectIndex : " + correctQuestionIndex + ", randImageIndex : " + randImageIndex)
                    Log.f("title : " + mQuizItemResultList!![i].getTitle())

                }
                incorrectBitmap.recycle()

            } catch(e : java.lang.Exception)
            {
                if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                {
                    val data = ErrorQuizImageNotHaveData(
                        mQuizInformationResult!!.getContentsId(),
                        java.lang.String.valueOf(mQuizInformationResult!!.getQuizId()),
                        mQuizInformationResult!!.getTitle(),
                        mQuizInformationResult!!.getSubTitle(),
                        mQuizInformationResult!!.getCorrectImageUrl(),
                        mQuizInformationResult!!.getInCorrectImageUrl(),
                        e
                    )
                    CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
                }
            }
        } // BITMAP RECYCLE

        mCorrectImageList.clear()


        mCurrentPageMaxCount = mPictureQuizList.size + 2
        postEvent(
            QuizEvent.SetQuizPlayData(
                QuizTypeData.Picture(mPictureQuizList)
            ),
            QuizEvent.SetViewPageCount(mCurrentPageMaxCount)
        )

        when(type)
        {
            PLAY_INIT -> readyToPlay()
            PLAY_REPLAY -> readyToReplay()
        }
    }

    /**
     * PlayFragment에 전달할 텍스트 문제를 만드는 메소드. 각각의 Play Fragment는 하나의 문제로 간주한다.
     * @param type PLAY_INIT : 퀴즈 시작(맨처음) , PLAY_REPLAY : 퀴즈 재시작
     */
    private fun makeTextQuestion(type : Int)
    {
        Log.f("type : $type")
        val maxQuestionCount = mQuizInformationResult!!.getQuizCount()
        mTextInformationList = ArrayList()
        mQuizItemResultList?.shuffle(Random(System.currentTimeMillis()))

        for(i in 0 until maxQuestionCount)
        {
            mTextInformationList.add(
                QuizTextData(
                    i, mQuizItemResultList!![i].getQuestionIndex(), mQuizItemResultList!![i]
                )
            )
        }
        mCurrentPageMaxCount = mTextInformationList.size + 2

        postEvent(
            QuizEvent.SetQuizPlayData(
                if(mCurrentQuizType == Common.QUIZ_CODE_SOUND_TEXT) {
                    QuizTypeData.SoundText(mTextInformationList)
                }
                else
                {
                    QuizTypeData.Text(mTextInformationList)
                }
            ),
            QuizEvent.SetViewPageCount(mCurrentPageMaxCount)
        )
        when(type)
        {
            PLAY_INIT -> readyToPlay()
            PLAY_REPLAY -> readyToReplay()
        }
    }

    /**
     * PlayFragment에 전달할 파닉스 문제를 만드는 메소드. 각각의 Play Fragment는 하나의 문제로 간주한다.
     * @param type PLAY_INIT : 퀴즈 시작(맨처음) , PLAY_REPLAY : 퀴즈 재시작
     */
    private fun makePhonicsTextQuestion(type : Int)
    {
        Log.f("type : $type")
        val maxQuestionCount = mQuizInformationResult!!.getQuizCount()
        mPhonicsTextInformationList = ArrayList()
        mQuizItemResultList?.shuffle(Random(System.nanoTime()))

        for(i in 0 until maxQuestionCount)
        {
            mPhonicsTextInformationList.add(QuizPhonicsTextData(i, mQuizItemResultList!!))
        }

        mCurrentPageMaxCount = mPhonicsTextInformationList.size + 2
        postEvent(
            QuizEvent.SetQuizPlayData(
                QuizTypeData.Phonics(mPhonicsTextInformationList)
            ),
            QuizEvent.SetViewPageCount(mCurrentPageMaxCount)
        )
        when(type)
        {
            PLAY_INIT -> readyToPlay()
            PLAY_REPLAY -> readyToReplay()
        }
    }


    /**
     * 퀴즈 시작 준비
     */
    private fun readyToPlay()
    {
        postEvent(
            QuizEvent.LoadingComplete(true)
        )
        initTaskBoxInformation()
    }

    /**
     * 퀴즈 재시작 준비
     */
    private fun readyToReplay()
    {
        stopEffectPlay()
        initTaskBoxInformation()
        setQuizPlayStatus()
    }

    /**
     * 문제 음성 플레이
     */
    private fun playQuestionSound(index : Int)
    {
        Log.i("playQuestionSound index : $index, list size : ${mQuizItemResultList?.size}")
        if(mCurrentQuizType == Common.QUIZ_CODE_PICTURE || mCurrentQuizType == Common.QUIZ_CODE_PHONICS_SOUND_TEXT)
        {
            playMediaPlay(mQuizItemResultList!![index].getSoundUrl())
        }
        else if(mCurrentQuizType == Common.QUIZ_CODE_SOUND_TEXT)
        {
            mMainExampleSoundIndex = 0
            playMediaPlay(getCurrentQuizMainExampleSoundList(index))
        }
    }

    private fun getCurrentQuizMainExampleSoundList(index : Int) : ArrayList<String>
    {
        val result = ArrayList<String>()
        result.add(mTextInformationList[index].getMainSoundUrl())
        val isHttpsUseModel = mTextInformationList[index].getMainSoundUrl().contains("https")
        for(i in 0 until mTextInformationList[index].getExampleList().size)
        {
            if(isHttpsUseModel)
            {
                result.add(EXAMPLE_INDEX_SOUND_LIST[i])
            }
            else
            {
                result.add(EXAMPLE_EXCEPTION_INDEX_SOUND_LIST[i])
            }
            result.add(mTextInformationList[index].getExampleList()[i]!!.getExampleSoundUrl())
            Log.i(
                "index :$i, url : " + mTextInformationList[index].getExampleList()[i]!!.getExampleSoundUrl()
            )
        }
        return result
    }

    /**
     * 상단 영역 설정 (타이머, 문제수)
     */
    private fun initTaskBoxInformation()
    {
        try
        {
            mCorrectAnswerCount = 0
            mQuizLimitTime = mQuizInformationResult!!.getTimeLimit()
            postEvent(
                QuizEvent.UpdatePlayTime(
                    CommonUtils.getInstance(mContext).getSecondTime(mQuizLimitTime)
                ),
                QuizEvent.UpdateCorrectAnswerText(
                    "$mCorrectAnswerCount/$mQuizPlayingCount"
                )
            )
        } catch(e : NullPointerException) { }
    }

    /**
     * 퀴즈 설정
     */
    private fun setQuizPlayStatus()
    {
        mCurrentQuizPageIndex++
        Log.f("Quiz Page : $mCurrentQuizPageIndex , Total Quiz Count : $mQuizPlayingCount, Correct Count : $mCorrectAnswerCount")

        if(mCurrentQuizPageIndex == 0)
        {
            mQuizUserSelectObjectList.clear()
            enableTimer(true)
        }
        else if(mCurrentQuizPageIndex == mQuizPlayingCount)
        {
            Log.i("-------- 정답 보내기 --------")
            enableTimer(false)
            postEvent(
                QuizEvent.EnableTaskBox(false),
                QuizEvent.SetResultData("$mQuizPlayingCount:$mCorrectAnswerCount")
            )
            viewModelScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO){
                    delay(Common.DURATION_NORMAL)
                }
                playResultByQuizCorrect()
            }
        }
        postEvent(
            QuizEvent.NotifyAnswerViewType(
                QuizAnswerViewType.HIDE
            ),
            QuizEvent.SetCurrentPage(
                mCurrentQuizPageIndex + 1
            )
        )
    }

    private fun enableTimer(isStart: Boolean)
    {
        if(isStart)
        {
            mQuizPlayTimerJob = viewModelScope.launch(Dispatchers.Default) {
                while(true)
                {
                    withContext(Dispatchers.IO){
                        delay(Common.DURATION_LONG)
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        setQuestionTaskInformation()
                    }
                }
            }
        }
        else
        {
            mQuizPlayTimerJob?.cancel()
            mQuizPlayTimerJob = null
        }
    }

    /**
     * 퀴즈의 현재 상태를 계속 표시해주는 메소드 (남은시간, 몇개 맞췃는지)
     */
    private fun setQuestionTaskInformation()
    {
        mQuizLimitTime--
        if(mQuizLimitTime >= 0)
        {
            postEvent(
                QuizEvent.UpdatePlayTime(
                    CommonUtils.getInstance(mContext).getSecondTime(mQuizLimitTime)
                )
            )
        }
        else
        {
            Log.f("Quiz End Not All Solved. Limit Time")
            mCurrentQuizPageIndex = mQuizPlayingCount
            stopMediaPlay()
            enableTimer(false)
            postEvent(
                QuizEvent.EnableTaskBox(false),
                QuizEvent.NotifyAnswerViewType(
                    QuizAnswerViewType.HIDE
                ),
                QuizEvent.SetResultData(
                    "$mQuizPlayingCount:$mCorrectAnswerCount"
                ),
                QuizEvent.SetCurrentPage(
                    mCurrentPageMaxCount - 1
                )
            )
            viewModelScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO){
                    delay(Common.DURATION_NORMAL)
                }
                playResultByQuizCorrect()
            }
        }
    }


    /**
     * O, X카드 표시
     */
    private fun showAnswerAnimation(isCorrect : Boolean)
    {
        if(isCorrect)
        {
            playEffectSound(MEDIA_CORRECT_PATH)
            postEvent(
                QuizEvent.NotifyAnswerViewType(
                    QuizAnswerViewType.SHOW_CORRECT
                )
            )
        } else
        {
            playEffectSound(MEDIA_INCORRECT_PATH)
            postEvent(
                QuizEvent.NotifyAnswerViewType(
                    QuizAnswerViewType.SHOW_INCORRECT
                )
            )
        }
    }

    private fun onQuizPageSelected()
    {
        Log.f("quizPageIndex : $mCurrentQuizPageIndex, mQuizPlayingCount : $mQuizPlayingCount")
        if(mCurrentQuizPageIndex == -1)
        {
            postEvent(
                QuizEvent.EnableTaskBox(false)
            )
            releaseMediaPlay()
            releaseEffectPlay()
            return
        }
        else
        {
            if(mCurrentQuizPageIndex != mQuizPlayingCount)
            {
                postEvent(
                    QuizEvent.EnableTaskBox(true) // 상단 영역 표시 (타이머, 정답수)
                )
                when(mCurrentQuizType)
                {
                    Common.QUIZ_CODE_PICTURE,
                    Common.QUIZ_CODE_PHONICS_SOUND_TEXT,
                    Common.QUIZ_CODE_SOUND_TEXT ->
                    {
                        viewModelScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_NORMAL)
                            }
                            playQuestionSound(mCurrentQuizPageIndex)
                        }
                    }
                }
            }
        }
    }


    /**
     * QuizPlayFragment에서 Sound 버튼 클릭 시
     */
    fun onPlaySound()
    {
        Log.f("index : $mCurrentQuizPageIndex")
        playQuestionSound(mCurrentQuizPageIndex)
    }

    /**
     * QuizPlayFragment에서 사용자가 보기 중에 선택 했을 때
     */
    private fun onChoiceItem(data : QuizUserInteractionData)
    {
        Log.i("")
        mQuizUserSelectObjectList.add(data)
        mCorrectAnswerCount = if(data.isCorrect()) mCorrectAnswerCount + 1 else mCorrectAnswerCount
        postEvent(
            QuizEvent.UpdateCorrectAnswerText(
                "$mCorrectAnswerCount/$mQuizPlayingCount"
            )
        )
        showAnswerAnimation(data.isCorrect())
    }


    /**
     * QuizResultFragment에서 퀴즈 저장 버튼 호출 시
     */
    private fun onSaveStudyInformation()
    {
        if(mQuizLimitTime <= 0)
        {
            // 풀이시간 초과는 저장불가
            postSideEffect(
                QuizSideEffect.ShowWarningMessageDialog(
                    mContext.resources.getString(R.string.message_quiz_limit_not_save)
                )
            )
        }
        else
        {
            mQuizRequestObject = QuizStudyRecordData(mQuizIntentParamsObject.getContentID(), mQuizUserSelectObjectList)
            requestQuizSaveRecord()
        }
    }

    /**
     * QuizPlayFragment에서 다음 버튼 클릭 시
     */
    private fun onGoNext()
    {
        Log.f("")
        stopMediaPlay()
        setQuizPlayStatus()
    }


    /**
     * QuizResultFragment에서 Replay 버튼 클릭 시
     */
    private fun onGoReplay()
    {
        Log.f("mCurrentQuizType : $mCurrentQuizType")
        when(mCurrentQuizType)
        {
            Common.QUIZ_CODE_PICTURE ->
                makePictureQuestion(PLAY_REPLAY)
            Common.QUIZ_CODE_TEXT,
            Common.QUIZ_CODE_SOUND_TEXT ->
                makeTextQuestion(PLAY_REPLAY)
            Common.QUIZ_CODE_PHONICS_SOUND_TEXT ->
                makePhonicsTextQuestion(PLAY_REPLAY)
        }
    }


}