package com.littlefox.app.foxschool.main.contract


import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MainContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter?)
        override fun showLoading()
        override fun hideLoading()
        override fun showSuccessMessage(message : String)
        override fun showErrorMessage(message : String)
        fun settingUserInformation(userInformationResult : UserInformationResult?)
        fun setCurrentPage(page : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun changeUser(index : Int)
        fun onClickMenuLogin()
        fun onClickPaidSignIn()
        fun onClickMenuNews()
        fun onClickMenuTestimonial()
        fun onClickMenuMyInformation()
        fun onClickMenuAppUseGuide()
        fun onClickMenuAddUser()
        fun onClickMenu1On1Ask()
        fun onClickMenuFAQ()
        fun onClickMenuDetailPaymentInformation()
        fun onClickMenuPublishSchedule()
        fun onClickMenuAttendance()
        fun onClickMenuRestore()
        fun onClickMenuStore()
        fun onClickMenuLogout()
        fun onClickMenuLearningLog()
        fun onClickMenuClass()
        fun onClickSearch()
        fun onBackPressed()
    }
}