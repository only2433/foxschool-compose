package com.littlefox.app.foxschool.`object`.data.crashtics.base

class ErrorRegisterData
{
    private val id : String
    private val name : String
    private val nickName : String
    private val birthYear : String
    private val mobile : String
    private val emailAgree : Boolean
    private val smsAgree : Boolean
    private val gender : String

    constructor(id : String,  name : String,  nickName : String, birthYear : Int, mobile : String, emailAgree : Boolean, smsAgree : Boolean, gender : String)
    {
        this.id = id
        this.name = name
        this.nickName = nickName
        this.birthYear = birthYear.toString()
        this.mobile = mobile
        this.emailAgree = emailAgree
        this.smsAgree = smsAgree
        this.gender = gender
    }

    fun getID() : String = id

    fun getName() : String = name

    fun getNickName() : String = nickName

    fun getBirthYear() : String = birthYear

    fun getMobile() : String = mobile

    fun isEmailAgree() : String
    {
        if(emailAgree == true)
            return "Y"
        else
            return "N"
    }

    fun isSmsAgree() : String
    {
        if(smsAgree == true)
            return "Y"
        else
            return "N"
    }

    fun getGender() : String = gender


}