package com.littlefox.app.foxschool.`object`.data.quiz

import java.util.*

class QuizPictureData
{
    private var mCurrentQuizIndex : Int = -1
    private var mTitleText : String?    = ""
    private val mQuizImageList = ArrayList<ExamplePictureData?>()

    /** 퀴즈의 정답 및 문제 번호 : 서버에서 받은 정보, 퀴즈의 순서가 곧 정답의 index 이다. */
    private var mRecordQuizCorrectIndex : Int   = -1

    /** 퀴즈에서 보여지는 오답 문제번호 */
    private var mRecordQuizInCorrectIndex : Int = -1

    constructor(quizIndex : Int, title : String?, firstExample : ExamplePictureData?, secondExample : ExamplePictureData?)
    {
        mCurrentQuizIndex = quizIndex
        mTitleText = title
        mQuizImageList.add(firstExample)
        mQuizImageList.add(secondExample)
        mQuizImageList[0]!!.setAnswer(true)
    }

    /**
     * 현재 화면에 보여질 문제의 번호와 잘못된 문제의 번호 : +1을 하는 이유는 서버에서 1,2,3,4,5 순으로 정보를 주기 때문이다.
     * @param correctIndex 정답의 인덱스
     * @param inCorrectIndex 잘못된 정보의 인덱스
     */
    fun setRecordQuizValue(correctIndex : Int, inCorrectIndex : Int)
    {
        mRecordQuizCorrectIndex = correctIndex + 1
        mRecordQuizInCorrectIndex = inCorrectIndex + 1
    }

    fun getQuizIndex() : Int = mCurrentQuizIndex + 1

    fun getTitle() : String? = mTitleText

    fun getImageInformationList() : ArrayList<ExamplePictureData?> = mQuizImageList

    fun getRecordQuizCorrectIndex() : Int = mRecordQuizCorrectIndex

    fun getRecordQuizInCorrectIndex() : Int = mRecordQuizInCorrectIndex

    /** 이미지객체를 섞는다 */
    fun shuffle()
    {
        mQuizImageList.shuffle(Random(System.nanoTime()))
    }
}