package com.littlefox.app.foxschool.`object`.data.quiz

import com.littlefox.app.foxschool.`object`.result.quiz.QuizItemResult
import java.util.*

class QuizPhonicsTextData : QuizTextData
{
    private val MAX_ITEM_SIZE : Int = 3
    private var mMakedQuizItemList : MutableList<QuizItemResult> = ArrayList<QuizItemResult>()

    constructor(mQuizIndex : Int, quizItemResultList : ArrayList<QuizItemResult>) : super(mQuizIndex, quizItemResultList[mQuizIndex].getQuestionIndex())
    {
        mMakedQuizItemList = ArrayList<QuizItemResult>(quizItemResultList)
    }

    init {
        val answerItem : QuizItemResult = mMakedQuizItemList.removeAt(mQuizIndex)
        mMakedQuizItemList.shuffle(Random(System.nanoTime()))
        mMakedQuizItemList = mMakedQuizItemList.subList(0, MAX_ITEM_SIZE - 1)
        mMakedQuizItemList.add(0, answerItem)
        for(i in mMakedQuizItemList.indices)
        {
            if(i == 0)
            {
                mExampleList.add(
                    ExampleTextData(
                        mMakedQuizItemList[i].getQuestionIndex(),
                        mMakedQuizItemList[i].getTitle(),
                        true
                    )
                )
            } else
            {
                mExampleList.add(
                    ExampleTextData(
                        mMakedQuizItemList[i].getQuestionIndex(),
                        mMakedQuizItemList[i].getTitle(),
                        false
                    )
                )
            }
        }
        shuffle()
    }
}