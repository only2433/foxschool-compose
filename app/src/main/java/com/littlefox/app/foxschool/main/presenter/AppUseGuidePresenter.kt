package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.main.contract.AppUseGuideContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.logmonitor.Log

class AppUseGuidePresenter : AppUseGuideContract.Presenter
{
    private lateinit var mContext : Context
    private lateinit var mAppUseGuideContractView : AppUseGuideContract.View
    private var mVersionDataResult : VersionDataResult? = null

    constructor(context : Context)
    {
        mContext = context
        mAppUseGuideContractView = (mContext as AppUseGuideContract.View).apply {
            initFont()
            initView()
        }
        Log.f("")
        init()
    }

    private fun init()
    {
        mVersionDataResult = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_VERSION_INFORMATION, VersionDataResult::class.java) as VersionDataResult?
        mAppUseGuideContractView.setAppVersion(mVersionDataResult)

        val mainInformationResult : MainInformationResult = CommonUtils.getInstance(mContext).loadMainData()
        mAppUseGuideContractView.setCompanyInformationLayout(mainInformationResult.getCompanyInformation())
    }

    override fun resume() { }

    override fun pause() { }

    override fun destroy() { }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message) { }

    /** ========== onClick Events ========== */

    /** 서비스 이용약관 클릭 */
    override fun onClickTermsOfService()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_POLICY_TERMS)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }

    /** 개인정보 처리방침 클릭 */
    override fun onClickPrivacyPolicy()
    {
        Log.f("")
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.WEBVIEW_POLICY_PRIVACY)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }
}