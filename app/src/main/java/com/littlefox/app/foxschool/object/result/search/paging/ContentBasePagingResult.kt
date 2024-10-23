package com.littlefox.app.foxschool.`object`.result.search.paging

import ServiceSupportedTypeResult
import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.common.Common


data class ContentBasePagingResult(
    @SerializedName("id")
    val id : String = "",

    @SerializedName("seq")
    val seq : Int = 0,

    @SerializedName("type")
    val type : String = Common.CONTENT_TYPE_STORY,

    @SerializedName("name")
    val name : String = "",

    @SerializedName("sub_name")
    val sub_name : String? = "",

    @SerializedName("thumbnail_url")
    val thumbnail_url : String = "",

    @SerializedName("service_info")
    val service_info : ServiceSupportedTypeResult? = null,

    @SerializedName("story_chk")
    val story_chk : String? = "")
{

    val isStoryViewComplete : Boolean
        get()
        {
            if(story_chk.equals("") == false)
            {
                return true
            }

            return false
        }

    /**
     * 화면에 보일 컨텐츠 이름을 리턴한다. 서브네임이 있을 경우엔 시리즈 명과 같이 노출
     * @return 컨텐츠 네임
     */
    fun getContentsName() : String
    {
        var result : String = ""
        if(sub_name == "")
        {
            result = name
        }
        else
        {
            result = "$name: ${sub_name}"
        }
        return result
    }
}