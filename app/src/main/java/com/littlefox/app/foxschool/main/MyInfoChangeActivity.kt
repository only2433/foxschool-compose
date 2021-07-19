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
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.MyInfoInputType
import com.littlefox.app.foxschool.main.contract.MyInfoChangeContract
import com.littlefox.app.foxschool.main.presenter.MyInfoChangePresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

class MyInfoChangeActivity : BaseActivity(), MessageHandlerCallback, MyInfoChangeContract.View
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

    @BindView(R.id._myInfoChangeLayout)
    lateinit var _MyInfoChangeLayout : ScalableLayout

    @BindView(R.id._idTitleText)
    lateinit var _IdTitleText : TextView

    @BindView(R.id._idText)
    lateinit var _IdText : TextView

    @BindView(R.id._nameTitleText)
    lateinit var _NameTitleText : TextView

    @BindView(R.id._inputNameEditText)
    lateinit var _InputNameEditText : EditText

    @BindView(R.id._inputNameDeleteButton)
    lateinit var _InputNameDeleteButton : ImageView

    @BindView(R.id._inputNameBg)
    lateinit var _InputNameBg : ImageView

    @BindView(R.id._inputEmailEditText)
    lateinit var _InputEmailEditText : EditText

    @BindView(R.id._inputEmailDeleteButton)
    lateinit var _InputEmailDeleteButton : ImageView

    @BindView(R.id._inputEmailBg)
    lateinit var _InputEmailBg : ImageView

    @BindView(R.id._emailAtText)
    lateinit var _EmailAtText : TextView

    @BindView(R.id._emailEndText)
    lateinit var _EmailEndText : TextView

    @BindView(R.id._inputEndSelectButton)
    lateinit var _InputEndSelectButton : ImageView

    @BindView(R.id._phoneTitleText)
    lateinit var _PhoneTitleText : TextView

    @BindView(R.id._phoneOptionTitleText)
    lateinit var _PhoneOptionTitleText : TextView

    @BindView(R.id._inputPhoneEditText)
    lateinit var _InputPhoneEditText : EditText

    @BindView(R.id._inputPhoneDeleteButton)
    lateinit var _InputPhoneDeleteButton : ImageView

    @BindView(R.id._inputPhoneBg)
    lateinit var _InputPhoneBg : ImageView

    @BindView(R.id._saveButton)
    lateinit var _SaveButton : TextView

    companion object
    {
        private const val MESSAGE_DATA_CHECK_ERROR : Int = 100 // 입력값 유효성 오류
    }

    private lateinit var mMyInfoChangePresenter : MyInfoChangePresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    var mErrorViewHandler : Handler = object : Handler()
    {
        override fun handleMessage(msg : Message)
        {
            when(msg.what)
            {
                MESSAGE_DATA_CHECK_ERROR ->
                {
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
            setContentView(R.layout.activity_my_info_change_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_my_info_change)
        }
        ButterKnife.bind(this)
        mMyInfoChangePresenter = MyInfoChangePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mMyInfoChangePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mMyInfoChangePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mMyInfoChangePresenter.destroy()
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
        _TitleText.text = resources.getString(R.string.text_my_info_change)
        _BackButton.visibility = View.VISIBLE
        _BackButtonRect.visibility = View.VISIBLE

        _InputNameEditText.onFocusChangeListener = mEditFocusListener
        _InputEmailEditText.onFocusChangeListener = mEditFocusListener
        _InputPhoneEditText.onFocusChangeListener = mEditFocusListener
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _IdTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _IdText.typeface = Font.getInstance(this).getRobotoMedium()
        _NameTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _EmailAtText.typeface = Font.getInstance(this).getRobotoRegular()
        _InputNameEditText.typeface = Font.getInstance(this).getRobotoMedium()
        _EmailEndText.typeface = Font.getInstance(this).getRobotoRegular()
        _InputEmailEditText.typeface = Font.getInstance(this).getRobotoMedium()
        _PhoneTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _PhoneOptionTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _InputPhoneEditText.typeface = Font.getInstance(this).getRobotoMedium()
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
        mMyInfoChangePresenter.sendMessageEvent(message)
    }

    /**
     * 화면에 사용자 정보 표시
     */
    override fun setUserInformation(userInformation : LoginInformationResult)
    {
        _IdText.text = userInformation.getUserInformation().getLoginID()
        _InputNameEditText.setText(userInformation.getUserInformation().getName())
        _InputPhoneEditText.setText(userInformation.getUserInformation().getPhone())
    }

    /**
     * 입력값 에러 표시
     */
    override fun showInputError(type : MyInfoInputType, message : String)
    {
        when(type)
        {
            MyInfoInputType.NAME -> _InputNameBg.setBackgroundResource(R.drawable.box_list_error)
            MyInfoInputType.EMAIL -> _InputEmailBg.setBackgroundResource(R.drawable.box_list_error)
            MyInfoInputType.PHONE -> _InputPhoneBg.setBackgroundResource(R.drawable.box_list_error)
        }

        val msg = Message.obtain()
        msg.what = MESSAGE_DATA_CHECK_ERROR
        msg.obj = message
        mErrorViewHandler.sendMessageDelayed(msg, Common.DURATION_NORMAL)
    }

    /**
     * 저장버튼 활성/비활성
     */
    private fun setSaveButtonEnable()
    {
        if (mMyInfoChangePresenter.checkInputData(
                _InputNameEditText.text.toString().trim(),
                _InputEmailEditText.text.toString().trim(),
                _InputPhoneEditText.text.toString().trim()
            ))
        {
            _SaveButton.isEnabled = true
            _SaveButton.setBackgroundResource(R.drawable.round_box_light_blue_84)
        }
        else
        {
            _SaveButton.isEnabled = false
            _SaveButton.setBackgroundResource(R.drawable.round_box_gray_84)
        }
    }

    @Optional
    @OnClick(
        R.id._myInfoChangeLayout, R.id._backButtonRect,
        R.id._inputNameDeleteButton, R.id._inputEmailDeleteButton, R.id._inputPhoneDeleteButton,
        R.id._inputNameBg, R.id._inputEmailBg, R.id._inputPhoneBg,  R.id._saveButton
    )
    fun onClickView(view : View)
    {
        // 입력필드 이외의 영역 탭하면 키보드 닫기
        if (view.id != R.id._inputNameEditText && view.id != R.id._inputEmailEditText && view.id != R.id._inputPhoneEditText &&
            view.id != R.id._inputNameBg && view.id != R.id._inputEmailBg && view.id != R.id._inputPhoneBg)
        {
            CommonUtils.getInstance(this).hideKeyboard()
            _InputNameEditText.clearFocus()
            _InputEmailEditText.clearFocus()
            _InputPhoneEditText.clearFocus()
        }

        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
            R.id._inputNameBg -> CommonUtils.getInstance(this).showKeyboard(_InputNameEditText)
            R.id._inputEmailBg -> CommonUtils.getInstance(this).showKeyboard(_InputEmailEditText)
            R.id._inputPhoneBg -> CommonUtils.getInstance(this).showKeyboard(_InputPhoneEditText)
            R.id._inputNameDeleteButton ->
            {
                _InputNameEditText.text.clear()
                if (!_InputNameEditText.hasFocus()) _InputNameBg.setBackgroundResource(R.drawable.text_box)
                setSaveButtonEnable()
            }
            R.id._inputEmailDeleteButton ->
            {
                _InputEmailEditText.text.clear()
                if (!_InputEmailEditText.hasFocus()) _InputEmailBg.setBackgroundResource(R.drawable.text_box)
                setSaveButtonEnable()
            }
            R.id._inputPhoneDeleteButton ->
            {
                _InputPhoneEditText.text.clear()
                if (!_InputPhoneEditText.hasFocus()) _InputPhoneBg.setBackgroundResource(R.drawable.text_box)
                setSaveButtonEnable()
            }
            R.id._saveButton ->
            {
                CommonUtils.getInstance(this).hideKeyboard()
                mMyInfoChangePresenter.onClickSave(
                    _InputNameEditText.text.toString().trim(),
                    "${_InputEmailEditText.text.trim()}@${_EmailEndText.text}",
                    _InputPhoneEditText.text.toString().trim()
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
                R.id._inputNameEditText ->
                {
                    if (hasFocus)
                    {
                        _InputNameBg.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputNameBg.setBackgroundResource(R.drawable.text_box)
                        mMyInfoChangePresenter.checkNameAvailable(_InputNameEditText.text.toString().trim())
                    }
                }
                R.id._inputEmailEditText ->
                {
                    if (hasFocus)
                    {
                        _InputEmailBg.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputEmailBg.setBackgroundResource(R.drawable.text_box)
                        mMyInfoChangePresenter.checkEmailAvailable(_InputEmailEditText.text.toString().trim())
                    }
                }
                R.id._inputPhoneEditText ->
                {
                    if (hasFocus)
                    {
                        _InputPhoneBg.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputPhoneBg.setBackgroundResource(R.drawable.text_box)
                        mMyInfoChangePresenter.checkPhoneAvailable(_InputPhoneEditText.text.toString().trim(), showMessage = true)
                    }
                }
            }

            setSaveButtonEnable()
        }
    }
}