package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.calendar.HomeworkCalendarItemData
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.StudentHomeworkApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.fragment.HomeworkFragmentViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.management.IntentManagementFactory
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
class StudentHomeworkFactoryViewModel @Inject constructor(private val apiViewModel : StudentHomeworkApiViewModel) : BaseFactoryViewModel()
{
    private val _settingViewPager = SingleLiveEvent<HomeworkPagerAdapter>()
    val settingViewPager: LiveData<HomeworkPagerAdapter> get() = _settingViewPager

    private val _currentViewPage = SingleLiveEvent<Pair<Int, HomeworkCommentType?>>()
    val currentViewPage: LiveData<Pair<Int, HomeworkCommentType?>> get() = _currentViewPage

    private val _showRecordPermissionDialog = SingleLiveEvent<Void>()
    val showRecordPermissionDialog: LiveData<Void> get() = _showRecordPermissionDialog

    private lateinit var mContext : Context

    // 통신 응답 데이터
    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null
    private var mHomeworkDetailBaseResult : HomeworkDetailBaseResult? = null

    // 숙제 페이지 Adapter 관련 데이터 (숙제관리, 숙제현황, 코멘트)
    private val mHomeworkFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private lateinit var mHomeworkPagerAdapter : HomeworkPagerAdapter

    private var mPagePosition : Int = Common.PAGE_HOMEWORK_CALENDAR // 현재 보여지고있는 페이지 포지션
    private var mCommentType : HomeworkCommentType = HomeworkCommentType.COMMENT_STUDENT // 코멘트 화면 타입
    private var mSelectedHomeworkPosition : Int = -1 // 숙제관리에서 선택한 숙제 포지션 (List/Comment 화면 공동 사용)
    private var mSelectHomeworkData : HomeworkCalendarItemData? = null // 선택한 숙제 아이템

    // 통신에 입력되는 년도, 월
    private var mYear : String  = ""
    private var mMonth : String = ""

    // 학습자 한마디 (통신 입력용)
    private var mStudentComment : String = ""

    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>
    private lateinit var fragmentViewModel : HomeworkFragmentViewModel

    override fun init(context : Context)
    {
        mContext = context
        fragmentViewModel = ViewModelProvider(mContext as AppCompatActivity).get(HomeworkFragmentViewModel::class.java)
        setupViewModelObserver()

        // set ViewPager
        mHomeworkPagerAdapter = HomeworkPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mHomeworkFragmentList)
        mHomeworkPagerAdapter.setFragment()
        _settingViewPager.value = mHomeworkPagerAdapter

        onPageChanged(Common.PAGE_HOMEWORK_CALENDAR)
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect {data ->
                data?.let {
                    if (data.first == RequestCode.CODE_STUDENT_HOMEWORK_CALENDAR ||
                        data.first == RequestCode.CODE_STUDENT_COMMENT_REGISTER ||
                        data.first == RequestCode.CODE_STUDENT_COMMENT_UPDATE ||
                        data.first == RequestCode.CODE_STUDENT_COMMENT_DELETE)
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
                    else if (data.first == RequestCode.CODE_STUDENT_HOMEWORK_DETAIL_LIST)
                    {
                        // 숙제 리스트의 경우 숙제를 학습하고 돌아왔을 때에만 로딩 다이얼로그 표시한다.
                        // 따라서 여기에서는 닫기 기능만 적용한다. (로딩 ON은 onActivityResultStatus 에서 따로 처리한다.)
                        if (data.second == false)
                        {
                            _isLoading.postValue(false)
                        }
                    }
                }
            }
        }

        // 숙제관리 (달력)
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.studentHomeworkCalendarData.collect { data ->
                data?.let {
                    val items = data as HomeworkCalendarBaseResult
                    mHomeworkCalendarBaseResult = items
                    fragmentViewModel.onSettingCalendarData(items)
                }
            }
        }

        // 숙제현황 (리스트)
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.studentHomeworkDetailData.collect { data ->
                data?.let {
                    val item = data as HomeworkDetailBaseResult
                    mHomeworkDetailBaseResult = item
                    fragmentViewModel.onUpdateHomeworkListScene(item)
                }
            }
        }

        // 학습자 한마디 등록
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.studentCommentRegisterData.collect { data ->
                data?.let {
                    // 학습자 한마디 등록, 수정, 삭제 성공했을 때 이전화면으로 이동
                    onClickBackButton()
                }
            }
        }

        // 학습자 한마디 수정
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.studentCommentUpdateData.collect { data ->
                data?.let {
                    // 학습자 한마디 등록, 수정, 삭제 성공했을 때 이전화면으로 이동
                    onClickBackButton()
                }
            }
        }

        // 학습자 한마디 삭제
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.studentCommentDeleteData.collect { data ->
                data?.let {
                    // 학습자 한마디 등록, 수정, 삭제 성공했을 때 이전화면으로 이동
                    onClickBackButton()
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect { data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    Log.f("status : ${result.status}, message : ${result.message} , code : $code")

                    if(result.isDuplicateLogin)
                    {
                        // 중복 로그인 재시작
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if (result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        _toast.value = result.message
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        if (code == RequestCode.CODE_STUDENT_HOMEWORK_CALENDAR ||
                            code == RequestCode.CODE_STUDENT_HOMEWORK_DETAIL_LIST)
                        {
                            _toast.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                        else if (code == RequestCode.CODE_STUDENT_COMMENT_REGISTER ||
                            code == RequestCode.CODE_STUDENT_COMMENT_UPDATE ||
                            code == RequestCode.CODE_STUDENT_COMMENT_DELETE)
                        {
                            _errorMessage.value = result.message
                        }
                    }
                }
            }
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

    private fun requestHomeworkCalendarAsync()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_STUDENT_HOMEWORK_CALENDAR,
            mYear,
            mMonth
        )
    }

    private fun requestHomeworkListAsync()
    {
        Log.f("")
        mSelectHomeworkData = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_STUDENT_HOMEWORK_DETAIL_LIST,
            mSelectHomeworkData!!.getHomeworkNumber()
        )
    }

    private fun requestCommentRegister()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_STUDENT_COMMENT_REGISTER,
            mStudentComment,
            homework.getHomeworkNumber()
        )
    }

    private fun requestCommentUpdate()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_STUDENT_COMMENT_UPDATE,
            mStudentComment,
            homework.getHomeworkNumber()
        )
    }

    private fun requestCommentDelete()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_STUDENT_COMMENT_DELETE,
            homework.getHomeworkNumber()
        )
    }

    override fun onAddResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(0))
    }

    override fun onActivityResult(code : ResultLauncherCode, intent : Intent?)
    {
        Log.f("")
        _isLoading.postValue(true)
        onPageChanged(Common.PAGE_HOMEWORK_STATUS)
    }

    fun onPageChanged(position : Int)
    {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }
            if (position == Common.PAGE_HOMEWORK_CALENDAR)
            {
                fragmentViewModel.onClearHomeworkListScene(true)
                requestHomeworkCalendarAsync() // 숙제관리(달력) 통신 요청
            }
            else if (position == Common.PAGE_HOMEWORK_STATUS)
            {
                 requestHomeworkListAsync() // 숙제현황(리스트) 통신 요청
            }
        }
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트
     * 뒤로가기 버튼은 숙제 현황 (리스트), 학습자 한마디, 선생님 화면에서만 표시
     * 숙제 관리 화면으로 이동 (달력)
     */
    fun onClickBackButton()
    {
        Log.f("currentPosition : $mPagePosition")

        if (mPagePosition == Common.PAGE_HOMEWORK_STATUS)
        {
            mPagePosition = Common.PAGE_HOMEWORK_CALENDAR
            _currentViewPage.value = Pair(mPagePosition, null)
        }
        else if (mPagePosition == Common.PAGE_HOMEWORK_COMMENT)
        {
            CommonUtils.getInstance(mContext).hideKeyboard() // 키보드 닫기 처리
            mPagePosition = Common.PAGE_HOMEWORK_STATUS
            _currentViewPage.value = Pair(mPagePosition, null)
        }
    }

    fun onRecordPermissionCancel()
    {
        // [취소] 컨텐츠 사용 불가 메세지 표시
        _errorMessage.value = mContext.getString(R.string.message_warning_record_permission)
    }

    fun onRecordPermissionChange()
    {
        // [권한 변경하기] 앱 정보 화면으로 이동
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", mContext.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
    }

    /** ===================== [숙제 관리] 달력 ======================== */
    fun onCompletedCalendarSet()
    {
        Log.f("")
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                delay(Common.DURATION_NORMAL)
            }
            _isLoading.postValue(false)
        }
    }

    fun onClickCalendarBefore()
    {
        Log.f("onClick Calendar Before")
        mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
        mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
        requestHomeworkCalendarAsync()
    }

    fun onClickCalendarAfter()
    {
        Log.f("onClick Calendar After")
        mYear = mHomeworkCalendarBaseResult!!.getNextYear()
        mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
        requestHomeworkCalendarAsync()
    }

    fun onClickCalendarItem(homeworkPosition : Int)
    {
        Log.f("onClick CalendarItem : $homeworkPosition")
        mSelectedHomeworkPosition = homeworkPosition // 선택한 숙제 인덱스 저장

        // 숙제 현황 페이지로 이동
        mPagePosition = Common.PAGE_HOMEWORK_STATUS
        _currentViewPage.value = Pair(mPagePosition, null)
    }

    /** ===================== [숙제 현황] 리스트 ======================== */
    fun onClickStudentCommentButton()
    {
        Log.f("onClick Student Comment - 학생용 ( 학생 코멘트 보기 )")
        mPagePosition = Common.PAGE_HOMEWORK_COMMENT
        mCommentType = HomeworkCommentType.COMMENT_STUDENT
        _currentViewPage.value = Pair(mPagePosition, mCommentType)
        fragmentViewModel.onSetCommentData(mHomeworkDetailBaseResult!!.getStudentComment())
        fragmentViewModel.onSettingStudentCommentPage(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
    }

    fun onClickTeacherCommentButton()
    {
        Log.f("onClick Teacher Comment - 학생용 ( 선생님 코멘트 보기 )")
        mPagePosition = Common.PAGE_HOMEWORK_COMMENT
        mCommentType = HomeworkCommentType.COMMENT_TEACHER
        _currentViewPage.value = Pair(mPagePosition, mCommentType)
        fragmentViewModel.onSetCommentData(mHomeworkDetailBaseResult!!.getTeacherComment())
        fragmentViewModel.onSettingStudentCommentPage(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
    }

    fun onClickHomeworkItem(item : HomeworkDetailItemData)
    {
        // 숙제목록 클릭 이벤트 (컨텐츠 이동)
        Log.f("Homework Type : ${item.getHomeworkType()}")
        val content = ContentsBaseResult()
        content.id = item.getContentID()
        content.setTitle(item.getName(), item.getSubName())
        content.thumbnail_url = item.getThumbnailUrl()

        when(item.getHomeworkType())
        {
            HomeworkType.ANIMATION -> startPlayerActivity(content)
            HomeworkType.EBOOK -> startEbookActivity(item.getContentID())
            HomeworkType.QUIZ -> startQuizActivity(item.getContentID())
            HomeworkType.CROSSWORD -> startCrosswordActivity(item.getContentID())
            HomeworkType.STARWORDS -> startStarWordsActivity(item.getContentID())
            HomeworkType.RECORDER ->
            {
                if (CommonUtils.getInstance(mContext).checkRecordPermission() == false)
                {
                    _showRecordPermissionDialog.call()
                }
                else
                {
                    startRecordPlayerActivity(content)
                }
            }
        }
    }

    /** ===================== [학습자/선생님 한마디] ======================== */
    fun onClickRegisterButton(comment : String)
    {
        mStudentComment = comment
        requestCommentRegister()
    }

    fun onClickUpdateButton(comment : String)
    {
        mStudentComment = comment
        requestCommentUpdate()
    }

    fun onClickDeleteButton()
    {
        requestCommentDelete()
    }

    private fun startPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")
        val playerIntentParamsObject = PlayerIntentParamsObject(arrayListOf(content), mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(playerIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun startEbookActivity(contentID : String)
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(contentID, mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun startQuizActivity(contentID : String)
    {
        Log.f("")
        val quizIntentParamsObject = QuizIntentParamsObject(contentID, mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun startCrosswordActivity(contentID : String)
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(contentID, mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun startStarWordsActivity(contentID : String)
    {
        Log.f("")
        val data : WebviewIntentParamsObject = WebviewIntentParamsObject(contentID, mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }

    private fun startRecordPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")
        val recordIntentParamsObject = RecordIntentParamsObject(content, mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(0))
            .startActivity()
    }
}