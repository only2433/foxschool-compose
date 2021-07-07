package com.littlefox.app.foxschool.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.QuizStatus
import com.littlefox.app.foxschool.fragment.QuizIntroFragment
import com.littlefox.app.foxschool.fragment.QuizPlayFragment
import com.littlefox.app.foxschool.fragment.QuizResultFragment
import java.util.*

class QuizSelectionPagerAdapter : FragmentStatePagerAdapter
{
    private var mQuizDisplayFragmentList : ArrayList<Fragment>? = null
    constructor(fragmentManager : FragmentManager, quizDisplayFragmentList : ArrayList<Fragment>) : super(fragmentManager)
    {
        mQuizDisplayFragmentList = quizDisplayFragmentList
    }

    fun addFragment(type : QuizStatus)
    {
        var fragment : Fragment? = null
        when(type)
        {
            QuizStatus.INTRO -> fragment = QuizIntroFragment()
            QuizStatus.RESULT -> fragment = QuizResultFragment()
        }
        mQuizDisplayFragmentList!!.add(fragment)
        notifyDataSetChanged()
    }

    fun addQuizPlayFragment(quizType : String, `object` : Any?)
    {
        var fragment : Fragment? = null
        fragment = QuizPlayFragment()
        if(`object` != null)
        {
            if(quizType == Common.QUIZ_CODE_PICTURE)
            {
                fragment.setQuestionItemObject(
                    Common.QUIZ_CODE_PICTURE,
                    `object`
                )
            } else if(quizType == Common.QUIZ_CODE_TEXT)
            {
                fragment.setQuestionItemObject(
                    Common.QUIZ_CODE_TEXT,
                    `object`
                )
            } else if(quizType == Common.QUIZ_CODE_SOUND_TEXT)
            {
                fragment.setQuestionItemObject(
                    Common.QUIZ_CODE_SOUND_TEXT,
                    `object`
                )
            } else
            {
                fragment.setQuestionItemObject(
                    Common.QUIZ_CODE_PHONICS_SOUND_TEXT,
                    `object`
                )
            }
        }
        mQuizDisplayFragmentList!!.add(fragment)
        notifyDataSetChanged()
    }

    override fun getCount() : Int = mQuizDisplayFragmentList!!.size

    override fun getItem(position : Int) : Fragment = mQuizDisplayFragmentList!![position]

    override fun getPageTitle(position : Int) : CharSequence? = null
}