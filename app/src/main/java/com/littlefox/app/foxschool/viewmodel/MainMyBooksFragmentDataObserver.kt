package com.littlefox.app.foxschool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class MainMyBooksFragmentDataObserver : ViewModel()
{
    var addBookshelfData = MutableLiveData<Boolean>()
    var addVocabularyData = MutableLiveData<Boolean>()
    var enterBookshelfListData = MutableLiveData<Int>()
    var enterVocabularyListData = MutableLiveData<Int>()
    var settingBookshelfData = MutableLiveData<Int>()
    var settingVocabularyData = MutableLiveData<Int>()

    fun onAddBookshelf()
    {
        addBookshelfData.value = true
    }

    fun onAddVocabulary()
    {
        addVocabularyData.value = true
    }

    fun onEnterBookshelfList(index : Int)
    {
        enterBookshelfListData.value = index
    }

    fun onEnterVocabularyList(index : Int)
    {
        enterVocabularyListData.value = index
    }

    fun onSettingBookshelf(index : Int)
    {
        settingBookshelfData.value = index
    }

    fun onSettingVocabulary(index : Int)
    {
        settingVocabularyData.value = index
    }
}