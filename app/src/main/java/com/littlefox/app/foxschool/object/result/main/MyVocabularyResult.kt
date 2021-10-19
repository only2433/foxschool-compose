package com.littlefox.app.foxschool.`object`.result.main

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.enumerate.VocabularyType

open class MyVocabularyResult : Parcelable
{
    /**
     * 보케블러리 책장 ID
     */
    private var id : String = ""
    private var name : String = ""
    private var color : String = ""
    private var words_count : Int = 0

    /**
     * 해당 컨텐츠 ID
     */
    private var contentID : String = ""
    private var vocabularyType : VocabularyType = VocabularyType.VOCABULARY_CONTENTS

    constructor(id : String, name : String, color : String, vocabularyType : VocabularyType)
    {
        contentID = id
        this.name = name
        this.color = color
        this.vocabularyType = vocabularyType
    }

    constructor(id : String, name : String, vocabularyType : VocabularyType)
    {
        contentID = id
        this.name = name
        this.vocabularyType = vocabularyType
    }

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        contentID = `in`.readString()!!
        name = `in`.readString()!!
        color = `in`.readString()!!
        words_count = `in`.readInt()
        vocabularyType = `in`.readSerializable() as VocabularyType
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeString(contentID)
        dest.writeString(name)
        dest.writeString(color)
        dest.writeInt(words_count)
        dest.writeSerializable(vocabularyType)
    }

    override fun describeContents() : Int = 0

    fun getID() : String = id

    fun getContentID() : String = contentID

    fun getName() : String = name

    fun getColor() : String = color

    fun getWordCount() : Int = words_count

    fun getVocabularyType() : VocabularyType = vocabularyType

    fun setWordCount(count : Int)
    {
        words_count = count
    }

    fun setVocabularyType(type : VocabularyType)
    {
        vocabularyType = type
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