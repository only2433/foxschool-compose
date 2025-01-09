package com.littlefox.app.foxschool.presentation.mvi.base

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseMVIViewModel<S: State, A: Action, SE: SideEffect>(
    initState: S,
) : ViewModel()
{

    // 이벤트를 받기 위한 Channel
    private val event = Channel<A>()

    // 상태를 유지하기 위한 StateFlow
    val state: StateFlow<S> = event.receiveAsFlow()
        .runningFold(initState, ::reduceState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initState)

    // 이벤트를 통해 최신화된 상태를 반환
    protected abstract suspend fun reduceState(current: S, action: A): S

    abstract fun init(context : Context)
    abstract fun onHandleApiObserver()
    abstract fun resume()
    abstract fun pause()
    abstract fun destroy()

    open fun onBackPressed() {}
    open fun onDialogClick(eventType : Int) {}
    open fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int){}

    // 일회성 이벤트 처리를 위한 Channel
    private val _sideEffect: Channel<SE> = Channel()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun postAction(action: A) {
        viewModelScope.launch {
            event.send(action)
        }
    }

    fun postSideEffect(effect: SE) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }
}