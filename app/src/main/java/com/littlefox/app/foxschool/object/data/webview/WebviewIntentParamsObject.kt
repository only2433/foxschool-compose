package com.littlefox.app.foxschool.`object`.data.webview

import android.os.Parcel
import android.os.Parcelable

class WebviewIntentParamsObject : Parcelable
{
    private var mContentID : String = ""
    private var mHomeworkNumber : Int = 0
    constructor(contentID : String, homeworkNumber : Int = 0)
    {
        mContentID = contentID
        mHomeworkNumber = homeworkNumber
    }

    constructor(`in` : Parcel)
    {
        mContentID = `in`.readString()!!
        mHomeworkNumber = `in`.readInt()
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(mContentID)
        dest.writeInt(mHomeworkNumber)
    }

    override fun describeContents() : Int = 0

    fun getContentID() : String = mContentID

    fun getHomeworkNumber() : Int = mHomeworkNumber

    companion object CREATOR : Parcelable.Creator<WebviewIntentParamsObject>
    {
        override fun createFromParcel(parcel : Parcel) : WebviewIntentParamsObject
        {
            return WebviewIntentParamsObject(parcel)
        }

        override fun newArray(size : Int) : Array<WebviewIntentParamsObject?>
        {
            return arrayOfNulls(size)
        }
    }
}