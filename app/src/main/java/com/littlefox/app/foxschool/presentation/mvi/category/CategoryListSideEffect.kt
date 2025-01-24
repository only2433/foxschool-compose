package com.littlefox.app.foxschool.presentation.mvi.category

import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class CategoryListSideEffect : SideEffect() {
    data class SetStatusBarColor(val color: String): CategoryListSideEffect()
}