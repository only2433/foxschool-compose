package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.LoginContract
import com.littlefox.app.foxschool.main.presenter.LoginPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

class LoginActivity : BaseActivity(), MessageHandlerCallback, LoginContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._contentsLayout)
    lateinit var _ContentsLayout : ScalableLayout

    @BindView(R.id._inputIdEditBackground)
    lateinit var _inputIdEditBackground : ImageView

    @BindView(R.id._inputIdEditText)
    lateinit var _InputIdEditText : EditText

    @BindView(R.id._inputPasswordEditBackground)
    lateinit var _inputPasswordEditBackground : ImageView

    @BindView(R.id._inputPasswordEditText)
    lateinit var _InputPasswordEditText : EditText

    @BindView(R.id._inputSchoolEditBackground)
    lateinit var _InputSchoolEditBackground : ImageView

    @BindView(R.id._inputSchoolLine)
    lateinit var _InputSchoolLine : ImageView

    @BindView(R.id._inputSchoolDeleteButton)
    lateinit var _InputSchoolDeleteButton : ImageView

    @BindView(R.id._inputSchoolEditText)
    lateinit var _InputSchoolEditText : EditText

    @BindView(R.id._autoLoginIcon)
    lateinit var _AutoLoginCheckIcon : ImageView

    @BindView(R.id._autoLoginText)
    lateinit var _AutoLoginText : TextView

    @BindView(R.id._loginButtonText)
    lateinit var _LoginButtonText : TextView

    @BindView(R.id._forgetIDText)
    lateinit var _ForgetIDText : TextView

    @BindView(R.id._forgetDividerLine)
    lateinit var _ForgetDividerLine : ImageView

    @BindView(R.id._forgetPasswordText)
    lateinit var _ForgetPasswordText : TextView

    @BindView(R.id._onlyWebSignPossibleTitleText)
    lateinit var _OnlyWebSignPossibleTitleText : TextView

    @BindView(R.id._customerCenterInfoText)
    lateinit var _CustomerCenterInfoText : TextView

    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private lateinit var mLoginPresenter : LoginPresenter
    private var isAutoLoginCheck : Boolean  = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).isTabletModel)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_login_tablet)
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_login)
        }
        ButterKnife.bind(this)
        mLoginPresenter = LoginPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mLoginPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mLoginPresenter.pause()
    }

    override fun onStop()
    {
        Log.f("")
        super.onStop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mLoginPresenter.destroy()
    }


    override fun initView()
    {
        TODO("Not yet implemented")
    }

    override fun initFont()
    {
        _InputIdEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _InputPasswordEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _InputSchoolEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _AutoLoginText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _LoginButtonText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _ForgetIDText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _ForgetPasswordText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _OnlyWebSignPossibleTitleText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CustomerCenterInfoText.setTypeface(Font.getInstance(this).getRobotoMedium())
    }

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
        CommonUtils.getInstance(this).showSuccessSnackMessage(
            _MainBaseLayout,
            message,
            Gravity.CENTER
        )
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(
            _MainBaseLayout,
            message,
            Gravity.CENTER
        )
    }

    override fun handlerMessage(message : Message)
    {
        mLoginPresenter.sendMessageEvent(message)
    }

    @OnClick(R.id._autoLoginIcon, R.id._loginButtonText, R.id._forgetIDText, R.id._forgetPasswordText)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._autoLoginIcon ->
            {
                isAutoLoginCheck = !isAutoLoginCheck
                if(isAutoLoginCheck)
                {
                    _AutoLoginCheckIcon.setImageResource(R.drawable.radio_on)
                } else
                {
                    _AutoLoginCheckIcon.setImageResource(R.drawable.radio_off)
                }
                mLoginPresenter.onCheckAutoLogin(isAutoLoginCheck)
            }

            R.id._loginButtonText ->
                mLoginPresenter.onClickLogin(
                    UserLoginData(
                        _InputIdEditText.text.toString().trim(),
                        _InputPasswordEditText.text.toString().trim(),
                        _InputSchoolEditText.text.toString().trim()
                        )
                )
            R.id._forgetIDText -> mLoginPresenter.onClickFindID()
            R.id._forgetPasswordText -> mLoginPresenter.onClickFindPassword()

        }
    }

}
