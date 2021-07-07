package com.littlefox.app.foxschool.`object`.data.quiz

class ExampleTextData
{
    private var mExampleIndex : Int = -1 // 서버에서 받은 Example 순서의 index
    private var mText : String      = ""
    private var mSoundUrl : String  = ""
    private var isAnswer : Boolean  = false

    constructor(index : Int, text : String, isAnswer : Boolean)
    {
        mExampleIndex = index
        mText = text
        this.isAnswer = isAnswer
    }

    constructor(index : Int, text : String, soundUrl : String, isAnswer : Boolean)
    {
        mExampleIndex = index
        mText = text
        mSoundUrl = soundUrl
        this.isAnswer = isAnswer
    }

    fun getExampleIndex() : Int = mExampleIndex

    fun getExampleText() : String = mText

    fun getExampleSoundUrl() : String = mSoundUrl

    fun isAnswer() : Boolean = isAnswer
}