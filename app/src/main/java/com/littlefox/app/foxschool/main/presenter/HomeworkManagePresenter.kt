package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.quiz.QuizDataObject
import com.littlefox.app.foxschool.`object`.result.HomeworkManageCalenderBaseObject
import com.littlefox.app.foxschool.`object`.result.HomeworkStatusListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarItemData
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListItemData
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.DURATION_NORMAL
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.*
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.app.foxschool.main.contract.HomeworkContract
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
 * 숙제관리 Presenter
 * @author 김태은
 */
class HomeworkManagePresenter : HomeworkContract.Presenter
{
    companion object
    {
        private const val MESSAGE_LIST_SET_COMPLETE : Int   = 100
        private const val MESSAGE_PAGE_CHANGE : Int         = 101

        private const val REQUEST_CODE_NOTIFY : Int         = 1000
    }

    private lateinit var mContext : Context
    private lateinit var mHomeworkContractView : HomeworkContract.View
    private var mMainHandler : WeakReferenceHandler? = null

    // 숙제 페이지 Adapter 관련 데이터 (숙제관리, 숙제현황, 코멘트)
    private val mHomeworkFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private var mHomeworkPagerAdapter : HomeworkPagerAdapter? = null

    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver
    private lateinit var mHomeworkCalendarFragmentObserver : HomeworkCalendarFragmentObserver
    private lateinit var mHomeworkListFragmentObserver : HomeworkListFragmentObserver
    private lateinit var mHomeworkCommentFragmentObserver : HomeworkCommentFragmentObserver

    // 통신
    private var mHomeworkManageStudentCoroutine : HomeworkManageStudentCoroutine? = null
    private var mHomeworkStatusListCoroutine : HomeworkStatusListCoroutine? = null
    private var mStudentCommentRegisterCoroutine : StudentCommentRegisterCoroutine? = null
    private var mStudentCommentUpdateCoroutine : StudentCommentUpdateCoroutine? = null
    private var mStudentCommentDeleteCoroutine : StudentCommentDeleteCoroutine? = null

    // 통신 응답 데이터
    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null
    private var mHomeworkListBaseResult : HomeworkListBaseResult? = null

    private var mPagePosition : Int = Common.PAGE_HOMEWORK_CALENDAR // 현재 보여지고있는 페이지 포지션

    /** 숙제관리 (달력) */
    // 통신에 입력되는 년도, 월
    private var mYear : String  = ""
    private var mMonth : String = ""

    private var mSelectedHomeworkPosition : Int = -1 // 숙제관리에서 선택한 숙제 포지션 (List/Comment 화면 공동 사용)

    /** 학습자한마디 **/
    // 학습자 한마디 (통신 입력용)
    private var mStudentComment : String = ""

    private var mSelectHomeworkData : HomeworkCalendarItemData? = null

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mHomeworkContractView = mContext as HomeworkContract.View
        mHomeworkContractView.initView()
        mHomeworkContractView.initFont()

        Log.f("")
        init()
    }

    private fun init()
    {
        Log.f("")

        // set ViewPager
        mHomeworkPagerAdapter = HomeworkPagerAdapter((mContext as AppCompatActivity).supportFragmentManager, mHomeworkFragmentList)
        mHomeworkPagerAdapter!!.setFragment()
        mHomeworkContractView.initViewPager(mHomeworkPagerAdapter!!)

        // set Observer
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)
        mHomeworkCalendarFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCalendarFragmentObserver::class.java)
        mHomeworkListFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkListFragmentObserver::class.java)
        mHomeworkCommentFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCommentFragmentObserver::class.java)
        setupCalendarFragmentListener()
        setupListFragmentListener()
        setupCommentFragmentListener()

        requestStudentHomework()
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
        mHomeworkManageStudentCoroutine?.cancel()
        mHomeworkManageStudentCoroutine = null
        mHomeworkStatusListCoroutine?.cancel()
        mHomeworkStatusListCoroutine = null
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
            REQUEST_CODE_NOTIFY -> onPageChanged(Common.PAGE_HOMEWORK_LIST)
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_LIST_SET_COMPLETE -> mHomeworkContractView.hideLoading()
            MESSAGE_PAGE_CHANGE ->
            {
                when(msg.obj)
                {
                    Common.PAGE_HOMEWORK_CALENDAR -> requestStudentHomework()   // 숙제관리(달력) 통신 요청
                    Common.PAGE_HOMEWORK_LIST ->
                    {
                        requestHomeworkList()       // 숙제현황(리스트) 통신 요청
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
    override fun onClickBackButton()
    {
        Log.f("")
        if (mPagePosition == Common.PAGE_HOMEWORK_LIST)
        {
            mHomeworkManagePresenterObserver.clearHomeworkList(true) // 숙제현황 리스트 초기화

            mPagePosition = Common.PAGE_HOMEWORK_CALENDAR
            mHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_CALENDAR)
        }
        else if (mPagePosition == Common.PAGE_HOMEWORK_STUDENT_COMMENT ||
                 mPagePosition == Common.PAGE_HOMEWORK_TEACHER_COMMENT)
        {
            CommonUtils.getInstance(mContext).hideKeyboard() // 키보드 닫기 처리
            mPagePosition = Common.PAGE_HOMEWORK_LIST
            mHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_LIST)
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
     *                              숙제 현황 (리스트) 화면
     * ======================================================================================
     */

    /**
     * 날짜 변경 화살표 표시 설정
     */
    private fun setHomeworkDateButton()
    {
        // 숙제현황 이전 버튼 설정
        if (mSelectedHomeworkPosition > 0)
        {
            mHomeworkManagePresenterObserver.setHomeworkPrevButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setHomeworkPrevButton(false)
        }

        // 숙제현황 다음 버튼 설정
        if (mSelectedHomeworkPosition < mHomeworkCalendarBaseResult!!.getHomeworkDataList().size - 1)
        {
            mHomeworkManagePresenterObserver.setHomeworkNextButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setHomeworkNextButton(false)
        }
    }

    /**
     * 숙제현황 리스트 클릭 이벤트
     */
    private fun onClickHomeworkItem(item : HomeworkListItemData)
    {
        val content = ContentsBaseResult()
        content.setID(item.getContentID())
        content.setTitle(CommonUtils.getInstance(mContext).getSubStringTitleName(item.getTitle()))

        when(item.getHomeworkType())
        {
            HomeworkType.ANIMATION -> startPlayerActivity(content)
            HomeworkType.EBOOK -> {
                if (CommonUtils.getInstance(mContext).checkTablet)
                {
                    startEBookActivity()
                }
                else
                {
                    // 모바일이면서 eBook 컨텐츠 학습 시도 시 이용안내 메세지 표시
                    mHomeworkContractView.showErrorMessage(mContext.resources.getString(R.string.message_warning_homework_ebook))
                }
            }
            HomeworkType.QUIZ -> startQuizActivity(item.getContentID())
            HomeworkType.CROSSWORD -> startCrosswordActivity(item.getContentID())
            HomeworkType.STARWORDS -> startStarWordsActivity(item.getContentID())
            HomeworkType.RECORDER -> startRecordActivity(content)
        }
    }

    /**
     * ======================================================================================
     *                              숙제 - 다른 화면으로 이동
     * ======================================================================================
     */

    /**
     * 동화/동요 플레이어로 이동
     */
    private fun startPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.PLAYER)
            .setData(arrayListOf(content))
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .setRequestCode(REQUEST_CODE_NOTIFY)
            .startActivity()
    }

    /**
     * eBook 학습화면으로 이동
     * TODO 김태은 EBOOK 화면 추가 후 연결하기
     */
    private fun startEBookActivity()
    {
        Log.f("")
    }

    /**
     * 퀴즈 학습화면으로 이동
     * TODO 김태은 퀴즈 API 완성된 후 확인화기
     */
    private fun startQuizActivity(contentID : String)
    {
        Log.f("")
        var quizDataObject : QuizDataObject = QuizDataObject(
            contentID,
            mSelectHomeworkData!!.getHomeworkNumber())

        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.QUIZ)
            .setData(quizDataObject)
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
        //        IntentManagementFactory.getInstance()
        //            .readyActivityMode(ActivityMode.WEBVIEW_GAME_CROSSWORD)
        //            .setData(contentID)
        //            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
        //            .startActivity()
    }

    /**
     * 스타워즈 학습화면으로 이동
     */
    private fun startStarWordsActivity(contentID : String)
    {
        Log.f("")
        //        IntentManagementFactory.getInstance()
        //            .readyActivityMode(ActivityMode.WEBVIEW_GAME_STARWORDS)
        //            .setData(contentID)
        //            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
        //            .startActivity()
    }

    /**
     * 녹음기 화면으로 이동
     */
    private fun startRecordActivity(content : ContentsBaseResult)
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.RECORD_PLAYER)
            .setData(content)
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
     * 숙제관리 통신 요청 (학생)
     */
    private fun requestStudentHomework()
    {
        Log.f("")
        mHomeworkContractView.showLoading()
        mHomeworkManageStudentCoroutine = HomeworkManageStudentCoroutine(mContext)
        mHomeworkManageStudentCoroutine!!.setData(mYear, mMonth)
        mHomeworkManageStudentCoroutine!!.asyncListener = mAsyncListener
        mHomeworkManageStudentCoroutine!!.execute()
    }

    /**
     * 숙제현황 통신 요청 (학생)
     */
    private fun requestHomeworkList()
    {
        Log.f("")

        // 숙제 아이템 정보 요청
        mSelectHomeworkData = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]

        mHomeworkStatusListCoroutine = HomeworkStatusListCoroutine(mContext)
        mHomeworkStatusListCoroutine!!.setData(mSelectHomeworkData!!.getHomeworkNumber().toString())
        mHomeworkStatusListCoroutine!!.asyncListener = mAsyncListener
        mHomeworkStatusListCoroutine!!.execute()
    }

    /**
     * 코멘트 등록 요청
     */
    private fun requestCommentRegister()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mSelectedHomeworkPosition]
        mHomeworkContractView.showLoading()
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
        mHomeworkContractView.showLoading()
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
        mHomeworkContractView.showLoading()
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
            mPagePosition = Common.PAGE_HOMEWORK_LIST
            mHomeworkContractView.setCurrentViewPage(mPagePosition)
        })

        // 달력 세팅 완료 (Activity 로딩 다이얼로그 닫기)
        mHomeworkCalendarFragmentObserver.onCompletedCalendarSet.observe(mContext as AppCompatActivity, {
            mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_LIST_SET_COMPLETE, Common.DURATION_NORMAL)
        })
    }

    private fun setupListFragmentListener()
    {
        // 이전 화살표 클릭 이벤트
        mHomeworkListFragmentObserver.onClickBeforeButton.observe(mContext as AppCompatActivity, {
            if (mSelectedHomeworkPosition > 0)
            {
                mSelectedHomeworkPosition -= 1
                requestHomeworkList() // 숙제현황 통신 요청
            }
        })

        // 다음 화살표 클릭 이벤트
        mHomeworkListFragmentObserver.onClickAfterButton.observe(mContext as AppCompatActivity, {
            if (mSelectedHomeworkPosition < mHomeworkCalendarBaseResult!!.getHomeworkDataList().size - 1)
            {
                mSelectedHomeworkPosition += 1
                requestHomeworkList() // 숙제현황 통신 요청
            }
        })

        // 학습자 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickStudentCommentButton.observe(mContext as AppCompatActivity, {
            mPagePosition = Common.PAGE_HOMEWORK_STUDENT_COMMENT
            mHomeworkContractView.setCurrentViewPage(mPagePosition)
            mHomeworkManagePresenterObserver.setCommentData(mHomeworkListBaseResult!!.getStudentComment())
            mHomeworkManagePresenterObserver.setPageType(mPagePosition, mHomeworkListBaseResult!!.isEvaluationComplete())
        })

        // 선생님 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickTeacherCommentButton.observe(mContext as AppCompatActivity, {
            mPagePosition = Common.PAGE_HOMEWORK_TEACHER_COMMENT
            mHomeworkContractView.setCurrentViewPage(mPagePosition)
            mHomeworkManagePresenterObserver.setCommentData(mHomeworkListBaseResult!!.getTeacherComment())
            mHomeworkManagePresenterObserver.setPageType(mPagePosition, mHomeworkListBaseResult!!.isEvaluationComplete())
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
            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                // 통신 성공
                if (code == Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT)
                {
                    // 숙제관리 (달력)
                    mHomeworkCalendarBaseResult = (result as HomeworkManageCalenderBaseObject).getData()
                    mHomeworkManagePresenterObserver.setCalendarData(mHomeworkCalendarBaseResult!!)
                }
                else if (code == Common.COROUTINE_CODE_HOMEWORK_STATUS_LIST)
                {
                    // 숙제현황 (리스트)
                    mHomeworkListBaseResult = (result as HomeworkStatusListBaseObject).getData()
                    mHomeworkManagePresenterObserver.updateHomeworkListData(mHomeworkListBaseResult!!)
                    setHomeworkDateButton()
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER)
                {
                    mHomeworkContractView.hideLoading()
                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mPagePosition, mHomeworkListBaseResult!!.isEvaluationComplete())
                    mHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_register))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE)
                {
                    mHomeworkContractView.hideLoading()
                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mPagePosition, mHomeworkListBaseResult!!.isEvaluationComplete())
                    mHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_update))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                {
                    mHomeworkContractView.hideLoading()
                    mStudentComment = ""
                    mHomeworkManagePresenterObserver.setCommentData(mStudentComment)
                    mHomeworkManagePresenterObserver.setPageType(mPagePosition, mHomeworkListBaseResult!!.isEvaluationComplete())
                    mHomeworkContractView.showErrorMessage(mContext.resources.getString(R.string.message_comment_delete))
                }
            }
            else
            {
                mHomeworkContractView.hideLoading()
                // 통신 실패
                if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    if (code == Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT ||
                        code == Common.COROUTINE_CODE_HOMEWORK_STATUS_LIST)
                    {
                        Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                    else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER ||
                             code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE   ||
                             code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                    {
                        mHomeworkContractView.showErrorMessage(result.getMessage())
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