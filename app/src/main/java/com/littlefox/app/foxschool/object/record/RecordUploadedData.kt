package com.littlefox.app.foxschool.`object`.record

import android.os.Parcel
import android.os.Parcelable

class RecordUploadedData : Parcelable
{
    private var class_id = -1

    private var study_date : String = ""

    constructor(class_id : Int, study_date : String)
    {
        this.class_id = class_id
        this.study_date = study_date
    }

    protected constructor(`in` : Parcel)
    {
        class_id = `in`.readInt()
        study_date = `in`.readString()!!
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeInt(class_id)
        dest.writeString(study_date)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    fun getStudyDate() : String
    {
        return study_date;
    }

    companion object
    {
        @JvmField val CREATOR : Parcelable.Creator<RecordUploadedData?> = object : Parcelable.Creator<RecordUploadedData?>
        {
            override fun createFromParcel(`in` : Parcel) : RecordUploadedData?
            {
                return RecordUploadedData(`in`)
            }

            override fun newArray(size : Int) : Array<RecordUploadedData?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}