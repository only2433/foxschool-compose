package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class PasswordChangeContract
{
    interface View : BaseContract.View
    {
        fun showInputError(type : InputDataType, message : String)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun checkPassword(password : String)
        fun checkNewPasswordAvailable(newPassword : String)
        fun checkNewPasswordConfirm(newPassword : String, newPasswordConfirm : String)
        fun onClickSave(password : String, newPassword : String, newPasswordConfirm : String)
    }
}