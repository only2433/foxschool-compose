package com.littlefox.app.foxschool.`object`.data.player

import com.littlefox.app.foxschool.enumerate.PlayerPageLineType

data class PageLineData(
    val type: PlayerPageLineType = PlayerPageLineType.NORMAL,
    val pageTagList: List<Int> = listOf()
)