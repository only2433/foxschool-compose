package com.littlefox.app.foxschool.`object`.data.login

/**
 * 오토 로그인 일때 정보를 지속적으로 저장하기 위해 사용
 * @author 정재현
 */
class UserLoginData
{
    var userID : String = ""
    var userPassword : String = ""
    var userSchoolCode : String = ""

    constructor(userId : String, password : String, schoolCode : String)
    {
        userID = userId
        userPassword = password
        userSchoolCode = schoolCode
    }

}