package com.littlefox.app.foxschool.`object`.data.player

/**
 * 플레이어가 끝났을 때 화면에 표시하는 아이콘을 위하여 만든 Builder 클래스
 */
class PlayEndViewSettingData
{
    var isEbookAvailable : Boolean = false
    var isQuizAvailable : Boolean  = false
    var isVocabularyAvailable : Boolean  = false
    var isFlashcardAvailable : Boolean  = false
    var isStarwordAvailable : Boolean  = false
    var isTranslateAvailable : Boolean  = false
    var isNextButtonVisible : Boolean  = false

    constructor(builder : Builder)
    {
        isEbookAvailable = builder.isEbookBuilder
        isQuizAvailable = builder.isQuizBuilder
        isVocabularyAvailable = builder.isVocabularyBuilder
        isFlashcardAvailable = builder.isFlashcardBuilder
        isTranslateAvailable = builder.isTranslateBuilder
        isStarwordAvailable = builder.isStarwordBuilder
        isNextButtonVisible = builder.isNextButtonBuilder
    }

    class Builder
    {
        var isEbookBuilder : Boolean  = false
        var isQuizBuilder : Boolean  = false
        var isVocabularyBuilder : Boolean  = false
        var isFlashcardBuilder : Boolean  = false
        var isStarwordBuilder : Boolean  = false
        var isTranslateBuilder : Boolean  = false
        var isNextButtonBuilder : Boolean  = false

        fun setEbookAvailable(isEnable : Boolean) : Builder
        {
            isEbookBuilder = isEnable
            return this
        }

        fun setQuizAvailable(isEnable : Boolean) : Builder
        {
            isQuizBuilder = isEnable
            return this
        }

        fun setVocabularyAvailable(isEnable : Boolean) : Builder
        {
            isVocabularyBuilder = isEnable
            return this
        }

        fun setFlashcardAvailable(isEnable : Boolean) : Builder
        {
            isFlashcardBuilder = isEnable
            return this
        }

        fun setStarwordAvailable(isEnable : Boolean) : Builder
        {
            isStarwordBuilder = isEnable
            return this
        }

        fun setTranslateAvailable(isEnable : Boolean) : Builder
        {
            isTranslateBuilder = isEnable
            return this
        }

        fun setNextButtonVisible(isEnable : Boolean) : Builder
        {
            isNextButtonBuilder = isEnable
            return this
        }

        fun build() : PlayEndViewSettingData
        {
            return PlayEndViewSettingData(this)
        }
    }


}