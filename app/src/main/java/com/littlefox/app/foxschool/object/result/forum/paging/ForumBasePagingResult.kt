package com.littlefox.app.foxschool.`object`.result.forum.paging

import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class ForumBasePagingResult(
    @SerializedName("isnew")
    private var isnew : String = "",

    @SerializedName("idx")
    private var idx : Int = 0,

    @SerializedName("title")
    private var title : String = "",

    @SerializedName("regDate")
    private var regDate : String = "",
)
{


    fun isShowNewIcon() : Boolean
    {
        if (isnew == "Y")
        {
            return true
        }
        return false
    }

    fun getForumId() : String = idx.toString()

    fun getTitle() : String = title

    fun getRegisterDate() : String
    {
        val dateFormat : DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val resultFormat : DateFormat = SimpleDateFormat("yyyy.M.d H:mm", Locale.KOREAN)
        val date = dateFormat.parse(regDate)
        return resultFormat.format(date)
    }
}