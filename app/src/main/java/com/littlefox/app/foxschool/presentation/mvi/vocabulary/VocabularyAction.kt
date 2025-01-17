package com.littlefox.app.foxschool.presentation.mvi.vocabulary

import com.littlefox.app.foxschool.enumerate.VocabularyBottomBarMenu
import com.littlefox.app.foxschool.enumerate.VocabularyTopBarMenu
import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class VocabularyAction: Action
{
    data class ClickTopBarMenu(val menu: VocabularyTopBarMenu) : VocabularyAction()
    data class ClickBottomBarMenu(val menu: VocabularyBottomBarMenu) : VocabularyAction()
    data class SelectIntervalSecond(val second: Int) : VocabularyAction()
    data class PlayContents(val index: Int) : VocabularyAction()
    data class SelectItem(val index: Int) : VocabularyAction()
    data class AddContentsInVocabulary(val index: Int) : VocabularyAction()
}