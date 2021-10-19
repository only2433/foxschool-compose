package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyListItemDataResult

class VocabularyShelfListItemBaseObject : BaseResult()
{
    private val data : VocabularyListItemDataResult? = null

    fun getData() : VocabularyListItemDataResult? = data!!
}