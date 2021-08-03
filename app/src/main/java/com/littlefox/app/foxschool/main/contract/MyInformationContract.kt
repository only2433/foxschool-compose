package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.MyInformationPagerAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MyInformationContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(myInformationPagerAdapter : MyInformationPagerAdapter)
        fun setCurrentViewPage(position : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickBackButton()
    }
}