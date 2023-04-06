package com.littlefox.app.foxschool.`object`.data.player

data class PlayerEndViewData(
    var isEbookAvailable : Boolean = true,
    var isQuizAvailable : Boolean  = true,
    var isVocabularyAvailable : Boolean  = true,
    var isFlashcardAvailable : Boolean  = true,
    var isStarwordsAvailable : Boolean  = true,
    var isCrosswordAvailable: Boolean = true,
    var isTranslateAvailable : Boolean  = true,
    var isNextButtonVisible : Boolean  = false
)
