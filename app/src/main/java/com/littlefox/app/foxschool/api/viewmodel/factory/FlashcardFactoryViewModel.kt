package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.FlashcardSelectionPagerAdapter
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.fragment.FlashcardFragmentViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.FlashcardApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.database.CoachmarkDao
import com.littlefox.app.foxschool.database.CoachmarkDatabase
import com.littlefox.app.foxschool.database.CoachmarkEntity
import com.littlefox.app.foxschool.dialog.BottomBookAddDialog
import com.littlefox.app.foxschool.dialog.BottomFlashcardIntervalSelectDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.enumerate.FlashcardStudyType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class FlashcardFactoryViewModel @Inject constructor(private val apiViewModel : FlashcardApiViewModel) : BaseFactoryViewModel()
{
    companion object
    {
        const val DIALOG_CLOSE_APP : Int = 10001
        const val DIALOG_BOOKMARK_INIT : Int = 10002

        private const val DEFAULT_INTERVAL_SECOND : Int = 3
    }

    private val _showPagerView = SingleLiveEvent<FlashcardSelectionPagerAdapter>()
    val showPagerView : LiveData<FlashcardSelectionPagerAdapter> = _showPagerView

    private val _settingSoundButton = SingleLiveEvent<Boolean>()
    val settingSoundButton : LiveData<Boolean> = _settingSoundButton

    private val _settingAutoPlayInterval = SingleLiveEvent<Int>()
    val settingAutoPlayInterval : LiveData<Int> = _settingAutoPlayInterval

    private val _settingBaseControlView = SingleLiveEvent<FlashcardStatus>()
    val settingBaseControlView : LiveData<FlashcardStatus> = _settingBaseControlView

    private val _checkAutoplayBox = SingleLiveEvent<Pair<FlashcardStatus, Boolean>>()
    val checkAutoplayBox : LiveData<Pair<FlashcardStatus, Boolean>> = _checkAutoplayBox

    private val _checkShuffleBox = SingleLiveEvent<Boolean>()
    val checkShuffleBox : LiveData<Boolean> = _checkShuffleBox

    private val _showCoachMarkView = SingleLiveEvent<Void>()
    val showCoachMarkView : LiveData<Void> = _showCoachMarkView

    private val _prevPageView = SingleLiveEvent<Void>()
    val prevPageView : LiveData<Void> = _prevPageView

    private val _nextPageView = SingleLiveEvent<Void>()
    val nextPageView : LiveData<Void> = _nextPageView

    private val _enableBottomViewLayout = SingleLiveEvent<Boolean>()
    val enableBottomViewLayout : LiveData<Boolean> = _enableBottomViewLayout

    private val _forceChangePageView = SingleLiveEvent<Int>()
    val forceChangePageView : LiveData<Int> = _forceChangePageView

    private val _dialogReplayWarningBookmark = SingleLiveEvent<Void>()
    val dialogReplayWarningBookmark : LiveData<Void> = _dialogReplayWarningBookmark

    private val _dialogCloseWarningBookmark = SingleLiveEvent<Void>()
    val dialogCloseWarningBookmark : LiveData<Void> = _dialogCloseWarningBookmark

    private val _dialogEmptyBookmark = SingleLiveEvent<Void>()
    val dialogEmptyBookmark : LiveData<Void> = _dialogEmptyBookmark

    private val _dialogBottomVocabularyContentAdd = SingleLiveEvent<ArrayList<MyVocabularyResult>>()
    val dialogBottomVocabularyContentAdd: LiveData<ArrayList<MyVocabularyResult>> get() = _dialogBottomVocabularyContentAdd


    // Presenter 기본적인 변수
    private lateinit var mContext : Context
    private var mFlashcardDisplayFragmentList : ArrayList<Fragment> = ArrayList()
    private lateinit var mFlashcardSelectionPagerAdapter : FlashcardSelectionPagerAdapter
    private var mMainHandler : WeakReferenceHandler? = null

    // Fragment 관련 변수
    private var mCurrentFlashcardStatus : FlashcardStatus = FlashcardStatus.INTRO

    // 플래시카드 데이터 변수
    private lateinit var mFlashcardDataObject : FlashcardDataObject
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mUserInformationResult : LoginInformationResult
    private var mOriginCardList : ArrayList<VocabularyDataResult>? = null
    private var mCurrentVocabularyAddResult : MyVocabularyResult? = null
    private var mCurrentStudyCardList : ArrayList<FlashCardDataResult>? = null
    private var mCurrentBookmarkCardList : ArrayList<FlashCardDataResult>? = null
    private var mCurrentFlashcardStudyType : FlashcardStudyType? = null
    private var mCoachingMarkUserDao : CoachmarkDao? = null

    // 플레이 관련 변수
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null
    private var mCurrentIntervalSecond : Int = DEFAULT_INTERVAL_SECOND // 자동재생 시간
    private var isSoundPlay : Boolean = true  // 사운드 플레이
    private var isCheckAutoPlay : Boolean = false // 자동재생
    private var isGotoIntroPage : Boolean = false // 처음으로 돌아가기
    private var isCheckShuffle : Boolean = false // 섞어보기 체크
    private var mCurrentUserID : String = ""
    private var mCurrentPageIndex : Int = 0
    private var mAutoPlayJob: Job? = null
    private lateinit var fragmentViewModel: FlashcardFragmentViewModel

    override fun init(context : Context)
    {
        mContext = context
        fragmentViewModel = ViewModelProvider(mContext as AppCompatActivity).get(
            FlashcardFragmentViewModel::class.java)
        setupViewModelObserver()
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mCoachingMarkUserDao = CoachmarkDatabase.getInstance(mContext)?.coachmarkDao()
        mUserInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
        mFlashcardDataObject = (mContext as AppCompatActivity).intent.getParcelableExtra<Parcelable>(
            Common.INTENT_FLASHCARD_DATA) as FlashcardDataObject

        mFlashcardDisplayFragmentList = ArrayList()
        mFlashcardSelectionPagerAdapter = FlashcardSelectionPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mFlashcardDisplayFragmentList)
        mFlashcardSelectionPagerAdapter.addFragment(FlashcardStatus.INTRO)
        _showPagerView.value = mFlashcardSelectionPagerAdapter

        mCurrentUserID = mUserInformationResult.getUserInformation().getFoxUserID()

        // 플래시카드 안내 화면 표시해야하는지 체크
        CoroutineScope(Dispatchers.Main).launch {
            if(isShowCoachMark(mCurrentUserID))
            {
                _showCoachMarkView.call()
            }
            else
            {
                readyToStudy()
            }
        }
        _settingAutoPlayInterval.value = mCurrentIntervalSecond
    }

    override fun resume()
    {

    }

    override fun pause()
    {

    }

    override fun destroy()
    {
        isCheckAutoPlay = false
        releaseAudio()
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect { data ->
                data?.let {
                    if (data.first == RequestCode.CODE_FLASHCARD_RECORD_SAVE ||
                        data.first == RequestCode.CODE_VOCABULARY_CONTENTS_ADD )
                    {
                        if(data.second)
                        {
                            _isLoading.postValue(true)
                        }
                        else
                        {
                            _isLoading.postValue(false)
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.getVocabularyContentsListData.collect { data ->
                data?.let {
                    mOriginCardList = data
                    initStudyFlashcard()
                    initAudio()
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.addVocabularyContentsData.collect{ data ->
                data?.let {
                    updateVocabularyData(data)
                    viewModelScope.launch(Dispatchers.Main) {
                        delay(Common.DURATION_NORMAL)
                        _successMessage.value = mContext.resources.getString(R.string.message_success_save_contents_in_vocabulary)
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.saveFlashcardData.collect{ data ->
                Log.f("SAVE FLASHCARD SUCCESS")
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect {data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    Log.f("status : ${result.status}, message : ${result.message} , code : $code")
                    if(result.isDuplicateLogin)
                    { // 중복 로그인 재시작
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if(result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show()
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        if(code == RequestCode.CODE_VOCABULARY_CONTENTS_LIST)
                        {
                            _toast.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                        else if(code == RequestCode.CODE_VOCABULARY_CONTENTS_ADD)
                        {
                            Log.f("FAIL ASYNC_CODE_VOCABULARY_CONTENTS_ADD")
                            viewModelScope.launch(Dispatchers.Main) {
                                delay(Common.DURATION_SHORT)
                                _errorMessage.value = result.message
                            }
                        }
                        else if(code == RequestCode.CODE_FLASHCARD_RECORD_SAVE)
                        {
                            Log.f("SAVE FLASHCARD FAIL")
                        }
                    }
                }
            }
        }

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
     * 학습 준비
     *  - 단어장 화면에서 넘어온 경우 플래시카드 아이템 생성
     *  - 컨텐츠 화면에서 넘어온 경우 단어장 통신 요청
     */
    private fun readyToStudy()
    {
        _isLoading.value = true
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
                viewModelScope.launch(Dispatchers.Main) {
                    requestVocabularyContentsListAsync()
                }
            }
        }
        fragmentViewModel.onSetIntroTitle(mFlashcardDataObject)
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
        _checkShuffleBox.value = isCheckShuffle

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
        _checkAutoplayBox.value = Pair(mCurrentFlashcardStatus, isCheckAutoPlay)

        if (mCurrentFlashcardStatus == FlashcardStatus.STUDY ||
            mCurrentFlashcardStatus == FlashcardStatus.BOOKMARK_STUDY)
        {
            if (isCheckAutoPlay)
            {
                Log.f("학습 중 자동넘기기 ON")
                enableAutoPlay(true)
            }
            else
            {
                Log.f("학습중 자동넘기기 OFF")
                enableAutoPlay(false)
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
            mMediaPlayer!!.run {
                setDataSource(mCurrentStudyCardList?.get(0)?.getSoundURL())
                prepareAsync()
                setOnPreparedListener {
                    _isLoading.value = false
                    Log.f("Init Prepare Complete")
                }
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
                enableAutoPlay(true)
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

            mMediaPlayer!!.run {
                setDataSource(item.getSoundURL())
                prepareAsync()
                setOnPreparedListener { mMediaPlayer!!.start() }
                setOnCompletionListener {
                    if(isCheckAutoPlay)
                    {
                        enableAutoPlay(true)
                    }
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
                    _settingBaseControlView.value = mCurrentFlashcardStatus
                    initBookmarkFlashcard()
                    mFlashcardSelectionPagerAdapter.addBookmarkFragment(
                        mFlashcardDataObject.getVocabularyType(),
                        mCurrentBookmarkCardList!!
                    )
                    _nextPageView.call()
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
                        fragmentViewModel.onSetFlashcardData(mCurrentStudyCardList!!)
                    }
                } else
                {
                    if(isHaveBookmarkedItem() == false)
                    {
                        mCurrentFlashcardStudyType = null
                        _errorMessage.value = mContext.resources.getString(R.string.message_warning_bookmark_empty)
                        return
                    }
                    initBookmarkFlashcard()
                    mFlashcardSelectionPagerAdapter.addStudyDataFragment(mCurrentBookmarkCardList!!)
                }
                fragmentViewModel.onSettingFlashcardView(mCurrentFlashcardStudyType!!)
                _settingBaseControlView.value = mCurrentFlashcardStatus
                _nextPageView.call()
            }

            FlashcardStatus.RESULT ->
            {
                fragmentViewModel.onSettingBookmarkButton(
                    isHaveBookmarkedItem()
                )
                _settingBaseControlView.value = mCurrentFlashcardStatus
                _nextPageView.call()
                requestFlashcardSaveAsync()
            }
        }
    }

    private fun enableAutoPlay(isEnable : Boolean)
    {
        if(isEnable)
        {
            mAutoPlayJob?.cancel()
            mAutoPlayJob  = viewModelScope.launch(Dispatchers.Default) {
                delay((mCurrentIntervalSecond * Common.SECOND).toLong())
                fragmentViewModel.onShowNextCard()
            }
        }
        else
        {
            mAutoPlayJob?.cancel()
            mAutoPlayJob = null
        }
    }

    private fun requestVocabularyContentsListAsync()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_LIST, mFlashcardDataObject.getContentID()
        )
    }

    private fun requestVocabularyContentsAddAsync()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_ADD,
            mFlashcardDataObject.getContentID(),
            mCurrentVocabularyAddResult?.getID(),
            getBookmarkedVocabularyItemList()
        )
    }

    private fun requestFlashcardSaveAsync()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_FLASHCARD_RECORD_SAVE,
            mFlashcardDataObject.getContentID()
        )
    }

    fun onClickSound()
    {
        isSoundPlay = !isSoundPlay
        _settingSoundButton.value = isSoundPlay
    }
    
    fun onCheckAutoPlay()
    {
        val isEnable =  !isCheckAutoPlay
        setCheckAutoPlay(isEnable)
    }
    
    fun onCheckShuffle()
    {
        val isEnable =  !isCheckShuffle
        setCheckShuffle(isEnable)
    }

    fun onClickClose()
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
                    enableAutoPlay(false)
                    _checkAutoplayBox.value = Pair(mCurrentFlashcardStatus, isCheckAutoPlay)
                    _dialogCloseWarningBookmark.call()
                    return
                }
            }
            else -> {}
        }
        (mContext as AppCompatActivity).finish()
    }
    
    fun onClickHelpViewBack()
    {
        _enableBottomViewLayout.value = true
        fragmentViewModel.onCloseHelpView()
    }
    
    fun onFlashCardPageSelected(pageIndex : Int)
    {
        if(isGotoIntroPage && pageIndex == 0)
        {
            _settingBaseControlView.value = FlashcardStatus.INTRO
        }
        mCurrentPageIndex = pageIndex
    }
    
    fun onCoachMarkNeverSeeAgain()
    {
        Log.f("")
        setFlashcardCoachMarkViewed(mCurrentUserID)
        readyToStudy()
    }

    fun onClickIntervalSecond(second : Int)
    {
        Log.f("second : $second")
        mCurrentIntervalSecond = second
        _settingAutoPlayInterval.value = second
    }

    fun onClickVocabularyBook(index : Int)
    {
        Log.f("index : $index")
        mCurrentVocabularyAddResult = mMainInformationResult.getVocabulariesList()[index]
        requestVocabularyContentsAddAsync()
    }

    /**
     * FlashcardIntroFragment 에서 단어 스터디 버튼 클릭
     */
    fun onClickStartWordStudy()
    {
        if(mCurrentFlashcardStudyType != null)
        {
            return
        }
        Log.f("단어로 학습하기 버튼 클릭")
        mCurrentFlashcardStudyType = FlashcardStudyType.WORD_START
        setPageAction(FlashcardStatus.STUDY)
    }

    /**
     * FlashcardIntroFragment 에서 뜻 스터디 버튼 클릭
     */
    fun onClickStartMeaningStudy()
    {
        if(mCurrentFlashcardStudyType != null)
        {
            return
        }
        Log.f("뜻으로 학습하기 버튼 클릭")
        mCurrentFlashcardStudyType = FlashcardStudyType.MEANING_START
        setPageAction(FlashcardStatus.STUDY)
    }

    /**
     * FlashcardIntroFragment 에서 플래시 카드 Info 버튼 클릭
     */
    fun onClickInformation()
    {
        _enableBottomViewLayout.value = false
    }

    /**
     * FlashcardStudyFragment, FlashcardBookmarkFragment 에서 찜하기 아이템 활성화/비활성화 클릭
     */
    fun onClickBookmark(wordID : String, isEnable : Boolean)
    {
        Log.f("Bookmark ID : " + wordID + " , isEnable  : " + isEnable)
        checkBookmarkItem(wordID, isEnable)
    }

    /**
     * FlashcardStudyFragment 에서 해당 단어 사운드 버튼 클릭
     */
    fun onClickSound(wordID : String)
    {
        Log.f("wordID : ${wordID}")
        startFlashcardAudio(wordID, true)
    }

    /**
     * FlashcardStudyFragment 에서 자동 넘기기 상태 일때 해당 단어의 사운드 재생
     */
    fun onActionAutoSound(wordID : String)
    {
        Log.f("wordID : ${wordID}")
        startFlashcardAudio(wordID, false)
    }

    /**
     * FlashcardStudyFragment 에서 마지막 카드 상태 일때 전달
     */
    fun onEndStudyFlashCard()
    {
        Log.f("")
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
                _dialogEmptyBookmark.call()
                setCheckAutoPlay(false)
            }
        }
        else
        {
            setPageAction(FlashcardStatus.RESULT)
        }
    }

    /**
     * FlashcardStudyFragment 에서 카드를 터치 했는 지의 유/무를 알려줘 자동넘기기를 멈추기 위해 사용
     */
    fun onActionStudyCard()
    {
        Log.f("")
        isCheckAutoPlay = false
        enableAutoPlay(false)
        _checkAutoplayBox.value = Pair(mCurrentFlashcardStatus, isCheckAutoPlay)
    }

    /**
     * FlashcardResultFragment 에서 Replay 버튼 클릭 시
     */
    fun onClickReplayStudy()
    {
        if(isHaveBookmarkedItem())
        {
            // 찜단어 있을 경우 초기화 알림 다이얼로그 표시
            _dialogReplayWarningBookmark.call()
        }
        else
        {
            // 인트로 화면으로 이동
            isGotoIntroPage = true
            _forceChangePageView.value = 0
            setPageAction(FlashcardStatus.INTRO)
        }
    }

    /**
     * FlashcardResultFragment 에서 찜하기 버튼 클릭 시
     */
    fun onClickBookmarkStudy()
    {
        Log.f("찜 단어 인트로 화면으로 이동 하기")
        setPageAction(FlashcardStatus.BOOKMARK_INTRO)
    }

    /**
     * FlashcardBookmarkFragment 에서 나의 단어장에 저장 버튼 클릭
     */
    fun onClickSaveVocabulary()
    {
        _dialogBottomVocabularyContentAdd.value = mMainInformationResult.getVocabulariesList()
    }

    /**
     * FlashcardBookmarkFragment 에서 찜한 단어를 단어 스터디 버튼 클릭
     */
    fun onClickStartWordStudyBookmark()
    {
        Log.f("찜한 단어를 단어로 학습하기 버튼 클릭")
        if(mCurrentFlashcardStudyType != null)
        {
            return
        }

        mCurrentFlashcardStudyType = FlashcardStudyType.WORD_START
        setPageAction(FlashcardStatus.BOOKMARK_STUDY)
    }

    /**
     * FlashcardBookmarkFragment 에서 찜한 단어를 뜻 스터디 버튼 클릭
     */
    fun onClickStartMeaningStudyBookmark()
    {
        Log.f("찜한 단어를 뜻으로 학습하기 버튼 클릭")
        if(mCurrentFlashcardStudyType != null)
        {
            return
        }
        mCurrentFlashcardStudyType = FlashcardStudyType.MEANING_START
        setPageAction(FlashcardStatus.BOOKMARK_STUDY)
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        when(eventType)
        {
            DIALOG_CLOSE_APP ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    (mContext as AppCompatActivity).finish()
                }
            }
            DIALOG_BOOKMARK_INIT ->
            {
                if(buttonType == DialogButtonType.BUTTON_2)
                {
                    clearBookmarkList()
                    isGotoIntroPage = true
                    _forceChangePageView.value = 0
                    setPageAction(FlashcardStatus.INTRO)
                }
            }
        }
    }
}