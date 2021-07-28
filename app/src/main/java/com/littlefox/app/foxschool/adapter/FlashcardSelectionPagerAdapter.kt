package com.littlefox.app.foxschool.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.littlefox.app.foxschool.`object`.result.flashcard.FlashCardDataResult
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.fragment.FlashCardBookmarkFragment
import com.littlefox.app.foxschool.fragment.FlashCardIntroFragment
import com.littlefox.app.foxschool.fragment.FlashCardResultFragment
import com.littlefox.app.foxschool.fragment.FlashCardStudyDataFragment

/**
 * 플래시카드 Pager Adapter
 */
class FlashcardSelectionPagerAdapter : FragmentStatePagerAdapter
{
    private var mFlashcardFragmentList : ArrayList<Fragment> = ArrayList()
    private lateinit var mFragmentManager : FragmentManager

    constructor(fragmentManager : FragmentManager, mFlashcardFragmentList : ArrayList<Fragment>) : super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
    {
        this.mFlashcardFragmentList = mFlashcardFragmentList
        this.mFragmentManager = fragmentManager
    }

    fun addFragment(status : FlashcardStatus)
    {
        var fragment = Fragment()

        when(status)
        {
            FlashcardStatus.INTRO -> fragment = FlashCardIntroFragment().getInstance()
            FlashcardStatus.RESULT -> fragment = FlashCardResultFragment().getInstance()
            else -> {}
        }
        mFlashcardFragmentList.add(fragment)
        notifyDataSetChanged()
    }

    /**
     * 학습 화면 추가
     */
    fun addStudyDataFragment(dataList : ArrayList<FlashCardDataResult>)
    {
        val fragment = FlashCardStudyDataFragment().getInstance()
        fragment.setData(dataList)
        mFlashcardFragmentList.add(fragment)
        notifyDataSetChanged()
    }

    /**
     * 북마크 화면 추가
     */
    fun addBookmarkFragment(type : VocabularyType, dataList : ArrayList<FlashCardDataResult>)
    {
        val fragment = FlashCardBookmarkFragment().getInstance()
        fragment.setData(type, dataList)
        mFlashcardFragmentList.add(fragment)
        notifyDataSetChanged()
    }

    override fun getCount() : Int
    {
        return mFlashcardFragmentList.size
    }

    override fun getItem(position : Int) : Fragment
    {
        return mFlashcardFragmentList[position]
    }

    override fun getItemPosition(`object` : Any) : Int
    {
        val index = mFlashcardFragmentList.indexOf(`object`)

        if(index == -1)
            return POSITION_NONE
        else
            return index
    }

    override fun destroyItem(container : ViewGroup, position : Int, `object` : Any)
    {
        if(position >= count)
        {
            val manager = (`object` as Fragment).fragmentManager
            val trans = manager!!.beginTransaction()
            trans.remove(`object`)
            trans.commit()
        }
    }
}