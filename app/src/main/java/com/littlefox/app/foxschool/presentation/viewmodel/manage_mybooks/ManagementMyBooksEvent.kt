package com.littlefox.app.foxschool.presentation.viewmodel.manage_mybooks

import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class ManagementMyBooksEvent: BaseEvent()
{
    object onCancelDeleteButton : ManagementMyBooksEvent()
    data class onSelectBooksItem(val color: String) : ManagementMyBooksEvent()
    data class onSelectSaveButton(val bookName: String) : ManagementMyBooksEvent()

}