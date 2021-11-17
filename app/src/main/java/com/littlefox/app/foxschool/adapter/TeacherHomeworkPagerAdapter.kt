package com.littlefox.app.foxschool.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.littlefox.app.foxschool.fragment.HomeworkCalendarFragment
import com.littlefox.app.foxschool.fragment.HomeworkCommentFragment
import com.littlefox.app.foxschool.fragment.TeacherHomeworkListFragment
import com.littlefox.app.foxschool.fragment.TeacherHomeworkStatusFragment
import java.util.ArrayList

/**
 * 숙제관리 화면 ViewPager Adapter (선생님용)
 * @author 김태은
 */
class TeacherHomeworkPagerAdapter : FragmentStatePagerAdapter
{
    private var mHomeworkFragmentList : MutableList<Fragment>? = null

    constructor(fragmentManager : FragmentManager, myInfoFragmentList : ArrayList<Fragment>) : super(fragmentManager)
    {
        mHomeworkFragmentList = myInfoFragmentList
    }

    fun setFragment()
    {
        mHomeworkFragmentList!!.add(HomeworkCalendarFragment())
        mHomeworkFragmentList!!.add(TeacherHomeworkStatusFragment())
        mHomeworkFragmentList!!.add(TeacherHomeworkListFragment())
        mHomeworkFragmentList!!.add(HomeworkCommentFragment())
        notifyDataSetChanged()
    }

    override fun getCount() : Int = mHomeworkFragmentList!!.size

    override fun getItem(position : Int) : Fragment = mHomeworkFragmentList!![position]

    override fun getPageTitle(position : Int) : CharSequence? = null
}