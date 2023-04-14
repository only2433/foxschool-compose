package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.fragment.HomeworkFragmentViewModel
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 코멘트 화면 (선생님용)
 * - 학습자 한마디 (보기)
 * - 선생님 한마디 (보기)
 * @author 김태은
 */
class TeacherHomeworkCommentFragment : Fragment()
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._commentInputLayout)
    lateinit var _CommentInputLayout : ScalableLayout

    @BindView(R.id._commentInputCountText)
    lateinit var _CommentInputCountText : TextView

    @BindView(R.id._commentBoxImage)
    lateinit var _CommentBoxImage : ImageView

    @BindView(R.id._commentEditText)
    lateinit var _CommentEditText : EditText

    @BindView(R.id._commentRegisterButton)
    lateinit var _CommentRegisterButton : TextView

    @BindView(R.id._commentUpdateButton)
    lateinit var _CommentUpdateButton : TextView

    @BindView(R.id._commentDeleteButton)
    lateinit var _CommentDeleteButton : TextView

    @BindView(R.id._commentTeacherMessage)
    lateinit var _CommentTeacherMessage : TextView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private var mComment : String = ""                  // 통신에서 응답받은 학습자/선생님 한마디

    private val fragmentViewModel : HomeworkFragmentViewModel by activityViewModels()

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
            view = inflater.inflate(R.layout.fragment_homework_comment_tablet, container, false)
        } else
        {
            view = inflater.inflate(R.layout.fragment_homework_comment, container, false)
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
    private fun initView()
    {
        _CommentUpdateButton.visibility = View.GONE // 수정 버튼 숨김
        _CommentRegisterButton.visibility = View.GONE // 등록 버튼 숨김
        _CommentDeleteButton.visibility = View.GONE // 삭제 버튼 숨김
    }

    private fun initFont()
    {
        _CommentInputCountText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _CommentEditText.typeface = Font.getInstance(mContext).getTypefaceRegular()
        _CommentRegisterButton.typeface = Font.getInstance(mContext).getTypefaceBold()
        _CommentUpdateButton.typeface = Font.getInstance(mContext).getTypefaceBold()
        _CommentDeleteButton.typeface = Font.getInstance(mContext).getTypefaceBold()
        _CommentTeacherMessage.typeface = Font.getInstance(mContext).getTypefaceRegular()
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        // 페이지 세팅
        fragmentViewModel.settingCommentPage.observe(viewLifecycleOwner) { pair ->
            val commentType = pair.first
            val comment = pair.second

            mComment = comment
            clearScreenData() // 화면 초기화
            settingPageType(commentType)
        }
    }

    /**
     * 화면 세팅
     */
    private fun settingPageType(commentType : HomeworkCommentType)
    {
        val boxLeft : Float = if (CommonUtils.getInstance(mContext).checkTablet) 46f else 28f
        var boxTop : Float = 0f
        val textLeft : Float = if (CommonUtils.getInstance(mContext).checkTablet) 66f else 62f
        var textTop : Float = 0f

        if (commentType == HomeworkCommentType.COMMENT_STUDENT)
        {
            // 학생용 한마디 화면
            _CommentInputCountText.visibility = View.VISIBLE

            boxTop = if (CommonUtils.getInstance(mContext).checkTablet) 42f else 60f
            textTop = if (CommonUtils.getInstance(mContext).checkTablet) 73f else 105f

            _CommentInputLayout.moveChildView(_CommentBoxImage, boxLeft, boxTop)
            _CommentInputLayout.moveChildView(_CommentEditText, textLeft, textTop)

            setStudentCommentLayout()
        }
        else if (commentType == HomeworkCommentType.COMMENT_TEACHER)
        {
            // 선생님 한마디 화면
            _CommentInputCountText.visibility = View.GONE
            _CommentTeacherMessage.visibility = View.VISIBLE
            boxTop = if (CommonUtils.getInstance(mContext).checkTablet) 76f else 116f
            textTop = if (CommonUtils.getInstance(mContext).checkTablet) 106f else 171f

            _CommentInputLayout.moveChildView(_CommentBoxImage, boxLeft, boxTop)
            _CommentInputLayout.moveChildView(_CommentEditText, textLeft, textTop)

            setTeacherCommentLayout()
        }
    }

    /**
     * [학습자 한마디] 코멘트 및 버튼 표시 설정
     * - 최종평가 이후에는 텍스트 수정 불가
     */
    private fun setStudentCommentLayout()
    {
        _CommentEditText.setText(mComment)

        _CommentEditText.visibility = View.VISIBLE
        _MainBaseLayout.requestFocus() // EditText 커서 제거하기 위해 사용

        // 최종평가를 한 경우 코멘트 수정 불가
        _CommentInputCountText.visibility = View.GONE
        _CommentEditText.run {
            setFocusableInTouchMode(false)
            clearFocus()
        }
        _CommentRegisterButton.visibility = View.GONE
        _CommentUpdateButton.visibility = View.GONE
        _CommentDeleteButton.visibility = View.GONE
    }

    /**
     * [선생님 한마디]
     * - 통신 응답받은 코멘트 입력
     * - 텍스트 수정 불가
     */
    private fun setTeacherCommentLayout()
    {
        _CommentEditText.run {
            visibility = View.VISIBLE
            setText(mComment)
            setFocusableInTouchMode(false)
            clearFocus()
        }
    }

    /**
     * 화면 초기화
     */
    private fun clearScreenData()
    {
        _CommentEditText.setText("")
        _CommentTeacherMessage.visibility = View.GONE
        _CommentRegisterButton.visibility = View.GONE
        _CommentUpdateButton.visibility = View.GONE
        _CommentDeleteButton.visibility = View.GONE
    }
}