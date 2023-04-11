package com.littlefox.app.foxschool.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.littlefox.app.foxschool.fragment.*
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
        mHomeworkFragmentList!!.add(TeacherHomeworkCalendarFragment())
        mHomeworkFragmentList!!.add(TeacherHomeworkStatusFragment())
        mHomeworkFragmentList!!.add(TeacherHomeworkListFragment())
        mHomeworkFragmentList!!.add(TeacherHomeworkCommentFragment())
        notifyDataSetChanged()
    }

    override fun getCount() : Int = mHomeworkFragmentList!!.size

    override fun getItem(position : Int) : Fragment = mHomeworkFragmentList!![position]

    override fun getPageTitle(position : Int) : CharSequence? = null
}