package com.littlefox.app.foxschool.`object`.data.player

import android.os.Parcel
import android.os.Parcelable

class IntroduceVideoItemData : Parcelable
{
    private var title : String

    private var mp4_url : String

    private var m3u8_url : String


    constructor(title : String, mp4Url : String, m3u8Url : String)
    {
        this.title = title
        mp4_url = mp4Url
        m3u8_url = m3u8Url
    }

    protected constructor(`in` : Parcel)
    {
        title = `in`.readString()!!
        mp4_url = `in`.readString()!!
        m3u8_url = `in`.readString()!!
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(title)
        dest.writeString(mp4_url)
        dest.writeString(m3u8_url)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    companion object
    {
        @JvmField val CREATOR : Parcelable.Creator<IntroduceVideoItemData?> = object : Parcelable.Creator<IntroduceVideoItemData?>
        {
            override fun createFromParcel(`in` : Parcel) : IntroduceVideoItemData?
            {
                return IntroduceVideoItemData(`in`)
            }

            override fun newArray(size : Int) : Array<IntroduceVideoItemData?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}