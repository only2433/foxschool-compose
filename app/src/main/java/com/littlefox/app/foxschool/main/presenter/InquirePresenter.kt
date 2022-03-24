package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.forum.InquireData
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.common.CheckUserInput
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.InquireCoroutine
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.enumerate.InquireType
import com.littlefox.app.foxschool.main.contract.InquireContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class InquirePresenter : InquireContract.Presenter
{
    companion object
    {
        private const val MESSAGE_CATEGORY_INPUT_ERROR : Int        = 100   // 카테고리 입력값 오류 (비었을 때)
        private const val MESSAGE_EMAIL_INPUT_ERROR : Int           = 101   // 이메일 입력값 오류 (비었을 때, 잘못 썼을 때)
        private const val MESSAGE_MESSAGE_INPUT_ERROR : Int         = 102   // 메세지 입력값 오류 (비었을 때)
        private const val MESSAGE_COMPLETE : Int                    = 103   // 등록 완료
    }

    private lateinit var mContext : Context
    private lateinit var mInquireContractView : InquireContract.View
    private lateinit var mMainHandler : WeakReferenceHandler

    private var mInquireCoroutine : InquireCoroutine? = null
    private var mInquireData : InquireData? = null
    private var mLoginInformation : LoginInformationResult? = null  // 로그인 시 응답받은 회원정보

    // 문의 종류 선택 다이얼로그 데이터
    private var mInquireTypeList : Array<String>? = null
    private var mInquireTypeSelected : Int = -1

    constructor(context : Context)
    {
        mContext = context
        mInquireContractView = (mContext as InquireContract.View).apply {
            initView()
            initFont()
        }
        mMainHandler = WeakReferenceHandler(context as MessageHandlerCallback)
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        mInquireTypeList = mContext.resources.getStringArray(R.array.text_list_1on1_ask)
        mLoginInformation = CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult?
        mInquireContractView.setUserEmail(mLoginInformation!!.getUserInformation().getEmail())
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
        mInquireCoroutine?.cancel()
        mInquireCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_CATEGORY_INPUT_ERROR ->
            {
                mInquireContractView.setInputError(InputDataType.NONE)
                mInquireContractView.showErrorMessage(msg.obj.toString())
            }
            MESSAGE_EMAIL_INPUT_ERROR ->
            {
                mInquireContractView.setInputError(InputDataType.EMAIL)
                mInquireContractView.showErrorMessage(msg.obj.toString())
            }
            MESSAGE_MESSAGE_INPUT_ERROR ->
            {
                mInquireContractView.setInputError(InputDataType.MESSAGE)
                mInquireContractView.showErrorMessage(msg.obj.toString())
            }
            MESSAGE_COMPLETE -> (mContext as AppCompatActivity).onBackPressed()
        }
    }

    /**
     * 문의하기 카테고리 선택 다이얼로그 표시
     */
    override fun onShowInquireCategoryDialog()
    {
        Log.f("")
        val builder = AlertDialog.Builder(mContext)
        builder.setSingleChoiceItems(mInquireTypeList, mInquireTypeSelected, DialogInterface.OnClickListener {dialog, index ->
            dialog.dismiss()
            mInquireTypeSelected = index
            mInquireContractView.setInquireCategoryText(mInquireTypeList!![mInquireTypeSelected])
        })

        val dialog : AlertDialog = builder.show()
        dialog.show()
    }

    /**
     * 사용자가 선택한 문의 카테고리 가져오기
     */
    private fun getInquireType() : InquireType?
    {
        when(mInquireTypeSelected)
        {
            0 -> return InquireType.ERROR
            1 -> return InquireType.STUDY
            2 -> return InquireType.ETC
        }
        return null
    }




    /**
     * 이메일 입력값 유효성 체크
     * showMessage : 화면으로 메세지 표시 이벤트 넘길지 말지
     */
    private fun checkEmailAvailable(email : String) : String
    {
        val emailResult = CheckUserInput.getInstance(mContext).checkEmailData(email).getResultValue()
        if(emailResult == CheckUserInput.WARNING_EMAIL_NOT_INPUT ||
            emailResult == CheckUserInput.WARNING_EMAIL_WRONG_INPUT)
        {
            return CheckUserInput().getErrorMessage(emailResult)
        }
        return ""
    }

    /**
     * 등록 버튼 클릭 이벤트
     */
    override fun onClickRegister(email : String, text : String)
    {
        CommonUtils.getInstance(mContext).hideKeyboard()
        val category = getInquireType()
        var message : Message? = checkRegisterData(category, email, text)

        if(message == null)
        {
            mInquireContractView.showLoading()

            mInquireData = InquireData(category!!, text, email)
            mInquireCoroutine = InquireCoroutine(mContext).apply {
                setData(mInquireData)
                asyncListener = mAsyncListener
                execute()
            }
        }
        else
        {
            mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
        }
    }

    /**
     * 이메일로 의견 보내기 버튼 클릭 이벤트
     */
    override fun onClickSendToEmail(text : String)
    {
        CommonUtils.getInstance(mContext).inquireForDeveloper(Common.DEVELOPER_EMAIL, text)
    }


    private fun checkRegisterData(category : InquireType?, email : String, text : String) : Message?
    {
        var message : Message? = Message.obtain()
        if (category == null)
        {
            message!!.what = MESSAGE_CATEGORY_INPUT_ERROR
            message!!.obj = mContext.resources.getString(R.string.message_warning_select_inquire_category)
        }
        else if(email == "" || email.length < 2)
        {
            // 이메일 빈 값
            message!!.what = MESSAGE_EMAIL_INPUT_ERROR
            message!!.obj = mContext.resources.getString(R.string.message_warning_empty_email)
        }
        else if (isEmailAvailable(email) == false)
        {
            // 이메일 유효성 불일치
            var resultData = getEmailResultValue(email)
            message!!.what = MESSAGE_EMAIL_INPUT_ERROR
            message!!.obj = CheckUserInput().getErrorMessage(resultData)
        }
        else if(text == "" || text.length < 2)
        {
            message!!.what = MESSAGE_MESSAGE_INPUT_ERROR
            message!!.obj = mContext.resources.getString(R.string.message_warning_empty_inquire)
        }
        else
        {
            message = null
        }
        return message
    }

    private fun isEmailAvailable(email : String) : Boolean
    {
        val emailResult = CheckUserInput.getInstance(mContext).checkEmailData(email).getResultValue()
        if(emailResult == CheckUserInput.WARNING_EMAIL_NOT_INPUT ||
            emailResult == CheckUserInput.WARNING_EMAIL_WRONG_INPUT)
        {
            return false
        }
        return true
    }

    private fun getEmailResultValue(email : String) : Int
    {
        return CheckUserInput.getInstance(mContext).checkEmailData(email).getResultValue()
    }

    /**
     * 통신 이벤트 리스너
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String?) { }

        override fun onRunningEnd(code : String?, mObject : Any?)
        {
            mInquireContractView.hideLoading()

            val result : BaseResult = mObject as BaseResult
            Log.f("code : $code, status : ${result.getStatus()}")

            if (result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if (code == Common.COROUTINE_CODE_INQUIRE)
                {
                    // 메세지 표시하고 화면 이동시키기 위해 핸들러 처리
                    mInquireContractView.showSuccessMessage(mContext.getString(R.string.message_1on1_success))
                    mMainHandler.sendEmptyMessageDelayed(MESSAGE_COMPLETE, Common.DURATION_SHORT)
                }
            }
            else
            {
                if(result.isDuplicateLogin)
                {
                    //중복 로그인 시 재시작
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initAutoIntroSequence()
                }
                else if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                }
                else
                {
                    mInquireContractView.showErrorMessage(result.getMessage())
                }
            }
        }

        override fun onRunningCanceled(code : String) { }

        override fun onRunningProgress(code : String, progress : Int) { }

        override fun onRunningAdvanceInformation(code : String, `object` : Any) { }

        override fun onErrorListener(code : String, message : String) { }
    }
}
