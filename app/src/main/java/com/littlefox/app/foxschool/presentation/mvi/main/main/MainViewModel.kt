package com.littlefox.app.foxschool.presentation.mvi.main.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.api.MainApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.DrawerMenu
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.observer.MainObserver
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.main.MainAction
import com.littlefox.app.foxschool.presentation.mvi.main.MainEvent
import com.littlefox.app.foxschool.presentation.mvi.main.MainSideEffect
import com.littlefox.app.foxschool.presentation.mvi.main.MainState
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val apiViewModel: MainApiViewModel) : BaseMVIViewModel<MainState, MainEvent, SideEffect>(
    MainState()
)
{
    companion object
    {
        const val DIALOG_TYPE_IAC: Int              = 10001
        const val DIALOG_TYPE_LOGOUT: Int           = 10002
        const val DIALOG_TYPE_APP_END: Int          = 10003
        const val DIALOG_TYPE_NOT_HAVE_CLASS: Int   = 10004
    }

    private lateinit var mContext: Context
    private lateinit var mMainInformationResult : MainInformationResult
    private var mLoginInformationResult : LoginInformationResult?= null

    override fun init(context : Context)
    {
        mContext = context
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?

        Log.i("size : " + mMainInformationResult.getMainStoryInformation().getContentByLevelToList().size)

        postEvent(
            MainEvent.NotifyStoryTab(
                mMainInformationResult.getMainStoryInformation()
            ),
            MainEvent.NotifySongTab(
                mMainInformationResult.getMainSongInformationList()
            ),
            MainEvent.NotifyMyBooksTab(
                mMainInformationResult
            )
        )

    }

    override fun resume()
    {
        updateUserInformation()
        updateSubScreen()
    }

    override fun pause() {}

    override fun destroy() {}

    override fun onBackPressed()
    {
        postSideEffect(
            MainSideEffect.ShowAppEndDialog
        )
    }

    override fun onHandleApiObserver() {}

    override fun onHandleAction(action : Action)
    {
        when(action)
        {
            is MainAction.ClickStoryLevelsItem ->
            {
                startSeriesContentsActivity(
                    action.seriesInformationResult
                )
            }
            is MainAction.ClickStoryCategoriesItem ->
            {
                startCategoriesContentsActivity(
                    action.seriesInformationResult
                )
            }
            is MainAction.ClickSongCategoriesItem ->
            {
                startSongCategoryActivity(
                    action.seriesInformationResult
                )
            }
            is MainAction.AddBookshelf ->
            {
                startManagementMyBooksActivity(
                    ManagementBooksData(MyBooksType.BOOKSHELF_ADD)
                )
            }
            is MainAction.AddVocabulary ->
            {
                startManagementMyBooksActivity(
                    ManagementBooksData(MyBooksType.VOCABULARY_ADD)
                )
            }
            is MainAction.SettingBookshelf ->
            {
                startManagementMyBooksActivity(
                    ManagementBooksData(
                        id = action.item.getID(),
                        name = action.item.getName(),
                        color = action.item.getColor(),
                        booksType = MyBooksType.BOOKSHELF_MODIFY
                    )
                )
            }
            is MainAction.SettingVocabulary ->
            {
                startManagementMyBooksActivity(
                    ManagementBooksData(
                        id = action.item.getID(),
                        name = action.item.getName(),
                        color = action.item.getColor(),
                        booksType = MyBooksType.VOCABULARY_MODIFY
                    )
                )
            }
            is MainAction.EnterBookshelfList ->
            {
                startMyBookshelfList(action.index)
            }
            is MainAction.EnterVocabularyList ->
            {
                startMyVocabularyList(action.index)
            }
            is MainAction.ClickDrawerItem ->
            {
                checkDrawerMenu(action.menu)
            }
            is MainAction.ClickSearch ->
            {
                startSearchActivity()
            }
        }
    }

    override suspend fun reduceState(current : MainState, event : MainEvent) : MainState
    {
        return when(event)
        {
            is MainEvent.NotifyNewsMenu ->
            {
                current.copy(
                    isUpdateNews = event.isUpdate
                )
            }
            is MainEvent.NotifyHomeworkMenu ->
            {
                current.copy(
                    isUpdateHomework = event.isUpdate
                )
            }
            is MainEvent.SettingUserInformation ->
            {
                current.copy(
                    userInformation = event.data
                )
            }
            is MainEvent.NotifyStoryTab ->
            {
                current.copy(
                    storyData = event.data
                )
            }
            is MainEvent.NotifySongTab ->
            {
                current.copy(
                    songData = event.data
                )
            }
            is MainEvent.NotifyMyBooksTab ->
            {
                current.copy(
                    myBooksData = event.data
                )
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

    private fun startSongCategoryActivity(data : SeriesInformationResult)
    {
        Log.f("onClick SongCategoriesItem")
        data.setTransitionType(TransitionType.PAIR_IMAGE)
        data.setSeriesType(Common.CONTENT_TYPE_SONG)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
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
                postSideEffect(
                    MainSideEffect.ShowLogoutDialog
                )
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
                postSideEffect(
                    MainSideEffect.ShowNoClassStudentDialog
                )
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
                postSideEffect(
                    MainSideEffect.ShowNoClassTeacherDialog
                )
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

    private fun startMyBookshelfList(index : Int)
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
            postSideEffect(
                SideEffect.ShowErrorMessage(mContext.resources.getString(R.string.message_empty_bookshelf_contents))
            )
        }
    }

    private fun startMyVocabularyList(index : Int)
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
            postSideEffect(
                SideEffect.ShowErrorMessage(mContext.resources.getString(R.string.message_empty_vocabulary_contents))
            )
        }
    }

    private fun downloadHomeNewsPaper()
    {
        Log.f("")
        postSideEffect(
            SideEffect.ShowSuccessMessage(mContext.resources.getString(R.string.message_download_home_newspaper))
        )
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getHomeNewsPaperLink(),
            Common.FILE_HOME_NEWSPAPER
        )
    }

    private fun downloadTeacherManual()
    {
        postSideEffect(
            SideEffect.ShowSuccessMessage(mContext.resources.getString(R.string.message_download_teacher_manual))
        )
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
                postEvent(
                    MainEvent.SettingUserInformation(
                        it
                    )
                )

            }
            postEvent(
                MainEvent.NotifyHomeworkMenu(mMainInformationResult.isUpdateHomework),
                MainEvent.NotifyNewsMenu(mMainInformationResult.isUpdateNews)
            )
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
                        postEvent(
                            MainEvent.NotifyStoryTab(
                                mMainInformationResult.getMainStoryInformation()
                            )
                        )
                    }
                    Common.PAGE_SONG ->{
                        postEvent(
                            MainEvent.NotifySongTab(
                                mMainInformationResult.getMainSongInformationList()
                            )
                        )
                    }
                    Common.PAGE_MY_BOOKS ->{
                        postEvent(
                            MainEvent.NotifyMyBooksTab(
                                mMainInformationResult
                            )
                        )
                    }
                }
            }
            MainObserver.clearAll()
        }
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
}