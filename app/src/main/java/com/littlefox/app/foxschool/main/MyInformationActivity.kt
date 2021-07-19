package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.MyInformationContract
import com.littlefox.app.foxschool.main.presenter.MyInformationPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

class MyInformationActivity : BaseActivity(), MessageHandlerCallback, MyInformationContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._myInfoMessageLayout)
    lateinit var _MyInfoMessageLayout : ScalableLayout

    /** 학생용 뷰 */
    @BindView(R.id._studentMessageText)
    lateinit var _StudentMessageText : TextView

    @BindView(R.id._studentInfoLayout)
    lateinit var _StudentInfoLayout : ScalableLayout

    @BindView(R.id._studentIdTitleText)
    lateinit var _StudentIdTitleText : TextView

    @BindView(R.id._studentIdText)
    lateinit var _StudentIdText : TextView

    @BindView(R.id._studentNameTitleText)
    lateinit var _StudentNameTitleText : TextView

    @BindView(R.id._studentNameText)
    lateinit var _StudentNameText : TextView

    @BindView(R.id._studentClassTitleText)
    lateinit var _StudentClassTitleText : TextView

    @BindView(R.id._studentClassText)
    lateinit var _StudentClassText : TextView

    /** 선생님용 뷰 */
    @BindView(R.id._teacherInfoLayout)
    lateinit var _TeacherInfoLayout : ScalableLayout

    @BindView(R.id._teacherIdTitleText)
    lateinit var _TeacherIdTitleText : TextView

    @BindView(R.id._teacherIdText)
    lateinit var _TeacherIdText : TextView

    @BindView(R.id._teacherNameTitleText)
    lateinit var _TeacherNameTitleText : TextView

    @BindView(R.id._teacherNameText)
    lateinit var _TeacherNameText : TextView

    /** 버튼 */
    @BindView(R.id._changeInfoButtonText)
    lateinit var _ChangeInfoButtonText : TextView

    @BindView(R.id._changePasswordButtonText)
    lateinit var _ChangePasswordButtonText : TextView

    /** 설정영역 */
    @BindView(R.id._settingText)
    lateinit var _SettingText : TextView

    @BindView(R.id._autoLoginText)
    lateinit var _AutoLoginText : TextView

    @BindView(R.id._bioLoginText)
    lateinit var _BioLoginText : TextView

    @BindView(R.id._bioLoginInfoText)
    lateinit var _BioLoginInfoText : TextView

    @BindView(R.id._pushText)
    lateinit var _PushText : TextView

    @BindView(R.id._pushInfoText)
    lateinit var _PushInfoText : TextView

    @BindView(R.id._switchAutoLogin)
    lateinit var _SwitchAutoLogin : ImageView

    @BindView(R.id._switchBioLogin)
    lateinit var _SwitchBioLogin : ImageView

    @BindView(R.id._switchPush)
    lateinit var _SwitchPush : ImageView

    private lateinit var mMyInformationPresenter : MyInformationPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_my_info_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activitiy_my_info)
        }

        ButterKnife.bind(this)
        mMyInformationPresenter = MyInformationPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mMyInformationPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mMyInformationPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mMyInformationPresenter.destroy()
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
        _TitleText.text = resources.getString(R.string.text_my_info)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE

        if(CommonUtils.getInstance(this).isTeacherMode)
        {
            _StudentMessageText.visibility = View.GONE
            _StudentInfoLayout.visibility = View.GONE
            _TeacherInfoLayout.visibility = View.VISIBLE
        }
        else
        {
            _StudentMessageText.visibility = View.VISIBLE
            _StudentInfoLayout.visibility = View.VISIBLE
            _TeacherInfoLayout.visibility = View.GONE
        }
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()

        // 학생 영역
        _StudentMessageText.typeface = Font.getInstance(this).getRobotoRegular()
        _StudentIdTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _StudentNameTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _StudentClassTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _StudentIdText.typeface = Font.getInstance(this).getRobotoMedium()
        _StudentNameText.typeface = Font.getInstance(this).getRobotoMedium()
        _StudentClassText.typeface = Font.getInstance(this).getRobotoMedium()

        // 선생님 영역
        _TeacherIdTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _TeacherNameTitleText.typeface = Font.getInstance(this).getRobotoRegular()
        _TeacherIdText.typeface = Font.getInstance(this).getRobotoMedium()
        _TeacherNameText.typeface = Font.getInstance(this).getRobotoMedium()

        // 버튼 영역
        _ChangeInfoButtonText.typeface = Font.getInstance(this).getRobotoMedium()
        _ChangePasswordButtonText.typeface = Font.getInstance(this).getRobotoMedium()

        // 설정 영역
        _SettingText.typeface = Font.getInstance(this).getRobotoMedium()
        _AutoLoginText.typeface = Font.getInstance(this).getRobotoMedium()
        _BioLoginText.typeface = Font.getInstance(this).getRobotoMedium()
        _PushText.typeface = Font.getInstance(this).getRobotoMedium()
        _BioLoginInfoText.typeface = Font.getInstance(this).getRobotoRegular()
        _PushInfoText.typeface = Font.getInstance(this).getRobotoRegular()
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
        mMyInformationPresenter.sendMessageEvent(message)
    }

    /**
     * 화면에 사용자 정보 표시
     */
    override fun setUserInformation(userInformation : LoginInformationResult)
    {
        if (CommonUtils.getInstance(this).isTeacherMode)
        {
            // 선생님 화면 세팅
            _TeacherIdText.text = userInformation.getUserInformation().getLoginID()
            _TeacherNameText.text = userInformation.getUserInformation().getName()
        }
        else
        {
            // 학생 화면 세팅
            _StudentIdText.text = userInformation.getUserInformation().getLoginID()
            _StudentNameText.text = userInformation.getUserInformation().getName()
            _StudentClassText.text = userInformation.getSchoolInformation().getClassName()
        }
    }

    /**
     *  자동로그인 스위치 ON/OFF 이미지 변경
     */
    override fun setSwitchAutoLogin(isEnable : Boolean)
    {
        if (isEnable)
        {
            _SwitchAutoLogin.setBackgroundResource(R.drawable.icon_switch_on)
        }
        else
        {
            _SwitchAutoLogin.setBackgroundResource(R.drawable.icon_switch_off)
        }
    }

    /**
     *  지문인증로그인 스위치 ON/OFF 이미지 변경
     */
    override fun setSwitchBioLogin(isEnable : Boolean)
    {
        if (isEnable)
        {
            _SwitchBioLogin.setBackgroundResource(R.drawable.icon_switch_on)
        }
        else
        {
            _SwitchBioLogin.setBackgroundResource(R.drawable.icon_switch_off)
        }
    }

    /**
     *  푸시알림 스위치 ON/OFF 이미지 변경
     */
    override fun setSwitchPush(isEnable : Boolean)
    {
        if (isEnable)
        {
            _SwitchPush.setBackgroundResource(R.drawable.icon_switch_on)
        }
        else
        {
            _SwitchPush.setBackgroundResource(R.drawable.icon_switch_off)
        }
    }

    @Optional
    @OnClick(
        R.id._closeButtonRect, R.id._changeInfoButtonText, R.id._changePasswordButtonText,
        R.id._switchAutoLogin, R.id._switchBioLogin, R.id._switchPush
    )
    fun onClickView(view: View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
            R.id._switchAutoLogin -> mMyInformationPresenter.onClickAutoLoginSwitch()
            R.id._switchBioLogin -> mMyInformationPresenter.onClickBioLoginSwitch()
            R.id._switchPush -> mMyInformationPresenter.onClickPushSwitch()
            R.id._changeInfoButtonText -> mMyInformationPresenter.onClickInfoChange()
            R.id._changePasswordButtonText -> mMyInformationPresenter.onClickPasswordChange()
        }
    }
}