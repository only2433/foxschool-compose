package com.littlefox.app.foxschool.presentation.mvi.management

import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect

sealed class ManagementMyBooksSideEffect : SideEffect(){
    object ShowDeleteBookshelfDialog : ManagementMyBooksSideEffect()
    object ShowDeleteVocabularyDialog : ManagementMyBooksSideEffect()
}