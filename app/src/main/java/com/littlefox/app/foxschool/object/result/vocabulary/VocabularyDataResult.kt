package com.littlefox.app.foxschool.`object`.result.vocabulary

import android.os.Parcel
import android.os.Parcelable

open class VocabularyDataResult : Parcelable
{
    private var content_id : String = ""
    private var id = ""
    private var text = ""
    private var mean : String = ""
    private var example = ""
    private var sound_url = ""
    private var contentViewSize = 0
    private var isSelected = false

    protected constructor(data : VocabularyDataResult)
    {
        content_id = data.content_id
        id = data.id
        text = data.text
        mean = data.mean
        example = data.example
        sound_url = data.sound_url
    }

    protected constructor(`in` : Parcel)
    {
        content_id = `in`.readString()!!
        id = `in`.readString()!!
        text = `in`.readString()!!
        mean = `in`.readString()!!
        example = `in`.readString()!!
        sound_url = `in`.readString()!!
        contentViewSize = `in`.readInt()
        isSelected = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(content_id)
        dest.writeString(id)
        dest.writeString(text)
        dest.writeString(mean)
        dest.writeString(example)
        dest.writeString(sound_url)
        dest.writeInt(contentViewSize)
        dest.writeByte((if (isSelected) 1 else 0).toByte())
    }

    fun getContentID() : String = content_id

    fun getID() : String = id

    fun getWordText() : String = text

    fun getMeaningText() : String = mean

    fun getExampleText() : String = example

    fun getSoundURL() : String = sound_url

    fun setContentViewSize(size : Int)
    {
        contentViewSize = size;
    }

    fun getContentViewSize() : Int
    {
        return contentViewSize;
    }

    fun setSelected(isSelect : Boolean)
    {
        isSelected = isSelect;
    }

    fun isSelected() : Boolean = isSelected

    override fun describeContents() : Int { return 0 }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<VocabularyDataResult?> = object : Parcelable.Creator<VocabularyDataResult?>
        {
            override fun createFromParcel(`in` : Parcel) : VocabularyDataResult?
            {
                return VocabularyDataResult(`in`)
            }

            override fun newArray(size : Int) : Array<VocabularyDataResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}