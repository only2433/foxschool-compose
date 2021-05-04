package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.player.PlayItemResult

class PlayerDataBaseObject : BaseResult()
{
    private val data : PlayItemResult? = null

    fun getData() : PlayItemResult
    {
        return data!!
    }
}