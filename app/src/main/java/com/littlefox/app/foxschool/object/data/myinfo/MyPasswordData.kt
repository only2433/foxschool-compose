package com.littlefox.app.foxschool.`object`.data.myinfo

/**
 * 비밀번호 변경 데이터
 */
class MyPasswordData
{
    private var password : String = ""
    private var newPassword : String = ""
    private var newPasswordConfirm : String = ""

    fun getPassword() = password
    fun getNewPassword() = newPassword
    fun getNewPasswordConfirm() = newPasswordConfirm

    fun setPassword(password : String)
    {
        this.password = password
    }

    fun setNewPassword(password : String)
    {
        this.newPassword = password
    }

    fun setNewPasswordConfirm(passwordConfirm : String)
    {
        this.newPasswordConfirm = passwordConfirm
    }

    fun isCompletePasswordData() : Boolean
    {
        if (this.password.isNotEmpty() && this.newPassword.isNotEmpty() && this.newPasswordConfirm.isNotEmpty())
        {
            return true
        }
        return false
    }

    fun clearData()
    {
        password = ""
        newPassword = ""
        newPasswordConfirm = ""
    }
}