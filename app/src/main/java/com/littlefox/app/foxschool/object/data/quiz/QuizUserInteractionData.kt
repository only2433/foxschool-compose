package com.littlefox.app.foxschool.`object`.data.quiz

class QuizUserInteractionData
{
    private var isCorrect : Boolean         = false // 정답 여부
    private var mQuestionSequence : String  = "" // 퀴즈 아이템 순서
    private var mCorrectNo : Int            = -1 // 정답인 번호
    private var mChosenNo : String          = "" // 사용자가 선택한 번호, 오류 이미지를 사용할 경우 뒤에 r을 붙히기 위해 String

    constructor(isCorrect : Boolean, questionSequence : String, correctNumber : Int, chosenNumber : String)
    {
        this.isCorrect = isCorrect
        mQuestionSequence = questionSequence
        mCorrectNo = correctNumber
        mChosenNo = chosenNumber
    }

    fun isCorrect() : Boolean = isCorrect

    fun getQuestionSequence() : String = mQuestionSequence

    fun getCorrectNumber() : Int = mCorrectNo

    fun getChosenNumber() : String = mChosenNo
}