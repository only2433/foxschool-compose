package com.littlefox.app.foxschool.presentation.mvi.search

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.SearchType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class SearchAction: Action
{
    data class ClickSearchType(val type: SearchType) : SearchAction()
    data class ClickSearchExecute(val keyword: String) : SearchAction()
    data class ClickBottomContentsType(val type: ActionContentsType) : SearchAction()
    data class ClickThumbnail(val item: ContentsBaseResult) : SearchAction()
    data class ClickOption(val item: ContentsBaseResult) : SearchAction()
    data class AddContentsInBookshelf(val index: Int) : SearchAction()
}