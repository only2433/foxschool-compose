package com.littlefox.app.foxschool.`object`.result.version

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.common.Common


class VersionDataResult
{
    @SerializedName("installed_version")
    private var installed_version : String = ""

    @SerializedName("latest_version")
    private var latest_version : String = ""

    @SerializedName("store_url")
    private var store_url : String = ""

    @SerializedName("player_type")
    private var player_type : String = ""

    @SerializedName("is_installed_latest")
    private var is_installed_latest = false

    @SerializedName("force_update")
    private var force_update : Boolean = false

    val isNeedUpdate : Boolean
        get()
        {
            if(is_installed_latest)
            {
                return false
            }
            else
            {
                return true
            }
        }

    /**
     * 해당 기기를 강제로 프로그래시브 모듈로 변경 하기 위해 사용
     * @return TRUE : 프로그래시브 플레이어 , FALSE : 기본 플레이어
     */
    val isForceProgressivePlay : Boolean
        get()
        {
            if(player_type == Common.PLAYER_TYPE_PROGRESSIVE)
                return true
            else
                return false
        }

    fun isForceUpdate() : Boolean = force_update

    fun getStoreUrl() : String = store_url

    fun getCurrentAppVersion() : String = installed_version

    fun getServerAppVersion() : String = latest_version

}