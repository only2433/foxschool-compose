package com.littlefox.app.foxschool.adapter.listener

/**
 * 숙제현황 리스트 Listener
 */
interface HomeworkStatusItemListener
{
    fun onClickCheck(count : Int)
    fun onClickShowDetail(index : Int)
    fun onClickHomeworkChecking(index : Int)
}