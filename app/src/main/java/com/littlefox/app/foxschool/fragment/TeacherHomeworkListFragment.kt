package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
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
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.HomeworkType
import com.littlefox.app.foxschool.viewmodel.HomeworkListFragmentObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.library.view.text.SeparateTextView
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 리스트 화면 (선생님용)
 * - 숙제 현황 상세 보기  (학생 숙제 현황)
 * - 숙제 내용          (반 숙제 내용)
 * @author 김태은
 */
class TeacherHomeworkListFragment : Fragment()
{
    /** 서브타이틀 (이름 숙제기간) */
    @BindView(R.id._homeworkSubTitleBackground)
    lateinit var _HomeworkSubTitleBackground : View

    @BindView(R.id._homeworkSubTitleSep)
    lateinit var _HomeworkSubTitle : SeparateTextView

    @BindView(R.id._resultCommentLayout)
    lateinit var _ResultCommentLayout : ScalableLayout

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

        const val CONTENTS_TYPE_ALL : Int = 0
        const val CONTENTS_TYPE_ANIMATION : Int = 1
        const val CONTENTS_TYPE_EBOOK : Int = 2
        const val CONTENTS_TYPE_QUIZ : Int = 3
        const val CONTENTS_TYPE_STARWORDS : Int = 4
        const val CONTENTS_TYPE_CROSSWORD : Int = 5
        const val CONTENTS_TYPE_RECORDER : Int = 6
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

    private var mOneCommentType : Int = -1              // 버튼1개 코멘트 영역 타입
    private var mClickEnable : Boolean = false          // 데이터 세팅 전 이벤트 막기 위한 플래그 || 디폴트 : 이벤트 막기
    private var mListAnimationEffect : Boolean = true   // 숙제현황 리스트 애니메이션 활성 플래그 || 디폴트 : 애니메이션 활성화

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
        mClickEnable = true
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
        mHomeworkFilterList = mContext.resources.getStringArray(R.array.text_list_homework_filter_teacher)
        _HomeworkSubTitleBackground.setBackgroundColor(mContext.resources.getColor(R.color.color_29c8e6_alpha_50))
        _HomeworkSubTitle.visibility = View.VISIBLE
    }

    private fun initFont()
    {
        _HomeworkResultText.setTypeface(Font.getInstance(mContext).getRobotoBold())
        _HomeworkOneCommentTitle.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkOneCommentButton.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkStudentComment.setTypeface(Font.getInstance(mContext).getRobotoMedium())
        _HomeworkStudentCommentButton.setTypeface(Font.getInstance(mContext).getRobotoMedium())
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

        // 숙제현황||숙제내용 데이터
        mHomeworkManagePresenterObserver.updateHomeworkListData.observe(viewLifecycleOwner, { item ->
            mHomeworkDetailBaseResult = item
            updateHomeworkListData()
        })

        // 숙제현황||숙제내용 화면 초기화
        mHomeworkManagePresenterObserver.clearHomeworkList.observe(viewLifecycleOwner, { allClear ->
            clearScreenData(allClear)
        })
    }

    /**
     * 숙제현황 데이터 받아서 처리
     */
    private fun updateHomeworkListData()
    {
        mClickEnable = false // 클릭 이벤트 막기
        if (mListAnimationEffect == true)
        {
            setContentListLoadingVisible(true) // 애니메이션 효과 켜져있을때만 컨텐츠 로딩 다이얼로그 표시
        }

        setHomeworkListData()       // 숙제 리스트
        setHomeworkSubTitleText()   // 숙제 기간 텍스트 (서브타이틀)
        setCommentLayout()          // 코멘트 영역

        mClickEnable = true     // 클릭 이벤트 허용
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
            val homeworkType : HomeworkType?
            when (mHomeworkFilterIndex)
            {
                CONTENTS_TYPE_ALL -> homeworkType = null
                CONTENTS_TYPE_ANIMATION -> homeworkType = HomeworkType.ANIMATION
                CONTENTS_TYPE_EBOOK -> homeworkType = HomeworkType.EBOOK
                CONTENTS_TYPE_QUIZ -> homeworkType = HomeworkType.QUIZ
                CONTENTS_TYPE_STARWORDS -> homeworkType = HomeworkType.STARWORDS
                CONTENTS_TYPE_CROSSWORD -> homeworkType = HomeworkType.CROSSWORD
                CONTENTS_TYPE_RECORDER -> homeworkType = HomeworkType.RECORDER
                else -> homeworkType = null
            }

            if (homeworkType == null)
            {
                // 전체
                mHomeworkItemDetail.addAll(mHomeworkDetailBaseResult!!.getHomeworkItemList())
            }
            else
            {
                // 컨텐츠 필터링
                for (i in mHomeworkDetailBaseResult!!.getHomeworkItemList().indices)
                {
                    if (homeworkType == mHomeworkDetailBaseResult!!.getHomeworkItemList()[i].getHomeworkType())
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
            Log.f("mHomeworkItemViewAdapter == null")
            mHomeworkItemViewAdapter = HomeworkItemViewAdapter(mContext, isTeacher = true)
                .setItemList(mHomeworkItemDetail)
                .setHomeworkItemListener(mHomeworkItemListener)
            if (mHomeworkDetailBaseResult!!.getFragmentType() == Common.PAGE_HOMEWORK_DETAIL)
            {
                mHomeworkItemViewAdapter!!.setButtonEnable(false)
            }
            else
            {
                mHomeworkItemViewAdapter!!.setButtonEnable(true)
            }
        }
        else
        {
            // 데이터 변경
            Log.f("mHomeworkItemViewAdapter notifyDataSetChanged")
            if (mHomeworkDetailBaseResult!!.getFragmentType() == Common.PAGE_HOMEWORK_DETAIL)
            {
                mHomeworkItemViewAdapter!!.setButtonEnable(false)
            }
            else
            {
                mHomeworkItemViewAdapter!!.setButtonEnable(true)
            }
            mHomeworkItemViewAdapter!!.setItemList(mHomeworkItemDetail)
            mHomeworkItemViewAdapter!!.notifyDataSetChanged()
        }

        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        _HomeworkListView.layoutManager = linearLayoutManager

        Log.f("[ListView Animation] || $mListAnimationEffect")
        if (mListAnimationEffect)
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

        val name = mHomeworkDetailBaseResult!!.getFragmentTitle()

        val textSize = if (CommonUtils.getInstance(mContext).checkTablet) 32 else 40
        _HomeworkSubTitle.setSeparateText(name, " $homeworkDate")
            .setSeparateColor(resources.getColor(R.color.color_000000), resources.getColor(R.color.color_000000))
            .setSeparateTextSize(CommonUtils.getInstance(mContext).getPixel(textSize), CommonUtils.getInstance(mContext).getPixel(textSize))
            .setSeparateTextStyle((Font.getInstance(mContext).getRobotoBold()), (Font.getInstance(mContext).getRobotoRegular()))
            .showView()
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
            _ResultCommentLayout.visibility = View.VISIBLE
            setResultCommentLayout()
        }

        // [학습자/선생님 코멘트 영역 설정]
        if (homework.getStudentComment() != "" && homework.getTeacherComment() != "")
        {
            Log.f("[COMMENT] 2")
            // 학습자/선생님 코멘트 둘 다 있는 경우
            _TwoCommentLayout.visibility = View.VISIBLE
            setHomeworkStudentLayout()
        }
        else if (homework.getStudentComment() != "" || homework.getTeacherComment() != "")
        {
            Log.f("[COMMENT] 1")
            // 학습자/선생님 코멘트 한개라도 있는 경우
            _OneCommentLayout.visibility = View.VISIBLE

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

    /**
     * 최종 평가 영역 설정
     */
    private fun setResultCommentLayout()
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
            .setSeparateTextStyle((Font.getInstance(mContext).getRobotoBold()), (Font.getInstance(mContext).getRobotoRegular()))
            .showView()
    }

    /**
     * 코멘트 1개, 학습자 한마디 설정
     */
    private fun setOneCommentStudent()
    {
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
            _HomeworkOneCommentButton.text = resources.getString(R.string.text_homework_comment_write)
            _HomeworkOneCommentButton.setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            _HomeworkOneCommentButton.background = resources.getDrawable(R.drawable.round_box_green_60)
        }
        else
        {
            _HomeworkOneCommentButton.text = resources.getString(R.string.text_homework_comment_watch)
            _HomeworkOneCommentButton.setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
            _HomeworkOneCommentButton.background = resources.getDrawable(R.drawable.round_box_empty_green_60)
        }
    }

    /**
     * 코멘트 1개, 선생님 한마디 설정
     */
    private fun setOneCommentTeacher()
    {
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

        _HomeworkOneCommentButton.visibility = View.VISIBLE
        _HomeworkOneCommentButton.text = resources.getString(R.string.text_homework_comment_watch)
        _HomeworkOneCommentButton.setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
        _HomeworkOneCommentButton.background = resources.getDrawable(R.drawable.round_box_empty_green_60)
    }

    /**
     * 학습자 한마디 레이아웃 설정
     * - 학습자 한마디 없을 때 : 작성 || 있을 때 : 보기
     */
    private fun setHomeworkStudentLayout()
    {
        if (mHomeworkDetailBaseResult!!.getStudentComment() == "")
        {
            // 학습자 한마디 없을 때 -> 작성
            _HomeworkStudentCommentButton.text = resources.getString(R.string.text_homework_comment_write)
            _HomeworkStudentCommentButton.setTextColor(mContext.resources.getColor(R.color.color_ffffff))
            _HomeworkStudentCommentButton.background = resources.getDrawable(R.drawable.round_box_green_60)
        }
        else
        {
            // 학습자 한마디 있을 때 -> 보기
            _HomeworkStudentCommentButton.text = resources.getString(R.string.text_homework_comment_watch)
            _HomeworkStudentCommentButton.setTextColor(mContext.resources.getColor(R.color.color_23cc8a))
            _HomeworkStudentCommentButton.background = resources.getDrawable(R.drawable.round_box_empty_green_60)
        }
        _HomeworkStudentCommentButton.visibility = View.VISIBLE
    }

    /**
     * 컨텐츠 로딩바 표시/비표시
     */
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

    /**
     * 화면 데이터 초기화
     */
    private fun clearScreenData(allClear : Boolean)
    {
        if (allClear)
        {
            // 화면을 완전히 떠나는 경우
            _HomeworkSubTitle.text = ""
            mListAnimationEffect = true
            setContentListLoadingVisible(true)
        }

        // 코멘트 영역 숨기기
        _ResultCommentLayout.visibility = View.GONE
        _OneCommentLayout.visibility = View.GONE
        _TwoCommentLayout.visibility = View.GONE

        mHomeworkItemDetail.clear()
        mHomeworkFilterIndex = 0
        _HomeworkFilterText.text = mHomeworkFilterList!![mHomeworkFilterIndex]
    }

    /**
     * 숙제목록 필터링 선택 다이얼로그
     */
    private fun showHomeworkFilterDialog()
    {
        Log.f("")
        val builder = AlertDialog.Builder(mContext)
        builder.setSingleChoiceItems(mHomeworkFilterList, mHomeworkFilterIndex, DialogInterface.OnClickListener{dialog, index ->
            dialog.dismiss()
            mHomeworkFilterIndex = index
            mListAnimationEffect = true
            setHomeworkListData()
        })

        val dialog : AlertDialog = builder.show()
        dialog.setOnDismissListener {
            mClickEnable = true // 다이얼로그 닫을 때 클릭 이벤트 막는 플래그 풀어주기
        }
        dialog.show()
    }

    /**
     * 숙제목록 안내 다이얼로그
     */
    private fun showHomeworkInfoDialog()
    {
        val message = mContext.getString(R.string.message_warning_homework_info)
        mTemplateAlertDialog = TemplateAlertDialog(mContext)
        mTemplateAlertDialog.setMessage(message)
        mTemplateAlertDialog.setButtonType(DialogButtonType.BUTTON_1)
        mTemplateAlertDialog.setDialogListener(mDialogListener)
        mTemplateAlertDialog.setGravity(Gravity.LEFT)
        mTemplateAlertDialog.setCancelPossible(false)
        mTemplateAlertDialog.show()
    }

    @OnClick(R.id._homeworkInfoButton, R.id._homeworkFilterButton, R.id._homeworkOneCommentButton, R.id._homeworkStudentCommentButton, R.id._homeworkTeacherCommentButton)
    fun onClickView(view : View)
    {
        if (mClickEnable == false) return // 중복 클릭이벤트 막기

        when(view.id)
        {
            R.id._homeworkOneCommentButton ->
            {
                mClickEnable = false
                mListAnimationEffect = false

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
                mClickEnable = false
                mListAnimationEffect = false
                mHomeworkListFragmentObserver.onClickStudentCommentButton()
            }
            R.id._homeworkTeacherCommentButton ->
            {
                mClickEnable = false
                mListAnimationEffect = false
                mHomeworkListFragmentObserver.onClickTeacherCommentButton()
            }
            R.id._homeworkInfoButton ->
            {
                mClickEnable = false
                showHomeworkInfoDialog()
            }
            R.id._homeworkFilterButton ->
            {
                mClickEnable = false
                showHomeworkFilterDialog()
            }
        }
    }

    /**
     * 숙제현황 리스트 클릭 이벤트 Listener
     */
    private val mHomeworkItemListener : OnItemViewClickListener = object : OnItemViewClickListener
    {
        override fun onItemClick(position : Int)
        {
            if (mClickEnable)
            {
                mClickEnable = false
                mListAnimationEffect = false
                mHomeworkListFragmentObserver.onClickHomeworkItem(mHomeworkItemDetail[position])
                mClickEnable = true // TODO 김태은 컨텐츠 연결 전까지 임시로 사용
            }
        }
    }

    /**
     * 다이얼로그 Listener
     */
    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            mClickEnable = true
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int) { }
    }
}