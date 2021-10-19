package com.littlefox.app.foxschool.`object`.result.vocabulary

import android.os.Parcel
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.enumerate.VocabularyType
import java.util.*

class VocabularyListItemDataResult : MyVocabularyResult
{
    private val words = ArrayList<VocabularyDataResult>()

    constructor(id : String, name : String, color : String, vocabularyType : VocabularyType) : super(id, name, color, vocabularyType) {}

    constructor(id : String, name : String, vocabularyType : VocabularyType) : super(id, name, vocabularyType) {}

    protected constructor(`in` : Parcel) : super(`in`) {}

    fun wordsList() : ArrayList<VocabularyDataResult> = words
}