package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import androidx.lifecycle.LiveData
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.result.main.CompanyInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppUseGuideFactoryViewModel @Inject constructor(): BaseFactoryViewModel()
{
    private lateinit var mContext : Context
    private lateinit var mVersionDataResult : VersionDataResult

    private val _appVersion = SingleLiveEvent<VersionDataResult>()
    val appVersion : LiveData<VersionDataResult> = _appVersion

    private val _companyInformation = SingleLiveEvent<CompanyInformationResult>()
    val companyInformation : LiveData<CompanyInformationResult> = _companyInformation

    override fun init(context : Context)
    {
        mContext = context
        mVersionDataResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_VERSION_INFORMATION, VersionDataResult::class.java) as VersionDataResult
        _appVersion.value = mVersionDataResult

        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        _companyInformation.value = mainInformationResult.getCompanyInformation()
    }

    override fun resume() {}

    override fun pause() {}

    override fun destroy() {}

    override fun setupViewModelObserver() {}

    /** ========== onClick Events ========== */

    /** 서비스 이용약관 클릭 */
    fun onClickTermsOfService()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_POLICY_TERMS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /** 개인정보 처리방침 클릭 */
    fun onClickPrivacyPolicy()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_POLICY_PRIVACY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }
}