package com.littlefox.app.foxschool.`object`.result.homework

import com.littlefox.app.foxschool.`object`.result.homework.status.HomeworkStatusItemData

class HomeworkStatusBaseResult
{
    private val start_date : String = ""
    private val end_date : String = ""
    private val list : ArrayList<HomeworkStatusItemData>? = null

    fun getStartDate() : String = start_date

    fun getEndDate() : String = end_date

    fun getStudentStatusItemList() : ArrayList<HomeworkStatusItemData>? = list
}