package com.littlefox.app.foxschool.`object`.data.series

data class SeriesViewData(
    val type : String = "",
    val seriesLevel : Int = 0,
    val contentsSize : Int = 0,
    val category : String = "",
    val isSingleSeries : Boolean = false,
    val arLevel : String = "",
    val introduction : String = ""
)
