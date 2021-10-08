package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeworkCommentFragmentObserver : ViewModel()
{
    var onClickRegisterButton = MutableLiveData<String>()
    var onClickUpdateButton = MutableLiveData<String>()
    var onClickDeleteButton = MutableLiveData<Boolean>()

    fun onClickRegisterButton(comment : String)
    {
        onClickRegisterButton.value = comment
    }

    fun onClickUpdateButton(comment : String)
    {
        onClickUpdateButton.value = comment
    }

    fun onClickDeleteButton()
    {
        onClickDeleteButton.value = true
    }
}