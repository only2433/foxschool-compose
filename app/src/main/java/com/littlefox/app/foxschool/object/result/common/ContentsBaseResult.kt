package com.littlefox.app.foxschool.`object`.result.common

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.common.Common

class ContentsBaseResult  : Parcelable
{
    private var id : String = ""
    private var index = 0
    private var type : String = Common.CONTENT_TYPE_STORY
    private var name : String = ""
    private var sub_name : String = ""
    private var thumbnailUrl : String = ""
    private var service_info : ServiceSupportedTypeResult? = null
    private var user_service_info : ServiceSupportedTypeResult? = null
    private var isSelected = false
    private var isOptionDisable = false


    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        index = `in`.readInt()
        type = `in`.readString()!!
        name = `in`.readString()!!
        sub_name = `in`.readString()!!
        thumbnailUrl = `in`.readString()!!
        isSelected = `in`.readByte().toInt() != 0
        isOptionDisable = `in`.readByte().toInt() != 0
        service_info = `in`.readSerializable() as ServiceSupportedTypeResult
        user_service_info = `in`.readSerializable() as ServiceSupportedTypeResult
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeInt(index)
        dest.writeString(type)
        dest.writeString(name)
        dest.writeString(sub_name)
        dest.writeString(thumbnailUrl)
        dest.writeByte((if(isSelected) 1 else 0).toByte())
        dest.writeByte((if(isOptionDisable) 1 else 0).toByte())
        dest.writeSerializable(service_info)
        dest.writeSerializable(user_service_info)

    }

    override fun describeContents() : Int
    {
        return 0
    }

    fun getID() : String
    {
        return id;
    }

    fun getIndex() : Int
    {
        return index;
    }

    fun getType() : String
    {
        return type;
    }

    fun getName() : String
    {
        return name;
    }

    fun getSubName() : String
    {
        return sub_name;
    }

    fun getThumbnailUrl() : String
    {
        return thumbnailUrl;
    }

    fun getServiceInformation() : ServiceSupportedTypeResult?
    {
        return service_info;
    }

    fun getUserServiceSupportedInformation() : ServiceSupportedTypeResult?
    {
        return user_service_info;
    }

    fun isSelected() : Boolean
    {
        return isSelected;
    }

    fun setSelected(isSelect : Boolean)
    {
        isSelected = isSelect;
    }

    fun isOptionDisable() : Boolean
    {
        return isOptionDisable;
    }

    fun setOptionDisable(isDisable : Boolean)
    {
        isOptionDisable = isDisable;
    }



    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<ContentsBaseResult?> = object : Parcelable.Creator<ContentsBaseResult?>
        {
            override fun createFromParcel(`in` : Parcel) : ContentsBaseResult?
            {
                return ContentsBaseResult(`in`)
            }

            override fun newArray(size : Int) : Array<ContentsBaseResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}