package com.littlefox.app.foxschool.presentation.mvi.category

import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action


sealed class CategoryListAction: Action
{
    data class ClickContentsItem(val data: SeriesInformationResult): CategoryListAction()
}