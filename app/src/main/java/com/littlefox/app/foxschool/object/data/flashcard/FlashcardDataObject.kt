package com.littlefox.app.foxschool.`object`.data.flashcard

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.enumerate.VocabularyType

class FlashcardDataObject : Parcelable
{
    private var mContentID : String = ""
    private var mTitleName : String = ""
    private var mTitleSubName : String = ""
    private var mVocabularyType : VocabularyType = VocabularyType.VOCABULARY_CONTENTS
    private var wordList : ArrayList<VocabularyDataResult> = ArrayList()

    constructor(seriesID : String, title : String, subTitle : String, type : VocabularyType, list : ArrayList<VocabularyDataResult>)
    {
        mContentID = seriesID
        mTitleName = title
        mTitleSubName = subTitle
        mVocabularyType = type
        wordList = list
    }

    constructor(seriesID : String, title : String, subTitle : String, type : VocabularyType)
    {
        mContentID = seriesID
        mTitleName = title
        mTitleSubName = subTitle
        mVocabularyType = type
    }

    constructor(`in` : Parcel)
    {
        mContentID = `in`.readString()!!
        mTitleName = `in`.readString()!!
        mTitleSubName = `in`.readString()!!
        mVocabularyType = `in`.readSerializable()!! as VocabularyType
        `in`.readTypedList(wordList as List<VocabularyDataResult?>, VocabularyDataResult.CREATOR)
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(mContentID)
        dest.writeString(mTitleName)
        dest.writeString(mTitleSubName)
        dest.writeSerializable(mVocabularyType)
        dest.writeTypedList(wordList)
    }

    fun getContentID() : String = mContentID

    fun getTitleName() : String = mTitleName

    fun getTitleSubName() : String = mTitleSubName

    fun getVocabularyType() : VocabularyType = mVocabularyType

    fun getWordList() : ArrayList<VocabularyDataResult> = wordList

    fun setVocabularyType(type : VocabularyType)
    {
        mVocabularyType = type
    }

    override fun describeContents() : Int { return 0 }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<FlashcardDataObject?> = object : Parcelable.Creator<FlashcardDataObject?>
        {
            override fun createFromParcel(`in` : Parcel) : FlashcardDataObject?
            {
                return FlashcardDataObject(`in`)
            }

            override fun newArray(size : Int) : Array<FlashcardDataObject?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}