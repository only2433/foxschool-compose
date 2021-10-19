package com.littlefox.app.foxschool.`object`.result.content

import com.littlefox.app.foxschool.common.Common
import java.io.Serializable

class ServiceSupportedTypeResult : Serializable
{
    private var story : String              = Common.SERVICE_NOT_SUPPORTED  // 동화 서비스
    private var service : String            = Common.SERVICE_NOT_SUPPORTED
    private var original_text : String      = Common.SERVICE_NOT_SUPPORTED  // 원문 서비스
    private var vocabulary : String         = Common.SERVICE_NOT_SUPPORTED  // 단어장 서비스
    private var quiz : String               = Common.SERVICE_NOT_SUPPORTED  // 퀴즈 서비스
    private var ebook : String              = Common.SERVICE_NOT_SUPPORTED  // ebook 서비스
    private var crossword : String          = Common.SERVICE_NOT_SUPPORTED  // 크로스워드 서비스
    private var starwords : String          = Common.SERVICE_NOT_SUPPORTED  // 스타워즈 서비스
    private var flash_card : String         = Common.SERVICE_NOT_SUPPORTED  // 플래시카드 서비스
    private var record : String             = Common.SERVICE_NOT_SUPPORTED  // 녹음기 서비스

    fun getStorySupportType() : String = story

    fun getServiceSupportType() : String = service

    fun getOriginalTextSupportType() : String = original_text

    fun getVocabularySupportType() : String = vocabulary

    fun getQuizSupportType() : String = quiz

    fun getEbookSupportType() : String = ebook

    fun getCrosswordSupportType() : String = crossword

    fun getStarwordsSupportType() : String = starwords

    fun getFlashcardSupportType() : String = flash_card

    fun getRecorderSupportType() : String = record
}