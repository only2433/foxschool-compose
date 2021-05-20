package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult

class VersionBaseObject : BaseResult()
{
    private var data : VersionDataResult? = null

    fun getData() : VersionDataResult
    {
        return data!!
    }
}
