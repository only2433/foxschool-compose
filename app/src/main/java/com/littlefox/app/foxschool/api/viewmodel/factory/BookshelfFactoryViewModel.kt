package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.adapter.listener.DetailItemListener
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.BookshelfApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.flashcard.FlashcardDataObject
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookshelfFactoryViewModel @Inject constructor(private val apiViewModel : BookshelfApiViewModel) : BaseFactoryViewModel()
{
    companion object
    {
        const val DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS : Int      = 10001
        const val DIALOG_EVENT_WARNING_RECORD_PERMISSION : Int      = 10002

        private const val INDEX_UPDATE_BOOKSHELF : Int                      = 0
    }

    private val _setTitle = SingleLiveEvent<String>()
    val setTitle : LiveData<String> get() = _setTitle

    private val _enableContentListLoading = SingleLiveEvent<Boolean>()
    val enableContentListLoading : LiveData<Boolean> get() = _enableContentListLoading

    private val _enableFloatingToolbarLayout = SingleLiveEvent<Boolean>()
    val enableFloatingToolbarLayout : LiveData<Boolean> get() = _enableFloatingToolbarLayout

    private val _setFloatingToolbarPlayCount = SingleLiveEvent<Int>()
    val setFloatingToolbarPlayCount : LiveData<Int> get() = _setFloatingToolbarPlayCount

    private val _showBookshelfDetailListView = SingleLiveEvent<DetailListItemAdapter>()
    val showBookshelfDetailListView : LiveData<DetailListItemAdapter> = _showBookshelfDetailListView

    private val _dialogBottomOption = SingleLiveEvent <ContentsBaseResult>()
    val dialogBottomOption: LiveData<ContentsBaseResult> get() = _dialogBottomOption

    private val _dialogBookshelfContentsDelete = SingleLiveEvent<Void>()
    val dialogBookshelfContentsDelete : LiveData<Void> get() = _dialogBookshelfContentsDelete

    private val _dialogWarningRecordPermission = SingleLiveEvent <Void>()
    val dialogWarningRecordPermission: LiveData<Void> get() = _dialogWarningRecordPermission


    private lateinit var mContext : Context
    private var mCurrentMyBookshelfResult : MyBookshelfResult? = null
    private var mBookItemInformationList : ArrayList<ContentsBaseResult>? = null
    private var mBookshelfDetailItemAdapter : DetailListItemAdapter? = null
    private var mDeleteBookItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentPlayIndex : Int = 0
    private var mCurrentOptionIndex : Int = 0
    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>

    override fun init(context : Context)
    {
        mContext = context
        mCurrentMyBookshelfResult = (mContext as AppCompatActivity).intent.getParcelableExtra<Parcelable>(
            Common.INTENT_BOOKSHELF_DATA) as MyBookshelfResult
        Log.f("ID : ${mCurrentMyBookshelfResult?.getID()}," +
                " Name : ${mCurrentMyBookshelfResult?.getName()}, " +
                "Color : ${ mCurrentMyBookshelfResult?.getColor()}")
        _setTitle.value = mCurrentMyBookshelfResult?.getName()
        _enableContentListLoading.value = true
        Log.f("onCreate")
        setupViewModelObserver()
        viewModelScope.launch(Dispatchers.Main) {
            delay(Common.DURATION_LONG)
            requestBookshelfDetailInformationAsync()
        }
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
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect { data ->
                data?.let {
                    if (data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE)
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
            apiViewModel.contentsList.collect { list ->
                list?.let {
                    _enableContentListLoading.value = false
                    mBookItemInformationList = list
                    initContentItemList()
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.myBookshelfResult.collect { data ->
                data?.let {
                    refreshBookshelfItemData()
                    _enableFloatingToolbarLayout.value = false
                    viewModelScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO){
                            delay(Common.DURATION_NORMAL)
                        }
                        _successMessage.value = mContext.resources.getString(R.string.message_success_delete_contents)
                        if(mBookItemInformationList!!.size == 0)
                        {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_NORMAL)
                            }
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect { data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    if(result.isDuplicateLogin)
                    {
                        // 중복 로그인 시 재시작
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if(result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_LIST)
                        {
                            _enableContentListLoading.value = false
                            _toast.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                        else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE)
                        {
                            _enableFloatingToolbarLayout.value = false
                            viewModelScope.launch(Dispatchers.Main) {
                                withContext(Dispatchers.IO){
                                    delay(Common.DURATION_NORMAL)
                                }
                                _errorMessage.value = result.message
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onAddResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(0))
    }

    override fun onActivityResult(code : ResultLauncherCode, intent : Intent?)
    {
        val bookName : String? = intent?.getStringExtra(Common.INTENT_MODIFY_BOOKSHELF_NAME)
        Log.f("bookName : $bookName")
        bookName?.let {
            _setTitle.value = it
        }
    }

    private fun initContentItemList()
    {
        mBookshelfDetailItemAdapter = DetailListItemAdapter(mContext)
            .setData(mBookItemInformationList!!)
            .setDetailItemListener(mDetailItemListener)

        _showBookshelfDetailListView.value = mBookshelfDetailItemAdapter!!
    }

    private fun refreshBookshelfItemData()
    {
        for(deleteItem in mDeleteBookItemList)
        {
            for(i in mBookItemInformationList!!.indices)
            {
                if(deleteItem.getID() == (mBookItemInformationList!![i].getID()))
                {
                    mBookItemInformationList!!.removeAt(i)
                    break
                }
            }
        }

        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        for(i in 0 until mainInformationResult.getBookShelvesList().size)
        {
            if(mCurrentMyBookshelfResult!!.getID() == (mainInformationResult.getBookShelvesList().get(i).getID()))
            {
                mainInformationResult.getBookShelvesList().get(i).setContentsCount(mBookItemInformationList!!.size)
                break
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)
        mBookshelfDetailItemAdapter!!.notifyDataListChanged(mBookItemInformationList!!)
    }

    /**
     * ================ 통신요청 ================
     */
    private fun requestBookshelfDetailInformationAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_LIST,
            mCurrentMyBookshelfResult!!.getID()
        )
    }

    private fun requestBookshelfRemoveAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE,
            mCurrentMyBookshelfResult!!.getID(),
            mDeleteBookItemList
        )
    }

    /**
     * ================ 컨텐츠 화면 이동 ================
     */
    private fun startCurrentSelectMovieActivity(index : Int)
    {
        Log.f("")
        mCurrentPlayIndex = index
        val sendItemList = ArrayList<ContentsBaseResult>()
        sendItemList.add(mBookItemInformationList!![mCurrentPlayIndex])
        val playerParamsObject = PlayerIntentParamsObject(sendItemList)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(playerParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startQuizActivity()
    {
        Log.f("")
        val quizIntentParamsObject = QuizIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startOriginTranslateActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ORIGIN_TRANSLATE)
            .setData(mBookItemInformationList!![mCurrentOptionIndex].getID())
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        val data  = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        val title = mBookItemInformationList!![mCurrentOptionIndex].getVocabularyName()
        val myVocabularyResult = MyVocabularyResult(
            mBookItemInformationList!![mCurrentOptionIndex].getID(),
            title,
            VocabularyType.VOCABULARY_CONTENTS
        )
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.VOCABULARY)
            .setData(myVocabularyResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameStarwordsActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].getID())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFlashcardActivity()
    {
        Log.f("")
        val data = FlashcardDataObject(
            mBookItemInformationList!![mCurrentOptionIndex].getID(),
            mBookItemInformationList!![mCurrentOptionIndex].getName(),
            mBookItemInformationList!![mCurrentOptionIndex].getSubName(),
            VocabularyType.VOCABULARY_CONTENTS
        )
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FLASHCARD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startRecordPlayerActivity()
    {
        Log.f("")
        val recordIntentParamsObject = RecordIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex])
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }


    fun onClickSelectAll()
    {
        Log.f("")
        mBookshelfDetailItemAdapter?.setSelectedAllData()
    }

    fun onClickSelectPlay()
    {
        Log.f("")
        if(mBookshelfDetailItemAdapter!!.getSelectedList().size > 0)
        {
            val sendItemList = mBookshelfDetailItemAdapter!!.getSelectedList()
            val playerParamsObject = PlayerIntentParamsObject(sendItemList)
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerParamsObject)
                .startActivity()
        }
        else
        {
            _errorMessage.value = mContext.resources.getString(R.string.message_not_selected_contents_list)
        }
    }

    fun onClickRemoveBookshelf()
    {
        Log.f("delete list size : " + mBookshelfDetailItemAdapter!!.getSelectedList().size)

        if(mBookshelfDetailItemAdapter!!.getSelectedList().size > 0)
        {
            mDeleteBookItemList.clear()
            mDeleteBookItemList = mBookshelfDetailItemAdapter!!.getSelectedList()
            _dialogBookshelfContentsDelete.call()
        }
    }

    fun onClickCancel()
    {
        Log.f("")
        mBookshelfDetailItemAdapter?.initSelectedData()
    }

    fun onClickQuizButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startQuizActivity()
        }
    }

    fun onClickTranslateButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startOriginTranslateActivity()
        }
    }

    fun onClickVocabularyButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startVocabularyActivity()
        }
    }

    fun onClickBookshelfButton()
    {
        Log.f("DELETE")
        mDeleteBookItemList.clear()
        mDeleteBookItemList.add(mBookItemInformationList!![mCurrentOptionIndex])
        _dialogBookshelfContentsDelete.call()
    }

    fun onClickEbookButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startEbookActivity()
        }
    }

    fun onClickStarwordsButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameStarwordsActivity()
        }
    }

    fun onClickCrosswordButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startGameCrosswordActivity()
        }
    }

    fun onClickFlashcardButton()
    {
        Log.f("")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            startFlashcardActivity()
        }
    }

    fun onClickRecordPlayerButton()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
        {
            _dialogWarningRecordPermission.call()
        }
        else
        {
            viewModelScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO){
                    delay(Common.DURATION_SHORT)
                }
                startRecordPlayerActivity()
            }
        }
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        Log.f("event type : $eventType, buttonType : $buttonType")
        if(eventType == DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_2 ->
                {
                    requestBookshelfRemoveAsync()
                }
            }
        }
        else if(eventType == DIALOG_EVENT_WARNING_RECORD_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    // [취소] 컨텐츠 사용 불가 메세지 표시
                    _errorMessage.value = mContext.getString(R.string.message_warning_record_permission)
                }
                DialogButtonType.BUTTON_2 ->
                {
                    // [권한 변경하기] 앱 정보 화면으로 이동
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", mContext.packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mContext.startActivity(intent)
                }
            }
        }
    }

    private val mDetailItemListener : DetailItemListener = object : DetailItemListener
    {
        override fun onItemClickThumbnail(index : Int)
        {
            Log.f("index : $index")
            mCurrentPlayIndex = index
            startCurrentSelectMovieActivity(mCurrentPlayIndex)
        }

        override fun onItemClickOption(index : Int)
        {
            Log.f("index : $index")
            mCurrentOptionIndex = index
            _dialogBottomOption.value = mBookItemInformationList!![mCurrentOptionIndex]
        }

        override fun onItemSelectCount(count : Int)
        {
            if(count == 0)
            {
                _enableFloatingToolbarLayout.value = false
            }
            else
            {
                if(count == 1)
                {
                    _enableFloatingToolbarLayout.value = true
                }
                _setFloatingToolbarPlayCount.value = count
            }
        }
    }
}