package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.HomeworkPagerAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class HomeworkContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(mHomeworkPagerAdapter : HomeworkPagerAdapter)
        fun setCurrentViewPage(position : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickBackButton()
    }
}