package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        private const val REQUEST_CODE_NOTIFY : Int         = 1000
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

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mStudentHomeworkContractView = mContext as StudentHomeworkContract.View
        mStudentHomeworkContractView.initView()
        mStudentHomeworkContractView.initFont()

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

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        Log.f("requestCode : $requestCode, resultCode : $resultCode")
        when(requestCode)
        {
            REQUEST_CODE_NOTIFY ->
            {
                mStudentHomeworkContractView.showLoading()
                onPageChanged(Common.PAGE_HOMEWORK_STATUS)
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_LIST_SET_COMPLETE -> mStudentHomeworkContractView.hideLoading()
            MESSAGE_PAGE_CHANGE ->
            {
                when(msg.obj)
                {
                    Common.PAGE_HOMEWORK_CALENDAR -> requestStudentHomework()   // 숙제관리(달력) 통신 요청
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
        Log.f("")
        if (mPagePosition == Common.PAGE_HOMEWORK_STATUS)
        {
            mHomeworkManagePresenterObserver.clearHomeworkList(true) // 숙제현황 리스트 초기화

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

    /**
     * 페이지 이동 이벤트
     */
    override fun onPageChanged(position : Int)
    {
        Log.f("")
        val msg = Message.obtain()
        msg.what = MESSAGE_PAGE_CHANGE
        msg.obj = position
        mMainHandler!!.sendMessageDelayed(msg, DURATION_NORMAL)
    }

    /**
     * ======================================================================================
     *                              숙제 - 다른 화면으로 이동
     * ======================================================================================
     */

    /**
     * 숙제현황 리스트 클릭 이벤트
     */
    private fun onClickHomeworkItem(item : HomeworkDetailItemData)
    {
        val content = ContentsBaseResult()
        content.setID(item.getContentID())
        content.setTitle(CommonUtils.getInstance(mContext).getSubStringTitleName(item.getTitle()))
        content.setThumbnailUrl(item.getThumbnailUrl())

        when(item.getHomeworkType())
        {
            HomeworkType.ANIMATION -> startPlayerActivity(content)
            HomeworkType.EBOOK -> startEbookActivity(item.getContentID())
            HomeworkType.QUIZ -> startQuizActivity(item.getContentID())
            HomeworkType.CROSSWORD -> startCrosswordActivity(item.getContentID())
            HomeworkType.STARWORDS -> startStarWordsActivity(item.getContentID())
            HomeworkType.RECORDER -> startRecordActivity(content)
        }
    }

    /**
     * 동화/동요 플레이어로 이동
     */
    private fun startPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")

        val playerIntentParamsObject = PlayerIntentParamsObject(
            arrayListOf(content),
            mSelectHomeworkData!!.getHomeworkNumber())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(playerIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_NOTIFY)
            .startActivity()
    }

    /**
     * eBook 학습화면으로 이동
     */
    private fun startEbookActivity(contentID : String)
    {
        Log.f("")
        val data : WebviewIntentParamsObject =
            WebviewIntentParamsObject(contentID, mSelectHomeworkData!!.getHomeworkNumber())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_EBOOK)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_NOTIFY)
            .startActivity()
    }

    /**
     * 퀴즈 학습화면으로 이동
     * TODO 김태은 퀴즈 API 완성된 후 확인화기
     */
    private fun startQuizActivity(contentID : String)
    {
        Log.f("")
        var quizIntentParamsObject : QuizIntentParamsObject = QuizIntentParamsObject(
            contentID,
            mSelectHomeworkData!!.getHomeworkNumber())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_NOTIFY)
            .startActivity()
    }

    /**
     * 크로스워드 학습화면으로 이동
     * TODO 김태은 크로스워드 API 완성된 후 확인화기
     */
    private fun startCrosswordActivity(contentID : String)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
            .setData(contentID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 스타워즈 학습화면으로 이동
     */
    private fun startStarWordsActivity(contentID : String)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
            .setData(contentID)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /**
     * 녹음기 화면으로 이동
     */
    private fun startRecordActivity(content : ContentsBaseResult)
    {
        Log.f("")

        val recordIntentParamsObject = RecordIntentParamsObject(
            content,
            mSelectHomeworkData!!.getHomeworkNumber())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(recordIntentParamsObject)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_NOTIFY)
            .startActivity()
    }

    /**
     * ======================================================================================
     *                                    통신 요청
     * ======================================================================================
     */

    /**
     * 숙제관리 달력 통신 요청 (학생)
     */
    private fun requestStudentHomework()
    {
        Log.f("")
        mStudentHomeworkContractView.showLoading()
        mStudentHomeworkCalenderCoroutine = StudentHomeworkCalenderCoroutine(mContext)
        mStudentHomeworkCalenderCoroutine!!.setData(mYear, mMonth)
        mStudentHomeworkCalenderCoroutine!!.asyncListener = mAsyncListener
        mStudentHomeworkCalenderCoroutine!!.execute()
    }

    /**
     * 숙제현황 통신 요청 (학생)
     */
    private fun requestHomeworkList()
    {
        Log.f("")

        // 숙제 아이템 정보 요청
        mSelectHomeworkData = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkDetailListCoroutine = StudentHomeworkDetailListCoroutine(mContext)
        mStudentHomeworkDetailListCoroutine!!.setData(mSelectHomeworkData!!.getHomeworkNumber())
        mStudentHomeworkDetailListCoroutine!!.asyncListener = mAsyncListener
        mStudentHomeworkDetailListCoroutine!!.execute()
    }

    /**
     * 코멘트 등록 요청
     */
    private fun requestCommentRegister()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentRegisterCoroutine = StudentCommentRegisterCoroutine(mContext)
        mStudentCommentRegisterCoroutine!!.setData(mStudentComment, homework.getHomeworkNumber())
        mStudentCommentRegisterCoroutine!!.asyncListener = mAsyncListener
        mStudentCommentRegisterCoroutine!!.execute()
    }

    /**
     * 코멘트 수정 요청
     */
    private fun requestCommentUpdate()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentUpdateCoroutine = StudentCommentUpdateCoroutine(mContext)
        mStudentCommentUpdateCoroutine!!.setData(mStudentComment, homework.getHomeworkNumber())
        mStudentCommentUpdateCoroutine!!.asyncListener = mAsyncListener
        mStudentCommentUpdateCoroutine!!.execute()
    }

    /**
     * 코멘트 수정 요청
     */
    private fun requestCommentDelete()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mStudentHomeworkContractView.showLoading()
        mStudentCommentDeleteCoroutine = StudentCommentDeleteCoroutine(mContext)
        mStudentCommentDeleteCoroutine!!.setData(homework.getHomeworkNumber())
        mStudentCommentDeleteCoroutine!!.asyncListener = mAsyncListener
        mStudentCommentDeleteCoroutine!!.execute()
    }

    /**
     * ======================================================================================
     *                                    Listeners
     * ======================================================================================
     */
    private fun setupCalendarFragmentListener()
    {
        // 이전 화살표 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarBefore.observe(mContext as AppCompatActivity, {
            mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
            mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
            requestStudentHomework()
        })

        // 다음 화살표 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarAfter.observe(mContext as AppCompatActivity, {
            mYear = mHomeworkCalendarBaseResult!!.getNextYear()
            mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
            requestStudentHomework()
        })

        // 달력 아이템 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarItem.observe(mContext as AppCompatActivity, { homeworkPosition ->
            mSelectedHomeworkPosition = homeworkPosition // 선택한 숙제 인덱스 저장

            // 숙제 현황 페이지로 이동
            mPagePosition = Common.PAGE_HOMEWORK_STATUS
            mStudentHomeworkContractView.setCurrentViewPage(mPagePosition)
        })

        // 달력 세팅 완료 (Activity 로딩 다이얼로그 닫기)
        mHomeworkCalendarFragmentObserver.onCompletedCalendarSet.observe(mContext as AppCompatActivity, {
            mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_LIST_SET_COMPLETE, Common.DURATION_NORMAL)
        })
    }

    private fun setupListFragmentListener()
    {
        // 학습자 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickStudentCommentButton.observe(mContext as AppCompatActivity, {
            mPagePosition = Common.PAGE_HOMEWORK_COMMENT
            mCommentType = HomeworkCommentType.COMMENT_STUDENT
            mStudentHomeworkContractView.setCurrentViewPage(mPagePosition, mCommentType)
            mHomeworkManagePresenterObserver.setCommentData(mHomeworkDetailBaseResult!!.getStudentComment())
            mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
        })

        // 선생님 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickTeacherCommentButton.observe(mContext as AppCompatActivity, {
            mPagePosition = Common.PAGE_HOMEWORK_COMMENT
            mCommentType = HomeworkCommentType.COMMENT_TEACHER
            mStudentHomeworkContractView.setCurrentViewPage(mPagePosition, mCommentType)
            mHomeworkManagePresenterObserver.setCommentData(mHomeworkDetailBaseResult!!.getTeacherComment())
            mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
        })

        // 숙제목록 클릭 이벤트 (컨텐츠 이동)
        mHomeworkListFragmentObserver.onClickHomeworkItem.observe(mContext as AppCompatActivity, { item ->
            onClickHomeworkItem(item)
        })
    }

    private fun setupCommentFragmentListener()
    {
        // 학습자 한마디 등록 버튼 클릭 이벤트
        mHomeworkCommentFragmentObserver.onClickRegisterButton.observe(mContext as AppCompatActivity, { comment ->
            mStudentComment = comment
            requestCommentRegister()
        })

        // 학습자 한마디 수정 버튼 클릭 이벤트
        mHomeworkCommentFragmentObserver.onClickUpdateButton.observe(mContext as AppCompatActivity, { comment ->
            mStudentComment = comment
            requestCommentUpdate()
        })

        // 학습자 한마디 삭제 버튼 클릭 이벤트
        mHomeworkCommentFragmentObserver.onClickDeleteButton.observe(mContext as AppCompatActivity, { bool ->
            requestCommentDelete()
        })
    }

    /**
     * 통신 응답 Listener
     */
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
                    if (mPagePosition == Common.PAGE_HOMEWORK_STATUS)
                    {
                        mHomeworkDetailBaseResult = (result as HomeworkDetailListBaseObject).getData()
                        mHomeworkManagePresenterObserver.updateHomeworkListData(mHomeworkDetailBaseResult!!)
                    }
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER)
                {
                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
                    mStudentHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_register))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE)
                {

                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
                    mStudentHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_update))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                {
                    mStudentComment = ""
                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mCommentType, mHomeworkDetailBaseResult!!.isEvaluationComplete())
                    mStudentHomeworkContractView.showErrorMessage(mContext.resources.getString(R.string.message_comment_delete))
                }
            }
            else
            {
                // 통신 실패
                if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
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


}