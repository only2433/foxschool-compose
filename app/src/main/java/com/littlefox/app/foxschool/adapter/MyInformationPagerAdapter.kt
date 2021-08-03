package com.littlefox.app.foxschool.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.littlefox.app.foxschool.fragment.MyInfoChangeFragment
import com.littlefox.app.foxschool.fragment.MyInfoShowFragment
import java.util.ArrayList

/**
 * 나의 정보화면 ViewPager Adapter
 */
class MyInformationPagerAdapter : FragmentStatePagerAdapter
{
    private var mMyInfoFragmentList : MutableList<Fragment>? = null

    constructor(fragmentManager : FragmentManager, myInfoFragmentList : ArrayList<Fragment>) : super(fragmentManager)
    {
        mMyInfoFragmentList = myInfoFragmentList
    }

    fun setFragment()
    {
        mMyInfoFragmentList!!.add(MyInfoShowFragment())
        mMyInfoFragmentList!!.add(MyInfoChangeFragment())
        notifyDataSetChanged()
    }

    override fun getCount() : Int = mMyInfoFragmentList!!.size

    override fun getItem(position : Int) : Fragment = mMyInfoFragmentList!![position]

    override fun getPageTitle(position : Int) : CharSequence? = null
}