package com.littlefox.app.foxschool.presentation.mvi.management

import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.presentation.mvi.base.Event

sealed class ManagementMyBooksEvent : Event
{
    data class UpdateData(val data: ManagementBooksData) : ManagementMyBooksEvent()
}