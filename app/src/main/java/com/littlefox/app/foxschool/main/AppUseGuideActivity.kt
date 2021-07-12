package com.littlefox.app.foxschool.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.main.CompanyInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.AppUseGuideContract
import com.littlefox.app.foxschool.main.presenter.AppUseGuidePresenter
import com.littlefox.library.view.text.SeparateTextView
import com.ssomai.android.scalablelayout.ScalableLayout
import kotlin.system.exitProcess

class AppUseGuideActivity : BaseActivity(), AppUseGuideContract.View
{
    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._appUseGuideScrollView)
    lateinit var _AppUseGuideScrollView : ScrollView

    @BindView(R.id._menuVersionLayout)
    lateinit var _MenuVersionLayout : ScalableLayout

    @BindView(R.id._versionText)
    lateinit var _VersionText : SeparateTextView

    @BindView(R.id._versionUpdateButton)
    lateinit var _VersionUpdateButton : TextView

    @BindView(R.id._menuServiceLayout)
    lateinit var _MenuServiceLayout : ScalableLayout

    @BindView(R.id._termsOfServiceButton)
    lateinit var _TermsOfServiceButton : ImageView

    @BindView(R.id._termsOfServiceText)
    lateinit var _TermsOfServiceText : TextView

    @BindView(R.id._privacyPolicyButton)
    lateinit var _PrivacyPolicyButton : ImageView

    @BindView(R.id._privacyPolicyText)
    lateinit var _PrivacyPolicyText : TextView

    @BindView(R.id._menuKoreaCompanyInformationLayout)
    lateinit var _MenuKoreaCompanyInformationLayout : ScalableLayout

    @BindView(R.id._companyNameText)
    lateinit var _CompanyNameText : TextView

    @BindViews(
        R.id._ceoTitleText,
        R.id._addressTitleText,
        R.id._companyRegistrationNumberTitleText,
        R.id._reportCompanyTitleText,
        R.id._electronicPublishingCertificationTitleText,
        R.id._phoneTitleText,
        R.id._faxTitleText
    )
    lateinit var _KoreaCompanyInformationTitleTextList : List<@JvmSuppressWildcards TextView>

    @BindViews(
        R.id._ceoNameText,
        R.id._addressNameText,
        R.id._companyRegistrationNumberNameText,
        R.id._reportCompanyNameText,
        R.id._electronicPublishingCertificationNameText,
        R.id._phoneNameText,
        R.id._faxNameText
    )
    lateinit var _KoreaCompanyInformationNameTextList : List<@JvmSuppressWildcards TextView>

    private lateinit var mAppUseGuidePresenter : AppUseGuidePresenter

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_app_use_guide_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_app_use_guide)
        }
        ButterKnife.bind(this)

        mAppUseGuidePresenter = AppUseGuidePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mAppUseGuidePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mAppUseGuidePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mAppUseGuidePresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    override fun initView()
    {
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.text_about_app)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _VersionText.typeface = Font.getInstance(this).getRobotoMedium()
        _VersionUpdateButton.typeface = Font.getInstance(this).getRobotoMedium()
        _TermsOfServiceText.typeface = Font.getInstance(this).getRobotoMedium()
        _PrivacyPolicyText.typeface = Font.getInstance(this).getRobotoMedium()
        _CompanyNameText.typeface = Font.getInstance(this).getRobotoMedium()

        for(titleText in _KoreaCompanyInformationTitleTextList)
        {
            titleText.typeface = Font.getInstance(this).getRobotoMedium()
        }

        for(nameText in _KoreaCompanyInformationNameTextList)
        {
            nameText.typeface = Font.getInstance(this).getRobotoMedium()
        }
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
    /** ========== Init end ========== */

    override fun setCompanyInformationLayout(result : CompanyInformationResult?)
    {
        if (result != null)
        {
            _KoreaCompanyInformationNameTextList[0].text = result.getCEO()
            _KoreaCompanyInformationNameTextList[1].text = result.getAddress()
            _KoreaCompanyInformationNameTextList[2].text = result.getCompanyRegistrationNumber()
            _KoreaCompanyInformationNameTextList[3].text = result.getReportCompany()
            _KoreaCompanyInformationNameTextList[4].text = result.getElectronicPublishingCertification()
            _KoreaCompanyInformationNameTextList[5].text = result.getPhoneNumber()
            _KoreaCompanyInformationNameTextList[6].text = result.getFaxNumber()
        }
    }

    override fun setAppVersion(result : VersionDataResult?)
    {
        _VersionText.setSeparateText(resources.getString(R.string.text_version), " ${CommonUtils.getInstance(this).getPackageVersionName(Common.PACKAGE_NAME)}")
            .setSeparateColor(resources.getColor(R.color.color_444444), resources.getColor(R.color.color_29c8e6))
            .showView()

        if(result!!.isNeedUpdate)
        {
            _VersionUpdateButton.visibility = View.VISIBLE
        }
        else
        {
            _VersionUpdateButton.visibility = View.GONE
        }
    }

    override fun showLoading() { }

    override fun hideLoading() { }

    override fun showSuccessMessage(message : String) { }

    override fun showErrorMessage(message : String) { }

    @Optional
    @OnClick(R.id._closeButtonRect, R.id._termsOfServiceButton, R.id._privacyPolicyButton, R.id._versionUpdateButton)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
            R.id._termsOfServiceButton -> mAppUseGuidePresenter.onClickTermsOfService()
            R.id._privacyPolicyButton -> mAppUseGuidePresenter.onClickPrivacyPolicy()
            R.id._versionUpdateButton -> {
                CommonUtils.getInstance(this@AppUseGuideActivity).startLinkMove(Common.APP_LINK)
                ActivityCompat.finishAffinity(this)
                exitProcess(0)
            }
        }
    }
}