package com.littlefox.app.foxschool.`object`.data.series

data class SeriesViewData(
    val type : String,
    val seriesLevel : Int,
    val contentsSize : Int,
    val category : String = "",
    val isSingleSeries : Boolean,
    val arLevel : String,
    val introduction : String = ""
)
