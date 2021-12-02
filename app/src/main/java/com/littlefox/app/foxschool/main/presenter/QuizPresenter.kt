package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorQuizImageNotHaveData
import com.littlefox.app.foxschool.`object`.data.crashtics.ErrorRequestData
import com.littlefox.app.foxschool.`object`.data.quiz.*
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.base.QuizBaseObject
import com.littlefox.app.foxschool.`object`.result.quiz.QuizInformationResult
import com.littlefox.app.foxschool.`object`.result.quiz.QuizItemResult
import com.littlefox.app.foxschool.adapter.QuizSelectionPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.FileDownloadCoroutine
import com.littlefox.app.foxschool.coroutine.QuizInformationRequestCoroutine
import com.littlefox.app.foxschool.coroutine.QuizSaveRecordCoroutine
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.Grade
import com.littlefox.app.foxschool.enumerate.QuizStatus
import com.littlefox.app.foxschool.fragment.QuizIntroFragment
import com.littlefox.app.foxschool.fragment.QuizResultFragment
import com.littlefox.app.foxschool.main.contract.QuizContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.QuizFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.QuizPresenterDataObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.io.File
import java.io.IOException
import java.util.*

class QuizPresenter : QuizContract.Presenter
{
    companion object
    {
        private const val MESSAGE_REQUEST_FILE_DOWNLOAD : Int           = 100
        private const val MESSAGE_FILE_DOWNLOAD_ERROR : Int             = 101
        private const val MESSAGE_REFRESH_TASK_BOX : Int                = 102
        private const val MESSAGE_PICTURE_QUESTION_SETTING : Int        = 103
        private const val MESSAGE_TEXT_QUESTION_SETTING : Int           = 104
        private const val MESSAGE_PHONICS_TEXT_QUESTION_SETTING : Int   = 105
        private const val MESSAGE_READY_TO_QUIZ : Int                   = 106
        private const val MESSAGE_READY_TO_REPLAY : Int                 = 107
        private const val MESSAGE_PLAY_SOUND_QUIZ : Int                 = 108
        private const val MESSAGE_FINISH : Int                          = 109
        private const val MESSAGE_QUIZ_RESULT_SOUND : Int               = 110

        // 퀴즈 효과음 PATH
        private const val MEDIA_EXCELLENT_PATH : String                 = "mp3/quiz_excellent.mp3"
        private const val MEDIA_VERYGOOD_PATH : String                  = "mp3/quiz_verygood.mp3"
        private const val MEDIA_GOODS_PATH : String                     = "mp3/quiz_good.mp3"
        private const val MEDIA_POOL_PATH : String                      = "mp3/quiz_tryagain.mp3"
        private const val MEDIA_CORRECT_PATH : String                   = "mp3/quiz_correct.mp3"
        private const val MEDIA_INCORRECT_PATH : String                 = "mp3/quiz_incorrect.mp3"

        private const val DURATION_VIEW_INIT : Int                      = 1000
        private const val DURATION_TIMER : Int                          = 1000
        private const val DURATION_ANIMATION : Int                      = 500

        private const val PLAY_INIT : Int                               = 0
        private const val PLAY_REPLAY : Int                             = 1

        private const val QUIZ_INTRO : Int                              = 0
        private const val QUIZ_IMAGE_WIDTH : Int                        = 479
        private const val QUIZ_IMAGE_HEIGHT : Int                       = 361
    }

    private lateinit var mContext : Context
    private lateinit var mQuizContractView : QuizContract.View
    private var mFileDownloadCoroutine : FileDownloadCoroutine? = null
    private var mQuizSaveRecordCoroutine : QuizSaveRecordCoroutine? = null
    private var mQuizInformationRequestCoroutine : QuizInformationRequestCoroutine? = null
    private lateinit var mMainHandler : WeakReferenceHandler

    private var mQuizPlayTimer : Timer? = null
    private var mQuizPassagePlayer : MediaPlayer? = null
    private var mQuizEffectPlayer : MediaPlayer? = null

    private var mQuizRequestObject : QuizStudyRecordData? = null
    private var mQuizUserSelectObjectList : ArrayList<QuizUserInteractionData>? = null
    private var mQuizInformationResult : QuizInformationResult? = null
    private var mQuizItemResultList : ArrayList<QuizItemResult>? = null

    private val mQuizDisplayFragmentList = ArrayList<Fragment>()
    private var mQuizSelectionPagerAdapter : QuizSelectionPagerAdapter? = null
    private var mTextInformationList : ArrayList<QuizTextData> = ArrayList<QuizTextData>()
    private var mPhonicsTextInformationList : ArrayList<QuizPhonicsTextData> = ArrayList<QuizPhonicsTextData>()

    private lateinit var mQuizPresenterDataObserver : QuizPresenterDataObserver
    private lateinit var mQuizFragmentDataObserver : QuizFragmentDataObserver

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

    private var mQuizIntentParamsObject : QuizIntentParamsObject? = null


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

    /** 퀴즈 타이머 */
    internal inner class QuizLimitPlayTask : TimerTask()
    {
        override fun run()
        {
            mMainHandler.sendEmptyMessage(MESSAGE_REFRESH_TASK_BOX)
        }
    }

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mQuizContractView = mContext as QuizContract.View
        mQuizContractView.initView()
        mQuizContractView.initFont()

        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        mQuizIntentParamsObject = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_QUIZ_PARAMS)
        mQuizUserSelectObjectList = ArrayList()
        mQuizSelectionPagerAdapter = QuizSelectionPagerAdapter(
            (mContext as AppCompatActivity).supportFragmentManager,
            mQuizDisplayFragmentList
        )
        mQuizSelectionPagerAdapter!!.addFragment(QuizStatus.INTRO)
        mQuizContractView.showPagerView(mQuizSelectionPagerAdapter)
        mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()

        requestQuizInformation()
        mQuizPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(QuizPresenterDataObserver::class.java)
        mQuizFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(QuizFragmentDataObserver::class.java)
        setupQuizCommunicateObserver()
    }

    override fun resume()
    {
        Log.f("")
        if(mCurrentQuizPageIndex != -1)
        {
            enableTimer(true)
        }
    }

    override fun pause()
    {
        Log.f("")
        if(mCurrentQuizPageIndex != -1)
        {
            enableTimer(false)
        }

        stopEffectPlay()
        stopMediaPlay()
    }

    override fun destroy()
    {
        Log.f("")
        mQuizInformationRequestCoroutine?.cancel()
        mQuizInformationRequestCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
        releaseMediaPlay()
        releaseEffectPlay()
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    /**
     * 퀴즈 옵저버 설정
     */
    private fun setupQuizCommunicateObserver()
    {
        // 사운드 플레이
        mQuizFragmentDataObserver.playSoundData.observe(
            (mContext as AppCompatActivity),
            {isPlaySound ->
                Log.f("isPlaySound : $isPlaySound")
                playQuestion(mCurrentQuizPageIndex)
            })

        // 다음 퀴즈
        mQuizFragmentDataObserver.nextData.observe((mContext as AppCompatActivity), {isGoNext ->
            Log.f("isGoNext : $isGoNext")
            stopMediaPlay()
            setQuizPlayStatus()
        })

        // 문제 선택
        mQuizFragmentDataObserver.choiceItemData.observe(
            (mContext as AppCompatActivity),
            {`object` ->
                mQuizUserSelectObjectList!!.add(`object`)
                mCorrectAnswerCount = if(`object`.isCorrect()) mCorrectAnswerCount + 1 else mCorrectAnswerCount
                mQuizContractView.showCorrectAnswerCount("$mCorrectAnswerCount/$mQuizPlayingCount")
                showAnswerAnimation(`object`.isCorrect())
            })

        // 퀴즈 결과 전송 완료
        mQuizFragmentDataObserver.studyInformationData.observe((mContext as AppCompatActivity),
            androidx.lifecycle.Observer {isSaveStudyInformation ->
                Log.f("isSaveStudyInformation : $isSaveStudyInformation")
                if(Feature.IS_FREE_USER)
                {
                    // 무료이용자는 저장불가
                    mQuizContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_login))
                    return@Observer
                }

                if(mQuizLimitTime <= 0)
                {
                    // 풀이시간 초과는 저장불가
                    showTemplateAlertDialog(
                        TemplateAlertDialog.DIALOG_EVENT_DEFAULT,
                        DialogButtonType.BUTTON_1,
                        mContext.resources.getString(R.string.message_quiz_limit_not_save)
                    )
                }
                else
                {
                    mQuizRequestObject =
                        QuizStudyRecordData(mQuizIntentParamsObject!!.getContentID(), mQuizUserSelectObjectList!!)
                    requestQuizSaveRecord()
                }
            })

        // 퀴즈 재시작
        mQuizFragmentDataObserver.replayData.observe((mContext as AppCompatActivity), {isGoReplay ->
            Log.f("isGoReplay : $isGoReplay")
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
        })
    }

    /**
     * 메세지 전달 이벤트
     */
    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_REQUEST_FILE_DOWNLOAD -> requestDownloadFile()
            MESSAGE_FILE_DOWNLOAD_ERROR -> errorDownload(msg.obj as String)
            MESSAGE_REFRESH_TASK_BOX -> setQuestionTaskInformation()
            MESSAGE_PICTURE_QUESTION_SETTING -> makePictureQuestion(PLAY_INIT)
            MESSAGE_TEXT_QUESTION_SETTING -> makeTextQuestion(PLAY_INIT)
            MESSAGE_PHONICS_TEXT_QUESTION_SETTING -> makePhonicsTextQuestion(PLAY_INIT)
            MESSAGE_READY_TO_QUIZ -> readyToPlay()
            MESSAGE_READY_TO_REPLAY -> readyToReplay()
            MESSAGE_PLAY_SOUND_QUIZ -> playQuestion(msg.arg1)
            MESSAGE_FINISH -> (mContext as Activity).finish()
            MESSAGE_QUIZ_RESULT_SOUND -> playResultByQuizCorrect()
        }
    }

    /** ========== 통신요청 ========== */
    /**
     * 퀴즈 정보 통신 요청
     */
    private fun requestQuizInformation()
    {
        Log.f("mContentID : $mQuizIntentParamsObject!!.getContentID()")
        mQuizInformationRequestCoroutine = QuizInformationRequestCoroutine(mContext)
        mQuizInformationRequestCoroutine?.setData(mQuizIntentParamsObject!!.getContentID())
        mQuizInformationRequestCoroutine?.asyncListener = mQuizRequestListener
        mQuizInformationRequestCoroutine?.execute()
    }

    /**
     * 이미지 파일 통신 요청
     */
    private fun requestDownloadFile()
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
        mFileDownloadCoroutine = FileDownloadCoroutine(mContext)
        mFileDownloadCoroutine?.setData(downloadUrlList, fileSavePathList)
        mFileDownloadCoroutine?.asyncListener = mFileDownloadListener
        mFileDownloadCoroutine?.execute()
    }

    /**
     * 퀴즈 결과 저장 통신 요청
     */
    private fun requestQuizSaveRecord()
    {
        mQuizContractView.showLoading()
        mQuizSaveRecordCoroutine = QuizSaveRecordCoroutine(mContext)
        mQuizSaveRecordCoroutine?.setData(mQuizRequestObject, mQuizIntentParamsObject!!.getHomeworkNumber())
        mQuizSaveRecordCoroutine?.asyncListener = mQuizRequestListener
        mQuizSaveRecordCoroutine?.execute()
    }
    /** ========== 통신요청 end ========== */
    /**
     * 퀴즈 시작 준비
     */
    private fun readyToPlay()
    {
        (mQuizDisplayFragmentList[QUIZ_INTRO] as QuizIntroFragment).loadingComplete()
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
     * 상단 영역 설정 (타이머, 문제수)
     */
    private fun initTaskBoxInformation()
    {
        try
        {
            mCorrectAnswerCount = 0
            mQuizLimitTime = mQuizInformationResult!!.getTimeLimit()
            mQuizContractView.showPlayTime(CommonUtils.getInstance(mContext).getSecondTime(mQuizLimitTime))
            mQuizContractView.showCorrectAnswerCount("$mCorrectAnswerCount/$mQuizPlayingCount")
        } catch(e : NullPointerException) { }
    }

    /**
     * 퀴즈화면 세팅
     */
    override fun onQuizPageSelected()
    {
        Log.f("quizPageIndex : $mCurrentQuizPageIndex")
        if(mCurrentQuizPageIndex == -1)
        {
            mQuizContractView.hideTaskBoxLayout() // 상단 영역 숨김 (타이머, 정답수)
            return
        }
        else
        {
            mQuizContractView.showTaskBoxLayout() // 상단 영역 표시 (타이머, 정답수)
        }

        when(mCurrentQuizType)
        {
            Common.QUIZ_CODE_PICTURE, Common.QUIZ_CODE_PHONICS_SOUND_TEXT, Common.QUIZ_CODE_SOUND_TEXT ->
            {
                val msg = Message.obtain()
                msg.what = MESSAGE_PLAY_SOUND_QUIZ
                msg.arg1 = mCurrentQuizPageIndex
                mMainHandler.sendMessageDelayed(msg, DURATION_ANIMATION.toLong())
            }
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
            mQuizContractView.showPlayTime(CommonUtils.getInstance(mContext).getSecondTime(mQuizLimitTime))
        }
        else
        {
            mCurrentQuizPageIndex = -1
            stopMediaPlay()
            enableTimer(false)
            Log.f("Quiz End Not All Solved. Limit Time")

            mQuizContractView.hideAnswerView()
            mQuizPresenterDataObserver.setResult(mQuizPlayingCount, mCorrectAnswerCount)
            mQuizContractView.forceChangePageView(mQuizDisplayFragmentList.size - 1)
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_QUIZ_RESULT_SOUND, DURATION_ANIMATION.toLong()
            )
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
            mQuizContractView.showCorrectAnswerView()
        } else
        {
            playEffectSound(MEDIA_INCORRECT_PATH)
            mQuizContractView.showInCorrectAnswerView()
        }
    }

    /**
     * UI 화면을 갱신하는 타이머를 동작시키거나 중지시킨다.
     * @param isStart
     */
    private fun enableTimer(isStart : Boolean)
    {
        if(isStart)
        {
            if(mQuizPlayTimer == null)
            {
                mQuizPlayTimer = Timer()
                mQuizPlayTimer!!.schedule(
                    QuizLimitPlayTask(),
                    0,
                    DURATION_TIMER.toLong()
                )
            }
        }
        else
        {
            if(mQuizPlayTimer != null)
            {
                mQuizPlayTimer!!.cancel()
                mQuizPlayTimer = null
            }
        }
    }

    /**
     * 문제 진행
     */
    private fun playQuestion(index : Int)
    {
        Log.f("index : $index")
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
            mQuizEffectPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mQuizEffectPlayer?.prepareAsync()
            mQuizEffectPlayer?.setOnPreparedListener {
                mQuizEffectPlayer?.start()
            }
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
            mQuizPassagePlayer?.setDataSource(mContext, Uri.parse(uri))
            mQuizPassagePlayer?.setAudioAttributes(mAudioAttributes)
            mQuizPassagePlayer?.prepareAsync()
            mQuizPassagePlayer?.setOnPreparedListener {mQuizPassagePlayer?.start()}
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
            mQuizPassagePlayer?.setDataSource(mContext, Uri.parse(uriList[mMainExampleSoundIndex]))
            mQuizPassagePlayer?.setAudioAttributes(mAudioAttributes)
            mQuizPassagePlayer?.prepareAsync()
            mQuizPassagePlayer?.setOnPreparedListener {mQuizPassagePlayer!!.start()}
        } catch(e : java.lang.Exception)
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
            val correctBitmap =
                BitmapFactory.decodeFile(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getCorrectImageFileName())
            for(i in mQuizItemResultList!!.indices)
            {
                mCorrectImageList.add(
                    Bitmap.createBitmap(
                        correctBitmap,
                        0,
                        i * QUIZ_IMAGE_HEIGHT,
                        QUIZ_IMAGE_WIDTH,
                        QUIZ_IMAGE_HEIGHT
                    )
                )
            }
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
                randImageIndex = CommonUtils.getInstance(mContext)
                    .getRandomNumber(maxQuestionCount, correctQuestionIndex)
                mQuizPictureData = QuizPictureData(
                    i,
                    mQuizItemResultList!![i].getTitle(),
                    ExamplePictureData(correctQuestionIndex, mCorrectImageList[correctQuestionIndex]),
                    ExamplePictureData(randImageIndex, mCorrectImageList[randImageIndex])
                )
                mQuizPictureData.setRecordQuizValue(correctQuestionIndex, randImageIndex)
                mPictureQuizList.add(mQuizPictureData)

                Log.f("Position : " + i + "CorrectIndex : " + correctQuestionIndex + ", randImageIndex : " + randImageIndex)
                Log.f("title : " + mQuizItemResultList!![i].getTitle())
            }
        }
        else
        {
            try
            {
                val incorrectBitmap =
                    BitmapFactory.decodeFile(mContext.filesDir.absolutePath + File.separator + "quiz/" + mQuizInformationResult!!.getInCorrectImageFileName())
                var incorrectPieceBitmap : Bitmap? = null
                for(i in 0 until maxQuestionCount)
                {
                    correctQuestionIndex = mQuizItemResultList!![i].getCorrectIndex()
                    incorrectPieceBitmap = Bitmap.createBitmap(
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
                    mPictureQuizList.add(mQuizPictureData)
                    Log.f("Position : " + i + "CorrectIndex : " + correctQuestionIndex + ", randImageIndex : " + randImageIndex)
                    Log.f("title : " + mQuizItemResultList!![i].getTitle())
                }
                incorrectBitmap.recycle()
                incorrectPieceBitmap!!.recycle()
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
        for(i in mPictureQuizList.indices)
        {
            mQuizSelectionPagerAdapter!!.addQuizPlayFragment(mCurrentQuizType, mPictureQuizList[i])
        }
        mQuizSelectionPagerAdapter!!.addFragment(QuizStatus.RESULT)

        when(type)
        {
            PLAY_INIT -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_QUIZ)
            PLAY_REPLAY -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_REPLAY)
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

        for(i in 0 until maxQuestionCount)
        {
            mQuizSelectionPagerAdapter!!.addQuizPlayFragment(
                mCurrentQuizType,
                mTextInformationList[i]
            )
        }
        mQuizSelectionPagerAdapter!!.addFragment(QuizStatus.RESULT)

        when(type)
        {
            PLAY_INIT -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_QUIZ)
            PLAY_REPLAY -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_REPLAY)
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

        for(i in 0 until maxQuestionCount)
        {
            mQuizSelectionPagerAdapter!!.addQuizPlayFragment(
                mCurrentQuizType, mPhonicsTextInformationList[i]
            )
        }
        mQuizSelectionPagerAdapter!!.addFragment(QuizStatus.RESULT)

        when(type)
        {
            PLAY_INIT -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_QUIZ)
            PLAY_REPLAY -> mMainHandler.sendEmptyMessage(MESSAGE_READY_TO_REPLAY)
        }
    }
    /** ========== 퀴즈 데이터 생성 end ========== */

    /**
     * 알림(메세지) 다이얼로그 표시
     */
    private fun showTemplateAlertDialog(type : Int, buttonType : DialogButtonType, message : String)
    {
        val dialog = TemplateAlertDialog(mContext)
        dialog.setMessage(message)
        dialog.setDialogEventType(type)
        dialog.setButtonType(buttonType)
        dialog.show()
    }

    /**
     * 다운로드 실패시 메세지를 띠우고 종료
     */
    private fun errorDownload(message : String)
    {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
        (mContext as Activity).finish()
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
            mQuizUserSelectObjectList!!.clear()
            enableTimer(true)
        }
        else if(mCurrentQuizPageIndex == mQuizPlayingCount)
        {
            enableTimer(false)
            mQuizPresenterDataObserver.setResult(mQuizPlayingCount, mCorrectAnswerCount)
            mMainHandler.sendEmptyMessageDelayed(
                MESSAGE_QUIZ_RESULT_SOUND, DURATION_ANIMATION.toLong()
            )
            mCurrentQuizPageIndex = -1
        }

        mQuizContractView.hideAnswerView()
        mQuizContractView.nextPageView()
    }

    /** ========== 통신 응답 리스너 ========== */
    /**
     * 파일 다운로드 통신 응답 리스너
     */
    private val mFileDownloadListener : AsyncListener = object : AsyncListener
    {
        var isError = false
        override fun onRunningStart(code : String) { }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            if(!isError)
            {
                mMainHandler.sendEmptyMessage(MESSAGE_PICTURE_QUESTION_SETTING)
            }
        }

        override fun onRunningCanceled(code : String) { }

        override fun onRunningProgress(code : String, progress : Int) { }

        override fun onRunningAdvanceInformation(code : String, `object` : Any) { }

        override fun onErrorListener(code : String, msg : String)
        {
            isError = true
            val message = Message.obtain()
            message.what = MESSAGE_FILE_DOWNLOAD_ERROR
            message.obj = msg
            mMainHandler.sendMessage(message)
        }
    }

    /**
     * 퀴즈 통신 응답 리스너
     */
    private val mQuizRequestListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) { }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_QUIZ_INFORMATION)
                {
                    // 퀴즈 데이터 Response
                    mQuizInformationResult = (result as QuizBaseObject).getData()
                    mCurrentQuizType = mQuizInformationResult!!.getType()
                    mQuizItemResultList = mQuizInformationResult!!.getQuestionItemInformationList()
                    mQuizPlayingCount = mQuizInformationResult!!.getQuizCount()

                    if(mCurrentQuizType == Common.QUIZ_CODE_TEXT && mQuizItemResultList!![0].getSoundUrl() != "")
                    {
                        mCurrentQuizType = Common.QUIZ_CODE_SOUND_TEXT
                    }
                    Log.f("Content Quiz Id Question : " + mQuizInformationResult!!.getContentsId())

                    when(mCurrentQuizType)
                    {
                        Common.QUIZ_CODE_PICTURE ->
                        {
                            mMainHandler.sendEmptyMessage(MESSAGE_REQUEST_FILE_DOWNLOAD)
                            Log.f("Image Question - " + mQuizInformationResult!!.getTitle())
                        }
                        Common.QUIZ_CODE_TEXT ->
                        {
                            mMainHandler.sendEmptyMessage(MESSAGE_TEXT_QUESTION_SETTING)
                            Log.f("Text Question - " + mQuizInformationResult!!.getTitle())
                        }
                        Common.QUIZ_CODE_PHONICS_SOUND_TEXT ->
                        {
                            mMainHandler.sendEmptyMessage(MESSAGE_PHONICS_TEXT_QUESTION_SETTING)
                            Log.f("Main Sound Question - " + mQuizInformationResult!!.getTitle())
                        }
                        Common.QUIZ_CODE_SOUND_TEXT ->
                        {
                            mMainHandler.sendEmptyMessage(MESSAGE_TEXT_QUESTION_SETTING)
                            Log.f("Main Example Text Question - " + mQuizInformationResult!!.getTitle())
                        }
                    }
                    (mQuizDisplayFragmentList[QUIZ_INTRO] as QuizIntroFragment).setTitle(
                        mQuizInformationResult!!.getTitle(),
                        mQuizInformationResult!!.getSubTitle()
                    )
                }
                else if(code == Common.COROUTINE_CODE_QUIZ_SAVE_RECORD)
                {
                    // 퀴즈 결과 저장 완료
                    mQuizContractView.hideLoading()
                    mQuizContractView.hideAnswerView()
                    showTemplateAlertDialog(
                        TemplateAlertDialog.DIALOG_EVENT_DEFAULT,
                        DialogButtonType.BUTTON_1,
                        mContext.resources.getString(R.string.message_quiz_save_record_success)
                    )
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                {
                    //중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if(code == Common.COROUTINE_CODE_QUIZ_INFORMATION)
                    {
                        Log.f("FAIL ASYNC_CODE_QUIZ_INFORMATION")
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        mMainHandler.sendEmptyMessageDelayed(
                            MESSAGE_FINISH, DURATION_VIEW_INIT.toLong()
                        )
                        if(Feature.IS_ENABLE_FIREBASE_CRASHLYTICS)
                        {
                            val data = ErrorRequestData(
                                CrashlyticsHelper.ERROR_CODE_QUIZ_REQUEST,
                                mQuizIntentParamsObject!!.getContentID(),
                                result.getStatus(),
                                result.getMessage(),
                                java.lang.Exception()
                            )
                            CrashlyticsHelper.getInstance(mContext).sendCrashlytics(data)
                        }
                    }
                    else if(code == Common.COROUTINE_CODE_QUIZ_SAVE_RECORD)
                    {
                        mQuizContractView.hideLoading()
                        Log.f("FAIL ASYNC_CODE_QUIZ_SAVE_RECORD")
                        (mQuizDisplayFragmentList[mQuizDisplayFragmentList.size - 1] as QuizResultFragment).enableSaveButton()
                        showTemplateAlertDialog(
                            TemplateAlertDialog.DIALOG_EVENT_DEFAULT,
                            DialogButtonType.BUTTON_1,
                            result.getMessage()
                        )
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String) { }

        override fun onRunningProgress(code : String, progress : Int) { }

        override fun onRunningAdvanceInformation(code : String, `object` : Any)
        {
            if(code == Common.COROUTINE_CODE_QUIZ_INFORMATION)
            {
                mCurrentQuizType = `object` as String
            }
        }

        override fun onErrorListener(code : String, message : String) { }
    }
}