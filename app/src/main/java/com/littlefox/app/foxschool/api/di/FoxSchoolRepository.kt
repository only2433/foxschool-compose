package com.littlefox.app.foxschool.api.di

import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.base.safeApiCall
import javax.inject.Inject

class FoxSchoolRepository @Inject constructor(private val remote: ApiService)
{
    /**
     * 버전 정보
     */
    suspend fun getVersion(deviceID: String, pushAddress: String, pushOn: String) = safeApiCall{
        remote.initAsync(deviceID, pushAddress, pushOn)
    }

    /**
     * 자동 로그인
     */
    suspend fun getAuthMe() = safeApiCall {
        remote.authMeAsync()
    }

    /**
     * 메인 정보
     */
    suspend fun getMain() = safeApiCall {
        remote.mainAsync()
    }

    /**
     * 비밀번호 변경
     */
    suspend fun setChangePassword(currentPassword: String, changePassword: String, changePasswordConfirm: String) = safeApiCall {
        remote.passwordChangeAsync(currentPassword, changePassword, changePasswordConfirm)
    }

    /**
     * 비밀번호 변경 - 다음에 하기
     */
    suspend fun setChangePasswordToDoNext() = safeApiCall {
        remote.passwordChangeNextAsync()
    }

    /**
     * 비밀번호 변경 - 유지 하기
     */
    suspend fun setChangePasswordToKeep() = safeApiCall {
        remote.passwordChangeKeepAsync()
    }
}