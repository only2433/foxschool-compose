package com.littlefox.app.foxschool.presentation.mvi.vocabulary

import VocabularySelectData
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class VocabularyState(
    val contentsList: ArrayList<VocabularyDataResult> = arrayListOf(),
    val selectCount: Int = 0,
    val title: String = "",
    val vocabularyType: VocabularyType = VocabularyType.VOCABULARY_CONTENTS,
    val intervalSecond: Int = 2,
    val isContentsLoading: Boolean = false,
    val studyTypeData: VocabularySelectData = VocabularySelectData(),
    val currentPlayingIndex: Int = 0,
    val isPlayingStatus: Boolean = false,
): State
