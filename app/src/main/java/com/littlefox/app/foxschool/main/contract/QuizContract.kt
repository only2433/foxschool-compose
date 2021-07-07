package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.QuizSelectionPagerAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class QuizContract
{
    interface View : BaseContract.View
    {
        fun showTaskBoxLayout()
        fun hideTaskBoxLayout()

        fun showPagerView(quizSelectionPagerAdapter : QuizSelectionPagerAdapter?)
        fun forceChangePageView(pageIndex : Int)
        fun nextPageView()

        fun showPlayTime(time : String?)
        fun showCorrectAnswerCount(message : String?)

        fun showCorrectAnswerView()
        fun showInCorrectAnswerView()
        fun hideAnswerView()
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onQuizPageSelected()
    }
}