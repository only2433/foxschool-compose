package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class InquireContract
{
    interface View : BaseContract.View
    {
        fun setUserEmail(email : String)
        fun setInquireCategoryText(category: String)
        fun setInputError(type : InputDataType)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onShowInquireCategoryDialog()
        fun onClickSendToEmail(text: String)
        fun onClickRegister(email: String, text: String)
    }
}
