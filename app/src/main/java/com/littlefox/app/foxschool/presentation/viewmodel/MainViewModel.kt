package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.api.MainApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.factory.MainFactoryViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.BookType
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.DrawerMenu
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.enumerate.VocabularyType

import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.InAppCompaignResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainSongInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.SeriesContentsListActivity
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val apiViewModel : MainApiViewModel) : BaseViewModel()
{
    companion object
    {
        const val DIALOG_TYPE_IAC: Int              = 10001
        const val DIALOG_TYPE_LOGOUT: Int           = 10002
        const val DIALOG_TYPE_APP_END: Int          = 10003
        const val DIALOG_TYPE_NOT_HAVE_CLASS: Int   = 10004
    }

    private val _settingMenuView = SingleLiveEvent<Pair<Boolean, Boolean>>()
    val settingMenuView: LiveData<Pair<Boolean, Boolean>> get() = _settingMenuView

    private val _settingUserInformation = SingleLiveEvent<LoginInformationResult>()
    val settingUserInformation: LiveData<LoginInformationResult> get() = _settingUserInformation

    private val _showLogoutDialog = SingleLiveEvent<Void>()
    val showLogoutDialog: LiveData<Void> get() = _showLogoutDialog

    private val _showAppEndDialog = SingleLiveEvent<Void>()
    val showAppEndDialog: LiveData<Void> get() = _showAppEndDialog

    private val _showNoClassStudentDialog = SingleLiveEvent<Void>()
    val showNoClassStudentDialog: LiveData<Void> get() = _showNoClassStudentDialog

    private val _showNoClassTeacherDialog = SingleLiveEvent<Void>()
    val showNoClassTeacherDialog: LiveData<Void> get() = _showNoClassTeacherDialog

    private val _showIACInformationDialog = SingleLiveEvent<InAppCompaignResult>()
    val showIACInformationDialog: LiveData<InAppCompaignResult> get() = _showIACInformationDialog

    private val _updateStoryData = SingleLiveEvent<MainStoryInformationResult>()
    val updateStoryData: LiveData<MainStoryInformationResult> get() = _updateStoryData

    private val _updateSongData = SingleLiveEvent<List<SeriesInformationResult>>()
    val updateSongData: LiveData<List<SeriesInformationResult>> get() = _updateSongData

    private val _updateMyBooksData = SingleLiveEvent<MainInformationResult>()
    val updateMyBooksData: LiveData<MainInformationResult> get() = _updateMyBooksData


    private lateinit var mContext: Context
    private lateinit var mMainInformationResult : MainInformationResult
    private var mLoginInformationResult : LoginInformationResult ?= null

    override fun init(context : Context)
    {
        mContext = context
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?

        Log.i("size : " + mMainInformationResult.getMainStoryInformation().getContentByLevelToList().size)

        _updateStoryData.value = mMainInformationResult.getMainStoryInformation()
        _updateSongData.value = mMainInformationResult.getMainSongInformationList()
        _updateMyBooksData.value = mMainInformationResult
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.onBackPressed ->{
                _showAppEndDialog.call()
            }
            is BaseEvent.DialogClick ->{
                onDialogClick(
                    event.eventType
                )
            }
            is BaseEvent.DialogChoiceClick ->
            {
                onDialogChoiceClick(
                    event.buttonType,
                    event.eventType
                )
            }
            is MainEvent.onClickStoryLevelsItem -> {
                startSeriesContentsActivity(
                    event.seriesInformationResult
                )
            }
            is MainEvent.onClickStoryCategoriesItem -> {
                startCategoriesContentsActivity(
                    event.seriesInformationResult
                )
            }

            is MainEvent.onAddBookshelf -> {
                startManagementMyBooksActivity(
                    ManagementBooksData(MyBooksType.BOOKSHELF_ADD)
                )
            }

            is MainEvent.onAddVocabulary ->{
                startManagementMyBooksActivity(
                    ManagementBooksData(MyBooksType.VOCABULARY_ADD)
                )
            }

            is MainEvent.onSettingBookshelf -> {
                startManagementMyBooksActivity(
                    ManagementBooksData(
                        id = event.item.getID(),
                        name = event.item.getName(),
                        color = event.item.getColor(),
                        booksType = MyBooksType.BOOKSHELF_MODIFY
                    )
                )
            }

            is MainEvent.onSettingVocabulary -> {
                startManagementMyBooksActivity(
                    ManagementBooksData(
                        id = event.item.getID(),
                        name = event.item.getName(),
                        color = event.item.getColor(),
                        booksType = MyBooksType.VOCABULARY_MODIFY
                    )
                )
            }

            is MainEvent.onEnterBookshelfList ->{
                startMyBookshelfList(event.index)
            }
            is MainEvent.onEnterVocabularyList ->{
                startMyVocabularyList(event.index)
            }

            is MainEvent.onClickDrawerItem -> {
                checkDrawerMenu(event.menu)
            }

            is MainEvent.onClickSearch -> {
                startSearchActivity()
            }

            else ->{}
        }
    }

    override fun onHandleApiObserver()
    {}

    override fun resume()
    {
        updateUserInformation()
        updateSubScreen()
    }

    override fun pause() {}

    override fun destroy() {}

    override fun onDialogClick(eventType : Int)
    {
        super.onDialogClick(eventType)
    }

    override fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int)
    {
        if(eventType == DIALOG_TYPE_APP_END)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_2 ->
                {
                    Log.f("============ APP END ============")
                    (mContext as AppCompatActivity).finish()
                }
                else ->{}
            }
        }
        else if(eventType == DIALOG_TYPE_LOGOUT)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_2 ->
                {
                    Log.f("============ LOGOUT COMPLETE ============")
                    IntentManagementFactory.getInstance().initScene()
                }
                else ->{}
            }
        }
    }

    private fun startSearchActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SEARCH)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }


    private fun startSeriesContentsActivity(data : SeriesBaseResult)
    {
        Log.i("")
        data.setTransitionType(TransitionType.PAIR_IMAGE)
        data.setSeriesType(Common.CONTENT_TYPE_STORY)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startCategoriesContentsActivity(data : SeriesInformationResult)
    {
        Log.f("onClick StoryCategoryItem")
        data.setTransitionType(TransitionType.PAIR_IMAGE)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.STORY_CATEGORY_LIST)
            .setData(data as SeriesBaseResult)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }



    private fun startManagementMyBooksActivity(data : ManagementBooksData)
    {
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MANAGEMENT_MYBOOKS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun checkDrawerMenu(menu: DrawerMenu)
    {
        Log.i("menu : $menu")
        when(menu)
        {
            DrawerMenu.MY_INFORMATION -> startMyInformationActivity()
            DrawerMenu.STUDY_RECORD -> startRecordHistoryActivity()
            DrawerMenu.RECORD_HISTORY -> startRecordHistoryActivity()
            DrawerMenu.HOMEWORK_MANAGEMENT -> checkHomeworkManage()
            DrawerMenu.NEWS -> startFoxSchoolNewsActivity()
            DrawerMenu.FAQ -> startFAQActivity()
            DrawerMenu.INQUIRY_ONE_TO_ONE -> startInquireActivity()
            DrawerMenu.APP_GUIDE -> startAppUseGuideActivity()
            DrawerMenu.HOME_NEWSPAPER -> downloadHomeNewsPaper()
            DrawerMenu.TEACHER_MANUAL -> downloadTeacherManual()
            DrawerMenu.LOGOUT -> {
                _showLogoutDialog.call()
            }

        }
    }

    private fun checkHomeworkManage()
    {
        Log.f("")
        if (CommonUtils.getInstance(mContext).isTeacherMode == false)
        {
            // 학생
            if(mLoginInformationResult!!.getSchoolInformation().isHaveClass() == false)
            {
                // 학급 정보 없을 때
                _showNoClassStudentDialog.call()
            }
            else
            {
                // 학급정보 있는 경우 화면 이동
                startHomeworkManageActivity()
            }
        }
        else
        {
            // 선생님
            if(mLoginInformationResult!!.getUserInformation().isHaveClass() == false)
            {
                // 학급 정보 없을 때
                _showNoClassTeacherDialog.call()
            }
            else
            {
                // 학급정보 있는 경우 화면 이동
                startHomeworkManageActivity()
            }
        }
    }

    private fun startAppUseGuideActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.APP_USE_GUIDE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startLearningLogActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_LEARNING_LOG)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startMyInformationActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.MY_INFORMATION)
            .setData("N")
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION).startActivity()
    }

    private fun startRecordHistoryActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_HISTORY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startWebviewAttendanceActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_ATTENDANCE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startInquireActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.INQUIRE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFAQActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FAQS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startFoxSchoolNewsActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.FOXSCHOOL_NEWS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    private fun startHomeworkManageActivity()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.HOMEWORK_MANAGE)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    fun startMyBookshelfList(index : Int)
    {
        Log.f("onEnterBookshelfList : $index")
        if(mMainInformationResult.getBookShelvesList().get(index).getContentsCount() > 0)
        {
            Log.f("Enter Bookshelf : " + mMainInformationResult.getBookShelvesList().get(index).getName())
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.BOOKSHELF)
                .setData(mMainInformationResult.getBookShelvesList()[index])
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        }
        else
        {
            Log.f("Empty Bookshelf")
            _errorMessage.value = mContext.resources.getString(R.string.message_empty_bookshelf_contents)
        }
    }

    fun startMyVocabularyList(index : Int)
    {
        Log.f("onEnterVocabularyList : $index")
        if(mMainInformationResult.getVocabulariesList().get(index).getWordCount() > 0)
        {
            Log.f("Enter Vocabulary : " + mMainInformationResult.getVocabulariesList().get(index).getName())
            mMainInformationResult.getVocabulariesList().get(index)
                .setVocabularyType(VocabularyType.VOCABULARY_SHELF)
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.VOCABULARY)
                .setData(mMainInformationResult.getVocabulariesList().get(index))
                .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
                .startActivity()
        } else
        {
            Log.f("Empty Vocabulary")
            _errorMessage.value = mContext.resources.getString(R.string.message_empty_vocabulary_contents)
        }
    }


    private fun downloadHomeNewsPaper()
    {
        Log.f("")
        _successMessage.value = mContext.resources.getString(R.string.message_download_home_newspaper)
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getHomeNewsPaperLink(),
            Common.FILE_HOME_NEWSPAPER
        )
    }

    private fun downloadTeacherManual()
    {
        _successMessage.value = mContext.resources.getString(R.string.message_download_teacher_manual)
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getTeacherManualLink(),
            Common.FILE_TEACHER_MANUAL
        )
    }

    private fun updateUserInformation()
    {
        Log.f("update Status : " + MainObserver.isUpdateUserStatus())
        if(MainObserver.isUpdateUserStatus())
        {
            mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            mLoginInformationResult?.let {
                _settingUserInformation.value = it
            }
            _settingMenuView.value = Pair(mMainInformationResult.isUpdateHomework, mMainInformationResult.isUpdateNews)
            MainObserver.clearUserStatus()
        }
    }

    private fun updateSubScreen()
    {
        if(MainObserver.getUpdatePageList().isNotEmpty())
        {
            mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
            for(page in MainObserver.getUpdatePageList())
            {
                when(page)
                {
                    Common.PAGE_STORY ->{
                        _updateStoryData.value = mMainInformationResult.getMainStoryInformation()
                    }
                    Common.PAGE_SONG ->{
                        _updateSongData.value = mMainInformationResult.getMainSongInformationList()
                    }
                    Common.PAGE_MY_BOOKS ->{
                        _updateMyBooksData.value = mMainInformationResult
                    }
                }
            }
            MainObserver.clearAll()
        }
    }


}