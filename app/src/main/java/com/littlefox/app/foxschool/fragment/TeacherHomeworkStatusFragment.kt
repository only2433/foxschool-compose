package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkStatusBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.status.HomeworkStatusItemData
import com.littlefox.app.foxschool.adapter.HomeworkStatusItemListAdapter
import com.littlefox.app.foxschool.adapter.listener.HomeworkStatusItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.app.foxschool.viewmodel.TeacherHomeworkStatusFragmentObserver
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 선생님용 학생리스트
 * @author 김태은
 */
class TeacherHomeworkStatusFragment : Fragment()
{
    @BindView(R.id._homeworkClassLayout)
    lateinit var _HomeworkClassLayout : ScalableLayout

    @BindView(R.id._textClassName)
    lateinit var _TextClassName : SeparateTextView

    @BindView(R.id._manageToolLayout)
    lateinit var _ManageToolLayout : ScalableLayout

    @BindView(R.id._allCheckIcon)
    lateinit var _AllCheckIcon : ImageView

    @BindView(R.id._allText)
    lateinit var _AllText : TextView

    @BindView(R.id._allHomeworkCheckingText)
    lateinit var _AllHomeworCheckingText : TextView

    @BindView(R.id._homeworkContentText)
    lateinit var _HomeworkContentText : TextView

    @BindView(R.id._statusListView)
    lateinit var _StatusListView : RecyclerView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private lateinit var mTeacherHomeworkStatusFragmentObserver : TeacherHomeworkStatusFragmentObserver
    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver

    private var mHomeworkStatusBaseResult : HomeworkStatusBaseResult? = null            // 통신 응답받은 데이터
    private var mHomeworkStatusItemListAdapter : HomeworkStatusItemListAdapter? = null  // 학생 리스트 Adapter
    private var mHomeworkStatusList : ArrayList<HomeworkStatusItemData> = ArrayList()   // 리스트 아이템

    private var mLastClickTime : Long = 0L              // 중복클릭 방지용

    private var mClassName : String = ""                // 학급명
    private var mHomeworkDate : String = ""             // 숙제기간

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        var view : View? = null
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            view = inflater.inflate(R.layout.fragment_homework_status_tablet, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_homework_status, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.f("")
        initView()
        initFont()
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setupObserverViewModel()
    }

    override fun onStart()
    {
        super.onStart()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onStop()
    {
        super.onStop()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        mUnbinder.unbind()
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initView() { }

    private fun initFont()
    {
        _AllText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _AllHomeworCheckingText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkContentText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mTeacherHomeworkStatusFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(TeacherHomeworkStatusFragmentObserver::class.java)
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)

        mHomeworkManagePresenterObserver.setClassName.observe(viewLifecycleOwner, { className ->
            mClassName = className
        })

        mHomeworkManagePresenterObserver.setStatusListData.observe(viewLifecycleOwner, { item ->
            mHomeworkStatusBaseResult = item
            mHomeworkStatusList.clear()
            mHomeworkStatusList.addAll(mHomeworkStatusBaseResult!!.getStudentStatusItemList()!!)
            updateStatusListData()
        })

        // 화면 초기화
        mHomeworkManagePresenterObserver.clearStatusList.observe(viewLifecycleOwner, {
            clearScreenData()
        })
    }

    private fun setClassNameText()
    {
        val textSize = if (CommonUtils.getInstance(mContext).checkTablet) 32 else 40
        _TextClassName.setSeparateText(mClassName, " $mHomeworkDate")
            .setSeparateColor(resources.getColor(R.color.color_000000), resources.getColor(R.color.color_000000))
            .setSeparateTextSize(CommonUtils.getInstance(mContext).getPixel(textSize), CommonUtils.getInstance(mContext).getPixel(textSize))
            .setSeparateTextStyle((Font.getInstance(mContext).getTypefaceBold()), (Font.getInstance(mContext).getTypefaceRegular()))
            .showView()
    }

    private fun updateStatusListData()
    {
        Log.f("Teacher Homework Status List update")
        setAllCheckDrawable(false)
        setStudentListView()
        setHomeworkDateText()
        setClassNameText()
    }

    private fun setStudentListView()
    {
        if (mHomeworkStatusItemListAdapter == null)
        {
            // 초기 생성
            Log.f("mHomeworkStatusItemListAdapter create")
            mHomeworkStatusItemListAdapter = HomeworkStatusItemListAdapter(mContext)
                .setItemList(mHomeworkStatusList)
                .setHomeworkItemListener(mHomeworkStatusItemListener)
        }
        else
        {
            // 데이터 변경
            Log.f("mHomeworkStatusItemListAdapter notifyDataSetChanged")
            mHomeworkStatusItemListAdapter!!.setItemList(mHomeworkStatusList)
            mHomeworkStatusItemListAdapter!!.notifyDataSetChanged()
        }

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        _StatusListView.layoutManager = linearLayoutManager
        _StatusListView.adapter = mHomeworkStatusItemListAdapter
    }

    private fun setHomeworkDateText()
    {
        var homeworkDate = ""
        if (mHomeworkStatusBaseResult!!.getStartDate() == mHomeworkStatusBaseResult!!.getEndDate())
        {
            homeworkDate = mHomeworkStatusBaseResult!!.getStartDate()
        }
        else
        {
            homeworkDate = "${mHomeworkStatusBaseResult!!.getStartDate()} ~ ${mHomeworkStatusBaseResult!!.getEndDate()}"
        }

        mHomeworkDate = homeworkDate
    }

    private fun clearScreenData()
    {
        // 화면을 완전히 떠나는 경우 데이터 초기화
        _TextClassName.text = ""

        setAllCheckDrawable(false)
        mHomeworkStatusList.clear()
        setStudentListView()
    }

    /**
     * 리스트 전체 체크 (Check/UnCheck)
     */
    private fun setListAllCheck(isAllCheck : Boolean)
    {
        mHomeworkStatusList.forEach { item ->
            item.setSelected(isAllCheck)
        }
        mHomeworkStatusItemListAdapter!!.notifyDataSetChanged()
    }

    /**
     * 선택한 ID리스트 Presenter로 전달
     */
    private fun sendIDList()
    {
        val data : ArrayList<String> = ArrayList<String>()
        mHomeworkStatusList.forEach {
            if (it.isSelected())
            {
                data.add(it.getUserID())
            }
        }
        mTeacherHomeworkStatusFragmentObserver.onClickHomeworkBundleChecking(data)
    }

    private fun setAllCheckDrawable(isAllCheck : Boolean)
    {
        if (isAllCheck)
        {
            _AllCheckIcon.setImageResource(R.drawable.radio_on)
            _AllCheckIcon.tag = "ON"
        }
        else
        {
            _AllCheckIcon.setImageResource(R.drawable.radio_off)
            _AllCheckIcon.tag = "OFF"
        }
    }

    @Optional
    @OnClick(R.id._allCheckIcon, R.id._homeworkContentText, R.id._allHomeworkCheckingText)
    fun onClickView(view : View)
    {
        //중복이벤트 방지
        if(SystemClock.elapsedRealtime() - mLastClickTime < Common.SECOND)
        {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when(view.id)
        {
            R.id._allCheckIcon ->
            {
                // [전체선택]
                if(_AllCheckIcon.tag == "ON")
                {
                    setAllCheckDrawable(false)
                    setListAllCheck(false)
                }
                else
                {
                    setAllCheckDrawable(true)
                    setListAllCheck(true)
                }
            }
            R.id._allHomeworkCheckingText ->
            {
                // [일괄 숙제 검사]
                sendIDList()
            }
            R.id._homeworkContentText ->
            {
                // [숙제 내용]
                mTeacherHomeworkStatusFragmentObserver.onClickHomeworkContents()
            }
        }
    }

    private val mHomeworkStatusItemListener : HomeworkStatusItemListener = object : HomeworkStatusItemListener
    {
        override fun onClickCheck(count : Int)
        {
            // 리스트에서 체크된 숫자가 리스트의 갯수와 동일한 경우 전체선택 플래그 활성화
            if (count == mHomeworkStatusList.size)
            {
                setAllCheckDrawable(true)
            }
            else
            {
                setAllCheckDrawable(false)
            }

        }

        override fun onClickShowDetail(index : Int)
        {
            // [숙제 현황 상세 보기] 클릭 이벤트
            mTeacherHomeworkStatusFragmentObserver.onClickShowDetailButton(index)
        }

        override fun onClickHomeworkChecking(index : Int)
        {
            // [숙제 검사] [검사 수정] 클릭 이벤트
            mTeacherHomeworkStatusFragmentObserver.onClickHomeworkChecking(index)
        }
    }
}