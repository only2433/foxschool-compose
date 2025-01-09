package com.littlefox.app.foxschool.presentation.mvi.management

import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class ManagementMyBooksState(
    val booksData: ManagementBooksData = ManagementBooksData(MyBooksType.BOOKSHELF_ADD),
) : State