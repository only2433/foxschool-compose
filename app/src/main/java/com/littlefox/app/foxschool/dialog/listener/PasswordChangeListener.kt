package com.littlefox.app.foxschool.dialog.listener

import com.littlefox.app.foxschool.enumerate.PasswordGuideType

interface PasswordChangeListener
{
    fun getScreenType() : PasswordGuideType
    fun checkPassword(password : String, showMessage : Boolean = false) : Boolean
    fun checkNewPasswordAvailable(newPassword : String, showMessage : Boolean = false) : Boolean
    fun checkNewPasswordConfirm(newPassword : String, newPasswordConfirm : String, showMessage : Boolean = false) : Boolean
    fun checkAllAvailable(oldPassword : String, newPassword : String, confirmPassword : String) : Boolean
    fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
    fun onClickLaterButton()
    fun onClickKeepButton()
}