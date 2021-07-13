package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInformationContract
{
    interface View : BaseContract.View
    {
        fun setSwitchView(switchPosition : Int, isEnable : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun setSwitchState(switchPosition : Int)
    }
}