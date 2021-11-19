package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.TeacherHomeworkPagerAdapter
import com.littlefox.app.foxschool.enumerate.HomeworkCommentType
import com.littlefox.app.foxschool.enumerate.HomeworkDetailType
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class TeacherHomeworkContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(mHomeworkPagerAdapter : TeacherHomeworkPagerAdapter)
        fun setCurrentViewPage(position : Int, detailType : HomeworkDetailType? = null, commentType : HomeworkCommentType? = null)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickBackButton()
        fun onPageChanged(position : Int)
    }
}