package com.littlefox.app.foxschool.`object`.result.flashcard

import android.os.Parcel
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult

class FlashCardDataResult : VocabularyDataResult
{
    private var index : Int = 0
    private var cardNumber : Int = 0
    private var isBookmark : Boolean = false
    private var isBackVisible : Boolean = false

    constructor(parcel : Parcel) : super(parcel)

    constructor(data : VocabularyDataResult) : super(data)

    fun getIndex() : Int
    {
        return index
    }

    fun setIndex(index : Int)
    {
        this.index = index
    }

    fun getCardNumber() : Int
    {
        return cardNumber
    }

    fun setCardNumber(cardNumber : Int)
    {
        this.cardNumber = cardNumber
    }

    fun isBookmarked() : Boolean
    {
        return isBookmark
    }

    fun enableBookmark(isEnable : Boolean)
    {
        isBookmark = isEnable
    }

    fun isBackVisible() : Boolean
    {
        return isBackVisible
    }

    fun setBackVisible(isBackVisible : Boolean)
    {
        this.isBackVisible = isBackVisible
    }

}