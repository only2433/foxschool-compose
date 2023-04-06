package com.littlefox.app.foxschool.api.base

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.common.Common
import kotlin.random.Random

data class BaseResponse<T>(
    @SerializedName("status")
    val status : Int = -1,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("access_token")
    val access_token : String = "",

    @SerializedName("data")
    val data: T? = null // Getters Setters..
)
{

    val isSuccess: Boolean
        get()
        {
            if(status == Common.SUCCESS_CODE_OK)
            {
                return true
            }
            else
            {
                return false
            }
        }

    /**
     * 통신 응답으로 동일한 메세지가 연속적으로 떨어지는 경우
     * 화면에 메세지가 표시되지 않는 현상이 발생하여 추가된 부분
     * equal 부분을 false로 강제 변경
     * (ex) 로그인 실패, 코멘트 등록, 수정, 삭제 등을 반복적으로 진행했을 때)
     */
    override fun equals(other : Any?) : Boolean
    {
        return false
    }

    override fun hashCode() : Int
    {
        return Random.nextInt()
    }
}
