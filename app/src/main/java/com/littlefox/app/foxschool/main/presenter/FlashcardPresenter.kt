package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Message
import android.os.Parcelable
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.VocabularyContentsBaseObject
import com.littlefox.app.foxschool.`object`.result.VocabularyShelfBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.adapter.FlashcardSelectionPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.coroutine.FlashcardSaveCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyContentsAddCoroutine
import com.littlefox.app.foxschool.coroutine.VocabularyContentsListCoroutine
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomFlashcardIntervalSelectDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.BookAddListener
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.IntervalSelectListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.enumerate.FlashcardStudyType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.FlashcardContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.*
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 플래시카드 Presenter
 */
class FlashcardPresenter : FlashcardContract.Presenter
{
    companion object
    {
        private const val MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST : Int  = 100
        private const val MESSAGE_AUTO_PLAY : Int                       = 101
        private const val MESSAGE_ALERT_TOAST : Int                     = 102

        private const val DIALOG_CLOSE_APP : Int                        = 10001
        private const val DIALOG_BOOKMARK_INIT : Int                    = 10002

        private const val DEFAULT_INTERVAL_SECOND : Int                 = 3
    }

    // Presenter 기본적인 변수
    private lateinit var mContext : Context
    private lateinit var mFlashcardContractView : FlashcardContract.View
    private var mFlashcardDisplayFragmentList : ArrayList<Fragment> = ArrayList()
    private lateinit var mFlashcardSelectionPagerAdapter : FlashcardSelectionPagerAdapter
    private var mMainHandler : WeakReferenceHandler? = null
    private var mVocabularyContentsListCoroutine : VocabularyContentsListCoroutine? = null
    private var mVocabularyContentsAddCoroutine : VocabularyContentsAddCoroutine? = null
    private var mFlashcardSaveCoroutine : FlashcardSaveCoroutine? = null

    // Fragment 관련 변수
    private var mCurrentFlashcardStatus : FlashcardStatus = FlashcardStatus.INTRO
    private lateinit var mFlashcardPresenterObserver : FlashcardPresenterObserver
    private lateinit var mFlashcardIntroFragmentObserver : FlashcardIntroFragmentObserver
    private lateinit var mFlashcardStudyFragmentObserver : FlashcardStudyFragmentObserver
    private lateinit var mFlashcardResultFragmentObserver : FlashcardResultFragmentObserver
    private lateinit var mFlashcardBookmarkFragmentObserver : FlashcardBookmarkFragmentObserver

    // 다이얼로그
    private var mBottomFlashcardIntervalSelectDialog : BottomFlashcardIntervalSelectDialog? = null
    private var mBottomBookAddDialog : BottomBookAddDialog? = null
    private var mTempleteAlertDialog : TemplateAlertDialog? = null

    // 플래시카드 데이터 변수
    private lateinit var mFlashcardDataObject : FlashcardDataObject
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mUserInformationResult : LoginInformationResult
    private var mOriginCardList : ArrayList<VocabularyDataResult>?          = null
    private var mCurrentVocabularyAddResult : MyVocabularyResult?           = null
    private var mCurrentStudyCardList : ArrayList<FlashCardDataResult>?     = null
    private var mCurrentBookmarkCardList : ArrayList<FlashCardDataResult>?  = null
    private var mCurrentFlashcardStudyType : FlashcardStudyType?            = null
    private var mCoachingMarkUserDao : CoachmarkDao?                        = null

    // 플레이 관련 변수
    private var mMediaPlayer : MediaPlayer?         = null
    private var mAudioAttributes : AudioAttributes? = null
    private var mCurrentIntervalSecond : Int        = DEFAULT_INTERVAL_SECOND // 자동재생 시간
    private var isSoundPlay : Boolean               = true  // 사운드 플레이
    private var isCheckAutoPlay : Boolean           = false // 자동재생
    private var isGotoIntroPage : Boolean           = false // 처음으로 돌아가기
    private var isCheckShuffle : Boolean            = false // 섞어보기 체크
    private var mCurrentUserID : String             = ""
    private var mCurrentPageIndex : Int             = 0

    constructor(context : Context)
    {
        this.mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mFlashcardContractView = mContext as FlashcardContract.View
        mFlashcardContractView.initView()
        mFlashcardContractView.initFont()
        init()
    }

    private fun init()
    {
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao()
        mUserInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
        mFlashcardDataObject = (mContext as AppCompatActivity).intent.getParcelableExtra<Parcelable>(Common.INTENT_FLASHCARD_DATA) as FlashcardDataObject

        mFlashcardDisplayFragmentList = ArrayList()
        mFlashcardSelectionPagerAdapter = FlashcardSelectionPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mFlashcardDisplayFragmentList)
        mFlashcardSelectionPagerAdapter.addFragment(FlashcardStatus.INTRO)
        mFlashcardContractView.showPagerView(mFlashcardSelectionPagerAdapter)

        mFlashcardPresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(FlashcardPresenterObserver::class.java)
        setupFlashcardIntroFragmentListener()
        setupFlashcardStudyFragmentListener()
        setupFlashcardResultFragmentListener()
        setupFlashcardBookmarkFragmentListener()

        mCurrentUserID = mUserInformationResult.getUserInformation().getFoxUserID()

        // 플래시카드 안내 화면 표시해야하는지 체크
        CoroutineScope(Dispatchers.Main).launch {
            if(isShowCoachMark(mCurrentUserID))
            {
                mFlashcardContractView.showCoachMarkView()
            }
            else
            {
                readyToStudy()
            }
        }

        mFlashcardContractView.settingAutoPlayInterval(mCurrentIntervalSecond)
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        isCheckAutoPlay = false
        releaseAudio()
        releaseCoroutine()
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    /**
     * 플래시카드 페이지 전환
     */
    private fun setPageAction(status : FlashcardStatus)
    {
        mCurrentFlashcardStatus = status
        Log.f("mCurrentFlashcardStatus : $mCurrentFlashcardStatus")
        when(status)
        {
            FlashcardStatus.INTRO,
            FlashcardStatus.BOOKMARK_INTRO ->
            {
                mCurrentFlashcardStudyType = null
                setCheckAutoPlay(false)
                setCheckShuffle(false)
                if(mCurrentFlashcardStatus == FlashcardStatus.BOOKMARK_INTRO)
                {
                    mFlashcardContractView.settingBaseControlView(mCurrentFlashcardStatus)
                    initBookmarkFlashcard()
                    mFlashcardSelectionPagerAdapter.addBookmarkFragment(
                        mFlashcardDataObject.getVocabularyType(),
                        mCurrentBookmarkCardList!!
                    )
                    mFlashcardContractView.nextPageView()
                }
            }

            FlashcardStatus.STUDY,
            FlashcardStatus.BOOKMARK_STUDY ->
            {
                if(status === FlashcardStatus.STUDY)
                {
                    if(isGotoIntroPage == false)
                    {
                        mFlashcardSelectionPagerAdapter.addStudyDataFragment(mCurrentStudyCardList!!)
                        mFlashcardSelectionPagerAdapter.addFragment(FlashcardStatus.RESULT)
                    } else
                    {
                        mFlashcardPresenterObserver.onNotifyUpdateList(mCurrentStudyCardList!!)
                    }
                } else
                {
                    if(isHaveBookmarkedItem() == false)
                    {
                        mCurrentFlashcardStudyType = null
                        mFlashcardContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_bookmark_empty))
                        return
                    }
                    initBookmarkFlashcard()
                    mFlashcardSelectionPagerAdapter.addStudyDataFragment(mCurrentBookmarkCardList!!)
                }
                mFlashcardContractView.settingBaseControlView(mCurrentFlashcardStatus)
                mFlashcardPresenterObserver.onInitStudySetting(mCurrentFlashcardStudyType!!)
                mFlashcardContractView.nextPageView()
            }

            FlashcardStatus.RESULT ->
            {
                mFlashcardPresenterObserver.setBookmarkButton(isHaveBookmarkedItem())
                mFlashcardContractView.settingBaseControlView(mCurrentFlashcardStatus)
                mFlashcardContractView.nextPageView()
                requestFlashcardSaveAsync()
            }
        }
    }

    /**
     * 플래시카드 페이지 선택
     */
    override fun onFlashCardPageSelected(pageIndex : Int)
    {
        if(isGotoIntroPage && pageIndex == 0)
        {
            mFlashcardContractView.settingBaseControlView(FlashcardStatus.INTRO)
        }
        mCurrentPageIndex = pageIndex
    }

    /**
     * 메세지 전달 이벤트
     */
    override fun sendMessageEvent(msg : Message)
    {
        when (msg.what)
        {
            MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST -> requestVocabularyContentsListAsync()
            MESSAGE_AUTO_PLAY -> mFlashcardPresenterObserver.onNextShowCard()
            MESSAGE_ALERT_TOAST ->
            {
                if (msg.arg1 == RESULT_OK)
                {
                    mFlashcardContractView.showSuccessMessage(msg.obj.toString())
                }
                else
                {
                    mFlashcardContractView.showErrorMessage(msg.obj.toString())
                }
            }
        }
    }

    /**
     * 코루틴 제거
     */
    private fun releaseCoroutine()
    {
        mVocabularyContentsListCoroutine?.cancel()
        mVocabularyContentsListCoroutine = null

        mVocabularyContentsAddCoroutine?.cancel()
        mVocabularyContentsAddCoroutine = null
    }

    /**
     * 단어장 리스트 통신 요청
     */
    private fun requestVocabularyContentsListAsync()
    {
        Log.f("Vocabulary Series ID : " + mFlashcardDataObject.getContentID())
        mVocabularyContentsListCoroutine = VocabularyContentsListCoroutine(mContext)
        mVocabularyContentsListCoroutine!!.setData(mFlashcardDataObject.getContentID())
        mVocabularyContentsListCoroutine!!.asyncListener = mAsyncListener
        mVocabularyContentsListCoroutine!!.execute()
    }

    /**
     * 단어장 추가 통신 요청
     */
    private fun requestVocabularyContentsAddAsync()
    {
        Log.f("Vocabulary Contents ID : " + mFlashcardDataObject.getContentID())
        Log.f("Vocabulary ID : " + mCurrentVocabularyAddResult?.getID())
        mVocabularyContentsAddCoroutine = VocabularyContentsAddCoroutine(mContext)
        mVocabularyContentsAddCoroutine!!.setData(
            mFlashcardDataObject.getContentID(),
            mCurrentVocabularyAddResult?.getID(),
            getBookmarkedVocabularyItemList()
        )
        mVocabularyContentsAddCoroutine!!.asyncListener = mAsyncListener
        mVocabularyContentsAddCoroutine!!.execute()
    }

    private fun requestFlashcardSaveAsync()
    {
        Log.f("")
        mFlashcardContractView.showLoading()

        mFlashcardSaveCoroutine = FlashcardSaveCoroutine(mContext)
        mFlashcardSaveCoroutine!!.setData(mFlashcardDataObject.getContentID())
        mFlashcardSaveCoroutine!!.asyncListener = mAsyncListener
        mFlashcardSaveCoroutine!!.execute()
    }

    /**
     * 학습 준비
     *  - 단어장 화면에서 넘어온 경우 플래시카드 아이템 생성
     *  - 컨텐츠 화면에서 넘어온 경우 단어장 통신 요청
     */
    private fun readyToStudy()
    {
        mFlashcardContractView.showLoading()
        when(mFlashcardDataObject.getVocabularyType())
        {
            VocabularyType.VOCABULARY_SHELF ->
            {
                mOriginCardList = mFlashcardDataObject.getWordList()
                initStudyFlashcard()
                initAudio()
            }
            VocabularyType.VOCABULARY_CONTENTS ->
            {
                mMainHandler!!.sendEmptyMessageDelayed(
                    MESSAGE_REQUEST_VOCABULARY_DETAIL_LIST, Common.DURATION_NORMAL
                )
            }
        }
        mFlashcardPresenterObserver.setIntroTitle(mFlashcardDataObject)
    }

    /**
     * 첫 가이드 화면 표시해야 하는지 체크
     */
    private suspend fun isShowCoachMark(userID : String) : Boolean
    {
        val data : CoachmarkEntity? = CoroutineScope(Dispatchers.IO).async {
            mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)
        }.await()

        if(data == null)
        {
            Log.f("data null story")
            return true
        }
        else
        {
            Log.f("userID : $mCurrentUserID, data.isFlashcardCoachMarkViewed : ${data.isFlashcardCoachmarkViewed}")
            if(data.isFlashcardCoachmarkViewed)
            {
                return false
            }
            return true
        }
    }

    /**
     * 첫 가이드 이미지 표시된 후 DB 값 세팅 (다시 보지 않도록)
     */
    private fun setFlashcardCoachMarkViewed(userID : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            var data : CoachmarkEntity? = mCoachingMarkUserDao?.getSavedCoachmarkUser(userID)

            if(data == null)
            {
                Log.f("data null ")
                data = CoachmarkEntity(userID,
                    isStoryCoachmarkViewed = false,
                    isSongCoachmarkViewed = false,
                    isRecordCoachmarkViewed = false,
                    isFlashcardCoachmarkViewed = true)
                mCoachingMarkUserDao?.insertItem(data)
            }
            else
            {
                Log.f("data update  ")
                data.isFlashcardCoachmarkViewed = true
                mCoachingMarkUserDao?.updateItem(data)
            }
        }.start()
    }

    /**
     * 첫 가이드 다시 보지 않기
     */
    override fun onCoachMarkNeverSeeAgain()
    {
        Log.f("")
        setFlashcardCoachMarkViewed(mCurrentUserID)
        readyToStudy()
    }

    /**
     * 컨텐츠의 단어장 리스트에서 나의단어장으로 컨텐츠를 추가해서 갱신할때 사용하는 메소드 ( 추가됨으로써 서버쪽의 해당 단어장의 정보를 갱신하기 위해 사용 )
     * 예) 단어장 ID , 단어의 개수, 단어 컬러 등등
     * @param result 서버쪽에서 받은 결과 단어장 정보
     */
    private fun updateVocabularyData(result : MyVocabularyResult)
    {
        for(i in 0 until mMainInformationResult.getVocabulariesList().size)
        {
            if(mMainInformationResult.getVocabulariesList()[i].getID() == result.getID())
            {
                mMainInformationResult.getVocabulariesList()[i] = result
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mMainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
    }

    /**
     * ==============================
     *       플래시카드 관련 함수
     * ==============================
     */
    /**
     * 플래시카드 아이템 리스트 생성
     */
    private fun initStudyFlashcard()
    {
        Log.f("")
        mCurrentStudyCardList = ArrayList()
        var cardNumber = 1
        for(i in mOriginCardList!!.indices)
        {
            val data = FlashCardDataResult(mOriginCardList!![i])
            data.setIndex(i)
            data.setCardNumber(cardNumber)
            mCurrentStudyCardList!!.add(data)
            cardNumber++
        }
    }

    /**
     * 섞어보기 기능 설정
     */
    private fun setCheckShuffle(isEnable : Boolean)
    {
        isCheckShuffle = isEnable
        mFlashcardContractView.checkShuffleBox(isCheckShuffle)

        if(isCheckShuffle)
        {
            Log.f("섞어보기 ON")
            shuffleFlashCardList()
        } else
        {
            Log.f("섞어보기 OFF")
            getSortFlashCardList()
        }
    }

    /**
     * 자동넘기기 기능 설정
     */
    private fun setCheckAutoPlay(isEnable : Boolean)
    {
        isCheckAutoPlay = isEnable
        mFlashcardContractView.checkAutoplayBox(mCurrentFlashcardStatus, isCheckAutoPlay)

        if (mCurrentFlashcardStatus == FlashcardStatus.STUDY ||
            mCurrentFlashcardStatus == FlashcardStatus.BOOKMARK_STUDY)
        {
            if (isCheckAutoPlay)
            {
                Log.f("학습 중 자동넘기기 ON")
                mMainHandler!!.removeMessages(MESSAGE_AUTO_PLAY)
                mMainHandler!!.sendEmptyMessageDelayed(
                    MESSAGE_AUTO_PLAY, (mCurrentIntervalSecond * Common.SECOND).toLong()
                )
            }
            else
            {
                Log.f("학습중 자동넘기기 OFF")
                mMainHandler!!.removeMessages(MESSAGE_AUTO_PLAY)
            }
        }
        else
        {
            if(isCheckAutoPlay)
            {
                Log.f("자동넘기기 ON")
            }
            else
            {
                Log.f("자동넘기기 OFF")
            }
        }
    }

    /**
     * 플래시카드 아이템 섞기
     */
    private fun shuffleFlashCardList()
    {
        var cardNumber = 1
        mCurrentStudyCardList!!.shuffle(Random(System.currentTimeMillis()))
        for(i in mCurrentStudyCardList!!.indices)
        {
            mCurrentStudyCardList!![i].setCardNumber(cardNumber)
            cardNumber++
        }
    }

    /**
     * 플래시카드 아이템 정렬
     */
    private fun getSortFlashCardList()
    {
        var cardNumber = 1
        Collections.sort(mCurrentStudyCardList, object : Comparator<FlashCardDataResult>
        {
            override fun compare(o1 : FlashCardDataResult, o2 : FlashCardDataResult) : Int
            {
                if(o1.getIndex() < o2.getIndex())
                {
                    return -1
                }
                else if(o1.getIndex() > o2.getIndex())
                {
                    return 1
                }
                else
                {
                    return 0
                }
            }
        })

        for(i in mCurrentStudyCardList!!.indices)
        {
            mCurrentStudyCardList!![i].setCardNumber(cardNumber)
            cardNumber++
        }
    }

    /**
     * 플래시카드 아이템 가져오기
     */
    private fun getFlashcardItem(wordID : String) : FlashCardDataResult?
    {
        if (mCurrentStudyCardList != null)
        {
            for(item in mCurrentStudyCardList!!)
            {
                if(item.getID() == wordID)
                {
                    return item
                }
            }
        }
        return null
    }

    /**
     * ==============================
     *        북마크 관련 함수
     * ==============================
     */
    /**
     * 북마크 아이템 리스트 생성
     */
    private fun initBookmarkFlashcard()
    {
        mCurrentBookmarkCardList = ArrayList()
        var cardIndex = 1
        for(item in mCurrentStudyCardList!!)
        {
            if(item.isBookmarked())
            {
                item.setCardNumber(cardIndex)
                item.enableBookmark(true)
                mCurrentBookmarkCardList!!.add(item)
                cardIndex++
            }
        }
        Log.f("Bookmark list Size : " + mCurrentBookmarkCardList!!.size)
    }

    /**
     * 북마크 리스트 초기화
     */
    private fun clearBookmarkList()
    {
        for(i in mCurrentStudyCardList!!.indices)
        {
            mCurrentStudyCardList!![i].enableBookmark(false)
        }
    }

    /**
     * 북마크 체크 ON/OFF
     */
    private fun checkBookmarkItem(wordID : String, isEnable : Boolean)
    {
        for(i in mCurrentStudyCardList!!.indices)
        {
            if(mCurrentStudyCardList!![i].getID() == wordID)
            {
                mCurrentStudyCardList!![i].enableBookmark(isEnable)
            }
        }
    }

    /**
     * 북마크 한 아이템이 있는지 확인
     */
    private fun isHaveBookmarkedItem() : Boolean
    {
        for(item in mCurrentStudyCardList!!)
        {
            if(item.isBookmarked())
            {
                return true
            }
        }
        return false
    }

    /**
     * 북마크 한 아이템 리스트 가져오기
     */
    private fun getBookmarkedVocabularyItemList() : ArrayList<VocabularyDataResult>
    {
        val result = ArrayList<VocabularyDataResult>()
        for(item in mCurrentStudyCardList!!)
        {
            if(item.isBookmarked())
            {
                result.add(item)
            }
        }
        return result
    }

    /**
     * ==============================
     *      AUDIO PLAY 관련 함수
     * ==============================
     */
    /**
     * Audio Player 생성
     */
    private fun initAudio()
    {
        mMediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if (mAudioAttributes == null)
            {
                mAudioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            }
            mMediaPlayer?.setAudioAttributes(mAudioAttributes)
        }
        else
        {
            mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        try
        {
            mMediaPlayer!!.setDataSource(mCurrentStudyCardList?.get(0)?.getSoundURL())
            mMediaPlayer!!.prepareAsync()
            mMediaPlayer!!.setOnPreparedListener {
                mFlashcardContractView.hideLoading()
                Log.f("Init Prepare Complete")
            }
        } catch(e : Exception) { }
    }

    /**
     * Audio Player 재생 (플래시카드)
     */
    private fun startFlashcardAudio(wordID : String, isForcePlay : Boolean)
    {
        val item : FlashCardDataResult? = getFlashcardItem(wordID)
        if (item != null)
        {
            startAudio(item, isForcePlay)
        }
    }

    /**
     * Audio Player 재생
     */
    private fun startAudio(item : FlashCardDataResult, isForcePlay : Boolean)
    {
        if ((isForcePlay == false && isSoundPlay == false) ||
            (isForcePlay == false && mCurrentFlashcardStudyType == FlashcardStudyType.MEANING_START))
        {
            if (isCheckAutoPlay)
            {
                Log.f("사운드 끄기 또는 뜻으로 학습하기 자동 플레이")
                mMainHandler?.removeMessages(MESSAGE_AUTO_PLAY)
                mMainHandler?.sendEmptyMessageDelayed(MESSAGE_AUTO_PLAY, (mCurrentIntervalSecond * Common.SECOND).toLong())
            }
            else
            {
                Log.f("사운드 끄기 또는 뜻으로 학습하기 수동 플레이")
            }
            return
        }
        else
        {
            Log.f("사운드 재생")
        }

        if (mMediaPlayer != null)
        {
            mMediaPlayer!!.reset()
        }
        else
        {
            initAudio()
        }

        try
        {
            Log.f("Play Word : " + item.getWordText())
            Log.f("Play URL : " + item.getSoundURL())

            mMediaPlayer!!.setDataSource(item.getSoundURL())
            mMediaPlayer!!.prepareAsync()
            mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
            mMediaPlayer!!.setOnCompletionListener {
                if(isCheckAutoPlay)
                {
                    mMainHandler!!.removeMessages(MESSAGE_AUTO_PLAY)
                    mMainHandler!!.sendEmptyMessageDelayed(
                        MESSAGE_AUTO_PLAY, (mCurrentIntervalSecond * Common.SECOND).toLong()
                    )
                }
            }
        }
        catch(e : Exception)
        {
            Log.f("Exception : "+ e.message)
        }
    }

    /**
     * Audio Player 제거
     */
    private fun releaseAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mAudioAttributes = null
    }

    /**
     * ==============================
     *            DIALOG
     * ==============================
     */
    /**
     * 자동재생 시간 선택 다이얼로그 표시
     */
    private fun showBottomIntervalDialog()
    {
        mBottomFlashcardIntervalSelectDialog = BottomFlashcardIntervalSelectDialog(mContext, mCurrentIntervalSecond)
        mBottomFlashcardIntervalSelectDialog!!.setOnIntervalSelectListener(mIntervalSelectListener)
        mBottomFlashcardIntervalSelectDialog!!.show()
    }

    /**
     * 책장에 담기 다이얼로그 표시
     */
    private fun showBottomVocabularyAddDialog()
    {
        mBottomBookAddDialog = BottomBookAddDialog(mContext)
        mBottomBookAddDialog!!.setCancelable(true)
        mBottomBookAddDialog!!.setLandScapeMode()
        mBottomBookAddDialog!!.setVocabularyData(mMainInformationResult.getVocabulariesList())
        mBottomBookAddDialog!!.setBookSelectListener(mBookAddListener)
        mBottomBookAddDialog!!.show()
    }

    /**
     * 메세지 다이얼로그
     */
    private fun showTemplateDialog(message : String)
    {
        Log.f("")
        mTempleteAlertDialog = TemplateAlertDialog(mContext)
        mTempleteAlertDialog!!.setMessage(message)
        mTempleteAlertDialog!!.setButtonType(DialogButtonType.BUTTON_1)
        mTempleteAlertDialog!!.setGravity(Gravity.LEFT)
        mTempleteAlertDialog!!.show()
    }

    /**
     * 메세지 다이얼로그
     */
    private fun showTemplateDialog(message : String, messageType : Int)
    {
        Log.f("")
        mTempleteAlertDialog = TemplateAlertDialog(mContext)
        mTempleteAlertDialog!!.setMessage(message)
        mTempleteAlertDialog!!.setDialogEventType(messageType)
        mTempleteAlertDialog!!.setButtonType(DialogButtonType.BUTTON_2)
        mTempleteAlertDialog!!.setGravity(Gravity.LEFT)
        mTempleteAlertDialog!!.setDialogListener(mDialogListener)
        mTempleteAlertDialog!!.show()
    }

    /**
     * ==============================
     *        onClick Events
     * ==============================
     */
    /**
     * 소리버튼 클릭 이벤트 (소리 ON/OFF)
     */
    override fun onClickSound()
    {
        isSoundPlay = !isSoundPlay
        mFlashcardContractView.settingSoundButton(isSoundPlay)
    }

    /**
     * 자동넘기기 클릭 이벤트
     */
    override fun onCheckAutoPlay()
    {
        setCheckAutoPlay(isCheckAutoPlay == false)
    }

    /**
     * 섞어보기 클릭 이벤트
     */
    override fun onCheckShuffle()
    {
        setCheckShuffle(isCheckShuffle == false)
    }

    /**
     * 자동넘기기 시간선택 클릭 이벤트
     */
    override fun onClickAutoPlayInterval()
    {
        Log.f("")
        showBottomIntervalDialog()
    }

    /**
     * 닫기 버튼 클릭 이벤트
     */
    override fun onClickClose()
    {
        when(mCurrentFlashcardStatus)
        {
            FlashcardStatus.STUDY,
            FlashcardStatus.BOOKMARK_INTRO,
            FlashcardStatus.BOOKMARK_STUDY,
            FlashcardStatus.RESULT ->
            {
                if(isHaveBookmarkedItem())
                {
                    Log.f("mCurrentFlashcardStatus : $mCurrentFlashcardStatus, BookmarkItem Have")
                    isCheckAutoPlay = false
                    mMainHandler!!.removeMessages(MESSAGE_AUTO_PLAY)
                    mFlashcardContractView.checkAutoplayBox(mCurrentFlashcardStatus, isCheckAutoPlay)
                    showTemplateDialog(
                        mContext.resources.getString(R.string.message_warning_bookmark_init),
                        DIALOG_CLOSE_APP
                    )
                    return
                }
            }
            else -> {}
        }


        (mContext as AppCompatActivity).finish()
    }

    /**
     * 도움말 페이지 Back버튼 클릭 이벤트
     */
    override fun onClickHelpViewBack()
    {
        mFlashcardContractView.showBottomViewLayout()
        mFlashcardPresenterObserver.onCloseHelpView()
    }

    /**
     * ==============================
     *           Observer
     * ==============================
     */
    /**
     * 플래시카드 인트로화면 옵저버 세팅
     */
    private fun setupFlashcardIntroFragmentListener()
    {
        mFlashcardIntroFragmentObserver = ViewModelProviders.of((mContext as AppCompatActivity)).get(FlashcardIntroFragmentObserver::class.java)

        // 단어로 학습하기 버튼 클릭
        mFlashcardIntroFragmentObserver.startWordStudyData.observe(mContext as AppCompatActivity, Observer<Void?> {
            if(mCurrentFlashcardStudyType != null)
            {
                return@Observer
            }
            Log.f("단어로 학습하기 버튼 클릭")
            mCurrentFlashcardStudyType = FlashcardStudyType.WORD_START
            setPageAction(FlashcardStatus.STUDY)
        })

        // 뜻으로 학습하기 버튼 클릭
        mFlashcardIntroFragmentObserver.startMeaningStudyData.observe(mContext as AppCompatActivity, Observer<Void?> {
            if(mCurrentFlashcardStudyType != null)
            {
                return@Observer
            }
            Log.f("뜻으로 학습하기 버튼 클릭")
            mCurrentFlashcardStudyType = FlashcardStudyType.MEANING_START
            setPageAction(FlashcardStatus.STUDY)
        })

        // 도움말 버튼 클릭 (하단 체크박스 영역 숨김)
        mFlashcardIntroFragmentObserver.infoButtonData.observe(mContext as AppCompatActivity, Observer<Void?> {
            mFlashcardContractView.hideBottomViewLayout()
        })
    }

    /**
     * 플래시카드 학습화면 옵저버 세팅
     */
    private fun setupFlashcardStudyFragmentListener()
    {
        mFlashcardStudyFragmentObserver = ViewModelProviders.of((mContext as AppCompatActivity))[FlashcardStudyFragmentObserver::class.java]

        // 학습중인 화면 클릭 (자동재생 정지)
        mFlashcardStudyFragmentObserver.buttonClickData.observe((mContext as AppCompatActivity), {
            isCheckAutoPlay = false
            mMainHandler!!.removeMessages(MESSAGE_AUTO_PLAY)
            mFlashcardContractView.checkAutoplayBox(mCurrentFlashcardStatus, isCheckAutoPlay)
        })

        // 북마크 버튼 클릭
        mFlashcardStudyFragmentObserver.enableBookmarkData.observe((mContext as AppCompatActivity), { data ->
            Log.f("Bookmark ID : " + data.first + " , isEnable  : " + data.second)
            checkBookmarkItem(data.first, data.second)
        })

        // 자동 넘기기에 의한 사운드 재생
        mFlashcardStudyFragmentObserver.autoStartSoundData.observe((mContext as AppCompatActivity), { wordID ->
            startFlashcardAudio(wordID!!, false)
        })

        // 사용자 클릭에 의한 사운드 재생
        mFlashcardStudyFragmentObserver.touchStartSoundData.observe((mContext as AppCompatActivity), { wordID ->
            startFlashcardAudio(wordID!!, true)
        })

        // 학습종료
        mFlashcardStudyFragmentObserver.studyEndData.observe((mContext as AppCompatActivity), {
            if(mCurrentFlashcardStatus === FlashcardStatus.BOOKMARK_STUDY)
            {
                // 찜단어 학습일 때
                if(isHaveBookmarkedItem())
                {
                    Log.f("찜 단어 인트로 화면으로 이동 하기")
                    setPageAction(FlashcardStatus.BOOKMARK_INTRO)
                }
                else
                {
                    showTemplateDialog(mContext.resources.getString(R.string.message_warning_bookmark_empty))
                    setCheckAutoPlay(false)
                }
            }
            else
            {
                setPageAction(FlashcardStatus.RESULT)
            }
        })
    }

    /**
     * 플래시카드 결과화면 옵저버 세팅
     */
    private fun setupFlashcardResultFragmentListener()
    {
        mFlashcardResultFragmentObserver = ViewModelProviders.of((mContext as AppCompatActivity))[FlashcardResultFragmentObserver::class.java]

        // 다시 학습하기 버튼 클릭
        mFlashcardResultFragmentObserver.replayStudyData.observe((mContext as AppCompatActivity), {
            if(isHaveBookmarkedItem())
            {
                // 찜단어 있을 경우 초기화 알림 다이얼로그 표시
                showTemplateDialog(
                    mContext.resources.getString(R.string.message_warning_bookmark_init),
                    DIALOG_BOOKMARK_INIT
                )
            }
            else
            {
                // 인트로 화면으로 이동
                isGotoIntroPage = true
                mFlashcardContractView.forceChangePageView(0)
                setPageAction(FlashcardStatus.INTRO)
            }
        })

        // 찜단어 학습하기 버튼 클릭
        mFlashcardResultFragmentObserver.bookmarkStudyData.observe((mContext as AppCompatActivity), {
            Log.f("찜 단어 인트로 화면으로 이동 하기")
            setPageAction(FlashcardStatus.BOOKMARK_INTRO)
        })
    }

    /**
     * 플래시카드 북마크화면 옵저버 세팅
     */
    private fun setupFlashcardBookmarkFragmentListener()
    {
        mFlashcardBookmarkFragmentObserver = ViewModelProviders.of((mContext as AppCompatActivity))[FlashcardBookmarkFragmentObserver::class.java]

        // 찜단어 - 단어로 학습하기 버튼 클릭
        mFlashcardBookmarkFragmentObserver.startWordStudyData.observe((mContext as AppCompatActivity), Observer {
            Log.f("찜한 단어를 단어로 학습하기 버튼 클릭")
            if(mCurrentFlashcardStudyType != null)
            {
                return@Observer
            }

            mCurrentFlashcardStudyType = FlashcardStudyType.WORD_START
            setPageAction(FlashcardStatus.BOOKMARK_STUDY)
        })

        // 찜단어 - 뜻으로 학습하기 버튼 클릭
        mFlashcardBookmarkFragmentObserver.startMeaningStudyData.observe((mContext as AppCompatActivity), Observer {
            Log.f("찜한 단어를 뜻으로 학습하기 버튼 클릭")
            if(mCurrentFlashcardStudyType != null)
            {
                return@Observer
            }
            mCurrentFlashcardStudyType = FlashcardStudyType.MEANING_START
            setPageAction(FlashcardStatus.BOOKMARK_STUDY)
        })

        // 나의 단어장에 저장하기 버튼 클릭
        mFlashcardBookmarkFragmentObserver.saveVocabularyData.observe((mContext as AppCompatActivity), Observer {
            if(Feature.IS_FREE_USER) // TODO : 무료이용자 플래그 추후 제거 예정
            {
                mFlashcardContractView.showErrorMessage(mContext.resources.getString(R.string.message_payment_service_login))
                return@Observer
            }
            else
            {
                showBottomVocabularyAddDialog()
            }
        })

        // 북마크 버튼 클릭
        mFlashcardBookmarkFragmentObserver.enableBookmarkData.observe((mContext as AppCompatActivity), { data ->
            Log.f("Bookmark ID : " + data.first + " , isEnable  : " + data.second)
            checkBookmarkItem(data.first, data.second)
        })
    }

    /**
     * ==============================
     *           Listener
     * ==============================
     */
    /**
     * 통신 Response Listener
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            val result : BaseResult = mObject as BaseResult

            Log.f("code : $code, status : ${result.getStatus()}")
            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if (code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_LIST)
                {
                    mOriginCardList = (mObject as VocabularyContentsBaseObject).getData()
                    initStudyFlashcard()
                    initAudio()
                }
                else if (code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_ADD)
                {
                    mFlashcardContractView.hideLoading()
                    val myVocabularyResult : MyVocabularyResult = (mObject as VocabularyShelfBaseObject).getData()
                    updateVocabularyData(myVocabularyResult)

                    val message : Message = Message.obtain()
                    message.what = MESSAGE_ALERT_TOAST
                    message.obj = mContext.resources.getString(R.string.message_success_save_contents_in_vocabulary)
                    message.arg1 = RESULT_OK
                    mMainHandler!!.sendMessageDelayed(message, Common.DURATION_NORMAL)
                }
                else if(code == Common.COROUTINE_CODE_FLASHCARD_SAVE)
                {
                    Log.f("SAVE FLASHCARD SUCCESS")
                    mFlashcardContractView.hideLoading()
                }
            }
            else
            {
                if (result.isDuplicateLogin)
                {
                    // 중복 로그인 재시작
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
                    if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_LIST ||
                        code == Common.COROUTINE_CODE_VOCABULARY_SHELF)
                    {
                        mFlashcardContractView.hideLoading()
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if(code == Common.COROUTINE_CODE_VOCABULARY_CONTENTS_ADD)
                    {
                        Log.f("FAIL ASYNC_CODE_VOCABULARY_CONTENTS_ADD")
                        mFlashcardContractView.hideLoading()
                        val message = Message.obtain()
                        message.what = MESSAGE_ALERT_TOAST
                        message.obj = result.getMessage()
                        message.arg1 = Activity.RESULT_CANCELED
                        mMainHandler!!.sendMessageDelayed(message, Common.DURATION_SHORT)
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            when(eventType)
            {
                DIALOG_CLOSE_APP -> if(buttonType == DialogButtonType.BUTTON_2)
                {
                    (mContext as AppCompatActivity).finish()
                }
                DIALOG_BOOKMARK_INIT -> if(buttonType == DialogButtonType.BUTTON_2)
                {
                    clearBookmarkList()
                    isGotoIntroPage = true
                    mFlashcardContractView.forceChangePageView(0)
                    setPageAction(FlashcardStatus.INTRO)
                }
            }
        }
    }

    /**
     * 자동 넘기기 시간 선택 다이얼로그 Listener
     */
    private val mIntervalSelectListener : IntervalSelectListener = object : IntervalSelectListener
    {
        override fun onClickIntervalSecond(second : Int)
        {
            Log.f("second : $second")
            mCurrentIntervalSecond = second
            mFlashcardContractView.settingAutoPlayInterval(second)
        }
    }

    /**
     * 책장에 추가 다이얼로그 Listener
     */
    private val mBookAddListener : BookAddListener = object : BookAddListener
    {
        override fun onClickBook(index : Int)
        {
            Log.f("index : $index")
            mCurrentVocabularyAddResult = mMainInformationResult.getVocabulariesList()[index]
            mFlashcardContractView.showLoading()
            requestVocabularyContentsAddAsync()
        }
    }
}