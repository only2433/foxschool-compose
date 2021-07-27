package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.main.contract.base.BaseContract

class InquireContract
{
    interface View : BaseContract.View
    {
        fun setInquireCategoryText(category: String)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onShowInquireCategoryDialog()
        fun onClickSendToEmail()
        fun onClickRegister(email: String, text: String)
    }
}
