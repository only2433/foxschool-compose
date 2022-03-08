package com.littlefox.app.foxschool.`object`.result.story

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.TransitionType
import java.io.Serializable

open class SeriesBaseResult : Parcelable
{
    private var id : String = ""
    private var name : String = ""
    private var thumbnail_url : String = ""
    private var colors : ColorData? = null
    private var series : SeriesData? = null
    private var free_single_sort_number : Int = 0
    private var free_series_sort_number : Int = 0
    private var basic_sort_number : Int = 0
    private var seriesType : String? = Common.CONTENT_TYPE_STORY
    private var transitionType : TransitionType = TransitionType.PAIR_IMAGE

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        name = `in`.readString()!!
        seriesType = `in`.readString()
        thumbnail_url = `in`.readString()!!
        transitionType = `in`.readSerializable() as TransitionType
        colors = `in`.readSerializable() as ColorData?
        free_single_sort_number = `in`.readInt()
        free_series_sort_number = `in`.readInt()
        basic_sort_number = `in`.readInt()
        series = `in`.readSerializable() as SeriesData?
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(seriesType)
        dest.writeString(thumbnail_url)
        dest.writeSerializable(transitionType)
        dest.writeSerializable(colors)
        dest.writeInt(free_single_sort_number)
        dest.writeInt(free_series_sort_number)
        dest.writeInt(basic_sort_number)
        dest.writeSerializable(series)
    }

    override fun describeContents() : Int = 0

    fun getDisplayID() : String = id

    fun getSeriesName() : String = name

    fun getSeriesType() : String
    {
        if(seriesType == null)
        {
            return  Common.CONTENT_TYPE_STORY
        }
        return seriesType!!
    }

    fun getThumbnailUrl() : String = thumbnail_url

    fun getTransitionType() : TransitionType = transitionType

    fun getFreeSingleSortNumber() : Int = free_single_sort_number

    fun getFreeSeriesSortNumber() : Int = free_series_sort_number

    fun getBasicSortNumber() : Int = basic_sort_number

    fun setContentsName(name : String)
    {
        this.name = name
    }

    fun setDisplayId(id : String)
    {
        this.id = id
    }

    fun setTransitionType(type : TransitionType)
    {
        transitionType = type
    }

    fun setSeriesType(seriesType : String)
    {
        this.seriesType = seriesType
    }

    val statusBarColor : String
        get()
        {
            if(colors == null)
            {
                return "#1a8ec7"
            }
            else
                return colors!!.status_bar
        }

    val titleColor : String
        get()
        {
            if(colors == null)
            {
               return "#20b1f9"
            }
            else
                return colors!!.title
        }

    val introduction : String
        get()
        {
            if(series == null || series?.introduction == null)
            {
                return ""
            }
            else
                return series!!.introduction
        }

    val categoryData : String
        get()
        {
            if(series == null || series?.categories == null)
            {
                return ""
            }
            else
                return series!!.categories
        }

    inner class ColorData : Serializable
    {
        var status_bar : String = ""
        var title : String  = ""
    }

    inner class SeriesData : Serializable
    {
        var introduction : String  = ""
        var categories : String = ""
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<SeriesBaseResult?> = object : Parcelable.Creator<SeriesBaseResult?>
        {
            override fun createFromParcel(`in` : Parcel) : SeriesBaseResult?
            {
                return SeriesBaseResult(`in`)
            }

            override fun newArray(size : Int) : Array<SeriesBaseResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }




}