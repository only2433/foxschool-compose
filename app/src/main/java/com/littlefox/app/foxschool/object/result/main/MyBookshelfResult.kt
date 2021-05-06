package com.littlefox.app.foxschool.`object`.result.main

import android.os.Parcel
import android.os.Parcelable

open class MyBookshelfResult : Parcelable
{
    private var id : String             = ""
    private var name : String           = ""
    private var color : String          = ""
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

    override fun describeContents() : Int
    {
        return 0
    }

    fun getID() : String
    {
        return id;
    }

    fun getName() : String
    {
        return name;
    }

    fun getColor() : String
    {
        return color;
    }

    fun getContentsCount() : Int
    {
        return contents_count;
    }

    fun setCountentsCount(count : Int)
    {
        contents_count = count;
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