package com.littlefox.app.foxschool.`object`.data.player

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult

class PlayerIntentParamsObject : Parcelable
{
    private var mPlayInformationList : ArrayList<ContentsBaseResult> = ArrayList()
    private var mHomeworkNumber : Int = 0

    constructor(playInformationList : ArrayList<ContentsBaseResult>)
    {
        mPlayInformationList = playInformationList
    }

    constructor(playInformationList : ArrayList<ContentsBaseResult>, homeworkNumber : Int) : this(playInformationList)
    {
        mHomeworkNumber = homeworkNumber
    }

    protected constructor(`in` : Parcel)
    {
        `in`.readTypedList(mPlayInformationList as List<ContentsBaseResult?>, ContentsBaseResult.CREATOR)
        mHomeworkNumber = `in`.readInt()
    }

    fun getPlayerInformationList() : ArrayList<ContentsBaseResult> = mPlayInformationList

    fun getHomeworkNumber() : Int = mHomeworkNumber

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeTypedList(mPlayInformationList)
        dest.writeInt(mHomeworkNumber)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<PlayerIntentParamsObject?> = object : Parcelable.Creator<PlayerIntentParamsObject?>
        {
            override fun createFromParcel(`in` : Parcel) : PlayerIntentParamsObject?
            {
                return PlayerIntentParamsObject(`in`)
            }

            override fun newArray(size : Int) : Array<PlayerIntentParamsObject?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}