package com.littlefox.app.foxschool.api.base

import com.littlefox.app.foxschool.api.data.ResultData
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import kotlin.random.Random

/**
 * 동일한 에러메세지가 연속적으로 떨어지는 경우
 * 화면에 메세지가 표시되지 않는 현상이 발생하여 Class로 처리
 * equal 부분을 false로 강제 변경
 * (ex)로그인 화면에서 잘못된 input값으로 계속 로그인 시도하는 경우)
 */
data class ErrorResponse(
    val result : ResultData.Fail,
    val code : RequestCode
) {
    override fun equals(other : Any?) : Boolean
    {
        return false
    }

    override fun hashCode() : Int
    {
        return Random.nextInt()
    }
}
