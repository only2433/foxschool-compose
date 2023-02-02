package com.littlefox.app.foxschool.api.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.logmonitor.Log
import java.util.LinkedList

abstract class BaseViewModel : ViewModel()
{
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    protected val errorReport = MutableLiveData<Pair<ResultData.Fail, String>>()
    val _errorReport : LiveData<Pair<ResultData.Fail, String>> = errorReport

    private val queueList: LinkedList<QueueData> = LinkedList<QueueData>()
    private var isRunningTask: Boolean = false


    fun setIsLoading(isLoading: Boolean)
    {
        _isLoading.value = isLoading
    }

    fun enqueueCommandStart(code: RequestCode, duration: Long = 0L, vararg objects : Any?)
    {
        if(_isLoading.value == false)
        {
            setIsLoading(true)
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

    open fun pullNext(data: QueueData) {}

    fun enqueueCommandEnd()
    {
        isRunningTask = false

        if(queueList.size > 0)
        {
            pullNext(queueList.poll())
        }
        else
        {
            setIsLoading(false)
        }
    }
}