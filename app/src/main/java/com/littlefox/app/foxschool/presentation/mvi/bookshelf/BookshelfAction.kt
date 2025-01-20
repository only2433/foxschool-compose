package com.littlefox.app.foxschool.presentation.mvi.bookshelf

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class BookshelfAction: Action
{
    data class ClickBottomBarMenu(val menu: ContentsListBottomBarMenu) : BookshelfAction()
    data class SelectedItem(val index: Int) : BookshelfAction()
    data class ClickThumbnail(val item: ContentsBaseResult) : BookshelfAction()
    data class ClickOption(val item: ContentsBaseResult) : BookshelfAction()
    data class ClickBottomContentsType(val type: ActionContentsType) : BookshelfAction()
    data class DeleteContentsInBookshelf(val index: Int): BookshelfAction()
}