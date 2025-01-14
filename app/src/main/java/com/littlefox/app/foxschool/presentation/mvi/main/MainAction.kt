package com.littlefox.app.foxschool.presentation.mvi.main

import com.littlefox.app.foxschool.enumerate.DrawerMenu
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent

sealed class MainAction : Action
{
    object AddBookshelf: MainAction()
    object AddVocabulary: MainAction()
    object ClickSearch: MainAction()
    object ClickIACPositiveButton: MainAction()
    object ClickIACCloseButton: MainAction()

    data class ClickIACLink(val articleID: String) : MainAction()
    data class ClickStoryLevelsItem(val seriesInformationResult : SeriesInformationResult) : MainAction()
    data class ClickStoryCategoriesItem(val seriesInformationResult : SeriesInformationResult) : MainAction()
    data class ClickSongCategoriesItem(val seriesInformationResult : SeriesInformationResult) : MainAction()
    data class SettingBookshelf(val item: MyBookshelfResult) : MainAction()
    data class SettingVocabulary(val item: MyVocabularyResult) : MainAction()
    data class EnterBookshelfList(val index: Int) : MainAction()
    data class EnterVocabularyList(val index: Int) : MainAction()
    data class ClickDrawerItem(val menu: DrawerMenu) : MainAction()
}