package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.viewmodel.MyInfoShowFragmentDataObserver
import com.littlefox.app.foxschool.viewmodel.MyInfoPresenterDataObserver
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 나의 정보 화면
 * @author 김태은
 */
class MyInfoShowFragment : Fragment()
{
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

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mMyInfoShowFragmentDataObserver : MyInfoShowFragmentDataObserver   // Fragment -> Presenter
    private lateinit var mMyInfoPresenterDataObserver : MyInfoPresenterDataObserver // Presenter -> Fragment

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
            view = inflater.inflate(R.layout.fragment_my_info_show_tablet, container, false)
        } else
        {
            view = inflater.inflate(R.layout.fragment_my_info_show, container, false)
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
        if(CommonUtils.getInstance(mContext).isTeacherMode)
        {
            // 선생님 모드
            _MyInfoMessageLayout.visibility = View.GONE
            _StudentInfoLayout.visibility = View.GONE
            _TeacherInfoLayout.visibility = View.VISIBLE
        }
        else
        {
            // 학생 모드
            _MyInfoMessageLayout.visibility = View.VISIBLE
            _StudentInfoLayout.visibility = View.VISIBLE
            _TeacherInfoLayout.visibility = View.GONE
        }
    }

    private fun initFont()
    {
        // 학생 영역
        _StudentMessageText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StudentIdTitleText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StudentNameTitleText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StudentClassTitleText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _StudentIdText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _StudentNameText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _StudentClassText.typeface = Font.getInstance(mContext).getRobotoMedium()

        // 선생님 영역
        _TeacherIdTitleText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _TeacherNameTitleText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _TeacherIdText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _TeacherNameText.typeface = Font.getInstance(mContext).getRobotoMedium()

        // 버튼 영역
        _ChangeInfoButtonText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _ChangePasswordButtonText.typeface = Font.getInstance(mContext).getRobotoMedium()

        // 설정 영역
        _SettingText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _AutoLoginText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _BioLoginText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _PushText.typeface = Font.getInstance(mContext).getRobotoMedium()
        _BioLoginInfoText.typeface = Font.getInstance(mContext).getRobotoRegular()
        _PushInfoText.typeface = Font.getInstance(mContext).getRobotoRegular()
    }
    /** ========== Init ========== */

    private fun setupObserverViewModel()
    {
        mMyInfoShowFragmentDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(MyInfoShowFragmentDataObserver::class.java)
        mMyInfoPresenterDataObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(MyInfoPresenterDataObserver::class.java)

        // 사용자 정보 화면에 세팅
        mMyInfoPresenterDataObserver.setMyInfoShowFragment.observe(viewLifecycleOwner, { userInfo ->
            setUserInformation(userInfo)
        })

        // 로그인 상태 유지 스위치 상태 변경
        mMyInfoPresenterDataObserver.changeAutoLogin.observe(viewLifecycleOwner, { isEnable ->
            setSwitchAutoLogin(isEnable)
        })

        // 지문인증 로그인 스위치 상태 변경
        mMyInfoPresenterDataObserver.changeBioLogin.observe(viewLifecycleOwner, { isEnable ->
            setSwitchBioLogin(isEnable)
        })

        // 알림 스위치 상태 변경
        mMyInfoPresenterDataObserver.changePush.observe(viewLifecycleOwner, { isEnable ->
            setSwitchPush(isEnable)
        })
    }

    /**
     * 화면에 사용자 정보 표시
     */
    private fun setUserInformation(userInformation : LoginInformationResult)
    {
        if (CommonUtils.getInstance(mContext).isTeacherMode)
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
    private fun setSwitchAutoLogin(isEnable : Boolean)
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
    private fun setSwitchBioLogin(isEnable : Boolean)
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
    private fun setSwitchPush(isEnable : Boolean)
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

    @OnClick(R.id._changeInfoButtonText, R.id._changePasswordButtonText, R.id._switchAutoLogin, R.id._switchBioLogin, R.id._switchPush)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._switchAutoLogin -> mMyInfoShowFragmentDataObserver.onClickAutoLoginSwitch()
            R.id._switchBioLogin -> mMyInfoShowFragmentDataObserver.onClickBioLoginSwitch()
            R.id._switchPush -> mMyInfoShowFragmentDataObserver.onClickPushSwitch()
            R.id._changeInfoButtonText -> mMyInfoShowFragmentDataObserver.onClickInfoChange()
            R.id._changePasswordButtonText -> mMyInfoShowFragmentDataObserver.onClickPasswordChange()
        }
    }
}