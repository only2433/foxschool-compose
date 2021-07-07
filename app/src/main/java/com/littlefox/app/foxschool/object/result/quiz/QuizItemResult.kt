package com.littlefox.app.foxschool.`object`.result.quiz

import java.util.*

class QuizItemResult
{
    private val number : Int       = -1
    private val text : String         = ""
    private val sound_url : String      = ""
    private val answer : Int           = -1

    private val items = ArrayList<TextExampleObject>()

    // 문제 인덱스
    fun getQuestionIndex() : Int = number

    // 제목
    fun getTitle() : String = text

    // 현재 이미지 리스트에서 정답인 이미지 조각의 Index
    fun getCorrectIndex() : Int = number - 1

    // 사운드 URL
    fun getSoundUrl() : String = sound_url

    // 텍스트 문제 - 정답 인덱스
    fun getTextAnswer() : Int = answer

    // 문제 리스트
    fun getExampleList() : ArrayList<TextExampleObject> = items

    inner class TextExampleObject
    {
        private val number : Int        = -1
        private val text : String       = ""
        private val sound_url : String  = ""

        fun getExampleIndex() : Int         = number
        fun getExampleText() : String       = text
        fun getExampleSoundUrl() : String   = sound_url ?: ""
    }
}