package com.littlefox.app.foxschool.`object`.result

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult

class VersionBaseObject : BaseResult()
{
    @SerializedName("data")
    private var data : VersionDataResult? = null

    fun getData() : VersionDataResult = data!!
}
