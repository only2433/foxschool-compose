package com.littlefox.app.foxschool.presentation.viewmodel.base

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel()
{
    protected val _toast = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST)
    val toast: SharedFlow<String> = _toast

    protected val _successMessage = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST)
    val successMessage: SharedFlow<String> = _successMessage

    protected val _errorMessage = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST)
    val errorMessage: SharedFlow<String> = _errorMessage

    protected val _isLoading = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST)
    val isLoading: SharedFlow<Boolean> = _isLoading

    abstract fun init(context : Context)
    abstract fun onHandleViewEvent(event: BaseEvent)
    abstract fun onHandleApiObserver()
    abstract fun resume()
    abstract fun pause()
    abstract fun destroy()

    open fun onDialogClick(eventType : Int) {}
    open fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int){}
}