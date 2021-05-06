package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult

class VocabularyShelfBaseObject : BaseResult()
{
    private val data : MyVocabularyResult? = null

    fun getData() : MyVocabularyResult
    {
        return data!!;
    }
}