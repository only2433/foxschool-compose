package com.littlefox.app.foxschool.presentation.viewmodel.vocabulary

import com.littlefox.app.foxschool.enumerate.VocabularyBottomBarMenu
import com.littlefox.app.foxschool.enumerate.VocabularyTopBarMenu
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class VocabularyEvent : BaseEvent()
{

    data class onClickTopBarMenu(val menu: VocabularyTopBarMenu) : VocabularyEvent()
    data class onClickBottomBarMenu(val menu: VocabularyBottomBarMenu) : VocabularyEvent()
    data class onSelectIntervalSecond(val second: Int) : VocabularyEvent()
    data class onPlayContents(val index: Int) : VocabularyEvent()
    data class onSelectItem(val index: Int) : VocabularyEvent()
    data class onAddContentsInVocabulary(val index: Int) : VocabularyEvent()
}