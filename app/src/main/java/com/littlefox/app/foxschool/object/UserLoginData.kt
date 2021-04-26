package com.littlefox.app.foxschool.`object`

/**
 * 오토 로그인 일때 정보를 지속적으로 저장하기 위해 사용
 * @author 정재현
 */
class UserLoginData(userId : String, password : String)
{
    var userID : String = ""
    var userPassword : String = ""

    init
    {
        userID = userId
        userPassword = password
    }


}