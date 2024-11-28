package com.littlefox.app.foxschool.`object`.data.quiz

import com.littlefox.app.foxschool.`object`.result.quiz.QuizItemResult
import java.util.*

open class QuizTextData
{
    protected var mQuizIndex : Int              = -1 // 화면에 뿌려주기 위한 퀴즈의 인덱스
    private var mRecordQuizIndex : Int        = -1 // 서버에서 받은 퀴즈순서의 인덱스
    private var mRequestCorrectIndex : Int    = -1 // 서버에서 받은 퀴즈 정답의 인덱스
    protected var mTitle : String               = ""
    private var mMainSoundUrl : String          = ""
    protected var mExampleList : ArrayList<ExampleTextData> = ArrayList<ExampleTextData>()

    constructor(quizIndex : Int, requestQuizIndex : Int)
    {
        mQuizIndex = quizIndex
        mRecordQuizIndex = requestQuizIndex
    }

    constructor(quizIndex : Int, requestQuizIndex : Int, quizItemResult : QuizItemResult)
    {
        mQuizIndex = quizIndex
        mRecordQuizIndex = requestQuizIndex
        mTitle = quizItemResult.getTitle()
        mMainSoundUrl = quizItemResult.getSoundUrl()
        init(quizItemResult)
    }

    private fun init(quizItemResult : QuizItemResult)
    {
        mRequestCorrectIndex = quizItemResult.getTextAnswer()
        for(i in quizItemResult.getExampleList().indices)
        {
            if(mRequestCorrectIndex == quizItemResult.getExampleList()[i].getExampleIndex())
            {
                mExampleList.add(
                    ExampleTextData(
                        quizItemResult.getExampleList()[i].getExampleIndex(),
                        quizItemResult.getExampleList()[i].getExampleText(),
                        quizItemResult.getExampleList()[i].getExampleSoundUrl(),
                        true
                    )
                )
            }
            else
            {
                mExampleList.add(
                    ExampleTextData(
                        quizItemResult.getExampleList()[i].getExampleIndex(),
                        quizItemResult.getExampleList()[i].getExampleText(),
                        quizItemResult.getExampleList()[i].getExampleSoundUrl(),
                        false
                    )
                )
            }
        }
        shuffle()
    }

    fun getQuizIndex() : Int = mQuizIndex + 1

    fun getRecordQuizIndex() : Int = mRecordQuizIndex

    fun getRecordCorrectIndex() : Int = mRequestCorrectIndex

    fun getTitle() : String = mTitle

    fun getExampleList() : ArrayList<ExampleTextData> = mExampleList

    fun getMainSoundUrl() : String = mMainSoundUrl

    fun shuffle()
    {
        mExampleList.shuffle(Random(System.nanoTime()))
    }

    /**
     * 사운드 텍스트 퀴즈의 정답의 서버 인덱스를 알기 위해 사용
     * @return 사운드 텍스트 퀴즈의 정답 순서
     */
    fun getAnswerDataIndex() : Int
    {
        for(i in mExampleList.indices)
        {
            if(mExampleList[i]!!.isAnswer())
            {
                mRequestCorrectIndex = mExampleList[i].getExampleIndex()
            }
        }
        return mRequestCorrectIndex
    }
}