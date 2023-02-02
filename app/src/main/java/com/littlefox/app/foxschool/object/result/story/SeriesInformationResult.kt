package com.littlefox.app.foxschool.`object`.result.story

import android.os.Parcel
import com.google.gson.annotations.SerializedName


class SeriesInformationResult : SeriesBaseResult
{

    @SerializedName("contents_count")
    private val contents_count = -1

    /**
     * 해당 부분은 카테고리별로 묶이는 경우는 데이터가 없을수 있다.
     * @return 컨텐츠의 해당 레벨
     */
    @SerializedName("level")
    private val level = -1

    @SerializedName("is_single")
    private val is_single : String? = ""

    constructor(`in` : Parcel) : super(`in`) {}

    /**
     * 해당 부분은 카테고리별로 묶이는 경우는 데이터가 없을수 있다.
     * @return 컨텐츠가 시리즈인지 단편인지 구분을 위해
     */
    val isSingle : Boolean
        get()
        {
            if(is_single != null)
            {
                if(is_single == "Y")
                    return true
                else
                    return false
            }
            else
                return false
        }

    fun getLevel() : Int = level

    fun getContentsCount() : Int = contents_count

}