package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInformationContract
{
    interface View : BaseContract.View
    {
        fun setUserInformation(userInformation : LoginInformationResult)
        fun setSwitchAutoLogin(isEnable : Boolean)
        fun setSwitchBioLogin(isEnable : Boolean)
        fun setSwitchPush(isEnable : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickAutoLoginSwitch()
        fun onClickBioLoginSwitch()
        fun onClickPushSwitch()
        fun onClickInfoChange()
        fun onClickPasswordChange()
    }
}