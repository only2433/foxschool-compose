package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInformationContract
{
    interface View : BaseContract.View
    {
        fun setSwitchView(switch : Int, state : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun setSwitchState(switch : Int)
    }
}