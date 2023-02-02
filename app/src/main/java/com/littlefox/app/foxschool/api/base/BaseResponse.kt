package com.littlefox.app.foxschool.api.base

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.logmonitor.Log

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

}
