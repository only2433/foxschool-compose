package com.littlefox.app.foxschool.`object`.result.login

class SchoolItemDataResult
{
    private var fg_id : String      = ""
    private var group_name : String = ""

    fun getSchoolID() : String
    {
        return fg_id
    }

    fun getSchoolName() : String
    {
        return group_name
    }
}
