package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.HomeworkManagePresenterObserver
import com.littlefox.app.foxschool.viewmodel.HomeworkCommentFragmentObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 숙제관리 코멘트 화면 (학생용)
 * - 학습자 한마디 (등록, 수정, 삭제)
 * - 선생님 한마디 (보기)
 * @author 김태은
 */
class HomeworkCommentFragment : Fragment()
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

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private lateinit var mHomeworkCommentFragmentObserver : HomeworkCommentFragmentObserver
    private lateinit var mHomeworkManagePresenterObserver : HomeworkManagePresenterObserver

    private var hasStudentComment : Boolean = false // 학습자 코멘트 여부 플래그

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
        _CommentEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && hasStudentComment)
            {
                setUpdateButtonEnable(true)
            }
        }
        _CommentEditText.addTextChangedListener(mEditTextChangeListener)
        setRegisterButtonEnable(false)
        setUpdateButtonEnable(false)
    }

    private fun initFont()
    {
        _CommentInputCountText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _CommentEditText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _CommentRegisterButton.typeface = Font.getInstance(mContext).getRobotoBold()
        _CommentUpdateButton.typeface = Font.getInstance(mContext).getRobotoBold()
        _CommentDeleteButton.typeface = Font.getInstance(mContext).getRobotoBold()
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mHomeworkCommentFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkCommentFragmentObserver::class.java)
        mHomeworkManagePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(HomeworkManagePresenterObserver::class.java)

        // 페이지 세팅
        mHomeworkManagePresenterObserver.setPageType.observe(mContext as AppCompatActivity, { position ->
            setPageType(position)
        })

        // 학습자 코멘트 설정
        mHomeworkManagePresenterObserver.setStudentCommentData.observe(mContext as AppCompatActivity, { comment ->
            hasStudentComment = (comment != "") // 기존에 입력된 코멘트 있는지 확인하는 플래그 설정
            setStudentCommentLayout(comment)
        })

        // 선생님 코멘트 설정
        mHomeworkManagePresenterObserver.setTeacherCommentData.observe(mContext as AppCompatActivity, { comment ->
            _CommentEditText.visibility = View.VISIBLE
            _CommentEditText.setText(comment)
            _CommentEditText.isEnabled = false
        })

        // 화면 데이터 초기화
        mHomeworkManagePresenterObserver.clearScreenData.observe(mContext as AppCompatActivity, {
            clearScreenData()
        })
    }

    /**
     * 화면 세팅
     */
    private fun setPageType(position : Int)
    {
        // 버튼 전부 숨김 (학생용 세팅은 따로 진행하기 때문에)
        setRegisterButtonVisible(false)
        setUpdateButtonVisible(false)
        setDeleteButtonVisible(false)

        if (position == Common.PAGE_HOMEWORK_STUDENT_COMMENT)
        {
            _CommentInputCountText.visibility = View.VISIBLE

            if (CommonUtils.getInstance(mContext).checkTablet)
            {
                _CommentInputLayout.moveChildView(_CommentBoxImage, 46f, 96f)
                _CommentInputLayout.moveChildView(_CommentEditText, 66f, 125f)
            }
            else
            {
                _CommentInputLayout.moveChildView(_CommentBoxImage, 28f, 140f)
                _CommentInputLayout.moveChildView(_CommentEditText, 62f, 180f)
            }
        }
        else if (position == Common.PAGE_HOMEWORK_TEACHER_COMMENT)
        {
            _CommentInputCountText.visibility = View.GONE

            if (CommonUtils.getInstance(mContext).checkTablet)
            {
                _CommentInputLayout.moveChildView(_CommentBoxImage, 46f, 42f)
                _CommentInputLayout.moveChildView(_CommentEditText, 66f, 73f)
            }
            else
            {
                _CommentInputLayout.moveChildView(_CommentBoxImage, 28f, 60f)
                _CommentInputLayout.moveChildView(_CommentEditText, 62f, 105f)
            }
        }
    }

    /**
     * [학습자 한마디] 코멘트 및 버튼 표시 설정
     */
    private fun setStudentCommentLayout(comment : String)
    {
        _CommentEditText.setText(comment)

        _CommentEditText.visibility = View.VISIBLE
        _CommentEditText.isEnabled = true
        _MainBaseLayout.requestFocus() // EditText 커서 제거하기 위해 사용

        if (hasStudentComment)
        {
            setCommentCountText()
            setUpdateButtonEnable(false)
            setRegisterButtonVisible(false)
            setUpdateButtonVisible(true)
            setDeleteButtonVisible(true)
        }
        else
        {
            setRegisterButtonVisible(true)
            setUpdateButtonVisible(false)
            setDeleteButtonVisible(false)
        }
    }

    /**
     * 등록 버튼 활성/비활성
     */
    private fun setRegisterButtonEnable(isEnable : Boolean)
    {
        _CommentRegisterButton.isEnabled = isEnable
        if (isEnable)
        {
            _CommentRegisterButton.setBackgroundResource(R.drawable.round_box_light_blue_84)
        }
        else
        {
            _CommentRegisterButton.setBackgroundResource(R.drawable.round_box_gray_84)
        }
    }

    /**
     * 수정 버튼 활성/비활성
     */
    private fun setUpdateButtonEnable(isEnable : Boolean)
    {
        _CommentUpdateButton.isEnabled = isEnable
        if (isEnable)
        {
            _CommentUpdateButton.setBackgroundResource(R.drawable.round_box_light_blue_84)
        }
        else
        {
            _CommentUpdateButton.setBackgroundResource(R.drawable.round_box_gray_84)
        }
    }

    /**
     * 등록 버튼 표시/비표시
     */
    private fun setRegisterButtonVisible(isVisible : Boolean)
    {
        if (isVisible)
        {
            _CommentRegisterButton.visibility = View.VISIBLE
        }
        else
        {
            _CommentRegisterButton.visibility = View.GONE
        }
    }

    /**
     * 수정 버튼 표시/비표시
     */
    private fun setUpdateButtonVisible(isVisible : Boolean)
    {
        if (isVisible)
        {
            _CommentUpdateButton.visibility = View.VISIBLE
        }
        else
        {
            _CommentUpdateButton.visibility = View.GONE
        }
    }

    /**
     * 삭제 버튼 표시/비표시
     */
    private fun setDeleteButtonVisible(isVisible : Boolean)
    {
        if (isVisible)
        {
            _CommentDeleteButton.visibility = View.VISIBLE
        }
        else
        {
            _CommentDeleteButton.visibility = View.GONE
        }
    }

    /**
     * 코멘트 카운트 텍스트 설정
     */
    private fun setCommentCountText()
    {
        // 바이트 사이즈 구하기 위해 코멘트 바이트로 변경
        val inputByte = (_CommentEditText.text.toString()).toByteArray(charset("EUC-KR"))
        val text = "${inputByte.size}${resources.getString(R.string.message_comment_max)}"
        _CommentInputCountText.text = text
    }

    /**
     * 화면 초기화
     */
    private fun clearScreenData()
    {
        hasStudentComment = false
        _CommentEditText.setText("")
        setRegisterButtonVisible(false)
        setUpdateButtonVisible(false)
        setDeleteButtonVisible(false)
    }

    @Optional
    @OnClick(R.id._commentInputLayout, R.id._commentRegisterButton, R.id._commentUpdateButton, R.id._commentDeleteButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._commentInputLayout -> CommonUtils.getInstance(mContext).hideKeyboard()
            R.id._commentRegisterButton -> mHomeworkCommentFragmentObserver.onClickRegisterButton(_CommentEditText.text.toString())
            R.id._commentUpdateButton -> mHomeworkCommentFragmentObserver.onClickUpdateButton(_CommentEditText.text.toString())
            R.id._commentDeleteButton -> mHomeworkCommentFragmentObserver.onClickDeleteButton()
        }
    }

    /**
     * EditText TextChange Listener
     */
    private val mEditTextChangeListener = object : TextWatcher
    {
        override fun beforeTextChanged(s : CharSequence?, start : Int, count : Int, after : Int) { }

        override fun onTextChanged(text : CharSequence?, start : Int, before : Int, count : Int)
        {
            // 입력된 글자가 2자 이상인 경우 등록 버튼 활성화
            if (text.toString().length >= 2)
            {
                setRegisterButtonEnable(true)
            }
            else // 2자 미만인 경우 버튼 비활성화
            {
                setRegisterButtonEnable(false)
            }

            val byte = (text.toString()).toByteArray(charset("EUC-KR"))
            if (byte.size > 400)
            {
                // 400바이트 이상 입력한 경우 텍스트 자르기
                val result = text?.dropLast(1)
                _CommentEditText.setText(result)
                _CommentEditText.setSelection(_CommentEditText.text.toString().length)
            }

            setCommentCountText() // 글자 byte 실시간 갱신
        }

        override fun afterTextChanged(text : Editable?)
        {

        }
    }
}