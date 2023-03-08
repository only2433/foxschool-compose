package com.littlefox.app.foxschool.main.contract


import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class MainContract
{
    interface View : BaseContract.View
    {
        fun initViewPager(mainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter)
        override fun showSuccessMessage(message : String)
        override fun showErrorMessage(message : String)
        fun showDownloadMessage(message : String)
        fun settingUserInformation(loginInformationResult : LoginInformationResult?, isUpdateHomework : Boolean, isUpdateNews : Boolean)
        fun setCurrentPage(page : Int)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickMenuLogin()
        fun onClickMenuMyInformation()
        fun onClickMenuAppUseGuide()
        fun onClickMenuAddUser()
        fun onClickMenu1On1Ask()
        fun onClickMenuFAQ()
        fun onClickMenuPublishSchedule()
        fun onClickMenuAttendance()
        fun onClickMenuLogout()
        fun onClickMenuLearningLog()
        fun onClickMenuHomeworkManage()
        fun onClickMenuTeacherManual()
        fun onClickMenuHomeNewsPaper()
        fun onClickRecordHistory()
        fun onClickFoxschoolNews()
        fun onClickSearch()
        fun onBackPressed()
    }
}