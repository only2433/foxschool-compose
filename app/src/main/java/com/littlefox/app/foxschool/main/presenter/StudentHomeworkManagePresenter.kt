package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.player.PlayerIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.quiz.QuizIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.HomeworkCalenderBaseObject
import com.littlefox.app.foxschool.`object`.result.HomeworkDetailListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult

import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.calendar.HomeworkCalendarItemData
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData

import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.DURATION_NORMAL
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.*
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.StudentHomeworkContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.HomeworkCalendarFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkCommentFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkListFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import kotlin.collections.ArrayList

/**
 * 학생 숙제관리 Presenter
 * @author 김태은
 */
class StudentHomeworkManagePresenter : StudentHomeworkContract.Presenter
{
    companion object
    {
        private const val MESSAGE_LIST_SET_COMPLETE : Int   = 100
        private const val MESSAGE_PAGE_CHANGE : Int         = 101

        private const val INDEX_STATUS : Int                = 0
    }

    private lateinit var mContext : Context
    private lateinit var mStudentHomeworkContractView : StudentHomeworkContract.View
    private var mMainHandler : WeakReferenceHandler? = null

    // 숙제 페이지 Adapter 관련 데이터 (숙제관리, 숙제현황, 코멘트)
    private val mHomeworkFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private var mHomeworkPagerAdapter : HomeworkPagerAdapter? = null

    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver
    private lateinit var mHomeworkCalendarFragmentObserver : HomeworkCalendarFragmentObserver
    private lateinit var mHomeworkListFragmentObserver : HomeworkListFragmentObserver
    private lateinit var mHomeworkCommentFragmentObserver : HomeworkCommentFragmentObserver

    // 통신
    private var mStudentHomeworkCalenderCoroutine : StudentHomeworkCalenderCoroutine? = null
    private var mStudentHomeworkDetailListCoroutine : StudentHomeworkDetailListCoroutine? = null
    private var mStudentCommentRegisterCoroutine : StudentCommentRegisterCoroutine? = null
    private var mStudentCommentUpdateCoroutine : StudentCommentUpdateCoroutine? = null
    private var mStudentCommentDeleteCoroutine : StudentCommentDeleteCoroutine? = null

    // 통신 응답 데이터
    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null
    private var mHomeworkDetailBaseResult : HomeworkDetailBaseResult? = null

    private var mPagePosition : Int = Common.PAGE_HOMEWORK_CALENDAR                         // 현재 보여지고있는 페이지 포지션
    private var mCommentType : HomeworkCommentType = HomeworkCommentType.COMMENT_STUDENT    // 코멘트 화면 타입

    // 통신에 입력되는 년도, 월
    private var mYear : String  = ""
    private var mMonth : String = ""

    private var mSelectedHomeworkPosition : Int = -1 // 숙제관리에서 선택한 숙제 포지션 (List/Comment 화면 공동 사용)

    // 학습자 한마디 (통신 입력용)
    private var mStudentComment : String = ""

    private var mSelectHomeworkData : HomeworkCalendarItemData? = null // 선택한 숙제 아이템
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private lateinit var mResultLauncherList : ArrayList<ActivityResultLauncher<Intent?>?>

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mStudentHomeworkContractView = (mContext as StudentHomeworkContract.View).apply {
            initView()
            initFont()
        }

        Log.f("")
        init()
    }

    private fun init()
    {
        Log.f("")

        // set ViewPager
        mHomeworkPagerAdapter = HomeworkPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mHomeworkFragmentList)
        mHomeworkPagerAdapter!!.setFragment()
        mStudentHomeworkContractView.initViewPager(mHomeworkPagerAdapter!!)

        // set Observer
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)
        mHomeworkCalendarFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCalendarFragmentObserver::class.java)
        mHomeworkListFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkListFragmentObserver::class.java)
        mHomeworkCommentFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCommentFragmentObserver::class.java)
        setupCalendarFragmentListener()
        setupListFragmentListener()
        setupCommentFragmentListener()

        onPageChanged(Common.PAGE_HOMEWORK_CALENDAR)
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
        mStudentHomeworkCalenderCoroutine?.cancel()
        mStudentHomeworkCalenderCoroutine = null
        mStudentHomeworkDetailListCoroutine?.cancel()
        mStudentHomeworkDetailListCoroutine = null
        mStudentCommentRegisterCoroutine?.cancel()
        mStudentCommentRegisterCoroutine = null
        mStudentCommentUpdateCoroutine?.cancel()
        mStudentCommentUpdateCoroutine = null
        mStudentCommentDeleteCoroutine?.cancel()
        mStudentCommentDeleteCoroutine = null
    }

    override fun onAddActivityResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
    {
        mResultLauncherList = arrayListOf()
        mResultLauncherList.add(launchers.get(0))
    }

    override fun onActivityResultStatus()
    {
        Log.f("")
        mStudentHomeworkContractView.showLoading()
        onPageChanged(Common.PAGE_HOMEWORK_STATUS)
    }

    override fun sendMessageEvent(msg : Message)
    {
        Log.f("message : ${msg.what}")
        when(msg.what)
        {
            MESSAGE_LIST_SET_COMPLETE -> mStudentHomeworkContractView.hideLoading()
            MESSAGE_PAGE_CHANGE ->
            {
                when(msg.obj)
                {
                    Common.PAGE_HOMEWORK_CALENDAR ->
                    {
                        mHomeworkManagePresenterObserver.clearHomeworkList(true)
                        requestHomeworkCalendar()   // 숙제관리(달력) 통신 요청
                    }
                    Common.PAGE_HOMEWORK_STATUS -> requestHomeworkList()        // 숙제현황(리스트) 통신 요청
                }
            }
        }
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트
     * 뒤로가기 버튼은 숙제 현황 (리스트), 학습자 한마디, 선생님 화면에서만 표시
     * 숙제 관리 화면으로 이동 (달력)
     */
    override fun onClickBackButton()
    {
        Log.f("currentPosition : "+mPagePosition)

        if (mPagePosition == Common.PAGE_HOMEWORK_STATUS)
        {
            mPagePosition = Common.PAGE_HOMEWORK_CALENDAR
            mStudentHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_CALENDAR)
        }
        else if (mPagePosition == Common.PAGE_HOMEWORK_COMMENT)
        {
            CommonUtils.getInstance(mContext).hideKeyboard() // 키보드 닫기 처리
            mPagePosition = Common.PAGE_HOMEWORK_STATUS
            mStudentHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_STATUS)
        }
    }

    override fun onPageChanged(position : Int)
    {
        Log.f("Page Change : "+ position)
        val msg = Message.obtain().apply {
            what = MESSAGE_PAGE_CHANGE
            obj = position
        }
        mMainHandler!!.sendMessageDelayed(msg, DURATION_NORMAL)
    }

    /**
     * ======================================================================================
     *                              숙제 - 다른 화면으로 이동
     * ======================================================================================
     */
    private fun onClickHomeworkItem(item : HomeworkDetailItemData)
    {
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
                    showChangeRecordPermissionDialog()
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
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
            .setResultLauncher(mResultLauncherList.get(INDEX_STATUS))
            .startActivity()
    }

    /**
     * 마이크 권한 허용 요청 다이얼로그
     * - 녹음기 기능 사용을 위해
     */
    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_record_permission))
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_change_permission))
            setDialogListener(mPermissionDialogListener)
            show()
        }
    }

    /**
     * ======================================================================================
     *                                    통신 요청
     * ======================================================================================
     */

    private fun requestHomeworkCalendar()
    {
        Log.f("")
        mStudentHomeworkContractView.showLoading()
        mStudentHomeworkCalenderCoroutine = StudentHomeworkCalenderCoroutine(mContext).apply {
            setData(mYear, mMonth)
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun requestHomeworkList()
    {
        Log.f("")
        mSelectHomeworkData = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkDetailListCoroutine = StudentHomeworkDetailListCoroutine(mContext).apply {
            setData(mSelectHomeworkData!!.getHomeworkNumber())
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun requestCommentRegister()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentRegisterCoroutine = StudentCommentRegisterCoroutine(mContext).apply {
            setData(mStudentComment, homework.getHomeworkNumber())
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun requestCommentUpdate()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentUpdateCoroutine = StudentCommentUpdateCoroutine(mContext).apply {
            setData(mStudentComment, homework.getHomeworkNumber())
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun requestCommentDelete()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentDeleteCoroutine = StudentCommentDeleteCoroutine(mContext).apply {
            setData(homework.getHomeworkNumber())
            asyncListener = mAsyncListener
            execute()
        }
    }

    /**
     * ======================================================================================
     *                                    Listeners
     * ======================================================================================
     */
    private fun setupCalendarFragmentListener()
    {
        mHomeworkCalendarFragmentObserver.onClickCalendarBefore.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onClick Calendar Before")
                mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
                mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
                requestHomeworkCalendar()
            })

        mHomeworkCalendarFragmentObserver.onClickCalendarAfter.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onClick Calendar After")
                mYear = mHomeworkCalendarBaseResult!!.getNextYear()
                mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
                requestHomeworkCalendar()
            })

        mHomeworkCalendarFragmentObserver.onClickCalendarItem.observe(mContext as AppCompatActivity,
            Observer<Int> { homeworkPosition ->
                Log.f("onClick CalendarItem : $homeworkPosition")
                mSelectedHomeworkPosition = homeworkPosition // 선택한 숙제 인덱스 저장

                // 숙제 현황 페이지로 이동
                mPagePosition = Common.PAGE_HOMEWORK_STATUS
                mStudentHomeworkContractView.setCurrentViewPage(mPagePosition)
            })

        // 달력 세팅 완료 (Activity 로딩 다이얼로그 닫기)
        mHomeworkCalendarFragmentObserver.onCompletedCalendarSet.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_LIST_SET_COMPLETE, Common.DURATION_NORMAL)
            })
    }

    private fun setupListFragmentListener()
    {
        mHomeworkListFragmentObserver.onClickStudentCommentButton.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onClick Student Comment")
                mPagePosition = Common.PAGE_HOMEWORK_COMMENT
                mCommentType = HomeworkCommentType.COMMENT_STUDENT
                mStudentHomeworkContractView.setCurrentViewPage(mPagePosition, mCommentType)
                mHomeworkManagePresenterObserver.setCommentData(mHomeworkDetailBaseResult!!.getStudentComment())
                mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
            })

        mHomeworkListFragmentObserver.onClickTeacherCommentButton.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                Log.f("onClick Teacher Comment")
                mPagePosition = Common.PAGE_HOMEWORK_COMMENT
                mCommentType = HomeworkCommentType.COMMENT_TEACHER
                mStudentHomeworkContractView.setCurrentViewPage(mPagePosition, mCommentType)
                mHomeworkManagePresenterObserver.setCommentData(mHomeworkDetailBaseResult!!.getTeacherComment())
                mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
            })

        // 숙제목록 클릭 이벤트 (컨텐츠 이동)
        mHomeworkListFragmentObserver.onClickHomeworkItem.observe(mContext as AppCompatActivity,
            Observer<HomeworkDetailItemData> {item ->
                onClickHomeworkItem(item)
            })
    }

    private fun setupCommentFragmentListener()
    {
        mHomeworkCommentFragmentObserver.onClickRegisterButton.observe(mContext as AppCompatActivity,
            Observer<String> { comment ->
                mStudentComment = comment
                requestCommentRegister()
            })

        mHomeworkCommentFragmentObserver.onClickUpdateButton.observe(mContext as AppCompatActivity,
            Observer<String> { comment ->
                mStudentComment = comment
                requestCommentUpdate()
            })

        mHomeworkCommentFragmentObserver.onClickDeleteButton.observe(mContext as AppCompatActivity,
            Observer<Boolean> {
                requestCommentDelete()
            })
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            val result : BaseResult? = mObject as BaseResult?

            if (result == null) return

            Log.f("code : $code, status : ${result.getStatus()}")

            mStudentHomeworkContractView.hideLoading()
            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                // 통신 성공
                if (code == Common.COROUTINE_CODE_STUDENT_HOMEWORK_CALENDER)
                {
                    // 숙제관리 (달력)
                    mHomeworkCalendarBaseResult = (result as HomeworkCalenderBaseObject).getData()
                    mHomeworkManagePresenterObserver.setCalendarData(mHomeworkCalendarBaseResult!!)
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_HOMEWORK_DETAIL_LIST)
                {
                    // 숙제현황 (리스트)
                    mHomeworkDetailBaseResult = (result as HomeworkDetailListBaseObject).getData()
                    mHomeworkManagePresenterObserver.updateHomeworkListData(mHomeworkDetailBaseResult!!)
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER ||
                         code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE ||
                         code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                {
                    // 학습자 한마디 등록, 수정, 삭제 성공했을 때 이전화면으로 이동
                    onClickBackButton()
                }
            }
            else
            {
                // 통신 실패
                if (result.isDuplicateLogin)
                {
                    // 중복 로그인 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_STUDENT_HOMEWORK_CALENDER ||
                        code == Common.COROUTINE_CODE_STUDENT_HOMEWORK_DETAIL_LIST)
                    {
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER ||
                             code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE   ||
                             code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                    {
                        mStudentHomeworkContractView.showErrorMessage(result.getMessage())
                    }
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }

    private val mPermissionDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(messageType : Int) {}

        override fun onChoiceButtonClick(buttonType : DialogButtonType, messageType : Int)
        {
            when(buttonType)
            {
                DialogButtonType.BUTTON_1 ->
                {
                    // [취소] 컨텐츠 사용 불가 메세지 표시
                    mStudentHomeworkContractView.showErrorMessage(mContext.getString(R.string.message_warning_record_permission))
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
}