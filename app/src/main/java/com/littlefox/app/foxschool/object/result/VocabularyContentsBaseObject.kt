package com.littlefox.app.foxschool.`object`.result

import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import java.util.ArrayList

class VocabularyContentsBaseObject : BaseResult()
{
    private val data : ArrayList<VocabularyDataResult> = ArrayList<VocabularyDataResult>()

    fun getData() : ArrayList<VocabularyDataResult>
    {
        return data;
    }

}