package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInfoChangeContract
{
    interface View : BaseContract.View
    {
        fun setUserInformation(userInformation : LoginInformationResult)
        fun showInputError(type : InputDataType, message : String)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun checkInputData(name : String, email : String, phone : String) : Boolean
        fun checkNameAvailable(name : String, showMessage : Boolean = true) : Boolean
        fun checkEmailAvailable(email : String, showMessage : Boolean = true) : Boolean
        fun checkPhoneAvailable(phone : String, showMessage : Boolean = true) : Boolean
        fun onClickSave(name : String, email : String, phone : String)
    }
}