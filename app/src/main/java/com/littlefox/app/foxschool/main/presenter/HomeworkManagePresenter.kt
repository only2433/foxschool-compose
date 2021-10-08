package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Message
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.homework.CalendarBaseData
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData
import com.littlefox.app.foxschool.`object`.result.HomeworkManageCalenderBaseObject
import com.littlefox.app.foxschool.`object`.result.HomeworkStatusListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListItemData
import com.littlefox.app.foxschool.adapter.CalendarItemViewAdapter
import com.littlefox.app.foxschool.adapter.HomeworkItemViewAdapter
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.adapter.listener.CalendarItemListener
import com.littlefox.app.foxschool.adapter.listener.HomeworkItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.Common.Companion.DURATION_NORMAL
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.*
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
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

        private const val DIALOG_COMMENT_DELETE : Int       = 10001
    }

    private lateinit var mContext : Context
    private lateinit var mHomeworkContractView : HomeworkContract.View
    private var mMainHandler : WeakReferenceHandler? = null
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

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
    private var mHomeworkSelected : Int = -1                        // 숙제관리에서 선택한 숙제 아이템 포지션

    private var isRequested : Boolean = false                       // 중복통신 막기 위해 사용하는 플래그그

    /** 숙제관리 (달력) */
    // 통신에 입력되는 년도, 월
    private var mYear : String  = ""
    private var mMonth : String = ""

    private var mCalendarItemViewAdapter : CalendarItemViewAdapter? = null // 숙제관리 리스트 Adapter
    private var mCalendarItemList : ArrayList<CalendarData> = ArrayList<CalendarData>()

    /** 숙제현황 (리스트) **/
    // 숙제 목록 필터링 다이얼로그 데이터
    private var mHomeworkFilterList : Array<String>?    = null
    private var mHomeworkFilterSelected : Int           = 0

    private var mHomeworkItemViewAdapter : HomeworkItemViewAdapter? = null // 숙제현황 리스트 Adapter
    private var mHomeworkItemList : ArrayList<HomeworkListItemData> = ArrayList<HomeworkListItemData>() // 숙제현황 리스트에 표시되는 숙제목록 아이템

    private var mListAnimationEffect : Boolean = true // 숙제현황 리스트 애니메이션 활성 플래그

    /** 학습자한마디 **/
    // 학습자 한마디 (통신 입력용)
    private var mStudentComment : String = ""

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

        mHomeworkFilterList = mContext.resources.getStringArray(R.array.text_list_homework_filter)

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

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

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
                        mHomeworkItemList.clear()   // 리스트 초기화
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
            clearHomeworkList(true) // 숙제현황 리스트 초기화

            mPagePosition = Common.PAGE_HOMEWORK_CALENDAR
            mHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_CALENDAR)
        }
        else if (mPagePosition == Common.PAGE_HOMEWORK_STUDENT_COMMENT ||
                 mPagePosition == Common.PAGE_HOMEWORK_TEACHER_COMMENT)
        {
            CommonUtils.getInstance(mContext).hideKeyboard() // 키보드 닫기 처리
            mListAnimationEffect = false
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
     *                              숙제 관리 (달력) 화면
     * ======================================================================================
     */

    /**
     * 달력 뷰 설정
     */
    private fun setCalendarView()
    {
        setCalendarTitle()
        setCalendarButton()
    }

    /**
     * 달력 아이템 생성
     */
    private fun getCalendarItemList()
    {
        val baseCalendar = CalendarBaseData(mContext, mHomeworkCalendarBaseResult!!)
        mCalendarItemList = baseCalendar.getDataList()
        setHomeworkInCalendarItem(mCalendarItemList)

        if (mCalendarItemViewAdapter == null)
        {
            // 초기생성
            Log.f("mCalendarItemViewAdapter == null")
            mCalendarItemViewAdapter = CalendarItemViewAdapter(mContext)
                .setItemList(mCalendarItemList)
                .setHomeworkList(mHomeworkCalendarBaseResult!!.getHomeworkDataList())
                .setCalendarItemListener(mCalendarItemListener)
            mHomeworkManagePresenterObserver.setCalendarListView(mCalendarItemViewAdapter!!)
        }
        else
        {
            // 데이터 변경
            Log.f("mCalendarItemViewAdapter notifyDataSetChanged")
            mCalendarItemViewAdapter!!.setItemList(mCalendarItemList)
            mCalendarItemViewAdapter!!.setHomeworkList(mHomeworkCalendarBaseResult!!.getHomeworkDataList())
            mHomeworkManagePresenterObserver.setCalendarListView(mCalendarItemViewAdapter!!)
            mCalendarItemViewAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 숙제 데이터 달력 아이템에 넣기
     */
    private fun setHomeworkInCalendarItem(dateList : ArrayList<CalendarData>)
    {
        // 숙제 데이터 수 만큼 반복
        for (pos in mHomeworkCalendarBaseResult!!.getHomeworkDataList().indices)
        {
            val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[pos]
            val homeworkTerm = CommonUtils.getInstance(mContext).getTerm(homework.getStartDate(), homework.getEndDate())                                // 숙제 기간 : 숙제 종료일 - 숙제 시작일
            val startPosition = CommonUtils.getInstance(mContext).getTerm(mHomeworkCalendarBaseResult!!.getMonthStartDate(), homework.getStartDate())   // 숙제 시작 포지션 : 숙제 시작일 - 달력 시작일
            val lastPosition = startPosition + homeworkTerm                                                                                             // 숙제 종료 포지션 : 숙제 시작 포지션 + 숙제 기간

            if (startPosition == lastPosition)
            {
                // 숙제 시작 포지션 == 숙제 종료 포지션 : 숙제가 1일 짜리
                dateList[startPosition].setImageType(CalendarImageType.ONE) // 색 바 이미지 타입 지정 (하루)
                dateList[startPosition].setHomeworkPosition(pos)            // 숙제 아이템 포지션 저장
            }
            else
            {
                // 숙제 기간이 하루 이상일 때
                // 숙제 기간만큼
                for (i in 0..homeworkTerm)
                {
                    val index = startPosition + i // 날짜 인덱스
                    if (index > dateList.size - 1)
                    {
                        // 날짜 인덱스가 달력 데이터 사이즈보다 넘어가면 반복문 정지
                        break
                    }

                    // 색 바 이미지 타입 지정
                    when
                    {
                        // 숙제 시작일
                        (index == startPosition) -> dateList[index].setImageType(CalendarImageType.START)
                        // 숙제 종료일
                        (index == lastPosition) -> dateList[index].setImageType(CalendarImageType.END)
                        // 숙제 중간날짜
                        else -> dateList[index].setImageType(CalendarImageType.CENTER)
                    }
                    dateList[startPosition + i].setHomeworkPosition(pos)    // 숙제 아이템 포지션 저장
                }
            }
        }
    }

    /**
     * 달력 타이틀 설정
     * - 0000년 00월 구조
     * - 월이 0으로 시작하는 경우 0 제거
     * Ex) 2021년 9월, 2021년 12월
     */
    private fun setCalendarTitle()
    {
        if (mHomeworkCalendarBaseResult != null)
        {
            val month = mHomeworkCalendarBaseResult!!.getCurrentMonth().toInt()
            val title = "${mHomeworkCalendarBaseResult!!.getCurrentYear()}년 ${month}월"
            mHomeworkManagePresenterObserver.setCalendarMonthTitle(title)
        }
    }

    /**
     * 달력 이전/다음 화살표 버튼 설정
     */
    private fun setCalendarButton()
    {
        if (mHomeworkCalendarBaseResult!!.isPossiblePrevMonth())
        {
            mHomeworkManagePresenterObserver.setCalendarPrevButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setCalendarPrevButton(false)
        }

        if (mHomeworkCalendarBaseResult!!.isPossibleNextMonth())
        {
            mHomeworkManagePresenterObserver.setCalendarNextButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setCalendarNextButton(false)
        }
    }

    /**
     * ======================================================================================
     *                              숙제 현황 (리스트) 화면
     * ======================================================================================
     */

    /**
     * 숙제현황 리스트 화면 세팅
     */
    private fun setHomeworkListView()
    {
        setHomeworkDateText()
        setHomeworkResultLayout()
        setHomeworkStudentLayout(mHomeworkListBaseResult!!.getStudentComment())
        setHomeworkTeacherLayout(mHomeworkListBaseResult!!.getTeacherComment())
        setHomeworkDateButton()
    }

    /**
     * 숙제기간 텍스트 설정
     */
    private fun setHomeworkDateText()
    {
        var homeworkDate = ""
        val startDate = CommonUtils.getInstance(mContext).getHomeworkDateText(mHomeworkListBaseResult!!.getStartDate())
        val endDate = CommonUtils.getInstance(mContext).getHomeworkDateText(mHomeworkListBaseResult!!.getEndDate())
        if (startDate == endDate)
        {
            homeworkDate = startDate
        }
        else
        {
            homeworkDate = "$startDate - $endDate"
        }
        mHomeworkManagePresenterObserver.setHomeworkDateText(homeworkDate)
    }

    /**
     * 숙제 평가 레이아웃 설정
     */
    private fun setHomeworkResultLayout()
    {
        mHomeworkManagePresenterObserver.setResultCommentLayout(mHomeworkListBaseResult!!)
    }

    /**
     * 학습자 한마디 레이아웃 설정
     */
    private fun setHomeworkStudentLayout(comment : String)
    {
        if (comment == "")
        {
            mHomeworkManagePresenterObserver.setStudentCommentLayout(false)
        }
        else
        {
            mHomeworkManagePresenterObserver.setStudentCommentLayout(true)
        }
    }

    /**
     * 선생님 한마디 레이아웃 설정
     */
    private fun setHomeworkTeacherLayout(comment : String)
    {
        if (comment == "")
        {
            mHomeworkManagePresenterObserver.setTeacherCommentLayout(false)
        }
        else
        {
            mHomeworkManagePresenterObserver.setTeacherCommentLayout(true)
        }
    }

    /**
     * 날짜 변경 화살표 표시 설정
     */
    private fun setHomeworkDateButton()
    {
        // 숙제현황 이전 버튼 설정
        if (mHomeworkSelected > 0)
        {
            mHomeworkManagePresenterObserver.setHomeworkPrevButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setHomeworkPrevButton(false)
        }

        // 숙제현황 다음 버튼 설정
        if (mHomeworkSelected < mHomeworkCalendarBaseResult!!.getHomeworkDataList().size - 1)
        {
            mHomeworkManagePresenterObserver.setHomeworkNextButton(true)
        }
        else
        {
            mHomeworkManagePresenterObserver.setHomeworkNextButton(false)
        }
    }

    /**
     * 숙제현황 리스트 필터링
     */
    private fun setHomeworkListData()
    {
        mHomeworkItemList.clear()

        if (mHomeworkListBaseResult != null)
        {
            if (mHomeworkFilterSelected == 0)
            {
                // 전체
                mHomeworkItemList.addAll(mHomeworkListBaseResult!!.getHomeworkItemList())
            }
            else if (mHomeworkFilterSelected == 1)
            {
                // 완료한 숙제
                for (i in mHomeworkListBaseResult!!.getHomeworkItemList().indices)
                {
                    if (mHomeworkListBaseResult!!.getHomeworkItemList()[i].isComplete == true)
                    {
                        mHomeworkItemList.add(mHomeworkListBaseResult!!.getHomeworkItemList()[i])
                    }
                }
            }
            else
            {
                // 남은 숙제
                for (i in mHomeworkListBaseResult!!.getHomeworkItemList().indices)
                {
                    if (mHomeworkListBaseResult!!.getHomeworkItemList()[i].isComplete == false)
                    {
                        mHomeworkItemList.add(mHomeworkListBaseResult!!.getHomeworkItemList()[i])
                    }
                }
            }
        }

        setHomeworkListItem()
        mHomeworkManagePresenterObserver.setHomeworkFilterText(mHomeworkFilterList!![mHomeworkFilterSelected])
    }

    /**
     * 숙제현황 리스트 뷰 세팅
     */
    private fun setHomeworkListItem()
    {
        if (mHomeworkItemViewAdapter == null)
        {
            // 초기 생성
            Log.f("mHomeworkItemViewAdapter == null")
            mHomeworkItemViewAdapter = HomeworkItemViewAdapter(mContext)
                .setItemList(mHomeworkItemList)
                .setHomeworkItemListener(mHomeworkItemListener)
            mHomeworkManagePresenterObserver.setHomeworkListView(mHomeworkItemViewAdapter!!, mListAnimationEffect)
        }
        else
        {
            // 데이터 변경
            Log.f("mHomeworkItemViewAdapter notifyDataSetChanged")
            mHomeworkItemViewAdapter!!.setItemList(mHomeworkItemList)
            mHomeworkManagePresenterObserver.setHomeworkListView(mHomeworkItemViewAdapter!!, mListAnimationEffect)
            mHomeworkItemViewAdapter!!.notifyDataSetChanged()
        }

        if (mListAnimationEffect == false) mListAnimationEffect = true
    }

    /**
     * 숙제현황 리스트 클릭 이벤트
     */
    private fun onClickHomeworkItem(item : HomeworkListItemData)
    {
        if (item.isComplete == false) // 숙제 미완료
        {
            val content = ContentsBaseResult()
            content.setID(item.getContentID())
            content.setTitle(CommonUtils.getInstance(mContext).getSubStringTitleName(item.getTitle()))

            when(item.getHomeworkType())
            {
                HomeworkType.ANIMATION -> startPlayerActivity(content)
                HomeworkType. EBOOK -> {
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
    }

    /**
     * 숙제현황 리스트, 필터링 기능 초기화
     */
    private fun clearHomeworkList(allClear : Boolean)
    {
        mHomeworkManagePresenterObserver.clearHomeworkList(allClear)
        mHomeworkItemList.clear()
        mHomeworkFilterSelected = 0
        mHomeworkManagePresenterObserver.setHomeworkFilterText(mHomeworkFilterList!![mHomeworkFilterSelected])
        setHomeworkListItem()
    }

    /**
     * ======================================================================================
     *                              숙제 - 다른 화면으로 이동
     * ======================================================================================
     */

    /**
     * 동화/동요 플레이어로 이동
     * TODO 김태은 플레이어/플레이기록 API 완성된 후 확인하기
     */
    private fun startPlayerActivity(content : ContentsBaseResult)
    {
        Log.f("")
//        IntentManagementFactory.getInstance()
//            .readyActivityMode(ActivityMode.PLAYER)
//            .setData(arrayListOf(content))
//            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
//            .startActivity()
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
        //        IntentManagementFactory.getInstance()
        //            .readyActivityMode(ActivityMode.QUIZ)
        //            .setData(contentID)
        //            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
        //            .startActivity()
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
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mHomeworkSelected]

        if (mListAnimationEffect) mHomeworkManagePresenterObserver.setHomeworkLoadingProgressBar(true)
        mHomeworkStatusListCoroutine = HomeworkStatusListCoroutine(mContext)
        mHomeworkStatusListCoroutine!!.setData(homework.getHomeworkNumber().toString())
        mHomeworkStatusListCoroutine!!.asyncListener = mAsyncListener
        mHomeworkStatusListCoroutine!!.execute()
    }

    /**
     * 코멘트 등록 요청
     */
    private fun requestCommentRegister()
    {
        Log.f("")
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mHomeworkSelected]
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
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mHomeworkSelected]
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
        val homework = mHomeworkCalendarBaseResult!!.getHomeworkDataList()[mHomeworkSelected]
        mHomeworkContractView.showLoading()
        mStudentCommentDeleteCoroutine = StudentCommentDeleteCoroutine(mContext)
        mStudentCommentDeleteCoroutine!!.setData(homework.getHomeworkNumber())
        mStudentCommentDeleteCoroutine!!.asyncListener = mAsyncListener
        mStudentCommentDeleteCoroutine!!.execute()
    }

    /**
     * ======================================================================================
     *                                  다이얼로그
     * ======================================================================================
     */

    /**
     * 숙제목록 정보 다이얼로그
     */
    private fun showHomeworkInfoDialog()
    {
        var message = mContext.getString(R.string.message_warning_homework_info)
        if (CommonUtils.getInstance(mContext).checkTablet == false)
        {
            // 모바일의 경우 eBook 이용안내 메세지도 추가
            message += "\n- ${mContext.getString(R.string.message_warning_homework_ebook)}"
        }
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog.setMessage(message)
        mTemplateAlertDialog.setButtonType(DialogButtonType.BUTTON_1)
        mTemplateAlertDialog.setDialogListener(mDialogListener)
        mTemplateAlertDialog.setGravity(Gravity.LEFT)
        mTemplateAlertDialog.show()
    }

    /**
     * 숙제목록 필터링 선택 다이얼로그
     */
    private fun showHomeworkFilterDialog()
    {
        Log.f("")
        val builder = AlertDialog.Builder(mContext)
        builder.setSingleChoiceItems(mHomeworkFilterList, mHomeworkFilterSelected, DialogInterface.OnClickListener{dialog, index ->
            dialog.dismiss()
            mHomeworkFilterSelected = index
            setHomeworkListData()
        })

        val dialog : AlertDialog = builder.show()
        dialog.show()
    }

    /**
     * 코멘트 삭제 확인 다이얼로그
     */
    private fun showCommentDeleteDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog.setMessage(mContext.resources.getString(R.string.message_comment_delete_check))
        mTemplateAlertDialog.setDialogEventType(DIALOG_COMMENT_DELETE)
        mTemplateAlertDialog.setButtonType(DialogButtonType.BUTTON_2)
        mTemplateAlertDialog.setDialogListener(mDialogListener)
        mTemplateAlertDialog.show()
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
            if (mHomeworkCalendarBaseResult != null && isRequested == false) // 데이터 있을 때 (방어코드)
            {
                isRequested = true // 중복통신 방지 플래그 활성화
                mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
                mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
                requestStudentHomework()
            }
        })

        // 다음 화살표 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarAfter.observe(mContext as AppCompatActivity, {
            if (mHomeworkCalendarBaseResult != null && isRequested == false) // 데이터 있을 때 (방어코드)
            {
                isRequested = true // 중복통신 방지 플래그 활성화
                mYear = mHomeworkCalendarBaseResult!!.getNextYear()
                mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
                requestStudentHomework()
            }
        })

        // 달력 아이템 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarItem.observe(mContext as AppCompatActivity, { homeworkPosition ->
            mHomeworkSelected = homeworkPosition // 선택한 포지션 저장

            // 숙제 현황 페이지로 이동
            mPagePosition = Common.PAGE_HOMEWORK_LIST
            mHomeworkContractView.setCurrentViewPage(mPagePosition)

            mHomeworkManagePresenterObserver.setScrollTop() // 달력 상단으로 이동
        })

        // 리스트 세팅 완료 (로딩 다이얼로그 닫기)
        mHomeworkCalendarFragmentObserver.onCompletedListSet.observe(mContext as AppCompatActivity, {
            mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_LIST_SET_COMPLETE, Common.DURATION_NORMAL)
        })
    }

    private fun setupListFragmentListener()
    {
        // 이전 화살표 클릭 이벤트
        mHomeworkListFragmentObserver.onClickBeforeButton.observe(mContext as AppCompatActivity, {
            if (mHomeworkSelected > 0 && (isRequested == false))
            {
                isRequested = true // 중복통신 방지 플래그 활성화
                mHomeworkSelected -= 1

                clearHomeworkList(false)
                requestHomeworkList() // 숙제현황 통신 요청
            }
        })

        // 다음 화살표 클릭 이벤트
        mHomeworkListFragmentObserver.onClickAfterButton.observe(mContext as AppCompatActivity, {
            if (mHomeworkSelected < mHomeworkCalendarBaseResult!!.getHomeworkDataList().size - 1 && (isRequested == false))
            {
                isRequested = true // 중복통신 방지 플래그 활성화
                mHomeworkSelected += 1

                clearHomeworkList(false)
                requestHomeworkList() // 숙제현황 통신 요청
            }
        })

        // 학습자 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickStudentCommentButton.observe(mContext as AppCompatActivity, {
            // 학습자 한마디 화면으로 이동
            // 학습자 한마디 없을 때 -> 작성
            // 학습자 한마디 있을 때 -> 보기
            mHomeworkManagePresenterObserver.setPageType(Common.PAGE_HOMEWORK_STUDENT_COMMENT)
            mHomeworkManagePresenterObserver.setStudentCommentData(mHomeworkListBaseResult!!.getStudentComment())
            mPagePosition = Common.PAGE_HOMEWORK_STUDENT_COMMENT
            mHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_STUDENT_COMMENT)
        })

        // 선생님 한마디 클릭 이벤트
        mHomeworkListFragmentObserver.onClickTeacherCommentButton.observe(mContext as AppCompatActivity, {
            mHomeworkManagePresenterObserver.setPageType(Common.PAGE_HOMEWORK_TEACHER_COMMENT)
            mHomeworkManagePresenterObserver.setTeacherCommentData(mHomeworkListBaseResult!!.getTeacherComment())
            mPagePosition = Common.PAGE_HOMEWORK_TEACHER_COMMENT
            mHomeworkContractView.setCurrentViewPage(Common.PAGE_HOMEWORK_TEACHER_COMMENT)
        })

        // 숙제목록 정보 버튼(i모양 아이콘) 클릭 이벤트
        mHomeworkListFragmentObserver.onClickHomeworkInfoButton.observe(mContext as AppCompatActivity, {
            showHomeworkInfoDialog()
        })

        // 숙제목록 필터링 선택
        mHomeworkListFragmentObserver.onClickListFilterButton.observe(mContext as AppCompatActivity, {
            showHomeworkFilterDialog()
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

        // 학습자 한마디 삭제 버튼 클릭 이벤트 (다이얼로그 메세지로 확인 -> 통신요청)
        mHomeworkCommentFragmentObserver.onClickDeleteButton.observe(mContext as AppCompatActivity, { bool ->
            showCommentDeleteDialog()
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

            isRequested = false // 중복통신 방지 플래그 해제
            Log.f("code : $code, status : ${result.getStatus()}")
            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                // 통신 성공
                if (code == Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT)
                {
                    mHomeworkCalendarBaseResult = (result as HomeworkManageCalenderBaseObject).getData()
                    getCalendarItemList()
                    setCalendarView()
                }
                else if (code == Common.COROUTINE_CODE_HOMEWORK_STATUS_LIST)
                {
                    mHomeworkManagePresenterObserver.setHomeworkLoadingProgressBar(false)
                    mHomeworkListBaseResult = (result as HomeworkStatusListBaseObject).getData()
                    setHomeworkListView()
                    setHomeworkListData()
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER)
                {
                    mHomeworkContractView.hideLoading()
                    mHomeworkManagePresenterObserver.setStudentCommentData(mStudentComment)
                    mHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_register))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE)
                {
                    mHomeworkContractView.hideLoading()
                    mHomeworkManagePresenterObserver.setStudentCommentData(mStudentComment)
                    mHomeworkContractView.showSuccessMessage(mContext.resources.getString(R.string.message_comment_update))
                }
                else if (code == Common.COROUTINE_CODE_STUDENT_COMMENT_DELETE)
                {
                    mHomeworkContractView.hideLoading()
                    mStudentComment = ""
                    mHomeworkManagePresenterObserver.setStudentCommentData(mStudentComment)
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
                    if (code == Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT   ||
                        code == Common.COROUTINE_CODE_HOMEWORK_STATUS_LIST      ||
                        code == Common.COROUTINE_CODE_STUDENT_COMMENT_REGISTER  ||
                        code == Common.COROUTINE_CODE_STUDENT_COMMENT_UPDATE    ||
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

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            if (eventType == DIALOG_COMMENT_DELETE)
            {
                when(buttonType)
                {
                    DialogButtonType.BUTTON_1 ->
                    {
                        // 코멘트 삭제 취소
                    }
                    DialogButtonType.BUTTON_2 ->
                    {
                        // 코멘트 삭제 통신 요청
                        requestCommentDelete()
                    }
                }
            }

        }
    }

    /**
     * 숙제관리 리스트 클릭 이벤트 Listener
     */
    private val mCalendarItemListener : CalendarItemListener = object : CalendarItemListener
    {
        override fun onClickItem(position : Int)
        {
            mCalendarItemList.let { list ->
                if (list[position].hasHomework())
                {
                    mHomeworkCalendarFragmentObserver.onClickCalendarItem(list[position].getHomeworkPosition())
                }
            }
        }
    }

    /**
     * 숙제현황 리스트 클릭 이벤트 Listener
     */
    private val mHomeworkItemListener : HomeworkItemListener = object : HomeworkItemListener
    {
        override fun onClickItem(position : Int)
        {
            onClickHomeworkItem(mHomeworkItemList[position])
        }
    }
}