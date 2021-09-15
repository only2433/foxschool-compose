package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.`object`.data.homework.CalendarBaseData
import com.littlefox.app.foxschool.`object`.data.homework.CalendarData
import com.littlefox.app.foxschool.`object`.result.HomeworkManageCalenderBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarItemData
import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.HomeworkManageStudentCoroutine
import com.littlefox.app.foxschool.enumerate.CalendarImageType
import com.littlefox.app.foxschool.main.contract.HomeworkContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.HomeworkCalendarFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import kotlin.collections.ArrayList

/**
 * 숙제관리
 */
class HomeworkManagePresenter : HomeworkContract.Presenter
{
    companion object
    {
        private const val MESSAGE_LIST_SET_COMPLETE : Int = 100
    }

    private lateinit var mContext : Context
    private lateinit var mHomeworkContractView : HomeworkContract.View
    private var mMainHandler : WeakReferenceHandler? = null

    private val mHomeworkFragmentList : ArrayList<Fragment> = ArrayList<Fragment>()
    private var mHomeworkPagerAdapter : HomeworkPagerAdapter? = null

    private var mHomeworkManageStudentCoroutine : HomeworkManageStudentCoroutine? = null
    private var mHomeworkCalendarBaseResult : HomeworkCalendarBaseResult? = null

    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver
    private lateinit var mHomeworkCalendarFragmentObserver : HomeworkCalendarFragmentObserver

    // 통신에 입력되는 년도, 월
    private var mYear = ""
    private var mMonth = ""

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
        setupCalendarFragmentListener()

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
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_LIST_SET_COMPLETE -> mHomeworkContractView.hideLoading()
        }
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트
     * 뒤로가기 버튼은 정보수정/비밀번호 변경 화면에서만 표시되며, 나의정보 화면으로 이동한다.
     */
    override fun onClickBackButton()
    {
        Log.f("")
    }

    /**
     * 달력 아이템 생성
     */
    private fun getCalendarItemList()
    {
        val baseCalendar = CalendarBaseData(mContext, mHomeworkCalendarBaseResult!!)
        val list = baseCalendar.getDataList()
        setHomeworkInCalendarItem(list)
        mHomeworkManagePresenterObserver.setCalendarData(list)
    }

    /**
     * 숙제 데이터 달력 아이템에 넣기
     */
    private fun setHomeworkInCalendarItem(dateList : ArrayList<CalendarData>)
    {
        // 숙제 데이터 수 만큼 반복
        mHomeworkCalendarBaseResult!!.getHomeworkDataList().forEach {homework ->
            val homeworkTerm = CommonUtils.getInstance(mContext).getTerm(homework.getStartDate(), homework.getEndDate())                        // 숙제 기간 : 숙제 종료일 - 숙제 시작일
            val startPosition = CommonUtils.getInstance(mContext).getTerm(mHomeworkCalendarBaseResult!!.getMonthStartDate(), homework.getStartDate())   // 숙제 시작 포지션 : 숙제 시작일 - 달력 시작일
            val lastPosition = startPosition + homeworkTerm                                                                                     // 숙제 종료 포지션 : 숙제 시작 포지션 + 숙제 기간

            if (startPosition == lastPosition)
            {
                // 숙제 시작 포지션 == 숙제 종료 포지션 : 숙제가 1일 짜리
                // 한 아이템으로 변경할 경우 주소값이 동일해서 이전에 담은 아이템도 영향이 가기 때문에 아이템을 복사해서 넣어줘야 한다.
                val copyItem = HomeworkCalendarItemData(homework)
                copyItem.setImageType(CalendarImageType.ONE) // 색 바 이미지 타입 지정 (하루)
                dateList[startPosition].setHasHomework(true)
                dateList[startPosition].setHomework(copyItem) // 달력 아이템에 숙제 세팅
            }
            else
            {
                // 숙제 기간이 하루 이상일 때
                // 숙제 기간만큼
                for (i in 0..homeworkTerm)
                {
                    // 한 아이템으로 변경할 경우 주소값이 동일해서 이전에 담은 아이템도 영향이 가기 때문에 아이템을 복사해서 넣어줘야 한다.
                    val copyItem = HomeworkCalendarItemData(homework)

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
                        (index == startPosition) -> copyItem.setImageType(CalendarImageType.START)
                        // 숙제 종료일
                        (index == lastPosition) -> copyItem.setImageType(CalendarImageType.END)
                        // 숙제 중간날짜
                        else -> copyItem.setImageType(CalendarImageType.CENTER)
                    }
                    dateList[startPosition + i].setHasHomework(true)
                    dateList[startPosition + i].setHomework(copyItem) // 달력 아이템에 숙제 세팅
                }
            }
        }
    }

    /**
     * 달력 뷰 설정
     */
    private fun setCalendarView()
    {
        setCalendarTitle()
        setCalendarButton()
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
            var month = mHomeworkCalendarBaseResult!!.getCurrentMonth()
            if (month[0] == '0') // 0으로 시작하면
            {
                month = month.drop(1) // 앞에부터 1자리 제거
            }
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
     * =========================================================
     *                      Listeners
     * =========================================================
     */
    private fun setupCalendarFragmentListener()
    {
        // 이전 화살표 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarBefore.observe(mContext as AppCompatActivity, {
            if (mHomeworkCalendarBaseResult != null) // 데이터 있을 때 (방어코드)
            {
                mYear = mHomeworkCalendarBaseResult!!.getPrevYear()
                mMonth = mHomeworkCalendarBaseResult!!.getPrevMonth()
                requestStudentHomework()
            }
        })

        // 다음 화살표 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarAfter.observe(mContext as AppCompatActivity, {
            if (mHomeworkCalendarBaseResult != null) // 데이터 있을 때 (방어코드)
            {
                mYear = mHomeworkCalendarBaseResult!!.getNextYear()
                mMonth = mHomeworkCalendarBaseResult!!.getNextMonth()
                requestStudentHomework()
            }
        })

        // 달력 아이템 클릭 이벤트
        mHomeworkCalendarFragmentObserver.onClickCalendarItem.observe(mContext as AppCompatActivity, { item ->
            // TODO 김태은 달력 클릭이벤트 생성 (숙제관리 상세화면 만들어지면 변경해놓기)
            mHomeworkContractView.showSuccessMessage(item.getHomeworkNumber().toString())
        })

        // 리스트 세팅 완료 (로딩 다이얼로그 닫기)
        mHomeworkCalendarFragmentObserver.onCompletedListSet.observe(mContext as AppCompatActivity, {
            mMainHandler!!.sendEmptyMessageDelayed(MESSAGE_LIST_SET_COMPLETE, Common.DURATION_NORMAL)
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
                    mHomeworkCalendarBaseResult = (result as HomeworkManageCalenderBaseObject).getData()
                    getCalendarItemList()
                    setCalendarView()
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
                    if (code == Common.COROUTINE_CODE_HOMEWORK_MANAGE_STUDENT)
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
    /** ========================================================= */
}