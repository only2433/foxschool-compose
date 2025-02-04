package com.littlefox.app.foxschool.presentation.mvi.series_contents_list

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class SeriesContentsListAction: Action
{
    data class ClickBottomBarMenu(val menu: ContentsListBottomBarMenu) : SeriesContentsListAction()
    data class SelectedItem(val index: Int) : SeriesContentsListAction()
    data class ClickThumbnail(val item: ContentsBaseResult) : SeriesContentsListAction()
    data class ClickOption(val item: ContentsBaseResult) : SeriesContentsListAction()
    data class ClickBottomContentsType(val type: ActionContentsType) : SeriesContentsListAction()
    data class AddContentsInBookshelf(val index: Int) : SeriesContentsListAction()
}