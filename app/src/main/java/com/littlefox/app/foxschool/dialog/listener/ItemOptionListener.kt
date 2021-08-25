package com.littlefox.app.foxschool.dialog.listener

interface ItemOptionListener
{
    fun onClickQuiz()
    fun onClickTranslate()
    fun onClickVocabulary()
    fun onClickBookshelf()
    fun onClickEbook()
    fun onClickGameStarwords()
    fun onClickGameCrossword()
    fun onClickFlashCard()
    fun onClickRecordPlayer()
    fun onErrorMessage(message : String)
}