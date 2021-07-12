package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class LoginContract
{
    interface View : BaseContract.View {
        fun setSchoolList(data : ArrayList<SchoolItemDataResult>)
    }
    interface Presenter : BaseContract.Presenter
    {
        fun onCheckAutoLogin(autoLogin : Boolean)
        fun onClickLogin(data : UserLoginData)
        fun onClickFindID()
        fun onClickFindPassword()
    }
}