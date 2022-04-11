package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.result.main.CompanyInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class AppUseGuideContract
{
    interface View : BaseContract.View
    {
        fun setCompanyInformationLayout(result : CompanyInformationResult?)
        fun setAppVersion(result : VersionDataResult?)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickTermsOfService()
        fun onClickPrivacyPolicy()
    }
}