package com.littlefox.app.foxschool.enumerate

enum class BioCheckResultType
{
    BIO_SUCCESS,            // 사용가능
    BIO_CANT_USE_HARDWARE,  // 기기적으로 사용 불가능
    BIO_UNABLE,             // 설정에서 기능 OFF
    BIO_NONE                // 등록된 생체인증이 없음
}