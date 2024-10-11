package com.littlefox.app.foxschool.api.base

import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.LinkedList

abstract class BaseApiViewModel : ViewModel()
{
    private val _isLoading = MutableStateFlow<Pair<RequestCode,Boolean>?>(null)
    val isLoading: MutableStateFlow<Pair<RequestCode,Boolean>?> get() = _isLoading

    protected val _errorReport = MutableSharedFlow<Pair<ResultData.Fail, RequestCode>?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val errorReport: SharedFlow<Pair<ResultData.Fail, RequestCode>?> = _errorReport

    private val queueList: LinkedList<QueueData> = LinkedList<QueueData>()
    private var isRunningTask: Boolean = false

    protected var mJob: Job? = null

    init
    {
        setIsLoading(RequestCode.CODE_DEFAULT, false)
    }

    override fun onCleared()
    {
        mJob?.cancel()
        super.onCleared()
    }

    fun setIsLoading(code: RequestCode, isLoading: Boolean)
    {
        Log.f("code : $code , isLoading : $isLoading")
        _isLoading.value = Pair(code, isLoading)
    }

    fun enqueueCommandStart(code: RequestCode, vararg objects : Any?)
    {
        enqueueCommandStart(code, 0L, *objects)
    }

    fun enqueueCommandStart(code: RequestCode, duration: Long = 0L, vararg objects : Any?)
    {
        Log.f("code : $code , _isLoading.value?.second : $_isLoading.value?.second")
        _isLoading.value?.let { data ->
            if(data.second == false)
            {
                setIsLoading(code,true)
            }
        }

        if(isRunningTask)
        {
            Log.f("Running API --- add Queue")
            queueList.add(QueueData(code, duration, *objects))
        }
        else
        {
            if(isRunningTask == false)
            {
                isRunningTask = true
            }

            Log.f("Stop API --- pull Queue")
            if(queueList.size > 0)
            {
                pullNext(queueList.poll())
            }
            else
            {
                pullNext(QueueData(code, duration, *objects))
            }
        }
    }

    fun enqueueCommandEnd()
    {
        isRunningTask = false

        if(queueList.size > 0)
        {
            pullNext(queueList.poll())
        }
        else
        {
            _isLoading.value?.let { data ->
                setIsLoading(data.first,false)
            }

        }
    }

    open fun pullNext(data: QueueData)
    {
        mJob?.cancel()
    }

}