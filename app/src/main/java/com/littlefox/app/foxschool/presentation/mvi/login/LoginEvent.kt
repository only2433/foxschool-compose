package com.littlefox.app.foxschool.presentation.mvi.login

import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event

sealed class LoginEvent: Event
{
    data class NotifySchoolList(val dataList: List<SchoolItemDataResult>): LoginEvent()
}