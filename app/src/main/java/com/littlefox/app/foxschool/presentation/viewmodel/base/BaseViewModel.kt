package com.littlefox.app.foxschool.presentation.viewmodel.base

import android.content.Context
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel()
{
    abstract fun init(context: Context)
    abstract fun onHandleViewEvent(event: BaseEvent)
    abstract fun onHandleApiObserver()
    abstract fun resume()
    abstract fun pause()
    abstract fun destroy()
}