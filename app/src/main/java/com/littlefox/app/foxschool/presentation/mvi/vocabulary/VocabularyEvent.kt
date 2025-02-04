package com.littlefox.app.foxschool.presentation.mvi.vocabulary

import VocabularySelectData
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class VocabularyEvent: Event
{
    data class NotifyContentsList(val list: ArrayList<VocabularyDataResult>): VocabularyEvent()
    data class SelectItemCount(val count: Int): VocabularyEvent()
    data class SetTitle(val title: String): VocabularyEvent()
    data class SetVocabularyType(val type: VocabularyType): VocabularyEvent()
    data class ChangeIntervalSecond(val second: Int): VocabularyEvent()
    data class EnableContentsLoading(val isLoading: Boolean): VocabularyEvent()
    data class ChangeStudyDataType(val data: VocabularySelectData): VocabularyEvent()
    data class NotifyCurrentPlayIndex(val index: Int): VocabularyEvent()
    data class EnablePlayStatus(val isPlaying: Boolean): VocabularyEvent()
}