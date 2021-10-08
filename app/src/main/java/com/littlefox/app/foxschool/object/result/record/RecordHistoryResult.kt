package com.littlefox.app.foxschool.`object`.result.record

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult

class RecordHistoryResult : Parcelable
{
    private var title : String = ""
    private var date : String = ""
    private var mp3_path : String = ""
    private var thumbnail_url : String = ""
    private var expire : Int = 0

    constructor(title : String, date : String, mp3Path : String, thumbnailUrl : String, expire : Int)
    {
        this.title = title
        this.date = date
        this.mp3_path = mp3Path
        this.thumbnail_url = thumbnailUrl
        this.expire = expire
    }

    protected constructor(`in` : Parcel)
    {
        title = `in`.readString()!!
        date = `in`.readString()!!
        mp3_path = `in`.readString()!!
        thumbnail_url = `in`.readString()!!
        expire = `in`.readInt()
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(title)
        dest.writeString(date)
        dest.writeString(mp3_path)
        dest.writeString(thumbnail_url)
        dest.writeInt(expire)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    fun getTitle() : String
    {
        return title
    }

    fun getDate() : String
    {
        return date
    }

    fun getMp3Path() : String
    {
        return mp3_path
    }

    fun getThumbnailUrl() : String
    {
        return thumbnail_url
    }

    fun getExpire() : Int
    {
        return expire
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<RecordHistoryResult?> = object : Parcelable.Creator<RecordHistoryResult?>
        {
            override fun createFromParcel(`in` : Parcel) : RecordHistoryResult?
            {
                return RecordHistoryResult(`in`)
            }

            override fun newArray(size : Int) : Array<RecordHistoryResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}