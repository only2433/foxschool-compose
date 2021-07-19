package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.MyInfoInputType
import com.littlefox.app.foxschool.enumerate.PasswordChangeInputType
import com.littlefox.app.foxschool.main.contract.PasswordChangeContract
import com.littlefox.app.foxschool.main.presenter.MyInfoChangePresenter
import com.littlefox.app.foxschool.main.presenter.PasswordChangePresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

class PasswordChangeActivity : BaseActivity(), MessageHandlerCallback, PasswordChangeContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._passwordChangeLayout)
    lateinit var _PasswordChangeLayout : ScalableLayout

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

    @BindView(R.id._saveButton)
    lateinit var _SaveButton : TextView

    companion object
    {
        private const val MESSAGE_DATA_CHECK_ERROR : Int = 100
    }

    private lateinit var mPasswordChangePresenter : PasswordChangePresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    var mErrorViewHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_DATA_CHECK_ERROR ->
                {
                    CommonUtils.getInstance(this@PasswordChangeActivity).hideKeyboard()
                    showErrorMessage(msg.obj as String)
                }
            }
        }
    }

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_password_change_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_password_change)
        }
        ButterKnife.bind(this)
        mPasswordChangePresenter = PasswordChangePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mPasswordChangePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mPasswordChangePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mPasswordChangePresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** LifeCycle end **/

    /** Init **/
    override fun initView()
    {
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.text_change_password)
        _BackButton.visibility = View.VISIBLE
        _BackButtonRect.visibility = View.VISIBLE

        _InputPasswordEditText.onFocusChangeListener = mEditFocusListener
        _InputNewPasswordEditText.onFocusChangeListener = mEditFocusListener
        _InputNewPasswordConfirmEditText.onFocusChangeListener = mEditFocusListener
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _InputPasswordEditText.typeface = Font.getInstance(this).getRobotoRegular()
        _InputNewPasswordEditText.typeface = Font.getInstance(this).getRobotoRegular()
        _InputNewPasswordConfirmEditText.typeface = Font.getInstance(this).getRobotoRegular()
        _SaveButton.typeface = Font.getInstance(this).getRobotoMedium()
    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _TitleBaselayout.setBackgroundColor(resources.getColor(backgroundColor))
    }

    /** Init end **/

    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    override fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    override fun handlerMessage(message : Message)
    {
        mPasswordChangePresenter.sendMessageEvent(message)
    }

    /**
     * 입력값 에러 표시
     */
    override fun showInputError(type : PasswordChangeInputType, message : String)
    {
        when(type)
        {
            PasswordChangeInputType.PASSWORD -> _InputPasswordEditBackground.setBackgroundResource(R.drawable.box_list_error)
            PasswordChangeInputType.NEW_PASSWORD -> _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.box_list_error)
            PasswordChangeInputType.NEW_PASSWORD_CONFIRM -> _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.box_list_error)
        }

        val msg = Message.obtain()
        msg.what = MESSAGE_DATA_CHECK_ERROR
        msg.obj = message
        mErrorViewHandler.sendMessageDelayed(msg, Common.DURATION_NORMAL)
    }

    @Optional
    @OnClick(
        R.id._backButtonRect, R.id._saveButton
    )
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
            R.id._saveButton ->
            {
                CommonUtils.getInstance(this).hideKeyboard()
                mPasswordChangePresenter.onClickSave(
                    _InputPasswordEditText.text.toString().trim(),
                    _InputNewPasswordEditText.text.toString().trim(),
                    _InputNewPasswordConfirmEditText.text.toString().trim()
                )
            }
        }
    }

    /**
     * EditText TextChange Listener
     */
    private val mEditFocusListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View?, hasFocus : Boolean)
        {
            when(view?.id)
            {
                R.id._inputPasswordEditText ->
                {
                    if (hasFocus)
                    {
                        _InputPasswordEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputPasswordEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangePresenter.checkPassword(_InputPasswordEditText.text.toString().trim())
                    }
                }
                R.id._inputNewPasswordEditText ->
                {
                    if (hasFocus)
                    {
                        _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputNewPasswordEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangePresenter.checkNewPasswordAvailable(_InputNewPasswordEditText.text.toString().trim())
                    }
                }
                R.id._inputNewPasswordConfirmEditText ->
                {
                    if (hasFocus)
                    {
                        _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputNewPasswordConfirmEditBackground.setBackgroundResource(R.drawable.text_box)
                        mPasswordChangePresenter.checkNewPasswordConfirm(
                            _InputNewPasswordEditText.text.toString().trim(),
                            _InputNewPasswordConfirmEditText.text.toString().trim()
                        )
                    }
                }
            }
        }
    }
}