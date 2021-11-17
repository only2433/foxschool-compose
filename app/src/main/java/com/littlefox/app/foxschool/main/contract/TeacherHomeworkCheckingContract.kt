package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.main.contract.base.BaseContract

class TeacherHomeworkCheckingContract
{
    interface View : BaseContract.View
    {
        fun setBeforeData(index : Int, comment : String)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickRegisterButton(index : Int, comment : String)
    }
}