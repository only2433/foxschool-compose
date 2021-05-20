package com.littlefox.app.foxschool.`object`.result.version

import com.littlefox.app.foxschool.common.Common


class VersionDataResult
{
    private var installed_version : String = ""
    private var latest_version : String = ""
    private var store_url : String = ""
    private var player_type : String = ""
    private var is_installed_latest = false
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

    fun isForceUpdate() : Boolean
    {
        return force_update;
    }

    fun getStoreUrl() : String
    {
        return store_url;
    }

    fun getCurrentAppVersion() : String
    {
        return installed_version
    }

    fun getServerAppVersion() : String
    {
        return latest_version;
    }


}