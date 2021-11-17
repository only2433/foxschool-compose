package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.littlefox.app.foxschool.`object`.result.homework.detail.HomeworkDetailItemData

class HomeworkListFragmentObserver : ViewModel()
{
    var onClickStudentCommentButton = MutableLiveData<Boolean>()
    var onClickTeacherCommentButton = MutableLiveData<Boolean>()
    var onClickHomeworkItem = MutableLiveData<HomeworkDetailItemData>()

    fun onClickStudentCommentButton()
    {
        onClickStudentCommentButton.value = true
    }

    fun onClickTeacherCommentButton()
    {
        onClickTeacherCommentButton.value = true
    }

    fun onClickHomeworkItem(item : HomeworkDetailItemData)
    {
        onClickHomeworkItem.value = item
    }
}