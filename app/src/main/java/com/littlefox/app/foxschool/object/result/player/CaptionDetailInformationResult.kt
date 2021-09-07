package com.littlefox.app.foxschool.`object`.result.player

class CaptionDetailInformationResult
{
    private var start_time = ""
    private var text = ""
    private var end_time = ""
    private var group_number = 0

    fun getStartTime() : Float
    {
        return java.lang.Float.valueOf(start_time)
    }

    fun getEndTime() : Float
    {
        return java.lang.Float.valueOf(end_time)
    }

    fun getText() : String
    {
        return text;
    }

    fun getPageByPageIndex() : Int
    {
        return group_number;
    }
}