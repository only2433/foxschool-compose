package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.adapter.FlashcardSelectionPagerAdapter
import com.littlefox.app.foxschool.enumerate.FlashcardStatus
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class FlashcardContract
{
    interface View : BaseContract.View
    {
        fun showPagerView(adapter : FlashcardSelectionPagerAdapter?)
        fun settingSoundButton(isEnable : Boolean)
        fun settingAutoPlayInterval(second : Int)
        fun settingBaseControlView(status : FlashcardStatus)
        fun checkAutoplayBox(status : FlashcardStatus, isEnable : Boolean)
        fun checkShuffleBox(isEnable : Boolean?)
        fun showCoachMarkView()
        fun showBottomViewLayout()
        fun hideBottomViewLayout()
        fun prevPageView()
        fun nextPageView()
        fun forceChangePageView(position : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickSound()
        fun onCheckAutoPlay()
        fun onCheckShuffle()
        fun onClickAutoPlayInterval()
        fun onClickClose()
        fun onClickHelpViewBack()
        fun onFlashCardPageSelected(pageIndex : Int)
        fun onCoachMarkNeverSeeAgain()
    }
}