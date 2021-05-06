package com.littlefox.app.foxschool.`object`.result.vocabulary

class VocabularyDataResult
{
    private val content_id = ""
    private val id = ""
    private val text = ""
    private val mean : String = ""
    private val example = ""
    private val sound_url = ""
    private var contentViewSize = 0
    private var isSelected = false

    fun getContentID() : String
    {
        return content_id;
    }

    fun getID() : String
    {
        return id;
    }

    fun getWordText() : String
    {
        return text;
    }

    fun getMeaningText() : String
    {
        return mean;
    }

    fun getExampleText() : String
    {
        return example;
    }

    fun getSoundURL() : String
    {
        return sound_url;
    }

    fun setContentViewSize(size : Int)
    {
        contentViewSize = size;
    }

    fun getContentViewSize() : Int
    {
        return contentViewSize;
    }

    fun setSelected(isSelect : Boolean)
    {
        isSelected = isSelect;
    }

    fun isSelected() : Boolean
    {
        return isSelected;
    }

}