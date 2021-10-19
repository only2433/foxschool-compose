package com.littlefox.app.foxschool.`object`.data.bookshelf

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.enumerate.MyBooksType


class ManagementBooksData : MyBookshelfResult, Parcelable
{
    private lateinit var booksType : MyBooksType

    constructor(booksType : MyBooksType) : super("", "", "")
    {
        this.booksType = booksType
    }

    constructor(id : String, name : String, color : String, booksType : MyBooksType) : super(id, name, color)
    {
        this.booksType = booksType
    }

    constructor(`in` : Parcel) : super(`in`)
    {
        booksType = MyBooksType.valueOf(`in`.readString()!!)
    }

    fun getBooksType() : MyBooksType = booksType

    override fun describeContents() : Int = 0

    override fun writeToParcel(parcel : Parcel, i : Int)
    {
        super.writeToParcel(parcel, i)
        parcel.writeString(booksType.name)
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<ManagementBooksData?> = object : Parcelable.Creator<ManagementBooksData?>
        {
            override fun createFromParcel(`in` : Parcel) : ManagementBooksData
            {
                return ManagementBooksData(`in`)
            }

            override fun newArray(size : Int) : Array<ManagementBooksData?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}