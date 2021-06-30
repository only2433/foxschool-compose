package com.littlefox.app.foxschool.`object`.data.vocabulary

/**
 * Vocabulary 의 체크박스 버튼의 상태값의 전달을 위해 사용
 */
class VocabularySelectData
{
    private var isSelectAll = false
    private var isSelectedWord = false
    private var isSelectedMeaning = false
    private var isSelectedExample = false

    constructor()
    {
        isSelectAll = true
        isSelectedWord = true
        isSelectedMeaning = true
        isSelectedExample = true
    }

    fun setSelectWord()
    {
        isSelectedWord = !isSelectedWord
    }

    fun setSelectMeaning()
    {
        isSelectedMeaning = !isSelectedMeaning
    }

    fun setSelectExample()
    {
        isSelectedExample = !isSelectedExample
    }

    fun setSelectAll()
    {
        if(isSelectAll)
        {
            isSelectAll = false
            isSelectedWord = false
            isSelectedMeaning = false
            isSelectedExample = false
        }
        else
        {
            isSelectAll = true
            isSelectedWord = true
            isSelectedMeaning = true
            isSelectedExample = true
        }
    }

    fun setData(vocabularySelectData : VocabularySelectData)
    {
        isSelectAll = vocabularySelectData.isSelectAll()
        isSelectedWord = vocabularySelectData.isSelectedWord()
        isSelectedMeaning = vocabularySelectData.isSelectedMeaning()
        isSelectedExample = vocabularySelectData.isSelectedExample()
    }

    fun isSelectAll() : Boolean
    {
        if(isSelectedWord && isSelectedMeaning && isSelectedExample)
        {
            isSelectAll = true
        }
        else
        {
            isSelectAll = false
        }
        return isSelectAll
    }

    fun isSelectedWord() : Boolean
    {
        return isSelectedWord;
    }

    fun isSelectedMeaning() : Boolean
    {
        return isSelectedMeaning;
    }

    fun isSelectedExample() : Boolean
    {
        return isSelectedExample;
    }


}