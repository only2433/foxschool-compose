package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.record.RecordHistoryResult

class RecordHistoryBaseObject : BaseResult()
{
    private val data : ArrayList<RecordHistoryResult>? = null

    fun getDate() : ArrayList<RecordHistoryResult> = data!!
}