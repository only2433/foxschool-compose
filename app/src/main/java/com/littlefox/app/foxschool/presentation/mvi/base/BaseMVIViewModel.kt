package com.littlefox.app.foxschool.presentation.mvi.base

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.ResultLauncherCode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseMVIViewModel<S: State, E: Event, SE: SideEffect>(
    initState: S,
) : ViewModel()
{

    // 이벤트를 받기 위한 Channel
    private val _event = Channel<E>()

    // 상태를 유지하기 위한 StateFlow
    val state: StateFlow<S> = _event.receiveAsFlow()
        .runningFold(initState, ::reduceState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initState)

    // 일회성 이벤트 처리를 위한 Channel
    private val _sideEffect: Channel<SE> = Channel()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun postEvent(vararg events: E) {
        viewModelScope.launch {
            for(event in events)
            {
                _event.send(event)
            }
        }
    }

    fun postSideEffect(effect: SE) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }


    abstract fun init(context : Context)
    abstract fun resume()
    abstract fun pause()
    abstract fun destroy()
    abstract fun onHandleApiObserver()
    abstract fun onHandleAction(action : Action)
    // 이벤트를 통해 최신화된 상태를 반환
    protected abstract suspend fun reduceState(current: S, event: E): S


    open fun onBackPressed() {}
    open fun onDialogClick(eventType : Int) {}
    open fun onDialogChoiceClick(buttonType : DialogButtonType, eventType : Int){}

    open fun onAddResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?){}
    open fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray){}
    open fun onActivityResult(code : ResultLauncherCode, intent : Intent?){}

}