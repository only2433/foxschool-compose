package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.renderscript.BaseObj
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.SchoolListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.SchoolListCoroutine
import com.littlefox.app.foxschool.enc.SimpleCrypto
import com.littlefox.app.foxschool.main.contract.LoginContract
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class LoginPresenter : LoginContract.Presenter
{
    companion object
    {
        private const val MESSAGE_INPUT_EMPTY_ERROR = 100
    }

    private lateinit var mContext : Context
    private lateinit var mMainHandler : WeakReferenceHandler
    private var mLoginContractView : LoginContract.View
    private var mUserLoginData : UserLoginData? = null
//    private var mLoginCoroutine : LoginCoroutine? = null
    private var mSchoolListCoroutine : SchoolListCoroutine? = null

    private var mSchoolListBaseObject : SchoolListBaseObject? = null
    private val mSchoolList : ArrayList<SchoolItemDataResult> = ArrayList<SchoolItemDataResult>()

    constructor(context : Context)
    {
        mContext = context
        mLoginContractView = mContext as LoginContract.View
        mLoginContractView.initView()
        mLoginContractView.initFont()
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)

        requestSchoolList()
    }

    override fun resume() { }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        mSchoolListCoroutine?.cancel()
        mSchoolListCoroutine = null
//        mLoginCoroutine?.cancel()
//        mLoginCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_INPUT_EMPTY_ERROR -> mLoginContractView.showErrorMessage(msg.obj.toString())
        }
    }

    override fun onCheckAutoLogin(autoLogin : Boolean)
    {

    }

    /**
     * 로그인 버튼 클릭 이벤트
     */
    override fun onClickLogin(data : UserLoginData)
    {
        Log.f("Login")
        requestLoginAsync(data.userID, data.userPassword, data.userSchoolCode)
    }

    override fun onClickFindID()
    {

    }

    override fun onClickFindPassword()
    {

    }

    /**
     * 학교 리스트 요청
     */
    private fun requestSchoolList()
    {
        mSchoolListCoroutine = SchoolListCoroutine(mContext)
        mSchoolListCoroutine!!.asyncListener = mAsyncListener
        mSchoolListCoroutine!!.execute()
    }

    /**
     * 학교 리스트 수신데이터 전달
     */
    private fun notifySchoolList()
    {
        Log.f("")
        mSchoolList.addAll(mSchoolListBaseObject!!.getData())
        mLoginContractView.setSchoolList(mSchoolList)
    }

    /**
     * 로그인 요청
     */
    private fun requestLoginAsync(id : String, password : String, schoolCode : String)
    {
        if(id == "" || password == "" || schoolCode == "")
        {
            CommonUtils.getInstance(mContext).hideKeyboard()
            val message = Message.obtain()
            message.what = MESSAGE_INPUT_EMPTY_ERROR
            when
            {
                (schoolCode == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_school)
                (id == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_id)
                (password == "") -> message.obj = mContext.resources.getString(R.string.message_warning_empty_password)
            }
            mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
            return
        }
        mLoginContractView.showLoading()

        try
        {
            mUserLoginData = UserLoginData(id, SimpleCrypto.encode(password), schoolCode)
        } catch(e : Exception)
        {
            e.printStackTrace()
            Log.f("errorMessage : " + e.message)
        }
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            val result : BaseResult = mObject as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())

            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_SCHOOL_LIST)
                {
                    mSchoolListBaseObject = mObject as SchoolListBaseObject
                    notifySchoolList()
                }
            }
            else { }
        }

        override fun onRunningCanceled(code : String?) { }

        override fun onRunningProgress(code : String?, progress : Int?) { }

        override fun onRunningAdvanceInformation(code : String?, `object` : Any?) { }

        override fun onErrorListener(code : String?, message : String?) { }
    }
}
