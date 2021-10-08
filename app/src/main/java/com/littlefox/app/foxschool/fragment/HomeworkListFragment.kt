package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListBaseResult
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.HomeworkListFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 리스트 화면 (학생용)
 * @author 김태은
 */
class HomeworkListFragment : Fragment()
{
    @BindView(R.id._datePickLayout)
    lateinit var _DatePickLayout : ScalableLayout

    @BindView(R.id._beforeButton)
    lateinit var _BeforeButton : ImageView

    @BindView(R.id._beforeButtonRect)
    lateinit var _BeforeButtonRect : ImageView

    @BindView(R.id._afterButton)
    lateinit var _AfterButton : ImageView

    @BindView(R.id._afterButtonRect)
    lateinit var _AfterButtonRect : ImageView

    @BindView(R.id._homeworkDateText)
    lateinit var _HomeworkDateText : TextView

    @BindView(R.id._resultCommentLayout)
    lateinit var _ResultCommentLayout : ScalableLayout

    @BindView(R.id._homeworkResultImage)
    lateinit var _HomeworkResultImage : ImageView

    @BindView(R.id._homeworkResultText)
    lateinit var _HomeworkResultText : SeparateTextView

    @BindView(R.id._oneCommentLayout)
    lateinit var _OneCommentLayout : ScalableLayout

    @BindView(R.id._homeworkStudentCommentBg1)
    lateinit var _HomeworkStudentCommentBg1 : ImageView

    @BindView(R.id._homeworkStudentComment1)
    lateinit var _HomeworkStudentComment1 : TextView

    @BindView(R.id._homeworkStudentCommentButton1)
    lateinit var _HomeworkStudentCommentButton1 : TextView

    @BindView(R.id._twoCommentLayout)
    lateinit var _TwoCommentLayout : ScalableLayout

    @BindView(R.id._homeworkStudentCommentBg2)
    lateinit var _HomeworkStudentCommentBg2 : ImageView

    @BindView(R.id._homeworkStudentComment2)
    lateinit var _HomeworkStudentComment2 : TextView

    @BindView(R.id._homeworkStudentCommentButton2)
    lateinit var _HomeworkStudentCommentButton2 : TextView

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

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private lateinit var mHomeworkListFragmentObserver : HomeworkListFragmentObserver
    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver

    private var mDataSetFlag : Boolean = false // 데이터 세팅 전 이벤트 막기 위한 플래그

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
        initView()
        initFont()
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
        _HomeworkDateText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkResultText.setTypeface(Font.getInstance(mContext).getRobotoBold())
        _HomeworkStudentComment1.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkStudentCommentButton1.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkStudentComment2.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkStudentCommentButton2.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkTeacherComment.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkTeacherCommentButton.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkListText.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkFilterText.setTypeface(Font.getInstance(mContext).getRobotoRegular())
    }

    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mHomeworkListFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkListFragmentObserver::class.java)
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)

        // 숙제기간 텍스트
        mHomeworkManagePresenterObserver.setHomeworkDateText.observe(mContext as AppCompatActivity, { date ->
            _HomeworkDateText.text = date
        })

        // 숙제 리스트 세팅
        mHomeworkManagePresenterObserver.setHomeworkListView.observe(mContext as AppCompatActivity, { pair ->
            val linearLayoutManager = LinearLayoutManager(mContext)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            _HomeworkListView.layoutManager = linearLayoutManager

            if (pair.second)
            {
                val animationController = AnimationUtils.loadLayoutAnimation(mContext, R.anim.listview_layoutanimation)
                _HomeworkListView.layoutAnimation = animationController
            }
            _HomeworkListView.adapter = pair.first

            mDataSetFlag = true
        })

        // 필터 선택 텍스트
        mHomeworkManagePresenterObserver.setHomeworkFilterText.observe(mContext as AppCompatActivity, { selected ->
            _HomeworkFilterText.text = selected
        })

        // 최종 평가 영역 세팅
        mHomeworkManagePresenterObserver.setResultCommentLayout.observe(mContext as AppCompatActivity, { item ->
            setResultCommentLayout(item)
        })

        // 학습자 한마디 버튼 세팅
        mHomeworkManagePresenterObserver.setStudentCommentLayout.observe(mContext as AppCompatActivity, { hasComment ->
            setStudentCommentButton(hasComment)
        })

        // 선생님 한마디 영역 세팅
        mHomeworkManagePresenterObserver.setTeacherCommentLayout.observe(mContext as AppCompatActivity, { hasComment ->
            setTeacherCommentVisible(hasComment)
        })

        // 이전버튼 표시여부
        mHomeworkManagePresenterObserver.setHomeworkPrevButton.observe((mContext as AppCompatActivity), { isEnable ->
            if (isEnable) showPrevButton()
            else hidePrevButton()
        })

        // 다음버튼 표시여부
        mHomeworkManagePresenterObserver.setHomeworkNextButton.observe((mContext as AppCompatActivity), { isEnable ->
            if (isEnable) showNextButton()
            else hideNexButton()
        })

        // 컨텐츠 영역 로딩바 표시여부
        mHomeworkManagePresenterObserver.setHomeworkLoadingProgressBar.observe((mContext as AppCompatActivity), { isVisible ->
            if (isVisible) showContentListLoading()
            else hideContentListLoading()
        })

        // 숙제 현황 화면 초기화
        mHomeworkManagePresenterObserver.clearHomeworkList.observe((mContext as AppCompatActivity), { allClear ->
            clearScreenData(allClear)
        })
    }

    /**
     * 컨텐츠 로딩바 표시
     */
    private fun showContentListLoading()
    {
        _LoadingProgressLayout.visibility = View.VISIBLE
    }

    /**
     * 컨텐츠 로딩바 숨김
     */
    private fun hideContentListLoading()
    {
        _LoadingProgressLayout.visibility = View.GONE
    }

    /**
     * 이전 버튼 표시
     */
    private fun showPrevButton()
    {
        _BeforeButton.visibility = View.VISIBLE
        _BeforeButtonRect.visibility = View.VISIBLE
    }

    /**
     * 이전 버튼 숨김
     */
    private fun hidePrevButton()
    {
        _BeforeButton.visibility = View.GONE
        _BeforeButtonRect.visibility = View.GONE
    }

    /**
     * 다음 버튼 표시
     */
    private fun showNextButton()
    {
        _AfterButton.visibility = View.VISIBLE
        _AfterButtonRect.visibility = View.VISIBLE
    }

    /**
     * 다음 버튼 숨김
     */
    private fun hideNexButton()
    {
        _AfterButton.visibility = View.GONE
        _AfterButtonRect.visibility = View.GONE
    }

    /**
     * 최종 평가 영역 설정
     */
    private fun setResultCommentLayout(item : HomeworkListBaseResult)
    {
        if (item.isEvaluationComplete())
        {
            // 평가 완료
            _ResultCommentLayout.visibility = View.VISIBLE
            _HomeworkResultImage.background = CommonUtils.getInstance(mContext).getCalendarEvalImage(item.getEvaluationState())

            var comment = ""
            var evalSize = 0
            var commentSize = 0

            if (CommonUtils.getInstance(mContext).checkTablet)
            {
                // 태블릿 설정
                comment = " ${item.getEvaluationComment()}"
                evalSize = 30
                commentSize = 30
            }
            else
            {
                // 스마트폰 설정
                comment = "\n${item.getEvaluationComment()}"
                evalSize = 45
                commentSize = 40
            }
            val evalText = CommonUtils.getInstance(mContext).getHomeworkEvalText(item.getEvaluationState())

            _HomeworkResultText.setSeparateText(evalText, comment)
                .setSeparateColor(resources.getColor(R.color.color_fa4959), resources.getColor(R.color.color_444444))
                .setSeparateTextSize(CommonUtils.getInstance(mContext).getPixel(evalSize), CommonUtils.getInstance(mContext).getPixel(commentSize))
                .setSeparateTextStyle((Font.getInstance(mContext).getRobotoBold()), (Font.getInstance(mContext).getRobotoRegular()))
                .showView()
        }
        else
        {
            // 평가 미완료
            _ResultCommentLayout.visibility = View.GONE
        }
    }

    /**
     * 학습자 한마디 버튼 설정
     */
    private fun setStudentCommentButton(hasComment : Boolean)
    {
        if (hasComment)
        {
            // 학습자 한마디 있을 때 -> 보기
            _HomeworkStudentCommentButton1.text = resources.getString(R.string.text_homework_comment_watch)
            _HomeworkStudentCommentButton1.setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
            _HomeworkStudentCommentButton1.background = resources.getDrawable(R.drawable.round_box_empty_green_60)
            _HomeworkStudentCommentButton2.text = resources.getString(R.string.text_homework_comment_watch)
            _HomeworkStudentCommentButton2.setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
            _HomeworkStudentCommentButton2.background = resources.getDrawable(R.drawable.round_box_empty_green_60)
        }
        else
        {
            // 학습자 한마디 없을 때 -> 작성
            _HomeworkStudentCommentButton1.text = resources.getString(R.string.text_homework_comment_write)
            _HomeworkStudentCommentButton1.setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            _HomeworkStudentCommentButton1.background = resources.getDrawable(R.drawable.round_box_green_60)
            _HomeworkStudentCommentButton2.text = resources.getString(R.string.text_homework_comment_write)
            _HomeworkStudentCommentButton2.setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            _HomeworkStudentCommentButton2.background = resources.getDrawable(R.drawable.round_box_green_60)
        }

        _HomeworkStudentCommentButton1.visibility = View.VISIBLE
        _HomeworkStudentCommentButton2.visibility = View.VISIBLE
    }

    /**
     * 선생님 한마디 영역 표시/비표시
     */
    private fun setTeacherCommentVisible(hasComment : Boolean)
    {
        if (hasComment)
        {
            _OneCommentLayout.visibility = View.GONE
            _TwoCommentLayout.visibility = View.VISIBLE
        }
        else
        {
            _OneCommentLayout.visibility = View.VISIBLE
            _TwoCommentLayout.visibility = View.GONE
        }
    }

    /**
     * 화면 데이터 초기화
     */
    private fun clearScreenData(allClear : Boolean)
    {
        if (allClear)
        {
            // 화면을 완전히 떠나는 경우 날짜 텍스트 초기화, 날짜 화살표 버튼 숨기기
            _HomeworkDateText.text = ""
            hidePrevButton()
            hideNexButton()
        }

        _ResultCommentLayout.visibility = View.GONE             // 최종 평가 영역 숨기기
        _HomeworkStudentCommentButton1.visibility = View.GONE   // 학생용 코멘트 버튼 숨기기
        _HomeworkStudentCommentButton2.visibility = View.GONE
        setTeacherCommentVisible(false)                         // 선생님 코멘트 영역 숨기기
    }

    @OnClick(R.id._beforeButtonRect, R.id._afterButtonRect, R.id._homeworkStudentCommentButton1, R.id._homeworkStudentCommentButton2, R.id._homeworkTeacherCommentButton,
             R.id._homeworkInfoButton, R.id._homeworkFilterButton)
    fun onClickView(view : View)
    {
        if (mDataSetFlag == false) return // 데이터 세팅하기 전일 때 이벤트 막기

        when(view.id)
        {
            R.id._beforeButtonRect -> mHomeworkListFragmentObserver.onClickBeforeButton()
            R.id._afterButtonRect -> mHomeworkListFragmentObserver.onClickAfterButton()
            R.id._homeworkStudentCommentButton1, R.id._homeworkStudentCommentButton2 -> mHomeworkListFragmentObserver.onClickStudentCommentButton()
            R.id._homeworkTeacherCommentButton -> mHomeworkListFragmentObserver.onClickTeacherCommentButton()
            R.id._homeworkInfoButton -> mHomeworkListFragmentObserver.onClickHomeworkInfoButton()
            R.id._homeworkFilterButton -> mHomeworkListFragmentObserver.onClickListFilterButton()
        }
    }
}