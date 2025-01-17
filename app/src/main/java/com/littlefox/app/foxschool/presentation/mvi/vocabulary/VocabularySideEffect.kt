package com.littlefox.app.foxschool.presentation.mvi.vocabulary

import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class VocabularySideEffect: SideEffect()
{
    object ShowContentsDeleteDialog: VocabularySideEffect()
    data class ShowIntervalSelectDialog(val currentIntervalIndex: Int): VocabularySideEffect()
    data class ShowContentsAddDialog(val list: ArrayList<MyVocabularyResult>): VocabularySideEffect()
}