package com.littlefox.app.foxschool.presentation.mvi.search

import com.littlefox.app.foxschool.presentation.mvi.base.Event

sealed class SearchEvent: Event
{
    data class EnableContentsLoading(val isLoading : Boolean): SearchEvent()
    data class ExecuteSearching(val type: String, val keyword: String): SearchEvent()
}