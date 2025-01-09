package com.littlefox.app.foxschool.presentation.mvi.management

import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.presentation.mvi.base.Action

sealed class ManagementMyBooksAction : Action
{
    object CancelDeleteButton: ManagementMyBooksAction()
    data class SelectBooksItem(val color: String) : ManagementMyBooksAction()
    data class SelectSaveButton(val bookName: String) : ManagementMyBooksAction()
    data class UpdateData(val data: ManagementBooksData) : ManagementMyBooksAction()
}