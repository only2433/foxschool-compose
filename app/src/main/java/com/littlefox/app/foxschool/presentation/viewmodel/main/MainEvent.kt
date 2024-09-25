package com.littlefox.app.foxschool.presentation.viewmodel.main

import android.view.View
import com.littlefox.app.foxschool.enumerate.DrawerMenu
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class MainEvent : BaseEvent()
{
    object onAddBookshelf: MainEvent()
    object onAddVocabulary: MainEvent()
    object onClickSearch: MainEvent()

    object onClickIACPositiveButton: MainEvent()
    object onClickIACCloseButton: MainEvent()


    data class onClickIACLink(val articleID: String) : MainEvent()
    data class onClickStoryLevelsItem(val seriesInformationResult : SeriesInformationResult) : MainEvent()
    data class onClickStoryCategoriesItem(val seriesInformationResult : SeriesInformationResult) : MainEvent()
    data class onClickSongCategoriesItem(val seriesInformationResult : SeriesInformationResult) : MainEvent()
    data class onSettingBookshelf(val index: Int) : MainEvent()
    data class onSettingVocabulary(val index: Int) : MainEvent()
    data class onEnterBookshelfList(val index: Int) : MainEvent()
    data class onEnterVocabularyList(val index: Int) : MainEvent()
    data class onClickDrawerItem(val menu: DrawerMenu) : MainEvent()

}