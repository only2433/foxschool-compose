package com.littlefox.app.foxschool.presentation.mvi.login

import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class LoginState(
    val schoolList: List<SchoolItemDataResult> = listOf(),
    val inputEmptyMessage: String = ""
): State
