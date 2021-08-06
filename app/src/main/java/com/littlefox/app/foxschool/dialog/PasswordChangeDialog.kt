package com.littlefox.app.foxschool.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.listener.PasswordChangeListener
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout


/**
 * 비밀번호 변경 안내 다이얼로그
 * @author 김태은
 *
 * 비밀번호 변경 후 90일 이상 경과했을 때 표시되는 다이얼로그
 * 화면 타입이 90일 / 180일 2가지 종류가 있다.
 */
class PasswordChangeDialog : Dialog
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._backgroundLayout)
    lateinit var _BackgroundLayout : ScalableLayout

    @BindView(R.id._passwordChangeLayout)
    lateinit var _PasswordChangeLayout : ScalableLayout

    @BindView(R.id._passwordChangeMessageTextView)
    lateinit var _PasswordChangeMessageTextView : TextView

    @BindView(R.id._inputPasswordEditBackground)
    lateinit var _InputPasswordEditBackground : ImageView

    @BindView(R.id._inputPasswordEditText)
    lateinit var _InputPasswordEditText : EditText

    @BindView(R.id._inputNewPasswordEditBackground)
    lateinit var _InputNewPasswordEditBackground : ImageView

    @BindView(R.id._inputNewPasswordEditText)
    lateinit var _InputNewPasswordEditText : EditText

    @BindView(R.id._inputNewPasswordConfirmEditBackground)
    lateinit var _InputNewPasswordConfirmEditBackground : ImageView

    @BindView(R.id._inputNewPasswordConfirmEditText)
    lateinit var _InputNewPasswordConfirmEditText : EditText

    @BindView(R.id._90buttonLayout)
    lateinit var _90buttonLayout : ScalableLayout

    @BindView(R.id._changeButton90)
    lateinit var _ChangeButton90 : TextView

    @BindView(R.id._laterButton)
    lateinit var _LaterButton : TextView

    @BindView(R.id._180buttonLayout)
    lateinit var _180buttonLayout : ScalableLayout

    @BindView(R.id._changeButton180)
    lateinit var _ChangeButton180 : TextView

    @BindView(R.id._keepButton)
    lateinit var _KeepButton : TextView

    companion object
    {
        private const val MESSAGE_DATA_CHECK_ERROR : Int = 100 // 입력값 유효성 오류
    }
    
    private lateinit var mContext : Context
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mPasswordChangeListener : PasswordChangeListener? = null
    private var mScreenType : PasswordGuideType = PasswordGuideType.CHANGE90
    private var mInputMethodManager : InputMethodManager? = null

    var mErrorViewHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_DATA_CHECK_ERROR ->
                {
                    hideKeyBoard()
                    showErrorMessage(msg.obj.toString())
                }
            }
        }
    }

    constructor(context : Context) : super(context, android.R.style.Theme_Translucent_NoTitleBar)
    {
        if(CommonUtils.getInstance(context).checkTablet)
        {
            setContentView(R.layout.dialog_password_change_tablet)
        } 
        else
        {
            setContentView(R.layout.dialog_password_change)
        }
        ButterKnife.bind(this)
        mContext = context
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
//            getWindow()!!.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            getWindow()!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            getWindow()!!.statusBarColor = mContext.resources.getColor(R.color.color_1fb77c)
        }

        val params : WindowManager.LayoutParams = getWindow()!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.windowAnimations = R.style.DialogScaleAnimation
        getWindow()!!.attributes = params

        initView()
        initFont()
    }

    private fun initView()
    {
        _TitleText.text = mContext.resources.getString(R.string.text_password_change_guide)
        _InputPasswordEditText.onFocusChangeListener = mEditPasswordFocusListener
        _InputNewPasswordEditText.onFocusChangeListener = mEditPasswordFocusListener
        _InputNewPasswordConfirmEditText.onFocusChangeListener = mEditPasswordFocusListener
        _InputNewPasswordConfirmEditText.setOnEditorActionListener(mEditKeyActionListener)
        setScreenTypeView()
    }

    private fun initFont()
    {
        _TitleText.typeface = Font.getInstance(mContext).getRobotoBold()
        _PasswordChangeMessageTextView.typeface = Font.getInstance(mContext).getRobotoMedium()
        _InputPasswordEditText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _InputNewPasswordEditText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _InputNewPasswordConfirmEditText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _ChangeButton90.typeface = Font.getInstance(mContext).getRobotoMedium()
        _ChangeButton180.typeface = Font.getInstance(mContext).getRobotoMedium()
        _LaterButton.typeface = Font.getInstance(mContext).getRobotoMedium()
        _KeepButton.typeface = Font.getInstance(mContext).getRobotoMedium()
    }

    fun setPasswordChangeListener(passwordChangeListener : PasswordChangeListener?)
    {
        mPasswordChangeListener = passwordChangeListener
    }

    fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(
            mContext,
            CommonUtils.getInstance(mContext).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(mContext).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(mContext).showErrorSnackMessage(_MainBaseLayout, message)
    }

    /**
     * 화면 타입에 따른 뷰 세팅
     */
    private fun setScreenTypeView()
    {
        mScreenType = mPasswordChangeListener!!.getScreenType()
        when(mScreenType)
        {
            PasswordGuideType.CHANGE90 ->
            {
                val message = String.format(mContext.resources.getString(R.string.message_password_change), 90)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    _PasswordChangeMessageTextView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
                }
                else
                {
                    _PasswordChangeMessageTextView.setText(Html.fromHtml(message))
                }
                _90buttonLayout.visibility = View.VISIBLE
                _180buttonLayout.visibility = View.GONE
            }
            PasswordGuideType.CHANGE180 ->
            {
                val message = String.format(mContext.resources.getString(R.string.message_password_change), 180)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    _PasswordChangeMessageTextView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
                }
                else
                {
                    _PasswordChangeMessageTextView.setText(Html.fromHtml(message))
                }
                _90buttonLayout.visibility = View.GONE
                _180buttonLayout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 입력값 오류 표시
     */
    fun setInputError(type : InputDataType, message : String)
    {
        when(type)
        {
            InputDataType.PASSWORD -> _InputPasswordEditBackground.setBackgroundResource(R.drawable.box_list_error)
            InputDataType.NEW_PASSWORD -> _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.box_list_error)
            InputDataType.NEW_PASSWORD_CONFIRM -> _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.box_list_error)
        }

        val msg = Message.obtain()
        msg.what = MESSAGE_DATA_CHECK_ERROR
        msg.obj = message
        mErrorViewHandler.sendMessageDelayed(msg, Common.DURATION_NORMAL)
    }

    /**
     * [비밀번호 변경] 버튼 활성/비활성 처리
     */
    fun setChangeButtonEnable(isEnable : Boolean)
    {
        val screenType = mPasswordChangeListener!!.getScreenType()
        when(screenType)
        {
            PasswordGuideType.CHANGE90 ->
            {
                _ChangeButton90.isEnabled = isEnable
                if(isEnable)
                {
                    _ChangeButton90.setBackgroundResource(R.drawable.round_box_light_blue_84)
                }
                else
                {
                    _ChangeButton90.setBackgroundResource(R.drawable.round_box_gray_84)
                }
            }
            PasswordGuideType.CHANGE180 ->
            {
                _ChangeButton180.isEnabled = isEnable
                if(isEnable)
                {
                    _ChangeButton180.setBackgroundResource(R.drawable.round_box_light_blue_84)
                }
                else
                {
                    _ChangeButton180.setBackgroundResource(R.drawable.round_box_gray_84)
                }
            }
        }
    }

    /**
     * 키보드 닫기
     */
    private fun hideKeyBoard()
    {
        mInputMethodManager = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager!!.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    @Optional
    @OnClick(
        R.id._passwordChangeLayout, R.id._90buttonLayout, R.id._180buttonLayout,
        R.id._changeButton90, R.id._changeButton180, R.id._laterButton, R.id._keepButton
    )
    fun onClickView(view : View)
    {
        // 바탕화면 클릭 시 EditText 포커싱 해제하고 키보드 닫기
        if (view.id == R.id._passwordChangeLayout || view.id == R.id._90buttonLayout || view.id == R.id._180buttonLayout)
        {
            hideKeyBoard()
            _InputPasswordEditText.clearFocus()
            _InputNewPasswordEditText.clearFocus()
            _InputNewPasswordConfirmEditText.clearFocus()
        }

        when(view.id)
        {
            // 비밀번호 변경
            R.id._changeButton90, R.id._changeButton180 ->
            {
                mPasswordChangeListener!!.onClickChangeButton(
                    oldPassword = _InputPasswordEditText.text.toString().trim(),
                    newPassword = _InputNewPasswordEditText.text.toString().trim(),
                    confirmPassword = _InputNewPasswordConfirmEditText.text.toString().trim()
                )
            }

            // 나중에 변경
            R.id._laterButton -> mPasswordChangeListener!!.onClickLaterButton()

            // 현재 비밀번호로 유지하기
            R.id._keepButton -> mPasswordChangeListener!!.onClickKeepButton()
        }
    }

    /**
     * EditText TextChange Listener
     */
    private val mEditPasswordFocusListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View?, hasFocus : Boolean)
        {
            when(view?.id)
            { // 기존 비밀번호
                R.id._inputPasswordEditText ->
                {
                    if(hasFocus)
                    {
                        _InputPasswordEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputPasswordEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangeListener!!.checkPassword(
                            _InputPasswordEditText.text.toString().trim(),
                            showMessage = true
                        )
                    }
                }

                // 신규 비밀번호
                R.id._inputNewPasswordEditText ->
                {
                    if(hasFocus)
                    {
                        _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangeListener!!.checkNewPasswordAvailable(
                            _InputNewPasswordEditText.text.toString().trim(),
                            showMessage = true
                        )
                    }
                }

                // 신규 비밀번호 확인
                R.id._inputNewPasswordConfirmEditText ->
                {
                    if(hasFocus)
                    {
                        _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangeListener!!.checkNewPasswordConfirm(
                            _InputNewPasswordEditText.text.toString().trim(),
                            _InputNewPasswordConfirmEditText.text.toString().trim(),
                            showMessage = true
                        )
                    }
                }
            }

            // 포커싱 해제시에만 유효성 체크
            if(hasFocus == false)
            {
                mPasswordChangeListener!!.checkAllAvailable(
                    oldPassword = _InputPasswordEditText.text.toString().trim(),
                    newPassword = _InputNewPasswordEditText.text.toString().trim(),
                    confirmPassword = _InputNewPasswordConfirmEditText.text.toString().trim()
                )
            }
        }
    }

    /**
     * EditText Key Action Listener
     * (키보드 완료 버튼 눌렀을 때 처리)
     */
    private val mEditKeyActionListener = object : TextView.OnEditorActionListener
    {
        override fun onEditorAction(v : TextView?, actionId : Int, event : KeyEvent?) : Boolean
        {
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
                when(v?.id)
                {
                    R.id._inputNewPasswordConfirmEditText ->
                    {
                        hideKeyBoard()
                        _InputNewPasswordConfirmEditText.clearFocus()
                    }
                }
                return true
            }
            return false
        }
    }
}