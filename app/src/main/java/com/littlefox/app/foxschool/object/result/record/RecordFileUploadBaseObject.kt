package com.littlefox.app.foxschool.`object`.result.record

import com.littlefox.app.foxschool.`object`.data.record.RecordUploadedData
import com.littlefox.app.foxschool.`object`.result.base.BaseResult

class RecordFileUploadBaseObject : BaseResult()
{
    private val data : RecordUploadedData? = null

    fun getData() : RecordUploadedData? = data
}