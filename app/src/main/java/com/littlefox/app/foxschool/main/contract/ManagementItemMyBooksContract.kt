package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class ManagementItemMyBooksContract
{
    interface View : BaseContract.View
    {
        fun settingBookColor(color : BookColor)
        fun settingLayoutView(type : MyBooksType)
        fun setBooksName(name : String)

        /**
         * 단어장,책장 추가 일때는 취소, 단어장, 책장 수정일때는 삭제
         * @param isDeleteAvailable TRUE 삭제 , FALSE 취소
         */
        fun setCancelButtonAction(isDeleteAvailable : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onSelectSaveButton(bookName : String)
        fun onSelectCloseButton()
        fun onSelectBooksItem(color : BookColor)
        fun onCancelActionButton()
    }
}