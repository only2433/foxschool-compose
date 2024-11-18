package com.littlefox.app.foxschool.api.viewmodel.api

import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.data.QueueData
import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.di.FoxSchoolRepository
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.`object`.result.story.StoryCategoryListResult
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CategoryListApiViewModel @Inject constructor(private val repository : FoxSchoolRepository) : BaseApiViewModel()
{
    private val _categoryListData = MutableStateFlow<StoryCategoryListResult?>(null)
    val categoryListData : MutableStateFlow<StoryCategoryListResult?> = _categoryListData

    private suspend fun getCategoryListData(displayID : String)
    {
        val result = repository.getCategoryList(displayID)
        Log.i("result : ${result.toString()}")
        withContext(Dispatchers.Main)
        {
            when(result)
            {
                is ResultData.Success ->
                {
                    val data = result.data as StoryCategoryListResult
                    _categoryListData.value = data
                }
                is ResultData.Fail ->
                {
                    _errorReport.emit(Pair(result, RequestCode.CODE_CATEGORY_LIST))
                }
                else -> {}
            }
        }
        enqueueCommandEnd()
    }

    override fun pullNext(data : QueueData)
    {
        super.pullNext(data)
        when(data.requestCode)
        {
            RequestCode.CODE_CATEGORY_LIST ->
            {
                mJob = viewModelScope.launch {
                    delay(data.duration)
                    getCategoryListData(
                        data.objects[0] as String
                    )
                }
            }
            else -> {}
        }
    }
}