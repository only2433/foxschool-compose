package com.littlefox.app.foxschool.presentation.mvi.base

import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent

abstract class SideEffect{
    data class ShowToast(val message: String) : SideEffect()
    data class ShowSuccessMessage(val message: String) : SideEffect()
    data class ShowErrorMessage(val message: String) : SideEffect()
    data class EnableLoading(val isLoading: Boolean) : SideEffect()
    object FinishActivity : SideEffect()
}
