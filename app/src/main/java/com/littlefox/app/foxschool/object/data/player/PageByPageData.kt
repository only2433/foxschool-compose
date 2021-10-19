package com.littlefox.app.foxschool.`object`.data.player

class PageByPageData
{
    private var startTime : Float   = 0.0f
    private var endTime : Float     = 0.0f
    private var currentIndex : Int  = 0

    constructor(index : Int)
    {
        currentIndex = index
        startTime = 0.0f
        endTime = 0.0f
    }

    fun getCurrentIndex() : Int = currentIndex

    fun getStartTime() : Float = startTime

    fun getEndTime() : Float = endTime

    fun setStartTime(time : Float)
    {
        startTime = time;
    }

    fun setEndTime(time : Float)
    {
        endTime = time;
    }
}