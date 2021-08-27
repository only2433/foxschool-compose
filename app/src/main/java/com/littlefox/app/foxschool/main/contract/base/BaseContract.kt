package com.littlefox.app.foxschool.main.contract.base

import android.content.Intent
import android.os.Message

/**
 * Contract 와 Presenter 의 기본 구조체
 * Created by 정재현 on 2017-12-21.
 */
class BaseContract
{
    interface View
    {
        fun initView()
        fun initFont()
        fun showLoading()
        fun hideLoading()
        fun showSuccessMessage(message : String)
        fun showErrorMessage(message : String)
    }

    interface Presenter
    {
        fun resume()
        fun pause()
        fun destroy()
        fun activityResult(requestCode : Int, resultCode : Int, data : Intent?)
        fun sendMessageEvent(msg : Message)
    }
}