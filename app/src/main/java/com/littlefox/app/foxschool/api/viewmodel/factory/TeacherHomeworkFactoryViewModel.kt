package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.*
import com.littlefox.app.foxschool.`object`.result.homework.calendar.HomeworkCalendarItemData
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData
import com.littlefox.app.foxschool.adapter.TeacherHomeworkPagerAdapter
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.TeacherHomeworkApiViewModel
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
import javax.inject.Inject

@HiltViewModel
class TeacherHomeworkFactoryViewModel @Inject constructor(private val apiViewModel : TeacherHomeworkApiViewModel) : BaseFactoryViewModel()
{
    companion object
    {
        private const val INDEX_HOMEWORK_DETAIL : Int       = 0
        private const val INDEX_HOMEWORK_STATUS : Int       = 1
    }

    private val _settingViewPager = SingleLiveEvent<TeacherHomeworkPagerAdapter>()
    val settingViewPager: LiveData<TeacherHomeworkPagerAdapter> get() = _settingViewPager

    private val _currentViewListPage = SingleLiveEvent<Pair<Int, HomeworkDetailType?>>()
    val currentViewListPage: LiveData<Pair<Int, HomeworkDetailType?>> get() = _currentViewListPage

    private val _currentViewCommentPage = SingleLiveEvent<Pair<Int, HomeworkCommentType?>>()
    val currentViewCommentPage: LiveData<Pair<Int, HomeworkCommentType?>> get() = _currentViewCommentPage

    private val _showAudioPlayDialog = SingleLiveEvent<HomeworkDetailItemData>()
    val showAudioPlayDialog: LiveData<HomeworkDetailItemData> get() = _showAudioPlayDialog

    private val _showRecordPermissionDialog = SingleLiveEvent<Void>()
    val showRecordPermissionDialog: LiveData<Void> get() = _showRecordPermissionDialog

    private lateinit var mContext : Context

    // 통신 응답 데이터
    private var mClassListBaseResult : ArrayList<TeacherClassItemData>? = null
    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null
    private var mHomeworkStatusBaseResult : HomeworkStatusBaseResult? = null
    private var mHomeworkDetailBaseResult : HomeworkDetailBaseResult? = null

    // 숙제 페이지 Adapter 관련 데이터 (숙제관리, 숙제현황, 코멘트)
    private val mHomeworkFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private lateinit var mHomeworkPagerAdapter : TeacherHomeworkPagerAdapter

    private var mClassIndex : Int = 0 // 선택한 학급 인덱스
    private var mSelectedHomeworkPosition : Int = -1 // 숙제관리에서 선택한 숙제 포지션 (List/Comment 화면 공동 사용)
    private var mSelectedStudentPosition : Int  = -1 // 숙제현황에서 선택한 학생 포지션 (상세화면으로 이동)

    private var mBeforePagePosition : Int = Common.PAGE_HOMEWORK_CALENDAR   // 이전 페이지 포지션
    private var mPagePosition : Int = Common.PAGE_HOMEWORK_CALENDAR         // 현재 보여지고있는 페이지 포지션
    private var mCommentType : HomeworkCommentType = HomeworkCommentType.COMMENT_STUDENT    // 코멘트 화면 타입
    private var mDetailType : HomeworkDetailType = HomeworkDetailType.TYPE_HOMEWORK_CONTENT // 리스트 상세 화면 타입

    private var mSelectHomeworkData : HomeworkCalendarItemData? = null // 선택한 숙제 아이템

    // 통신에 입력되는 년도, 월
    private var mYear : String  = ""
    private var mMonth : String = ""

    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>
    private lateinit var fragmentViewModel : HomeworkFragmentViewModel

    override fun init(context : Context)
    {
        mContext = context
        fragmentViewModel = ViewModelProvider(mContext as AppCompatActivity).get(
            HomeworkFragmentViewModel::class.java)
        setupViewModelObserver()

        // set ViewPager
        mHomeworkPagerAdapter = TeacherHomeworkPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mHomeworkFragmentList)
        mHomeworkPagerAdapter.setFragment()
        _settingViewPager.value = mHomeworkPagerAdapter

        onPageChanged(Common.PAGE_HOMEWORK_CALENDAR)
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.isLoading.collect {data ->
                data?.let {
                    if(data.second)
                    {
                        // RequestCode.CODE_TEACHER_HOMEWORK_DETAIL_LIST, data.first == RequestCode.CODE_TEACHER_HOMEWORK_CONTENTS
                        // 숙제 현황 상세 보기, 숙제 내용 화면에서는 리스트의 로딩을 사용하기 때문에 전체 로딩 다이얼로그를 표시하지 않는다.
                        // 하지만 "숙제 내용" 화면에서 다른 Activity로 이동했다가 돌아오는 경우에는 Loading을 사용하기 때문에
                        // 따라서 여기에서는 닫기 기능만 적용한다. (로딩 ON은 onActivityResultHomeworkDetail 에서 따로 처리한다.)
                        if (data.first == RequestCode.CODE_TEACHER_HOMEWORK_CLASS_LIST ||
                            data.first == RequestCode.CODE_TEACHER_HOMEWORK_CALENDAR ||
                            data.first == RequestCode.CODE_TEACHER_HOMEWORK_STATUS)
                        {
                            _isLoading.postValue(true)
                        }
                    }
                    else
                    {
                        _isLoading.postValue(false)
                    }
                }
            }
        }

        // 학급 리스트
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkClassListData.collect {data ->
                data?.let {
                    val items = data as ArrayList<TeacherClassItemData>
                    mClassListBaseResult = items
                    fragmentViewModel.onSettingClassData(mClassListBaseResult!!)

                    requestClassCalendar() // 클래스 달력 통신 요청
                }
            }
        }

        // 숙제관리 (달력)
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkCalendarData.collect { data ->
                data?.let {
                    val items = data as HomeworkCalendarBaseResult
                    mHomeworkCalendarBaseResult = items
                    fragmentViewModel.onSettingCalendarData(items)
                }
            }
        }

        // 숙제현황
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkStatusData.collect { data ->
                data?.let {
                    val items = data as HomeworkStatusBaseResult
                    mHomeworkStatusBaseResult = items
                    fragmentViewModel.onUpdateClassName(mClassListBaseResult!![mClassIndex].getClassName())
                    fragmentViewModel.onUpdateHomeworkStatusListScene(mHomeworkStatusBaseResult!!)
                }
            }
        }

        // 숙제현황 상세보기
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkDetailData.collect { data ->
                data?.let {
                    val item = data as HomeworkDetailBaseResult
                    val name = mHomeworkStatusBaseResult!!.getStudentStatusItemList()!![mSelectedStudentPosition].getUserName()
                    mHomeworkDetailBaseResult = item.apply {
                        setFragmentTitle("$name 학생")
                        setFragmentType(mDetailType)
                    }
                    fragmentViewModel.onUpdateHomeworkListScene(mHomeworkDetailBaseResult!!)
                }
            }
        }

        // 숙제내용
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.teacherHomeworkContentsData.collect { data ->
                data?.let {
                    val item = data as HomeworkDetailBaseResult
                    mHomeworkDetailBaseResult = item.apply {
                        setFragmentTitle(mClassListBaseResult!!.get(mClassIndex).getClassName())
                        setFragmentType(mDetailType)
                    }
                    fragmentViewModel.onUpdateHomeworkListScene(mHomeworkDetailBaseResult!!)
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
                        if (code == RequestCode.CODE_TEACHER_HOMEWORK_CLASS_LIST ||
                            code == RequestCode.CODE_TEACHER_HOMEWORK_CALENDAR)
                        {
                            _toast.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                        else if (code == RequestCode.CODE_TEACHER_HOMEWORK_STATUS ||
                                code == RequestCode.CODE_TEACHER_HOMEWORK_DETAIL_LIST ||
                                code == RequestCode.CODE_TEACHER_HOMEWORK_CONTENTS)
                        {
                            _errorMessage.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
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

    private fun requestClassList()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_CLASS_LIST
        )
    }

    private fun requestClassCalendar()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_CALENDAR,
            mClassListBaseResult!!.get(mClassIndex).getClassID().toString(),
            mYear,
            mMonth
        )
    }

    private fun requestStatusList()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_STATUS,
            mClassListBaseResult!!.get(mClassIndex).getClassID(),
            mHomeworkCalendarBaseResult!!.getHomeworkDataList().get(mSelectedHomeworkPosition).getHomeworkNumber()
        )
    }

    private fun requestStudentHomework()
    {
        Log.f("")
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_DETAIL_LIST,
            mClassListBaseResult!!.get(mClassIndex).getClassID(),
            mHomeworkCalendarBaseResult!!.getHomeworkDataList().get(mSelectedHomeworkPosition).getHomeworkNumber(),
            mHomeworkStatusBaseResult!!.getStudentStatusItemList()!!.get(mSelectedStudentPosition).getUserID()
        )
    }

    private fun requestHomeworkDetail()
    {
        Log.f("")
        // 숙제 아이템 정보 요청
        mSelectHomeworkData = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_TEACHER_HOMEWORK_CONTENTS,
            mClassListBaseResult!!.get(mClassIndex).getClassID(),
            mSelectHomeworkData!!.getHomeworkNumber()
        )
    }

    override fun onAddResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(INDEX_HOMEWORK_DETAIL))
        mResultLauncherList.add(launchers.get(INDEX_HOMEWORK_STATUS))
    }

    override fun onActivityResult(code : ResultLauncherCode, intent : Intent?)
    {
        when(code)
        {
            ResultLauncherCode.HOMEWORK_DETAIL ->
            {
                Log.f("ResultLauncherCode.HOMEWORK_DETAIL")
                _isLoading.postValue(true)
                onPageChanged(Common.PAGE_HOMEWORK_DETAIL)
            }
            ResultLauncherCode.HOMEWORK_STATUS ->
            {
                Log.f("ResultLauncherCode.HOMEWORK_STATUS")
                onPageChanged(Common.PAGE_HOMEWORK_STATUS)
            }
        }
    }

    fun onPageChanged(position : Int)
    {
        viewModelScope.launch(Dispatchers.Main) {
            delay(Common.DURATION_NORMAL)
            when(position)
            {
                Common.PAGE_HOMEWORK_CALENDAR ->
                {
                    // 화면에 표시되는 데이터 초기화
                    fragmentViewModel.onClearHomeworkStatusListScene()
                    requestClassList() // 클래스 리스트 통신 요청
                }
                Common.PAGE_HOMEWORK_STATUS ->
                {
                    // 숙제 리스트 초기화
                    fragmentViewModel.onClearHomeworkListScene(true)
                    requestStatusList() // 숙제 현황 통신 요청
                }
                Common.PAGE_HOMEWORK_DETAIL ->
                {
                    if (mDetailType == HomeworkDetailType.TYPE_HOMEWORK_CURRENT_STATUS_DETAIL)
                    {
                        requestStudentHomework() // 숙제 현황 상세 보기 통신 요청
                    }
                    else if (mDetailType == HomeworkDetailType.TYPE_HOMEWORK_CONTENT)
                    {
                        requestHomeworkDetail() // 숙제 내용 통신 요청
                    }
                }
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

        if (mPagePosition == Common.PAGE_HOMEWORK_COMMENT)
        {
            // 이전 화면에 대한 포지션을 들고있다가 세팅
            mPagePosition = mBeforePagePosition
            _currentViewListPage.value = Pair(mPagePosition, mDetailType)
        }
        else
        {
            mPagePosition -= 1
            _currentViewListPage.value = Pair(mPagePosition, null)
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
        _isLoading.postValue(false) // 달력 세팅 완료 (Activity 로딩 다이얼로그 닫기)
    }

    fun onClickClassPicker(index : Int)
    {
        Log.f("onClick ClassItem : $index")
        mClassIndex = index
        requestClassCalendar()
    }

    fun onClickCalendarBefore()
    {
        Log.f("onClick Calendar Before")
        mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
        mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
        requestClassCalendar()
    }

    fun onClickCalendarAfter()
    {
        Log.f("onClick Calendar After")
        mYear = mHomeworkCalendarBaseResult!!.getNextYear()
        mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
        requestClassCalendar()
    }

    fun onClickCalendarItem(homeworkPosition : Int)
    {
        Log.f("onClick CalendarItem : $homeworkPosition")
        mSelectedHomeworkPosition = homeworkPosition // 선택한 숙제 인덱스 저장

        // 숙제 현황 페이지로 이동
        mPagePosition = Common.PAGE_HOMEWORK_STATUS
        _currentViewListPage.value = Pair(mPagePosition, null)
    }

    /** ===================== [숙제 현황] ======================== */
    fun onClickHomeworkContents()
    {
        // 숙제 내용 페이지로 이동
        Log.f("onClick Homework Contents")
        mPagePosition = Common.PAGE_HOMEWORK_DETAIL
        mDetailType = HomeworkDetailType.TYPE_HOMEWORK_CONTENT
        _currentViewListPage.value = Pair(mPagePosition, mDetailType)
    }

    fun onClickShowDetailButton(index : Int)
    {
        Log.f("onClick Homework Detail : $index")
        mSelectedStudentPosition = index

        // 숙제 현황 상세 페이지로 이동
        mPagePosition = Common.PAGE_HOMEWORK_DETAIL
        mDetailType = HomeworkDetailType.TYPE_HOMEWORK_CURRENT_STATUS_DETAIL
        _currentViewListPage.value = Pair(mPagePosition, mDetailType)
    }

    fun onClickHomeworkBundleChecking(IDList : ArrayList<String>)
    {
        // [일괄 숙제 검사] 클릭 이벤트
        Log.f("onClick HomeworkChecking multi")
        if (IDList.isEmpty())
        {
            _errorMessage.value = mContext.getString(R.string.message_warning_choose_student)
        }
        else
        {
            val data = HomeworkCheckingIntentParamsObject(
                mHomeworkCalendarBaseResult!!.getHomeworkDataList().get(mSelectedHomeworkPosition).getHomeworkNumber(),
                mClassListBaseResult!!.get(mClassIndex).getClassID(),
                IDList
            )
            startHomeworkCheckingActivity(data)
        }
    }

    fun onClickHomeworkChecking(index : Int)
    {
        // [숙제 검사] 클릭 이벤트 (1건)
        Log.f("onClick HomeworkChecking one")
        val data = HomeworkCheckingIntentParamsObject(
            mHomeworkCalendarBaseResult!!.getHomeworkDataList().get(mSelectedHomeworkPosition).getHomeworkNumber(),
            mClassListBaseResult!!.get(mClassIndex).getClassID(),
            mHomeworkStatusBaseResult!!.getStudentStatusItemList()!!.get(index)
        )

        startHomeworkCheckingActivity(data)
    }

    /** ===================== [숙제 현황 상세 보기 || 숙제 내용] ======================== */
    fun onClickStudentCommentButton()
    {
        Log.f("onClick Student Comment")
        mBeforePagePosition = mPagePosition
        mPagePosition = Common.PAGE_HOMEWORK_COMMENT
        mCommentType = HomeworkCommentType.COMMENT_STUDENT
        _currentViewCommentPage.value = Pair(mPagePosition, mCommentType)
        fragmentViewModel.onSettingTeacherCommentPage(mCommentType, mHomeworkDetailBaseResult!!.getStudentComment())
    }

    fun onClickTeacherCommentButton()
    {
        Log.f("onClick Teacher Comment")
        mBeforePagePosition = mPagePosition
        mPagePosition = Common.PAGE_HOMEWORK_COMMENT
        mCommentType = HomeworkCommentType.COMMENT_TEACHER
        _currentViewCommentPage.value = Pair(mPagePosition, mCommentType)
        fragmentViewModel.onSettingTeacherCommentPage(mCommentType, mHomeworkDetailBaseResult!!.getTeacherComment())
    }

    // 숙제목록 클릭 이벤트 (컨텐츠 이동)
    fun onClickHomeworkItem(item : HomeworkDetailItemData)
    {
        // 숙제내용 화면 인 경우에만 학습 가능
        if (mDetailType == HomeworkDetailType.TYPE_HOMEWORK_CONTENT)
        {
            Log.f("onClick play homework")
            moveHomeworkItemActivity(item)
        }
        else if (mDetailType == HomeworkDetailType.TYPE_HOMEWORK_CURRENT_STATUS_DETAIL &&
            item.getHomeworkType() == HomeworkType.RECORDER &&
            item.isComplete && item.getExpired() > 0)
        {
            Log.f("onClick play record audio")
            // 숙제 현황 상세 보기 화면
            // 녹음 데이터가 있는 경우
            _showAudioPlayDialog.value = item
        }
    }

    private fun moveHomeworkItemActivity(item : HomeworkDetailItemData)
    {
        Log.f("Homework Type : ${item.getHomeworkType()}")
        val content = ContentsBaseResult()
        content.setID(item.getContentID())
        content.setTitle(item.getName(), item.getSubName())
        content.setThumbnailUrl(item.getThumbnailUrl())

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

    private fun startPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")
        val playerIntentParamsObject = PlayerIntentParamsObject(arrayListOf(content), mSelectHomeworkData!!.getHomeworkNumber())
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(playerIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_DETAIL))
            .startActivity()
    }

    private fun startHomeworkCheckingActivity(data : HomeworkCheckingIntentParamsObject)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.HOMEWORK_CHECKING)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setResultLauncher(mResultLauncherList.get(INDEX_HOMEWORK_STATUS))
            .startActivity()
    }
}