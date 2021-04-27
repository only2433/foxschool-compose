package com.littlefox.app.foxschool.`object`.result.main

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.enumerate.VocabularyType

class MyVocabularyResult : Parcelable
{
    /**
     * 보케블러리 책장 ID
     */
    private var id : String? = ""
    private var name : String? = ""
    private var color : String? = ""
    private var wordCount : Int = 0

    /**
     * 해당 컨텐츠 ID
     */
    private var contentID : String? = ""
    private var vocabularyType : VocabularyType? = null

    constructor(id : String?, name : String?, color : String?, vocabularyType : VocabularyType?)
    {
        contentID = id
        this.name = name
        this.color = color
        this.vocabularyType = vocabularyType
    }

    constructor(id : String?, name : String?, vocabularyType : VocabularyType?)
    {
        contentID = id
        this.name = name
        this.vocabularyType = vocabularyType
    }

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()
        contentID = `in`.readString()
        name = `in`.readString()
        color = `in`.readString()
        wordCount = `in`.readInt()
        vocabularyType = `in`.readSerializable() as VocabularyType?
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeString(contentID)
        dest.writeString(name)
        dest.writeString(color)
        dest.writeInt(wordCount)
        dest.writeSerializable(vocabularyType)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    fun getID() : String?
    {
        return id;
    }

    fun getContentID() : String?
    {
        return contentID;
    }

    fun getName() : String?
    {
        return name;
    }

    fun getColor() : String?
    {
        return color;
    }

    fun getWordCount() : Int
    {
        return wordCount;
    }

    fun getVocabularyType() : VocabularyType?
    {
        return vocabularyType;
    }

    fun setWordcount(count : Int)
    {
        wordCount = count;
    }

    fun setVocabularyType(type : VocabularyType)
    {
        vocabularyType = type;
    }


    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<MyVocabularyResult?> = object : Parcelable.Creator<MyVocabularyResult?>
        {
            override fun createFromParcel(`in` : Parcel) : MyVocabularyResult?
            {
                return MyVocabularyResult(`in`)
            }

            override fun newArray(size : Int) : Array<MyVocabularyResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}