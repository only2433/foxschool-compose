package com.littlefox.app.foxschool.`object`.data.player

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult

class PlayerIntentParamsObject(
    private var mHomeworkNumber : Int = 0,
    private var mPlayInformationList : ArrayList<ContentsBaseResult> = ArrayList()
) : Parcelable
{
    private constructor(`in` : Parcel) : this(
        `in`.readInt(),
        `in`.createTypedArrayList(ContentsBaseResult.CREATOR) ?: ArrayList()
    )

    constructor(playInformationList : ArrayList<ContentsBaseResult>) : this()
    {
        mPlayInformationList = playInformationList
        mHomeworkNumber = 0
    }

    constructor(playInformationList : ArrayList<ContentsBaseResult>, homeworkNumber : Int) : this(playInformationList)
    {
        mHomeworkNumber = homeworkNumber
    }


    fun getPlayerInformationList() : ArrayList<ContentsBaseResult> = mPlayInformationList

    fun getHomeworkNumber() : Int = mHomeworkNumber

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeInt(mHomeworkNumber)
        dest.writeTypedList(mPlayInformationList)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayerIntentParamsObject>
    {
        override fun createFromParcel(`in` : Parcel) : PlayerIntentParamsObject
        {
            return PlayerIntentParamsObject(`in`)
        }

        override fun newArray(size : Int) : Array<PlayerIntentParamsObject?>
        {
            return arrayOfNulls(size)
        }
    }
}