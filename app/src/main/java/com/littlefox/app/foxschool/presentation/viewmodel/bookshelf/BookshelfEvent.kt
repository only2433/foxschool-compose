package com.littlefox.app.foxschool.presentation.viewmodel.bookshelf

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class BookshelfEvent : BaseEvent()
{
    data class onClickBottomBarMenu(val menu: ContentsListBottomBarMenu) : BookshelfEvent()
    data class onSelectedItem(val index: Int) : BookshelfEvent()
    data class onClickThumbnail(val item: ContentsBaseResult) : BookshelfEvent()
    data class onClickOption(val item: ContentsBaseResult) : BookshelfEvent()
    data class onClickBottomContentsType(val type: ActionContentsType) : BookshelfEvent()
    data class onDeleteContentsInBookshelf(val index: Int): BookshelfEvent()
}