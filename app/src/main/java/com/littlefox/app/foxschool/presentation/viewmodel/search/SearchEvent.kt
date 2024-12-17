package com.littlefox.app.foxschool.presentation.viewmodel.search

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.SearchType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class SearchEvent: BaseEvent()
{
    data class onClickSearchType(val type: SearchType) : SearchEvent()
    data class onClickSearchExecute(val keyword: String) : SearchEvent()
    data class onClickBottomContentsType(val type: ActionContentsType) : SearchEvent()
    data class onClickThumbnail(val item: ContentsBaseResult) : SearchEvent()
    data class onClickOption(val item: ContentsBaseResult) : SearchEvent()
    data class onAddContentsInBookshelf(val index: Int) : SearchEvent()
}