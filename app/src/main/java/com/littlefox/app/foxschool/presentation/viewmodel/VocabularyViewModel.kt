package com.littlefox.app.foxschool.presentation.viewmodel

import VocabularySelectData
import android.content.Context
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.VocabularyApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Event
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyBottomBarMenu
import com.littlefox.app.foxschool.enumerate.VocabularyTopBarMenu
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.presenter.VocabularyPresenter
import com.littlefox.app.foxschool.main.presenter.VocabularyPresenter.Companion

import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.vocabulary.VocabularyEvent
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class VocabularyViewModel @Inject constructor(private val apiViewModel : VocabularyApiViewModel) : BaseViewModel()
{

    companion object
    {
        const val DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS : Int = 10001
        const val INDEX_UPDATE_VOCABULARY : Int                 = 0

    }

    private val _vocabularyContentsList = SingleLiveEvent<EventWrapper<ArrayList<VocabularyDataResult>>>()
    val vocabularyContentsList: LiveData<EventWrapper<ArrayList<VocabularyDataResult>>> get() = _vocabularyContentsList

    private val _itemSelectedCount = SingleLiveEvent<EventWrapper<Int>>()
    val itemSelectedCount: LiveData<EventWrapper<Int>> get() = _itemSelectedCount

    private val _vocabularyTitle = SingleLiveEvent<String>()
    val vocabularyTitle: LiveData<String> get() = _vocabularyTitle

    private val _vocabularyType = SingleLiveEvent<VocabularyType>()
    val vocabularyType: LiveData<VocabularyType> get() = _vocabularyType

    private val _intervalSecond = SingleLiveEvent<Int>()
    val intervalSecond: LiveData<Int> get() = _intervalSecond

    private val _isContentsLoading = SingleLiveEvent<Boolean>()
    val isContentsLoading: LiveData<Boolean> get() = _isContentsLoading

    private val _vocabularySelectType = SingleLiveEvent<VocabularySelectData>()
    val vocabularySelectType: LiveData<VocabularySelectData> get() = _vocabularySelectType

    private val _currentPlayingIndex = SingleLiveEvent<Int>()
    val currentPlayingIndex: LiveData<Int> get() = _currentPlayingIndex

    private val _isPlayingStatus = SingleLiveEvent<Boolean>()
    val isPlayingStatus: LiveData<Boolean> get() = _isPlayingStatus

    private val _dialogIntervalSelect = SingleLiveEvent<Int>()
    val dialogIntervalSelect: LiveData<Int> get() = _dialogIntervalSelect

    private val _dialogBottomVocabularyContentsAdd = SingleLiveEvent<ArrayList<MyVocabularyResult>>()
    val dialogBottomVocabularyContentsAdd: LiveData<ArrayList<MyVocabularyResult>> get() = _dialogBottomVocabularyContentsAdd

    private val _dialogVocabularyContentsDelete = SingleLiveEvent<Void>()
    val dialogVocabularyContentsDelete: LiveData<Void> get() = _dialogVocabularyContentsDelete

    private var mVocabularyItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
    private var mRequestItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()
    private var mSelectedPlayItemList : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()

    private lateinit var mVocabularySelectData : VocabularySelectData
    private lateinit var mMainInformationResult : MainInformationResult
    private lateinit var mCurrentMyVocabularyResult : MyVocabularyResult

    private lateinit var mContext : Context
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null
    private var mCurrentIntervalSecond : Int = 2
    private var mCurrentPlayIndex : Int = 0
    private var isSequencePlay : Boolean = false
    private var isPause : Boolean = false

    private var mSequencePlayJob: Job? = null


    override fun init(context : Context)
    {
        mContext = context
        mCurrentMyVocabularyResult = (mContext as AppCompatActivity).intent.getParcelableExtra(
            Common.INTENT_VOCABULARY_DATA)!!
        mVocabularySelectData = VocabularySelectData()
        _vocabularyTitle.value = mCurrentMyVocabularyResult.getName()
        _vocabularyType.value = mCurrentMyVocabularyResult.getVocabularyType()
        settingData()
        setupMediaPlayer()
        onHandleApiObserver()
        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                delay(Common.DURATION_NORMAL)
            }
            if(mCurrentMyVocabularyResult.getVocabularyType() == VocabularyType.VOCABULARY_CONTENTS)
            {
                requestVocabularyContentsListAsync()
            }
            else if(mCurrentMyVocabularyResult.getVocabularyType() == VocabularyType.VOCABULARY_SHELF)
            {
                requestVocabularyShelfContentsAsync()
            }
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.DialogChoiceClick ->
            {
                onDialogChoiceClick(
                    event.buttonType,
                    event.eventType
                )
            }

            is VocabularyEvent.onClickTopBarMenu ->
            {
                Log.i("receive : $event.menu")
                when(event.menu)
                {
                    VocabularyTopBarMenu.ALL ->
                    {
                        mVocabularySelectData.setSelectAll()
                        _vocabularySelectType.value = VocabularySelectData(mVocabularySelectData)
                    }

                    VocabularyTopBarMenu.WORD ->
                    {
                        mVocabularySelectData.setSelectWord()
                        _vocabularySelectType.value = VocabularySelectData(mVocabularySelectData)
                    }
                    VocabularyTopBarMenu.MEANING ->
                    {
                        mVocabularySelectData.setSelectMeaning()
                        _vocabularySelectType.value = VocabularySelectData(mVocabularySelectData)
                    }
                    VocabularyTopBarMenu.EXAMPLE ->
                    {
                        mVocabularySelectData.setSelectExample()
                        _vocabularySelectType.value = VocabularySelectData(mVocabularySelectData)
                    }
                }
            }

            is VocabularyEvent.onClickBottomBarMenu ->
            {
                when(event.menu)
                {
                    VocabularyBottomBarMenu.INTERVAL ->
                    {
                        Log.f("")
                        _dialogIntervalSelect.value = mCurrentIntervalSecond
                    }
                    VocabularyBottomBarMenu.SELECT_ALL ->
                    {
                        checkSelectedDataAll(true)
                    }
                    VocabularyBottomBarMenu.SELECT_CLEAR ->
                    {
                        checkSelectedDataAll(false)
                    }
                    VocabularyBottomBarMenu.SELECT_PLAY_START ->
                    {
                        onClickBottomPlayAction(true)
                    }
                    VocabularyBottomBarMenu.SELECT_PLAY_STOP ->
                    {
                        onClickBottomPlayAction(false)
                    }
                    VocabularyBottomBarMenu.FLASHCARD ->
                    {
                        startFlashcardActivity()
                    }
                    VocabularyBottomBarMenu.MY_VOCABULARY_CONTENTS_ADD ->
                    {
                        onClickBottomPutInVocabularyShelf()
                    }
                    VocabularyBottomBarMenu.MY_VOCABULARY_CONTENTS_DELETE ->
                    {
                        onClickBottomDeleteInVocabularyShelf()
                    }
                }
            }

            is VocabularyEvent.onSelectIntervalSecond ->
            {
                mCurrentIntervalSecond = event.second
                CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_VOCABULARY_INTERVAL, mCurrentIntervalSecond)
                _intervalSecond.value = mCurrentIntervalSecond
            }

            is VocabularyEvent.onSelectItem ->
            {
                setItemSelected(event.index)
            }
            is VocabularyEvent.onPlayContents ->
            {
                mCurrentPlayIndex = event.index
                startAudio(mVocabularyItemList)
            }
            is VocabularyEvent.onAddContentsInVocabulary ->
            {
                val vocabularyID = mMainInformationResult.getVocabulariesList()[event.index].getID()
                mRequestItemList.clear()
                mRequestItemList = getSelectedItemList()
                requestVocabularyContentsAddAsync(vocabularyID)
            }
        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.isLoading.collect{ data ->
                    data?.let {
                        if(it.first == RequestCode.CODE_VOCABULARY_CONTENTS_ADD
                            || it.first == RequestCode.CODE_VOCABULARY_CONTENTS_DELETE)
                        {
                            if(it.second)
                            {
                                _isLoading.value = true
                            }
                            else
                            {
                                _isLoading.value = false
                            }
                        }

                    }

                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.contentsListData.collect{ data ->
                    data?.let {
                        mVocabularyItemList = data
                        measureContentsViewSize()
                        _isContentsLoading.value = false

                        Log.f("size : ${mVocabularyItemList.size}")

                        viewModelScope.launch {
                            withContext(Dispatchers.Main)
                            {
                                delay(Common.DURATION_SHORT)
                            }
                            _vocabularyContentsList.value = EventWrapper(mVocabularyItemList)
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.addVocabularyContentsData.collect{ data ->
                    data?.let {
                        updateVocabularyData(data)
                        checkSelectedDataAll(false)
                        viewModelScope.launch {
                            withContext(Dispatchers.Main)
                            {
                                delay(Common.DURATION_NORMAL)
                            }
                            _successMessage.value = mContext.resources.getString(R.string.message_success_save_contents_in_vocabulary)
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.deleteVocabularyContentsData.collect{ data ->
                    data?.let {
                        refreshVocabularyItemData()
                        checkSelectedDataAll(false)
                        viewModelScope.launch {
                            withContext(Dispatchers.Main)
                            {
                                delay(Common.DURATION_NORMAL)
                            }
                            _successMessage.value = mContext.resources.getString(R.string.message_success_delete_contents)
                        }
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
                            (mContext as AppCompatActivity).finish()
                            _toast.value = result.message
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            (mContext as AppCompatActivity).finish()
                            _toast.value = result.message
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else
                        {
                            if(code == RequestCode.CODE_VOCABULARY_CONTENTS_LIST)
                            {
                                _toast.value = result.message
                                (mContext as AppCompatActivity).onBackPressed()
                            }
                            else
                            {
                                _isLoading.value = false
                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    _errorMessage.value = result.message
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun resume()
    {

    }

    override fun pause()
    {

    }

    override fun destroy()
    {

    }

    private fun settingData()
    {
        mCurrentIntervalSecond = CommonUtils.getInstance(mContext).getSharedPreferenceInteger(Common.PARAMS_VOCABULARY_INTERVAL, mCurrentIntervalSecond)
        _intervalSecond.value = mCurrentIntervalSecond
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        _isContentsLoading.value = true
    }

    private fun setupMediaPlayer()
    {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener
        {
            override fun onCompletion(mediaPlayer : MediaPlayer)
            {
                Log.f("audio complete ")
                if(isSequencePlay)
                {
                    Log.f("isSequencePlay currentIndex : " + mCurrentPlayIndex + " , list size : " + mSelectedPlayItemList.size)
                    if(mCurrentPlayIndex >= mSelectedPlayItemList.size - 1)
                    {
                        mCurrentPlayIndex = 0
                    }
                    else
                    {
                        mCurrentPlayIndex += 1
                    }
                    enableSequencePlayAudio(true)
                }
            }
        })
    }

    private fun setStatusCurrentItem()
    {
        Log.f("current Playing Index: $mCurrentPlayIndex")
        _currentPlayingIndex.value = mCurrentPlayIndex
    }

    private fun startAudio(playList : ArrayList<VocabularyDataResult>)
    {
        Log.f("startAudio")
        if(mMediaPlayer != null)
        {
            mMediaPlayer?.reset()
        }
        else
        {
            mMediaPlayer = MediaPlayer()
        }
        try
        {
            Log.f("Play Word : " + playList[mCurrentPlayIndex].getWordText())
            Log.f("Play URL : " + playList[mCurrentPlayIndex].getSoundURL())
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mMediaPlayer!!.run {
                setDataSource(playList[mCurrentPlayIndex].getSoundURL())
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
            }
        }
        catch(e : Exception)
        {
            Log.f("Exception : " + e.message)
        }
    }

    private fun releaseAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mAudioAttributes = null
    }

    private fun enableSequencePlayAudio(isEnable: Boolean)
    {
        if(isEnable)
        {
            mSequencePlayJob?.cancel()
            mSequencePlayJob = viewModelScope.launch {
                withContext(Dispatchers.IO)
                {
                    delay(mCurrentIntervalSecond * Common.SECOND + Common.DURATION_SHORT)
                }
                setStatusCurrentItem()
                startAudio(mSelectedPlayItemList)
            }

        }
        else
        {
            mSequencePlayJob?.cancel()
        }
    }

    /**
     * 예문이나 뜻의 길이에 따라 컨텐츠 뷰 크기가 동적으로 변경되어하는 작업을 담당하는 변수
     */
    private fun measureContentsViewSize()
    {
        val fontSize : Float =
            if(CommonUtils.getInstance(mContext).checkTablet)
                CommonUtils.getInstance(mContext).getPixel(28.0f)
            else
                CommonUtils.getInstance(mContext).getPixel(38.0f)
        var widthSize : Float =
            if(CommonUtils.getInstance(mContext).checkTablet)
                CommonUtils.getInstance(mContext).getPixel(864.0f)
            else
                CommonUtils.getInstance(mContext).getPixel(940.0f)
        var meaningLineSize = 0
        var ExampleLineSize = 0
        var meaningText = ""
        var htmlRemovedExampleText : String? = ""
        if(Feature.IS_MINIMUM_DISPLAY_SIZE)
        {
            Log.f("IS_MINIMUM_DISPLAY_SIZE")
            widthSize = CommonUtils.getInstance(mContext).getPixel(364.0f)
        }
        val paint = Paint()
        paint.typeface = Font.getInstance(mContext).getTypefaceRegular()
        paint.textSize = fontSize.toFloat()
        var i = 0
        while(i < mVocabularyItemList.size)
        {
            meaningText = mVocabularyItemList[i].getMeaningText()
            htmlRemovedExampleText = CommonUtils.getInstance(mContext).removeHtmlTag(mVocabularyItemList[i].getExampleText())
            if(meaningText.equals("")
                || htmlRemovedExampleText.equals(""))
            {
                Log.f("meaningText == empty or htmlRemovedExampleText empty : index = $i")
                mVocabularyItemList.removeAt(i--)
                i++
                continue
            }
            meaningLineSize = CommonUtils.getInstance(mContext).splitWordsIntoStringsThatFit(meaningText, widthSize, paint).size
            ExampleLineSize = CommonUtils.getInstance(mContext).splitWordsIntoStringsThatFit(htmlRemovedExampleText, widthSize, paint).size
            mVocabularyItemList[i].setContentViewSize(CommonUtils.getInstance(mContext).getVocabularyContentViewSize(meaningLineSize + ExampleLineSize + 1))
            i++
        }
    }

    private fun checkSelectedDataAll(isSelected : Boolean)
    {
        Log.f("isSelected : $isSelected")
        mVocabularyItemList.forEach {
            it.setSelected(isSelected)
        }

        _vocabularyContentsList.value = EventWrapper(mVocabularyItemList)

        _itemSelectedCount.value = when(isSelected)
        {
            true -> EventWrapper(mVocabularyItemList.size)
            false -> EventWrapper(0)
        }
    }

    private fun getSelectItemCount() : Int
    {
        return mVocabularyItemList.count { it.isSelected()}
    }

    private fun getSelectedItemList() : ArrayList<VocabularyDataResult>
    {
        return ArrayList(mVocabularyItemList.filter {
            it.isSelected()
        })
    }

    private fun setItemSelected(index : Int)
    {
        mVocabularyItemList.forEachIndexed{ position, item ->
            if(position == index)
            {
                item.setSelected(!item.isSelected())
            }
        }

        _vocabularyContentsList.value = EventWrapper(mVocabularyItemList)

        val selectedItemCount = getSelectItemCount()
        _itemSelectedCount.value = EventWrapper(selectedItemCount)
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

    private fun deleteRequestData()
    {
        for(deleteItem in mRequestItemList)
        {
            for(i in mVocabularyItemList.indices)
            {
                if(deleteItem.getID() == mVocabularyItemList[i].getID())
                {
                    mVocabularyItemList.removeAt(i)
                    break
                }
            }
        }
    }

    /**
     * 단어장에서 요청 데이터를 삭제후 화면 및 메인정보를 Syncronize 한다.
     */
    private fun refreshVocabularyItemData()
    {
        deleteRequestData()
        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        for(i in 0 until mainInformationResult.getVocabulariesList().size)
        {
            if(mCurrentMyVocabularyResult.getID() == mainInformationResult.getVocabulariesList()[i].getID())
            {
                mainInformationResult.getVocabulariesList()[i].setWordCount(mVocabularyItemList.size)
                break
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)

        //TODO: 리스트 뷰 초기화
        // mVocabularyItemListAdapter.notifyDataListChanged(mVocabularyItemList, false)
    }



    private fun requestVocabularyContentsListAsync()
    {
        Log.f("Contents ID : " + mCurrentMyVocabularyResult.getContentID())
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_LIST,
            mCurrentMyVocabularyResult.getContentID()
        )
    }

    private fun requestVocabularyShelfContentsAsync()
    {
        Log.f("Vocabulary ID : " + mCurrentMyVocabularyResult.getID())
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_LIST,
            mCurrentMyVocabularyResult.getID()
        )
    }

    private fun requestVocabularyContentsAddAsync(vocabularyId: String)
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_ADD,
            mCurrentMyVocabularyResult.getContentID(),
            vocabularyId,
            mRequestItemList
        )
    }

    private fun requestVocabularyContentsDeleteAsync()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_VOCABULARY_CONTENTS_DELETE,
            mCurrentMyVocabularyResult.getID(),
            mRequestItemList
        )
    }

    private fun startFlashcardActivity()
    {
        mSelectedPlayItemList = getSelectedItemList()
        Log.f("mSelectedPlayItemList.size() : " + mSelectedPlayItemList.size)
        if(mSelectedPlayItemList.size <= 0)
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_select_word_to_study)
            return
        }

        val data = FlashcardDataObject(
            mCurrentMyVocabularyResult.getID(),
            mCurrentMyVocabularyResult.getName(),
            "",
            VocabularyType.VOCABULARY_SHELF,
            mSelectedPlayItemList
        )

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun setVocabularyControlPlay()
    {
        Log.f("isSequencePlay : $isSequencePlay")
        _isPlayingStatus.value = isSequencePlay
        if(isSequencePlay)
        {
            Log.f("Vocabulary Sound Play")
            mCurrentPlayIndex = 0
            mSelectedPlayItemList = getSelectedItemList()
            _vocabularyContentsList.value = EventWrapper(ArrayList<VocabularyDataResult>())
            _isContentsLoading.value = true
            viewModelScope.launch {
                withContext(Dispatchers.Main)
                {
                    delay(Common.DURATION_NORMAL)
                }
                _isContentsLoading.value = false


                withContext(Dispatchers.Main)
                {
                    delay(Common.DURATION_NORMAL)
                }
                _vocabularyContentsList.value = EventWrapper(mSelectedPlayItemList)
                _currentPlayingIndex.value = mCurrentPlayIndex
                startAudio(mSelectedPlayItemList)
            }
        }
        else
        {
            Log.f("Vocabulary Sound Stop")
            enableSequencePlayAudio(false)
            _vocabularyContentsList.value = EventWrapper(ArrayList<VocabularyDataResult>())
            _currentPlayingIndex.value = 0
            _isContentsLoading.value = true
            viewModelScope.launch {
                withContext(Dispatchers.Main)
                {
                    delay(Common.DURATION_NORMAL)
                }
                _isContentsLoading.value = false

                _vocabularyContentsList.value = EventWrapper(mVocabularyItemList)
            }
        }
    }

    /**
     * 재생
     */
    private fun onClickBottomPlayAction(isPlay : Boolean)
    {
        if(getSelectItemCount()<= 0)
        {
            Log.f("Not Select ITEM")
            _errorMessage.value = mContext.resources.getString(R.string.message_not_have_play_vocabulary)
            return
        }
        isSequencePlay = isPlay
        setVocabularyControlPlay()
    }

    private fun onClickBottomPutInVocabularyShelf()
    {
        Log.f("")
        if(getSelectItemCount() > 0)
        {
            _dialogBottomVocabularyContentsAdd.value = mMainInformationResult.getVocabulariesList()
        }
        else
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_select_words_put_in_vocabulary)
        }
    }

    private fun onClickBottomDeleteInVocabularyShelf()
    {
        if(getSelectItemCount() > 0)
        {
            _dialogVocabularyContentsDelete.call()
        }
        else
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_select_words_delete_in_vocabulary)
        }
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {

        if(eventType == DIALOG_EVENT_DELETE_VOCABULARY_CONTENTS)
        {
            if(buttonType == DialogButtonType.BUTTON_2)
            {
                mRequestItemList.clear()
                mRequestItemList = getSelectedItemList()
                requestVocabularyContentsDeleteAsync()
            }
        }

    }
}