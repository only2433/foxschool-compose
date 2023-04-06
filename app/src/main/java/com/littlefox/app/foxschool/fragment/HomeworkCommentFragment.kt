package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
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
import com.littlefox.app.foxschool.api.viewmodel.factory.StudentHomeworkFactoryViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
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

    @BindView(R.id._commentTeacherMessage)
    lateinit var _CommentTeacherMessage : TextView

    companion object
    {
        private const val DIALOG_COMMENT_DELETE : Int = 10001
    }

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog

    private var mLastClickTime : Long = 0L              // 중복클릭 방지용

    private var isCompleted : Boolean = false           // 최종평가 여부
    private var mComment : String = ""                  // 통신에서 응답받은 학습자/선생님 한마디

    private val factoryViewModel : StudentHomeworkFactoryViewModel by activityViewModels()

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
        _CommentEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && mComment != "")
            {
                setUpdateButtonEnable(true)
            }
        }
        setRegisterButtonEnable(false)
        setUpdateButtonEnable(false)
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
        // 코멘트 적용
        factoryViewModel.commentData.observe(viewLifecycleOwner, { comment ->
            mComment = comment
        })

        // 페이지 세팅
        factoryViewModel.settingCommentPage.observe(viewLifecycleOwner, { pair ->
            clearScreenData() // 화면 초기화
            isCompleted = pair.second
            settingPageType(pair.first)
        })
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
            if (CommonUtils.getInstance(mContext).isTeacherMode == false)
            {
                // 학생모드
                if (CommonUtils.getInstance(mContext).checkTablet)
                {
                    boxTop = if (isCompleted == true) 42f else 96f
                    textTop = if (isCompleted == true) 73f else 125f
                }
                else
                {
                    boxTop = if (isCompleted == true) 60f else 140f
                    textTop = if (isCompleted == true) 105f else 195f
                }
            }
            else
            {
                // 선생님모드
                boxTop = if (CommonUtils.getInstance(mContext).checkTablet) 42f else 60f
                textTop = if (CommonUtils.getInstance(mContext).checkTablet) 73f else 105f
            }

            _CommentInputLayout.moveChildView(_CommentBoxImage, boxLeft, boxTop)
            _CommentInputLayout.moveChildView(_CommentEditText, textLeft, textTop)

            setStudentCommentLayout()
        }
        else if (commentType == HomeworkCommentType.COMMENT_TEACHER)
        {
            // 선생님 한마디 화면
            _CommentInputCountText.visibility = View.GONE
            if (CommonUtils.getInstance(mContext).isTeacherMode == false)
            {
                // 학생 모드
                boxTop = if (   CommonUtils.getInstance(mContext).checkTablet) 42f else 60f
                textTop = if (CommonUtils.getInstance(mContext).checkTablet) 73f else 105f
            }
            else
            {
                // 선생님 모드
                _CommentTeacherMessage.visibility = View.VISIBLE
                boxTop = if (CommonUtils.getInstance(mContext).checkTablet) 76f else 116f
                textTop = if (CommonUtils.getInstance(mContext).checkTablet) 106f else 171f
            }

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
        _CommentEditText.addTextChangedListener(mEditTextChangeListener)
        _CommentEditText.setText(mComment)

        _CommentEditText.visibility = View.VISIBLE
        _MainBaseLayout.requestFocus() // EditText 커서 제거하기 위해 사용

        if (isCompleted)
        {
            // 최종평가를 한 경우 코멘트 수정 불가
            _CommentInputCountText.visibility = View.GONE
            setEditTextModifyEnable(false)
            setRegisterButtonVisible(false)
            setUpdateButtonVisible(false)
            setDeleteButtonVisible(false)
            return
        }
        else if (mComment != "")
        {
            // 코멘트가 있는 경우
            setCommentCountText()
            setUpdateButtonEnable(false)
            setRegisterButtonVisible(false)
            setUpdateButtonVisible(true)
            setDeleteButtonVisible(true)
        }
        else
        {
            // 코멘트가 없는 경우
            setRegisterButtonVisible(true)
            setUpdateButtonVisible(false)
            setDeleteButtonVisible(false)
        }
        setEditTextModifyEnable(true)
    }

    /**
     * [학습자 한마디]코멘트 카운트 텍스트 설정
     */
    private fun setCommentCountText()
    {
        // 바이트 사이즈 구하기 위해 코멘트 바이트로 변경
        val inputByte = (_CommentEditText.text.toString()).toByteArray(charset("ms949"))
        val text = "${inputByte.size}/400 byte"
        _CommentInputCountText.text = text
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
        }
        setEditTextModifyEnable(false)
    }

    /**
     * EditText 수정 활성/비활성
     */
    private fun setEditTextModifyEnable(isEnable : Boolean)
    {
        if (isEnable)
        {
            _CommentEditText.setFocusableInTouchMode(true)
        }
        else
        {
            _CommentEditText.run {
                setFocusableInTouchMode(false)
                clearFocus()
            }
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
     * 화면 초기화
     */
    private fun clearScreenData()
    {
        _CommentEditText.setText("")
        _CommentTeacherMessage.visibility = View.GONE
        setRegisterButtonVisible(false)
        setUpdateButtonVisible(false)
        setDeleteButtonVisible(false)
        _CommentEditText.removeTextChangedListener(mEditTextChangeListener)
    }

    /**
     * 코멘트 삭제 확인 다이얼로그
     */
    private fun showCommentDeleteDialog()
    {
        Log.f("")
        mTemplateAlertDialog = TemplateAlertDialog(mContext).apply {
            setMessage(mContext.resources.getString(R.string.message_comment_delete_check))
            setDialogEventType(DIALOG_COMMENT_DELETE)
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogListener(mDialogListener)
            show()
        }

    }

    @Optional
    @OnClick(R.id._commentRegisterButton, R.id._commentUpdateButton, R.id._commentDeleteButton)
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
            R.id._commentRegisterButton -> factoryViewModel.onClickRegisterButton(_CommentEditText.text.toString())
            R.id._commentUpdateButton -> factoryViewModel.onClickUpdateButton(_CommentEditText.text.toString())
            R.id._commentDeleteButton -> showCommentDeleteDialog()
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

            val byte = (text.toString()).toByteArray(charset("ms949"))
            if (byte.size > 400)
            {
                // 400바이트 이상 입력한 경우 텍스트 자르기
                val result = text?.dropLast(1)
                _CommentEditText.setText(result)
                _CommentEditText.setSelection(_CommentEditText.text.toString().length)
            }

            setCommentCountText() // 글자 byte 실시간 갱신
        }

        override fun afterTextChanged(text : Editable?) { }
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
                        factoryViewModel.onClickDeleteButton()
                    }
                }
            }
        }
    }
}