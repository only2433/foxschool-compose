package com.littlefox.app.foxschool.`object`.data.record

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult

/**
 * 녹음기 Intent 용 아이템
 */
class  RecordIntentParamsObject : Parcelable
{
    private var id : String = ""
    private var name : String = ""
    private var sub_name : String? = ""
    private var mHomeworkNumber : Int = 0

    constructor(id : String, name : String, sub_name : String)
    {
        this.id = id
        this.name = name
        this.sub_name = sub_name
    }

    constructor(id : String, name : String, sub_name : String, homeworkNo : Int) : this(id, name, sub_name)
    {
        this.mHomeworkNumber = homeworkNo
    }

    constructor(content : ContentsBaseResult)
    {
        this.id = content.getID()
        this.name = content.getName()
        this.sub_name = content.getSubName()
    }

    constructor(content : ContentsBaseResult, homeworkNo : Int) : this(content)
    {
        this.mHomeworkNumber = homeworkNo
    }

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        name = `in`.readString()!!
        sub_name = `in`.readString()!!
        mHomeworkNumber = `in`.readInt()
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(sub_name)
        dest.writeInt(mHomeworkNumber)
    }

    override fun describeContents() : Int = 0

    fun getID() : String = id

    fun getName() : String = name

    fun getSubName() : String
    {
        if(sub_name == null)
        {
            return ""
        }
        return sub_name!!
    }

    fun getHomeworkNumber() : Int = mHomeworkNumber

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<RecordIntentParamsObject?> = object : Parcelable.Creator<RecordIntentParamsObject?>
        {
            override fun createFromParcel(`in` : Parcel) : RecordIntentParamsObject?
            {
                return RecordIntentParamsObject(`in`)
            }

            override fun newArray(size : Int) : Array<RecordIntentParamsObject?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}