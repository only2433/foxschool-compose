package com.littlefox.app.foxschool.dialog.listener

interface PasswordChangeListener
{
    fun onClickChangeButton(oldPassword : String, newPassword : String, confirmPassword : String)
    fun onClickLaterButton()
    fun onClickKeepButton()
}