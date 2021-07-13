package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.enumerate.MyInformationSwitch
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInformationContract
{
    interface View : BaseContract.View
    {
        fun setSwitchAutoLogin(isEnable : Boolean)
        fun setSwitchBioLogin(isEnable : Boolean)
        fun setSwitchPush(isEnable : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun setSwitchState(switch : MyInformationSwitch)
    }
}