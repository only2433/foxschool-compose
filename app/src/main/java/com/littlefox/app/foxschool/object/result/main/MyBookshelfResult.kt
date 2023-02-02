package com.littlefox.app.foxschool.`object`.result.main

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

open class MyBookshelfResult : Parcelable
{
    @SerializedName("id")
    private var id : String             = ""

    @SerializedName("name")
    private var name : String           = ""

    @SerializedName("color")
    private var color : String          = ""

    @SerializedName("contents_count")
    private var contents_count : Int    = 0

    constructor(id : String, name : String, color : String)
    {
        this.id = id
        this.name = name
        this.color = color
    }

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        name = `in`.readString()!!
        color = `in`.readString()!!
        contents_count = `in`.readInt()
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(color)
        dest.writeInt(contents_count)
    }

    override fun describeContents() : Int = 0

    fun getID() : String = id

    fun getName() : String = name

    fun getColor() : String = color

    fun getContentsCount() : Int = contents_count

    fun setContentsCount(count : Int)
    {
        contents_count = count
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<MyBookshelfResult?> = object : Parcelable.Creator<MyBookshelfResult?>
        {
            override fun createFromParcel(`in` : Parcel) : MyBookshelfResult?
            {
                return MyBookshelfResult(`in`)
            }

            override fun newArray(size : Int) : Array<MyBookshelfResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}