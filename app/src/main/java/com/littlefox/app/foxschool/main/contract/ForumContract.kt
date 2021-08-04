package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class ForumContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter?)
        fun setCurrentViewPage(position : Int)
        fun setBackButton(isVisible : Boolean)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onPageSelected(position : Int)
    }
}