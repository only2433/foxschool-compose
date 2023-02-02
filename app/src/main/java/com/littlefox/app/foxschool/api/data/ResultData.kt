package com.littlefox.app.foxschool.api.data

import com.littlefox.app.foxschool.common.Common
import com.littlefox.logmonitor.Log

sealed class ResultData<out T: Any>
{
    data class Success<out T: Any>(val data: T) : ResultData<T>()

    data class Fail(val status: Int, val message: String) : ResultData<Nothing>()
    {
        val isNetworkError : Boolean
            get()
            {
                if(status == Common.FAIL_CODE_NETWORK_NOT_CONNECT)
                {
                    return true
                }
                else
                    return false
            }


        /**
         * Access Token이 없거나 다른 사용자가 사용시 TRUE . 아닐경우 FALSE
         * @return Access Token이 없거나 다른 사용자가 사용시 TRUE . 아닐경우 FALSE
         */
        val isAuthenticationBroken : Boolean
            get()
            {
                if(status == Common.FAIL_CODE_USER_AUTHORIZATION_INVALID_TOKEN
                    || status == Common.FAIL_CODE_USER_AUTHORIZATION_NO_TAKEN
                    || status == Common.FAIL_CODE_USER_EXPIRE_USER
                    || status == Common.FAIL_CODE_USER_PAID_CHANGE
                    || status == Common.FAIL_CODE_USER_DUPLICATE_LOGIN
                )
                {
                    Log.f("Access Token 을 사용할 수 없다.")
                    return true
                }
                else
                {
                    return false
                }
            }

        /**
         * 중복 로그인 상태인지 여부
         * @return TRUE : 중복, FALSE : 중복 아님.
         */
        val isDuplicateLogin : Boolean
            get()
            {
                if(status == Common.FAIL_CODE_USER_DUPLICATE_LOGIN)
                {
                    return true
                }
                else
                    return false
            }
    }

    override fun toString(): String
    {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Fail -> "Fail[data=$message]"
        }
    }
}