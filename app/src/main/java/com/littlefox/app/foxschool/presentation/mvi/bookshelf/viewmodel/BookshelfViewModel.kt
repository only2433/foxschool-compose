package com.littlefox.app.foxschool.presentation.mvi.bookshelf.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import android.view.WindowInsets.Side
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.BookshelfApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.BookshelfFactoryViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.VocabularyType
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
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfAction
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfEvent
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfSideEffect
import com.littlefox.app.foxschool.presentation.mvi.bookshelf.BookshelfState
import com.littlefox.app.foxschool.presentation.mvi.vocabulary.VocabularySideEffect
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(private val apiViewModel : BookshelfApiViewModel): BaseMVIViewModel<BookshelfState, BookshelfEvent, SideEffect>(
    BookshelfState()
)
{
    companion object
    {
        const val DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS : Int      = 10001
        const val DIALOG_EVENT_WARNING_RECORD_PERMISSION : Int      = 10002
    }

    private lateinit var mContext : Context
    private lateinit var mCurrentMyBookshelfResult : MyBookshelfResult
    private var mCurrentSelectItem: ContentsBaseResult? = null
    private var mBookItemInformationList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mDeleteBookItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var mCurrentPlayIndex : Int = 0
    private var mCurrentOptionIndex : Int = 0

    override fun init(context : Context)
    {
        mContext = context
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            mCurrentMyBookshelfResult = (mContext as AppCompatActivity).intent.getParcelableExtra(
                Common.INTENT_BOOKSHELF_DATA, MyBookshelfResult::class.java)!!
        }
        else
        {
            mCurrentMyBookshelfResult = (mContext as AppCompatActivity).intent.getParcelableExtra<Parcelable>(
                Common.INTENT_BOOKSHELF_DATA) as MyBookshelfResult
        }

        Log.f("ID : ${mCurrentMyBookshelfResult.getID()}," +
                " Name : ${mCurrentMyBookshelfResult.getName()}, " +
                "Color : ${mCurrentMyBookshelfResult.getColor()}")

        postEvent(
            BookshelfEvent.SetTitle(
                mCurrentMyBookshelfResult.getName()
            ),
            BookshelfEvent.EnableContentsLoading(true)
        )

        Log.f("onCreate")
        onHandleApiObserver()
        viewModelScope.launch(Dispatchers.Main) {
            delay(Common.DURATION_LONG)
            requestBookshelfDetailInformationAsync()
        }
    }

    override fun resume() {}

    override fun pause() {}

    override fun destroy() {}

    override fun onBackPressed()
    {
        (mContext as AppCompatActivity).finish()
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE)
                        {
                            if(data.second)
                            {
                                postSideEffect(
                                    SideEffect.EnableLoading(true)
                                )
                            }
                            else
                            {
                                postSideEffect(
                                    SideEffect.EnableLoading(false)
                                )
                            }
                        }
                    }
                }
            }

        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.contentsList.collect { list ->
                    list?.let {
                        mBookItemInformationList = list
                        postEvent(
                            BookshelfEvent.EnableContentsLoading(false),
                            BookshelfEvent.UpdateContentsList(mBookItemInformationList)
                        )
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.myBookshelfResult.collect { data ->
                    data?.let {
                        refreshBookshelfItemData()
                        checkSelectedItemAll(
                            isSelected = false
                        )
                        viewModelScope.launch {
                            withContext(Dispatchers.IO){
                                delay(Common.DURATION_NORMAL)
                            }
                            postSideEffect(
                                SideEffect.ShowSuccessMessage(
                                    mContext.resources.getString(R.string.message_success_delete_contents)
                                )
                            )
                            if(mBookItemInformationList.size == 0)
                            {
                                withContext(Dispatchers.IO){
                                    delay(Common.DURATION_NORMAL)
                                }
                                onBackPressed()
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED){
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        if(result.isDuplicateLogin)
                        {
                            // 중복 로그인 시 재시작
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
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
                            Log.f("== isAuthenticationBroken ==")
                            postSideEffect(
                                SideEffect.ShowToast(
                                    result.message
                                )
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
                            if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_LIST)
                            {
                                postEvent(
                                    BookshelfEvent.EnableContentsLoading(false)
                                )
                                postSideEffect(
                                    SideEffect.ShowToast(
                                        result.message
                                    )
                                )
                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    (mContext as AppCompatActivity).finish()
                                }
                            }
                            else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE)
                            {
                                checkSelectedItemAll(
                                    isSelected = false
                                )
                                viewModelScope.launch(Dispatchers.Main) {
                                    withContext(Dispatchers.IO){
                                        delay(Common.DURATION_NORMAL)
                                    }
                                    postSideEffect(
                                        SideEffect.ShowErrorMessage(
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
    }

    override fun onHandleAction(action : Action)
    {
        when(action)
        {
            is BookshelfAction.ClickBottomBarMenu ->
            {
                when(action.menu)
                {
                    ContentsListBottomBarMenu.SELECT_ALL ->
                    {
                        checkSelectedItemAll(
                            isSelected = true
                        )
                    }
                    ContentsListBottomBarMenu.SELECT_PLAY ->
                    {
                        startSelectedListMovieActivity()
                    }
                    ContentsListBottomBarMenu.BOOKSHELF_DELETE ->
                    {
                        removeContentsInBookshelf()
                    }
                    ContentsListBottomBarMenu.CANCEL ->
                    {
                        checkSelectedItemAll(
                            isSelected = false
                        )
                    }
                    else -> {}
                }
            }
            is BookshelfAction.ClickBottomContentsType ->
            {
                checkBottomSelectItemType(action.type)
            }
            is BookshelfAction.SelectedItem ->
            {
                onSelectItem(action.index)
            }
            is BookshelfAction.ClickThumbnail ->
            {
                mCurrentSelectItem = action.item
                startCurrentSelectMovieActivity()
            }
            is BookshelfAction.ClickOption ->
            {
                mCurrentSelectItem = action.item
                postSideEffect(
                    BookshelfSideEffect.ShowBottomOptionDialog(
                        action.item
                    )
                )
            }
        }
    }

    override suspend fun reduceState(current : BookshelfState, event : BookshelfEvent) : BookshelfState
    {
        return when(event)
        {
            is BookshelfEvent.UpdateContentsList ->
            {
                current.copy(
                    contentsList = event.contentsList
                )
            }
            is BookshelfEvent.SetTitle ->
            {
                current.copy(
                    title = event.title
                )
            }
            is BookshelfEvent.SelectItemCount ->
            {
                current.copy(
                    selectCount = event.count
                )
            }
            is BookshelfEvent.EnableContentsLoading ->
            {
                current.copy(
                    isContentsLoading = event.isLoading
                )
            }
        }
    }

    private fun refreshBookshelfItemData()
    {
        for(deleteItem in mDeleteBookItemList)
        {
            for(i in mBookItemInformationList.indices)
            {
                if(deleteItem.id == (mBookItemInformationList[i].id))
                {
                    mBookItemInformationList.removeAt(i)
                    break
                }
            }
        }

        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        for(i in 0 until mainInformationResult.getBookShelvesList().size)
        {
            if(mCurrentMyBookshelfResult.getID() == (mainInformationResult.getBookShelvesList()[i].getID()))
            {
                mainInformationResult.getBookShelvesList()[i].setContentsCount(mBookItemInformationList.size)
                break
            }
        }
        CommonUtils.getInstance(mContext).saveMainData(mainInformationResult)
        MainObserver.updatePage(Common.PAGE_MY_BOOKS)

        postEvent(
            BookshelfEvent.UpdateContentsList(mBookItemInformationList)
        )
    }

    /**
     * ================ 통신요청 ================
     */
    private fun requestBookshelfDetailInformationAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_LIST,
            mCurrentMyBookshelfResult.getID()
        )
    }

    private fun requestBookshelfRemoveAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_BOOKSHELF_CONTENTS_DELETE,
            mCurrentMyBookshelfResult.getID(),
            mDeleteBookItemList
        )
    }

    private fun checkBottomSelectItemType(type: ActionContentsType)
    {
        when(type)
        {
            ActionContentsType.QUIZ -> startQuizActivity()
            ActionContentsType.EBOOK -> startEbookActivity()
            ActionContentsType.FLASHCARD -> startFlashcardActivity()
            ActionContentsType.VOCABULARY -> startVocabularyActivity()
            ActionContentsType.CROSSWORD -> startGameCrosswordActivity()
            ActionContentsType.STARWORDS -> startGameStarwordsActivity()
            ActionContentsType.TRANSLATE -> startOriginTranslateActivity()
            ActionContentsType.RECORD_PLAYER -> {
                Log.f("")
                if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
                {
                    postSideEffect(
                        BookshelfSideEffect.ShowRecordPermissionDialog
                    )
                }
                else
                {
                    startRecordPlayerActivity()
                }
            }
            ActionContentsType.DELETE_BOOKSHELF -> {
                Log.f("")
                mCurrentSelectItem?.let {
                    mDeleteBookItemList.clear()
                    mDeleteBookItemList.add(it)

                    postSideEffect(
                        BookshelfSideEffect.ShowContentsDeleteDialog
                    )
                }
            }
            else -> {}
        }
    }



    /**
     * ================ 컨텐츠 화면 이동 ================
     */
    private fun startSelectedListMovieActivity()
    {
        val sendItemList = getSelectedItemList()

        if(sendItemList.isNotEmpty())
        {
            val playerIntentParamsObject = PlayerIntentParamsObject(sendItemList)

            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerIntentParamsObject)
                .startActivity()
        }
        else
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(
                    mContext.resources.getString(R.string.message_not_selected_contents_list)
                )
            )
        }
    }
    private fun startCurrentSelectMovieActivity()
    {
        Log.f("")
        mCurrentSelectItem?.let { item ->
            Log.f("Movie  : " + item.toString())
            val sendItemList = ArrayList<ContentsBaseResult>()
            sendItemList.add(item)
            val playerParamsObject = PlayerIntentParamsObject(sendItemList)

            IntentManagementFactory.getInstance().readyActivityMode(ActivityMode.PLAYER)
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .setData(playerParamsObject)
                .startActivity()
        }
    }

    private fun startQuizActivity()
    {
        Log.f("")
        val quizIntentParamsObject = QuizIntentParamsObject(mBookItemInformationList!![mCurrentOptionIndex].id)
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
            .setData(mBookItemInformationList[mCurrentOptionIndex].id)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startEbookActivity()
    {
        Log.f("")
        val data  = WebviewIntentParamsObject(mBookItemInformationList[mCurrentOptionIndex].id)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startVocabularyActivity()
    {
        Log.f("")
        val title = mBookItemInformationList[mCurrentOptionIndex].getVocabularyName()
        val myVocabularyResult = MyVocabularyResult(
            mBookItemInformationList[mCurrentOptionIndex].id,
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
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList[mCurrentOptionIndex].id)

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startGameCrosswordActivity()
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(mBookItemInformationList[mCurrentOptionIndex].id)

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
            mBookItemInformationList[mCurrentOptionIndex].id,
            mBookItemInformationList[mCurrentOptionIndex].name,
            mBookItemInformationList[mCurrentOptionIndex].sub_name,
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
        val recordIntentParamsObject = RecordIntentParamsObject(mBookItemInformationList[mCurrentOptionIndex])
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun removeContentsInBookshelf()
    {
        mDeleteBookItemList.clear()
        mDeleteBookItemList = getSelectedItemList()

        if(mDeleteBookItemList.size > 0)
        {
            postSideEffect(
                BookshelfSideEffect.ShowContentsDeleteDialog
            )
        }
        else
        {
            postSideEffect(
                SideEffect.ShowErrorMessage(
                    mContext.resources.getString(R.string.message_select_contents_delete_in_bookshelf)
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSelectItem(index : Int)
    {
        // 현재 선택 상태를 반전
        mBookItemInformationList.forEachIndexed { position, item ->
            if (position == index) { // index 값 비교
                item.isSelected = !item.isSelected // item을 직접 수정
            }
        }
        // mCurrentContentsItemList를 ArrayList로 변환하여 방출
        postEvent(
            BookshelfEvent.UpdateContentsList(mBookItemInformationList)
        )

        sendSelectedItem()
    }

    private fun getSelectedItemList() : ArrayList<ContentsBaseResult>
    {
        return ArrayList(mBookItemInformationList.filter {
            it.isSelected
        })
    }

    private fun sendSelectedItem()
    {
        val selectedItemCount = mBookItemInformationList.count { it.isSelected }
        postEvent(
            BookshelfEvent.SelectItemCount(selectedItemCount)
        )
    }

    private fun checkSelectedItemAll(isSelected : Boolean)
    {
        Log.i("isSelected : ${isSelected}")
        mBookItemInformationList.forEach {
            it.isSelected = isSelected
        }

        // mCurrentContentsItemList를 ArrayList로 변환하여 방출

        postEvent(
            BookshelfEvent.UpdateContentsList(mBookItemInformationList),
            if(isSelected)
            {
                BookshelfEvent.SelectItemCount(mBookItemInformationList.size)
            }
            else
            {
                BookshelfEvent.SelectItemCount(0)
            }
        )
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        Log.f("event type : $eventType, buttonType : $buttonType")
        if(eventType == BookshelfFactoryViewModel.DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_2 ->
                {
                    requestBookshelfRemoveAsync()
                }
                else ->{}
            }
        }
        else if(eventType == BookshelfFactoryViewModel.DIALOG_EVENT_WARNING_RECORD_PERMISSION)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                { // [취소] 컨텐츠 사용 불가 메세지 표시
                    postSideEffect(
                        SideEffect.ShowErrorMessage(
                            mContext.getString(R.string.message_warning_record_permission)
                        )
                    )
                }

                DialogButtonType.BUTTON_2 ->
                { // [권한 변경하기] 앱 정보 화면으로 이동
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
}