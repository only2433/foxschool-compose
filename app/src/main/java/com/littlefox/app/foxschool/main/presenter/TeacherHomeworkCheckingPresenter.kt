package com.littlefox.app.foxschool.main.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCheckingIntentParamsObject
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.coroutine.TeacherHomeworkCheckingCoroutine
import com.littlefox.app.foxschool.main.contract.TeacherHomeworkCheckingContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class TeacherHomeworkCheckingPresenter : TeacherHomeworkCheckingContract.Presenter
{
    companion object
    {
        private const val MESSAGE_CHECKING_SUCCESS : Int    = 101
    }

    private lateinit var mContext : Context
    private lateinit var mHomeworkCheckingContractView : TeacherHomeworkCheckingContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private lateinit var mHomeworkCheckingInformation : HomeworkCheckingIntentParamsObject

    private var mTeacherHomeworkCheckingCoroutine : TeacherHomeworkCheckingCoroutine? = null

    // 통신에 입력되는 데이터
    private var mEval : String = ""
    private var mComment : String = ""

    constructor(context : Context)
    {
        mContext = context
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        mHomeworkCheckingContractView = (mContext as TeacherHomeworkCheckingContract.View).apply {
            initView()
            initFont()
        }
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")
        mHomeworkCheckingInformation = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_HOMEWORK_CHECKING_DATA)!!
        if (mHomeworkCheckingInformation.isEvalComplete())
        {
            mHomeworkCheckingContractView.setBeforeData(
                mHomeworkCheckingInformation.getEval(),
                mHomeworkCheckingInformation.getComment()
            )
        }
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        mTeacherHomeworkCheckingCoroutine?.cancel()
        mTeacherHomeworkCheckingCoroutine = null
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_CHECKING_SUCCESS -> (mContext as AppCompatActivity).finish()
        }
    }

    override fun onClickRegisterButton(index : Int, comment : String)
    {
        when(index)
        {
            0 -> mEval = "E0"
            1 -> mEval = "E1"
            2 -> mEval = "E2"
            else -> mEval = "E1"
        }
        mComment = comment
        requestHomeworkCheck()
    }

    private fun requestHomeworkCheck()
    {
        Log.f("")
        mHomeworkCheckingContractView.showLoading()
        mTeacherHomeworkCheckingCoroutine = TeacherHomeworkCheckingCoroutine(mContext).apply {
            setData(
                mHomeworkCheckingInformation.getHomeworkNumber(),
                mHomeworkCheckingInformation.getClassNumber(),
                mHomeworkCheckingInformation.getID(),
                mEval,
                mComment
            )
            asyncListener = mAsyncListener
            execute()
        }
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            val result : BaseResult = mObject as BaseResult

            Log.f("code : $code, status : ${result.getStatus()}")

            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if (code == Common.COROUTINE_CODE_TEACHER_HOMEWORK_CHECKING)
                {
                    mHomeworkCheckingContractView.hideLoading()
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_CHECKING_SUCCESS, Common.DURATION_NORMAL)
                }
            }
            else
            {
                if (result.isDuplicateLogin)
                {
                    // 중복 로그인 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if (result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    mHomeworkCheckingContractView.hideLoading()
                    mHomeworkCheckingContractView.showErrorMessage(result.getMessage())
                }
            }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}