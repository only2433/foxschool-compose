package com.littlefox.app.foxschool.main.contract

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.littlefox.app.foxschool.`object`.data.vocabulary.VocabularySelectData
import com.littlefox.app.foxschool.adapter.VocabularyItemListAdapter
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.base.BaseContract


class VocabularyContract
{
    interface View : BaseContract.View
    {
        fun setTitle(title : String)
        fun showContentListLoading()
        fun hideContentListLoading()
        fun setBottomWordsActionType(type : VocabularyType)
        fun setBottomIntervalValue(interval : Int)
        fun setBottomPlayItemCount(count : Int)
        fun showListView(adapter : VocabularyItemListAdapter)
        fun checkIconStatusMenu(vocabularySelectData : VocabularySelectData)
        fun setBottomPlayStatus()
        fun setBottomStopStatus()
        fun scrollPosition(position : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickMenuSelectAll()
        fun onClickMenuWord()
        fun onClickMenuMeaning()
        fun onClickMenuExample()
        fun onClickBottomInterval()
        fun onClickBottomPlayAction()
        fun onClickBottomPutInVocabularyShelf()
        fun onClickBottomDeleteInVocabularyShelf()
        fun onClickBottomSelectAll()
        fun onClickBottomRemoveAll()
        fun onClickBottomFlashcard()
        fun onListLayoutChangedComplete()
        fun onAddActivityResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
        fun onActivityResultUpdateVocabulary(data : Intent?)
    }
}