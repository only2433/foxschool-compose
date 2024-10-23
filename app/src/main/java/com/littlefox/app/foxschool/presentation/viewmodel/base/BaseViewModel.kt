package com.littlefox.app.foxschool.presentation.viewmodel.base

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel()
{
    protected val _toast = SingleLiveEvent<String>()
    val toast: LiveData<String> get() = _toast

    protected val _successMessage = SingleLiveEvent<String>()
    val successMessage: LiveData<String> get() = _successMessage

    protected val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    protected val _isLoading = SingleLiveEvent<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    abstract fun init(context : Context)
    abstract fun onHandleViewEvent(event: BaseEvent)
    abstract fun onHandleApiObserver()
    abstract fun resume()
    abstract fun pause()
    abstract fun destroy()

    open fun onDialogClick(eventType : Int) {}
    open fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int){}
}