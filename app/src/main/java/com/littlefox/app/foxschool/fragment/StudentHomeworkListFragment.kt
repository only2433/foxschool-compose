package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData
import com.littlefox.app.foxschool.adapter.HomeworkItemViewAdapter
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.viewmodel.HomeworkListFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 리스트 화면 (학생용)
 * @author 김태은
 */
class StudentHomeworkListFragment : Fragment()
{
    /** 서브타이틀 (이름 숙제기간) */
    @BindView(R.id._homeworkSubTitleBackground)
    lateinit var _HomeworkSubTitleBackground : View

    @BindView(R.id._homeworkSubTitle)
    lateinit var _HomeworkSubTitle : TextView

    @BindView(R.id._evaluationCompleteLayout)
    lateinit var _EvaluationCompleteLayout : ScalableLayout

    @BindView(R.id._homeworkResultImage)
    lateinit var _HomeworkResultImage : ImageView

    @BindView(R.id._homeworkResultText)
    lateinit var _HomeworkResultText : SeparateTextView

    /** 코멘트 1개 */
    @BindView(R.id._oneCommentLayout)
    lateinit var _OneCommentLayout : ScalableLayout

    @BindView(R.id._homeworkOneCommentBg)
    lateinit var _HomeworkOneCommentBg : ImageView

    @BindView(R.id._homeworkOneCommentIcon)
    lateinit var _HomeworkOneCommentIcon : ImageView

    @BindView(R.id._homeworkOneCommentTitle)
    lateinit var _HomeworkOneCommentTitle : TextView

    @BindView(R.id._homeworkOneCommentButton)
    lateinit var _HomeworkOneCommentButton : TextView

    /** 코멘트 2개 */
    @BindView(R.id._twoCommentLayout)
    lateinit var _TwoCommentLayout : ScalableLayout

    @BindView(R.id._homeworkStudentCommentBg)
    lateinit var _HomeworkStudentCommentBg : ImageView

    @BindView(R.id._homeworkStudentComment)
    lateinit var _HomeworkStudentComment : TextView

    @BindView(R.id._homeworkStudentCommentButton)
    lateinit var _HomeworkStudentCommentButton : TextView

    @BindView(R.id._homeworkTeacherComment)
    lateinit var _HomeworkTeacherComment : TextView

    @BindView(R.id._homeworkTeacherCommentButton)
    lateinit var _HomeworkTeacherCommentButton : TextView

    @BindView(R.id._homeworkListLayout)
    lateinit var _HomeworkListLayout : ScalableLayout

    @BindView(R.id._homeworkListText)
    lateinit var _HomeworkListText : TextView

    @BindView(R.id._homeworkInfoButton)
    lateinit var _HomeworkInfoButton : ImageView

    @BindView(R.id._homeworkFilterButton)
    lateinit var _HomeworkFilterButton : ImageView

    @BindView(R.id._homeworkFilterDownImage)
    lateinit var _HomeworkFilterDownImage : ImageView

    @BindView(R.id._homeworkFilterText)
    lateinit var _HomeworkFilterText : TextView

    @BindView(R.id._homeworkListView)
    lateinit var _HomeworkListView : RecyclerView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : RelativeLayout

    companion object
    {
        const val COMMENT_ONLY_STUDENT : Int = 100  // 코멘트 1개일 때 (학습자)
        const val COMMENT_ONLY_TEACHER : Int = 101  // 코멘트 1개일 때 (선생님)
    }

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private lateinit var mHomeworkListFragmentObserver : HomeworkListFragmentObserver
    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver

    private var mHomeworkDetailBaseResult : HomeworkDetailBaseResult? = null // 통신 응답받은 데이터
    private var mHomeworkItemViewAdapter : HomeworkItemViewAdapter? = null // 숙제현황 리스트 Adapter
    private var mHomeworkItemDetail : ArrayList<HomeworkDetailItemData> = ArrayList<HomeworkDetailItemData>() // 숙제현황 리스트에 표시되는 숙제목록 아이템

    // 숙제 목록 필터링 다이얼로그 데이터
    private var mHomeworkFilterList : Array<String>?    = null
    private var mHomeworkFilterIndex : Int              = 0

    private var mLastClickTime : Long = 0L              // 중복클릭 방지용

    private var mOneCommentType : Int = -1              // 버튼1개 코멘트 영역 타입
    private var isListAnimationEffect : Boolean = true  // 숙제현황 리스트 애니메이션 활성 플래그 || 디폴트 : 애니메이션 활성화

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
            view = inflater.inflate(R.layout.fragment_homework_list_tablet, container, false)
        } else
        {
            view = inflater.inflate(R.layout.fragment_homework_list, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.f("")
        setContentListLoadingVisible(true)
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
    private fun initView()
    {
        mHomeworkFilterList = mContext.resources.getStringArray(R.array.text_list_homework_filter)
        _HomeworkSubTitleBackground.setBackgroundColor(mContext.resources.getColor(R.color.color_23cc8a_alpha_50))
        _HomeworkSubTitle.visibility = View.VISIBLE
    }

    private fun initFont()
    {
        _HomeworkSubTitle.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkResultText.setTypeface(Font.getInstance(mContext).getTypefaceBold())
        _HomeworkOneCommentTitle.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkOneCommentButton.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkStudentComment.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkStudentCommentButton.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkTeacherComment.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkTeacherCommentButton.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkListText.setTypeface(Font.getInstance(mContext).getTypefaceMedium())
        _HomeworkFilterText.setTypeface(Font.getInstance(mContext).getTypefaceRegular())
    }

    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mHomeworkListFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkListFragmentObserver::class.java)
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)

        // 숙제현황 데이터
        mHomeworkManagePresenterObserver.updateHomeworkListData.observe(viewLifecycleOwner, { item ->
            mHomeworkDetailBaseResult = item
            updateHomeworkListData()
        })

        // 숙제 현황 화면 초기화
        mHomeworkManagePresenterObserver.clearHomeworkList.observe(viewLifecycleOwner, { allClear ->
            clearScreenData(allClear)
        })
    }

    /**
     * 숙제현황 데이터 받아서 처리
     */
    private fun updateHomeworkListData()
    {
        if (isListAnimationEffect == true)
        {
            // 애니메이션 효과 켜져있을때만 컨텐츠 로딩 다이얼로그 표시
            setContentListLoadingVisible(true)
        }

        setHomeworkListData()       // 숙제 리스트
        setHomeworkSubTitleText()   // 숙제 기간 텍스트 (서브타이틀)
        setCommentLayout()          // 코멘트 영역

        setContentListLoadingVisible(false) // 컨텐츠 로딩 다이얼로그 숨김
    }

    /**
     * 숙제현황 리스트 필터링
     */
    private fun setHomeworkListData()
    {
        _HomeworkFilterText.text = mHomeworkFilterList!![mHomeworkFilterIndex] // 리스트 필터링 텍스트 설정

        // 리스트 아이템 생성
        mHomeworkItemDetail.clear()
        if (mHomeworkDetailBaseResult != null)
        {
            if (mHomeworkFilterIndex == 0)
            {
                // 전체
                mHomeworkItemDetail.addAll(mHomeworkDetailBaseResult!!.getHomeworkItemList())
            }
            else if (mHomeworkFilterIndex == 1)
            {
                // 완료한 숙제
                for (i in mHomeworkDetailBaseResult!!.getHomeworkItemList().indices)
                {
                    if (mHomeworkDetailBaseResult!!.getHomeworkItemList()[i].isComplete == true)
                    {
                        mHomeworkItemDetail.add(mHomeworkDetailBaseResult!!.getHomeworkItemList()[i])
                    }
                }
            }
            else
            {
                // 남은 숙제
                for (i in mHomeworkDetailBaseResult!!.getHomeworkItemList().indices)
                {
                    if (mHomeworkDetailBaseResult!!.getHomeworkItemList()[i].isComplete == false)
                    {
                        mHomeworkItemDetail.add(mHomeworkDetailBaseResult!!.getHomeworkItemList()[i])
                    }
                }
            }
        }

        setHomeworkListView()
    }

    /**
     * 숙제현황 리스트 뷰 세팅
     */
    private fun setHomeworkListView()
    {
        if (mHomeworkItemViewAdapter == null)
        {
            // 초기 생성
            Log.f("mHomeworkItemViewAdapter create")
            mHomeworkItemViewAdapter = HomeworkItemViewAdapter(mContext, isTeacher = false)
                .setItemList(mHomeworkItemDetail)
                .setHomeworkItemListener(mHomeworkItemListener)
        }
        else
        {
            // 데이터 변경
            Log.f("mHomeworkItemViewAdapter notifyDataSetChanged")
            mHomeworkItemViewAdapter!!.setItemList(mHomeworkItemDetail)
            mHomeworkItemViewAdapter!!.notifyDataSetChanged()
        }

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        _HomeworkListView.layoutManager = linearLayoutManager

        Log.f("[ListView Animation] || $isListAnimationEffect")
        if (isListAnimationEffect)
        {
            val animationController = AnimationUtils.loadLayoutAnimation(mContext, R.anim.listview_layoutanimation)
            _HomeworkListView.layoutAnimation = animationController
        }
        _HomeworkListView.adapter = mHomeworkItemViewAdapter
    }

    /**
     * 숙제기간 텍스트 설정
     */
    private fun setHomeworkSubTitleText()
    {
        var homeworkDate = ""
        val startDate = CommonUtils.getInstance(mContext).getHomeworkDateText(mHomeworkDetailBaseResult!!.getStartDate())
        val endDate = CommonUtils.getInstance(mContext).getHomeworkDateText(mHomeworkDetailBaseResult!!.getEndDate())
        if (startDate == endDate)
        {
            homeworkDate = startDate
        }
        else
        {
            homeworkDate = "$startDate ~ $endDate"
        }
        _HomeworkSubTitle.text = homeworkDate
    }

    /**
     * 코멘트 영역 설정
     */
    private fun setCommentLayout()
    {
        val homework = mHomeworkDetailBaseResult!!

        // [최종평가 영역 설정]
        if (homework.isEvaluationComplete())
        {
            // 최종평가 완료
            Log.f("EvaluationCompleteLayout VISIBLE")
            _EvaluationCompleteLayout.visibility = View.VISIBLE
            setEvaluationCompleteLayout()
        }

        // [학습자/선생님 코멘트 영역 설정]
        if (homework.isEvaluationComplete())
        {
            // 최종평가 완료
            if (homework.getStudentComment() != "" && homework.getTeacherComment() != "")
            {
                // 학습자/선생님 코멘트 둘 다 있는 경우
                _TwoCommentLayout.visibility = View.VISIBLE
                setTwoCommentLayout()
            }
            else
            {
                // 학습자/선생님 코멘트 한개라도 있는 경우
                if (homework.getStudentComment() != "")
                {
                    mOneCommentType = COMMENT_ONLY_STUDENT
                    setOneCommentStudent()
                }
                else if (homework.getTeacherComment() != "")
                {
                    mOneCommentType = COMMENT_ONLY_TEACHER
                    setOneCommentTeacher()
                }
            }
        }
        else
        {
            // 최종평가 미완료
            if (homework.getTeacherComment() != "")
            {
                // 선생님 코멘트 있을 때
                // 선생님 - 보기, 학생 - 작성 (2개)
                _TwoCommentLayout.visibility = View.VISIBLE
                setTwoCommentLayout()
            }
            else
            {
                // 학생 코멘트만 있을 때
                // 학생 - 작성 or 보기 (1개)
                _OneCommentLayout.visibility = View.VISIBLE
                mOneCommentType = COMMENT_ONLY_STUDENT
                setOneCommentStudent()
            }
        }
    }

    /**
     * 최종 평가 영역 설정
     */
    private fun setEvaluationCompleteLayout()
    {
        val homework = mHomeworkDetailBaseResult!!

        _HomeworkResultImage.background = CommonUtils.getInstance(mContext).getHomeworkEvalImage(homework.getEvaluationState())

        var comment = ""
        var evalSize = 0
        var commentSize = 0

        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            // 태블릿 설정
            comment = " ${homework.getEvaluationComment()}"
            evalSize = 30
            commentSize = 30
        }
        else
        {
            // 스마트폰 설정
            comment = "\n${homework.getEvaluationComment()}"
            evalSize = 45
            commentSize = 40
        }
        val evalText = CommonUtils.getInstance(mContext).getHomeworkEvalText(homework.getEvaluationState())

        _HomeworkResultText.setSeparateText(evalText, comment)
            .setSeparateColor(resources.getColor(R.color.color_fa4959), resources.getColor(R.color.color_444444))
            .setSeparateTextSize(CommonUtils.getInstance(mContext).getPixel(evalSize), CommonUtils.getInstance(mContext).getPixel(commentSize))
            .setSeparateTextStyle((Font.getInstance(mContext).getTypefaceBold()), (Font.getInstance(mContext).getTypefaceRegular()))
            .showView()
    }

    /**
     * 코멘트 1개, 학습자 한마디 설정
     */
    private fun setOneCommentStudent()
    {
        Log.f("Comment One - STUDENT")
        _OneCommentLayout.visibility = View.VISIBLE
        _HomeworkOneCommentTitle.text = mContext.resources.getString(R.string.text_homework_student_comment)
        _HomeworkOneCommentBg.background = mContext.resources.getDrawable(R.drawable.box_list_green)
        _HomeworkOneCommentIcon.background = mContext.resources.getDrawable(R.drawable.icon_smile_chat)
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            _OneCommentLayout.moveChildView(_HomeworkOneCommentIcon, 96f, 11f, 55f, 55f)
        }
        else
        {
            _OneCommentLayout.moveChildView(_HomeworkOneCommentIcon, 74f, 30f, 60f, 60f)
        }

        _HomeworkOneCommentButton.visibility = View.VISIBLE
        if (mHomeworkDetailBaseResult!!.getStudentComment() == "")
        {
            _HomeworkOneCommentButton.run {
                text = resources.getString(R.string.text_homework_comment_write)
                setTextColor(mContext.resources.getColor(R.color.color_ffffff))
                background = resources.getDrawable(R.drawable.round_box_green_60)
            }
        }
        else
        {
            _HomeworkOneCommentButton.run {
                text = resources.getString(R.string.text_homework_comment_watch)
                setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
                background = resources.getDrawable(R.drawable.round_box_empty_green_60)
            }
        }
    }

    /**
     * 코멘트 1개, 선생님 한마디 설정
     */
    private fun setOneCommentTeacher()
    {
        Log.f("Comment One - TEACHER")
        _OneCommentLayout.visibility = View.VISIBLE
        _HomeworkOneCommentTitle.text = mContext.resources.getString(R.string.text_homework_teacher_comment)
        _HomeworkOneCommentBg.background = mContext.resources.getDrawable(R.drawable.box_list_yellow)
        _HomeworkOneCommentIcon.background = mContext.resources.getDrawable(R.drawable.icon_homework_speaker)
        if (CommonUtils.getInstance(mContext).checkTablet)
        {
            _OneCommentLayout.moveChildView(_HomeworkOneCommentIcon, 96f, 19f, 55f, 45f)
        }
        else
        {
            _OneCommentLayout.moveChildView(_HomeworkOneCommentIcon, 74f, 40f, 65f, 45f)
        }

        _HomeworkOneCommentButton.run {
            visibility = View.VISIBLE
            text = resources.getString(R.string.text_homework_comment_watch)
            setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
            background = resources.getDrawable(R.drawable.round_box_empty_green_60)
        }
    }

    /**
     * 코멘트 2개, 학습자 한마디 레이아웃 설정
     * - 학습자 한마디 없을 때 : 작성 || 있을 때 : 보기
     */
    private fun setTwoCommentLayout()
    {
        if (mHomeworkDetailBaseResult!!.getStudentComment() == "")
        {
            // 학습자 한마디 없을 때 -> 작성
            _HomeworkStudentCommentButton.run {
                text = resources.getString(R.string.text_homework_comment_write)
                setTextColor(mContext.resources.getColor(R.color.color_ffffff))
                background = resources.getDrawable(R.drawable.round_box_green_60)
            }
        }
        else
        {
            // 학습자 한마디 있을 때 -> 보기
            _HomeworkStudentCommentButton.run {
                text = resources.getString(R.string.text_homework_comment_watch)
                setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
                background = resources.getDrawable(R.drawable.round_box_empty_green_60)
            }
        }
        _HomeworkStudentCommentButton.visibility = View.VISIBLE
    }

    private fun setContentListLoadingVisible(isVisible : Boolean)
    {
        Log.f("[LIST LOADING] : $isVisible")
        if (isVisible)
        {
            _LoadingProgressLayout.visibility = View.VISIBLE
        }
        else
        {
            _LoadingProgressLayout.visibility = View.GONE
        }
    }

    private fun clearScreenData(allClear : Boolean)
    {
        Log.f("")
        if (allClear)
        {
            // 화면을 완전히 떠나는 경우
            _HomeworkSubTitle.text = ""
            isListAnimationEffect = true
            setContentListLoadingVisible(true)
        }

        // 코멘트 영역 숨기기
        _EvaluationCompleteLayout.visibility = View.GONE
        _OneCommentLayout.visibility = View.GONE
        _TwoCommentLayout.visibility = View.GONE

        mHomeworkItemDetail.clear()
        mHomeworkFilterIndex = 0
        _HomeworkFilterText.text = mHomeworkFilterList!![mHomeworkFilterIndex]
        setHomeworkListView()
    }

    /**
     * 숙제목록 필터링 선택 다이얼로그
     */
    private fun showHomeworkFilterDialog()
    {
        Log.f("")
        val builder = AlertDialog.Builder(mContext)
        builder.setSingleChoiceItems(mHomeworkFilterList, mHomeworkFilterIndex, DialogInterface.OnClickListener{dialog, index ->
            Log.f("Homework Filter Selected : ${mHomeworkFilterList!![index]} ")
            dialog.dismiss()
            mHomeworkFilterIndex = index
            isListAnimationEffect = true
            setHomeworkListData()
        })

        val dialog : AlertDialog = builder.show()
        dialog.show()
    }

    /**
     * 숙제목록 안내 다이얼로그
     */
    private fun showHomeworkInfoDialog()
    {
        val message = mContext.getString(R.string.message_warning_homework_info)
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(message)
            setButtonType(DialogButtonType.BUTTON_1)
            setGravity(Gravity.LEFT)
            setCancelPossible(false)
            show()
        }
    }

    @OnClick(R.id._homeworkInfoButton, R.id._homeworkFilterButton, R.id._homeworkOneCommentButton, R.id._homeworkStudentCommentButton, R.id._homeworkTeacherCommentButton)
    fun onClickView(view : View)
    {
        //중복이벤트 방지
        if(SystemClock.elapsedRealtime() - mLastClickTime < Common.HALF_SECOND)
        {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when(view.id)
        {
            R.id._homeworkOneCommentButton ->
            {
                Log.f("One Comment :: $mOneCommentType")
                isListAnimationEffect = false
                if (mOneCommentType == COMMENT_ONLY_STUDENT)
                {
                    mHomeworkListFragmentObserver.onClickStudentCommentButton()
                }
                else if (mOneCommentType == COMMENT_ONLY_TEACHER)
                {
                    mHomeworkListFragmentObserver.onClickTeacherCommentButton()
                }
            }
            R.id._homeworkStudentCommentButton ->
            {
                Log.f("Student Comment")
                isListAnimationEffect = false
                mHomeworkListFragmentObserver.onClickStudentCommentButton()
            }
            R.id._homeworkTeacherCommentButton ->
            {
                Log.f("Teacher Comment")
                isListAnimationEffect = false
                mHomeworkListFragmentObserver.onClickTeacherCommentButton()
            }
            R.id._homeworkInfoButton ->
            {
                showHomeworkInfoDialog()
            }
            R.id._homeworkFilterButton ->
            {
                showHomeworkFilterDialog()
            }
        }
    }

    private val mHomeworkItemListener : OnItemViewClickListener = object : OnItemViewClickListener
    {
        override fun onItemClick(position : Int)
        {
            isListAnimationEffect = false
            mHomeworkListFragmentObserver.onClickHomeworkItem(mHomeworkItemDetail[position])
        }
    }
}