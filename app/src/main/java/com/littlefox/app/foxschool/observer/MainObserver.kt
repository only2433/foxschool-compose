package com.littlefox.app.foxschool.observer


import com.littlefox.app.foxschool.observer.MainObserver
import java.util.ArrayList

object MainObserver
{
    private var mUpdateMainFragmentList : ArrayList<Int>? = null
    private var isUpdateUserStatus : Boolean = false
    private var isEnterPaymentPage : Boolean = false


    private fun init()
    {
        if(mUpdateMainFragmentList == null)
        {
            mUpdateMainFragmentList = ArrayList()
        }
    }

    fun updatePage(page : Int)
    {
        init()
        if(mUpdateMainFragmentList?.contains(page) == false)
        {
            mUpdateMainFragmentList?.add(page)
        }
    }

    fun clearAll()
    {
        mUpdateMainFragmentList?.clear()
    }


    fun getUpdatePageList() : ArrayList<Int>?
    {
        init()
        return mUpdateMainFragmentList
    }

    fun updateUserStatus()
    {
        isUpdateUserStatus = true
    }

    fun clearUserStatus()
    {
        isUpdateUserStatus = false
    }

    fun executeToEnterPaymentPage()
    {
        isEnterPaymentPage = true
    }

    fun clearEnterPaymentPage()
    {
        isEnterPaymentPage = false
    }

    fun isUpdateUserStatus() : Boolean
    {
        return isUpdateUserStatus;
    }

    fun isEnterPaymentPage() : Boolean
    {
        return isEnterPaymentPage;
    }


}