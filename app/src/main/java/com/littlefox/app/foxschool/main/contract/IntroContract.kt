package com.littlefox.app.foxschool.main.contract

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class IntroContract
{
    interface View : BaseContract.View
    {
        fun showToast(message : String)
        fun showItemSelectView()
        fun showProgressView()
        fun setProgressPercent(fromPercent : Float, toPercent : Float)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onClickIntroduce()
        fun onClickHomeButton()
        fun onClickLogin()
        fun onRequestPermissionsResult(requestCode : Int, permissions : Array<out String>, grantResults : IntArray)
        fun onActivateEasterEgg()
        fun onDeactivateEasterEgg()
        fun onAddActivityResultLaunchers(vararg launchers : ActivityResultLauncher<Intent?>?)
        fun onActivityResultLogin()
    }
}