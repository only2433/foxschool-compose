package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkListItemData

class HomeworkListFragmentObserver : ViewModel()
{
    var onClickBeforeButton = MutableLiveData<Boolean>()
    var onClickAfterButton = MutableLiveData<Boolean>()
    var onClickStudentCommentButton = MutableLiveData<Boolean>()
    var onClickTeacherCommentButton = MutableLiveData<Boolean>()
    var onClickHomeworkItem = MutableLiveData<HomeworkListItemData>()

    fun onClickBeforeButton()
    {
        onClickBeforeButton.value = true
    }

    fun onClickAfterButton()
    {
        onClickAfterButton.value = true
    }

    fun onClickStudentCommentButton()
    {
        onClickStudentCommentButton.value = true
    }

    fun onClickTeacherCommentButton()
    {
        onClickTeacherCommentButton.value = true
    }

    fun onClickHomeworkItem(item : HomeworkListItemData)
    {
        onClickHomeworkItem.value = item
    }
}