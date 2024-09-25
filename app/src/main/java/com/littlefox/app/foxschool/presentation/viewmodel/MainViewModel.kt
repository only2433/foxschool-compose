package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.enumerate.TransitionType

import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.InAppCompaignResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainSongInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.screen.series_contents_list.SeriesContentsListActivity
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
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
    private val _settingMenuView = MutableSharedFlow<Pair<Boolean, Boolean>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val settingMenuView: SharedFlow<Pair<Boolean, Boolean>> = _settingMenuView

    private val _settingUserInformation = MutableSharedFlow<LoginInformationResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val settingUserInformation: SharedFlow<LoginInformationResult> = _settingUserInformation

    private val _showLogoutDialog = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showLogoutDialog: SharedFlow<Unit> = _showLogoutDialog

    private val _showAppEndDialog = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showAppEndDialog: SharedFlow<Unit> = _showAppEndDialog

    private val _showNoClassStudentDialog = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showNoClassStudentDialog: SharedFlow<Unit> = _showNoClassStudentDialog

    private val _showNoClassTeacherDialog = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showNoClassTeacherDialog: SharedFlow<Unit> = _showNoClassTeacherDialog

    private val _showIACInformationDialog = MutableSharedFlow<InAppCompaignResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showIACInformationDialog: SharedFlow<InAppCompaignResult> = _showIACInformationDialog

    private val _updateStoryData = MutableSharedFlow<MainStoryInformationResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val updateStoryData: SharedFlow<MainStoryInformationResult> = _updateStoryData

    private val _updateSongData = MutableSharedFlow<List<SeriesInformationResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val updateSongData: SharedFlow<List<SeriesInformationResult>> = _updateSongData

    private val _updateMyBooksData = MutableSharedFlow<MainInformationResult>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val updateMyBooksData: SharedFlow<MainInformationResult> = _updateMyBooksData

    private lateinit var mContext: Context
    private lateinit var mMainInformationResult : MainInformationResult
    private var mLoginInformationResult : LoginInformationResult ?= null

    override fun init(context : Context)
    {
        mContext = context
        mMainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mLoginInformationResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?

        Log.i("size : " + mMainInformationResult.getMainStoryInformation().getContentByLevelToList().size)

        viewModelScope.launch {
            _updateStoryData.emit(mMainInformationResult.getMainStoryInformation())
        }
        viewModelScope.launch {
            _updateSongData.emit(mMainInformationResult.getMainSongInformationList())
        }
        viewModelScope.launch {
            _updateMyBooksData.emit(mMainInformationResult)
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.onBackPressed ->{
                viewModelScope.launch {
                    _showAppEndDialog.emit(Unit)
                }
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

            is MainEvent.onClickDrawerItem -> {
                checkDrawerMenu(event.menu)
            }

            else ->{}
        }
    }

    override fun onHandleApiObserver()
    {
        TODO("Not yet implemented")
    }

    override fun resume()
    {
        TODO("Not yet implemented")
    }

    override fun pause()
    {
        TODO("Not yet implemented")
    }

    override fun destroy()
    {
        TODO("Not yet implemented")
    }

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

    private fun checkDrawerMenu(menu: DrawerMenu)
    {
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
                viewModelScope.launch {
                    _showLogoutDialog.emit(Unit)
                }
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
                viewModelScope.launch {
                    _showNoClassStudentDialog.emit(Unit)
                }
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
                viewModelScope.launch {
                    _showNoClassTeacherDialog.emit(Unit)
                }
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


    private fun downloadHomeNewsPaper()
    {
        Log.f("")
        viewModelScope.launch {
            _successMessage.emit(mContext.resources.getString(R.string.message_download_home_newspaper))
        }
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getHomeNewsPaperLink(),
            Common.FILE_HOME_NEWSPAPER
        )
    }

    private fun downloadTeacherManual()
    {
        viewModelScope.launch {
            _successMessage.emit(mContext.resources.getString(R.string.message_download_teacher_manual))
        }
        CommonUtils.getInstance(mContext).downloadFileToExternalPublicDir(
            mMainInformationResult.getFileInformation()!!.getTeacherManualLink(),
            Common.FILE_TEACHER_MANUAL
        )
    }



}