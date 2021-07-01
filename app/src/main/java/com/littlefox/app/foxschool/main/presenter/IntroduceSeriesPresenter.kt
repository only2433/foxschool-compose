package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.`object`.result.IntroduceSeriesBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.introduceSeries.IntroduceSeriesInformationResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.coroutine.IntroduceSeriesCoroutine
import com.littlefox.app.foxschool.main.contract.IntroduceSeriesContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class IntroduceSeriesPresenter : IntroduceSeriesContract.Presenter
{
    companion object
    {
        private const val MESSAGE_INTRODUCE_SERIES_INFORMATION_REQUEST_COMPLETE : Int = 100
    }

    private lateinit var mContext : Context
    private lateinit var mIntroduceSeriesContractView : IntroduceSeriesContract.View
    private var mIntroduceSeriesCoroutine : IntroduceSeriesCoroutine? = null
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mSeriesID = ""

    constructor(context : Context)
    {
        mContext = context

        mSeriesID = (mContext as AppCompatActivity).intent.getStringExtra(Common.INTENT_SERIES_INFORMATION_ID)
        Log.f("SeriesID : $mSeriesID")

        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mIntroduceSeriesContractView = mContext as IntroduceSeriesContract.View
        mIntroduceSeriesContractView.initView()
        mIntroduceSeriesContractView.initFont()
        mIntroduceSeriesContractView.showLoading()

        Log.f("onCreate")
        requestIntroduceSeriesInformation()
    }

    override fun resume() { }

    override fun pause() { }

    override fun destroy()
    {
        if(mIntroduceSeriesCoroutine != null)
        {
            mIntroduceSeriesCoroutine!!.cancel()
            mIntroduceSeriesCoroutine = null
        }
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_INTRODUCE_SERIES_INFORMATION_REQUEST_COMPLETE ->
            {
                mIntroduceSeriesContractView.hideLoading()
                mIntroduceSeriesContractView.showIntroduceSeriesData(msg.obj as IntroduceSeriesInformationResult)
            }
        }
    }

    /**
     * 컨텐츠 소개 데이터 요청
     */
    private fun requestIntroduceSeriesInformation()
    {
        mIntroduceSeriesCoroutine = IntroduceSeriesCoroutine(mContext)
        mIntroduceSeriesCoroutine?.setData(mSeriesID)
        mIntroduceSeriesCoroutine?.asyncListener = mAsyncListener
        mIntroduceSeriesCoroutine?.execute()
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) { }

        override fun onRunningEnd(code : String?, `object` : Any?)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_INTRODUCE_SERIES)
                {
                    val message = Message.obtain()
                    message.what = MESSAGE_INTRODUCE_SERIES_INFORMATION_REQUEST_COMPLETE
                    message.obj = (`object` as IntroduceSeriesBaseObject).getData()
                    mMainHandler.sendMessage(message)
                }
            }
            else
            {
                Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    IntentManagementFactory.getInstance().initScene()
                } else
                {
                    (mContext as AppCompatActivity).onBackPressed()
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}