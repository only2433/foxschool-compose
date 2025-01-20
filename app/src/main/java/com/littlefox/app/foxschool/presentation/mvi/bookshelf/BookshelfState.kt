package com.littlefox.app.foxschool.presentation.mvi.bookshelf

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class BookshelfState(
    val contentsList: ArrayList<ContentsBaseResult> = arrayListOf(),
    val selectCount: Int = 0,
    val title: String = "",
    val isContentsLoading: Boolean = false
) : State