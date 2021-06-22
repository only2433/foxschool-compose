package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class LoginContract
{
    interface View : BaseContract.View {}
    interface Presenter : BaseContract.Presenter
    {
        fun onCheckAutoLogin(autoLogin : Boolean)
        fun onClickLogin(data : UserLoginData)
        fun onClickFindID()
        fun onClickFindPassword()
    }
}