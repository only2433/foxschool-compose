package com.littlefox.app.foxschool.presentation.viewmodel.base

open class BaseState(
    val toast: String = "",
    val errorMessage: String = "",
    val successMessage: String = "",
    val isLoading: Boolean = false
)