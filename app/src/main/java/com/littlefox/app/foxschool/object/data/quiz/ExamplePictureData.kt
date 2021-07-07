package com.littlefox.app.foxschool.`object`.data.quiz

import android.graphics.Bitmap

class ExamplePictureData
{
    private var mIndex : Int        = 0 // 문항번호는 1번부터 시작이므로 1을 더한다.
    private var mImage : Bitmap?    = null // 해당 이미지
    private var isAnswer : Boolean  = false // 정답 여부

    constructor(index : Int, image : Bitmap?)
    {
        mIndex = index + 1
        mImage = image
    }

    fun getIndex() : Int = mIndex

    fun getImage() : Bitmap? = mImage

    fun isAnswer() : Boolean = isAnswer

    fun setAnswer(isAnswer : Boolean)
    {
        this.isAnswer = isAnswer
    }
}