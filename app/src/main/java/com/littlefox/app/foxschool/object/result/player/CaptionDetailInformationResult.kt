package com.littlefox.app.foxschool.`object`.result.player

class CaptionDetailInformationResult
{
    private var start_time = ""
    private var text = ""
    private var end_time = ""
    private var group_number = 0

    fun getStartTime() : Float = start_time.toFloat()

    fun getEndTime() : Float = end_time.toFloat()

    fun getText() : String = text

    fun getPageByPageIndex() : Int = group_number
}