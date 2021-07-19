package com.littlefox.app.foxschool.common

import android.content.Context
import com.littlefox.logmonitor.Log
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern

/**
 * 입력필드 유효성 체크용 클래스
 */
class CheckUserInput
{
    companion object
    {
        // [a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?
        private const val TEXT_EMAIL : String           = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}\$/i"
        private const val TEXT_PASSWORD : String        = "^[a-zA-Z0-9\\~!@#\$%^&*()_+-=[]{};':\",./?<>|\\]+\$"
        private const val TEXT_CHECK_NAME : String      = "^[a-zA-Z가-힣]+[^ㄱ-ㅎ]+\$"
        private const val TEXT_CHECK_PHONE : String     = "^\\d{2,3}-\\d{3,4}-\\d{4}\$"

        const val INPUT_SUCCESS : Int               = 100
        const val WARNING_PASSWORD_NOT_INPUT : Int          = 0
        const val WARNING_PASSWORD_WRONG_INPUT : Int        = 1
        const val WARNING_PASSWORD_NOT_INPUT_CONFIRM : Int  = 2
        const val WARNING_PASSWORD_NOT_EQUAL_CONFIRM : Int  = 3
        const val WARNING_NAME_NOT_INPUT : Int              = 4
        const val WARNING_NAME_WRONG_INPUT : Int            = 5
        const val WARNING_EMAIL_NOT_INPUT : Int             = 6
        const val WARNING_EMAIL_WRONG_INPUT : Int           = 7
        const val WARNING_PHONE_NOT_INPUT : Int             = 8
        const val WARNING_PHONE_WRONG_INPUT : Int           = 9

        var sCheckUserInput : CheckUserInput? = null
        lateinit var sContext : Context
        private var sResultValue = INPUT_SUCCESS

        fun getInstance(context : Context) : CheckUserInput
        {
            if(sCheckUserInput == null)
            {
                sCheckUserInput = CheckUserInput()
            }
            sContext = context
            sResultValue = INPUT_SUCCESS
            return sCheckUserInput!!
        }
    }

    /**
     * 해당 패턴말고 다른 글자가 들어있는 지 체크
     * @param pattenText 패턴
     * @param text 해당 텍스트
     * @return TRUE : 패턴안에 들어있다. FALSE : 패턴안에 없는 글자가 있다.
     */
    private fun isExceptTextHave(pattenText : String, text : String) : Boolean
    {
        return if(Pattern.matches(pattenText, text))
        {
            Log.f("Match")
            true
        } else
        {
            Log.f("Not Match")
            false
        }
    }

    /**
     * 특정 스트링의 바이트 사이즈가 start와 end 사이에 있는 지 확인
     * @param text 해당 텍스트
     * @param start 시작 인텍스 크기
     * @param end 종료 인덱스 크기
     * @return
     */
    private fun isByteSizeFit(text : String, start : Int, end : Int) : Boolean
    {
        try
        {
            Log.f("text : $text, text.getBytes().length : ${text.toByteArray(charset("ms949")).size}")
            if(text.toByteArray().size >= start && text.toByteArray(charset("ms949")).size <= end)
            {
                return true
            }
        } catch(e : UnsupportedEncodingException)
        {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return false
    }

    /**
     * 패스워드 유효성 체크
     * @param passwordText 패스워드
     * @return
     */
    fun checkPasswordData(passwordText : String) : CheckUserInput
    {
        Log.f("passwordText : $passwordText")
        if(sResultValue != INPUT_SUCCESS)
        {
            return this
        }

        if(passwordText == "")
        {
            sResultValue = WARNING_PASSWORD_NOT_INPUT
        }
        else if(isExceptTextHave(TEXT_PASSWORD, passwordText) == false || isByteSizeFit(passwordText, 4, 10) == false)
        {
            sResultValue = WARNING_PASSWORD_WRONG_INPUT
        }
        return this
    }

    /**
     * 패스워드 일치한지 확인
     * @param passwordText 패스워드
     * @param confirmPasswordText 다시 입력한 패스워드
     * @return
     */
    fun checkPasswordData(passwordText : String, confirmPasswordText : String) : CheckUserInput
    {
        Log.f("passwordText : $passwordText, confirmPasswordText : $confirmPasswordText")
        if(sResultValue != INPUT_SUCCESS)
        {
            return this
        }

        if(passwordText == "")
        {
            sResultValue = WARNING_PASSWORD_NOT_INPUT
        }
        else if(isExceptTextHave(TEXT_PASSWORD, passwordText) == false || isByteSizeFit(passwordText, 4, 10) == false)
        {
            sResultValue = WARNING_PASSWORD_WRONG_INPUT
        }
        else if(confirmPasswordText == "")
        {
            sResultValue = WARNING_PASSWORD_NOT_INPUT_CONFIRM
        }
        else if(passwordText != confirmPasswordText)
        {
            sResultValue = WARNING_PASSWORD_NOT_EQUAL_CONFIRM
        }
        return this
    }

    /**
     * Name 유효성 체크
     * @param name
     * @return
     */
    fun checkNameData(name : String) : CheckUserInput
    {
        Log.f("name : $name")
        if(sResultValue != INPUT_SUCCESS)
        {
            return this
        }

        if(name == "")
        {
            sResultValue = WARNING_NAME_NOT_INPUT
        }
        else if(isByteSizeFit(name, 4, 16) == false)
        {
            sResultValue = WARNING_NAME_WRONG_INPUT
        }
        else
        {
            if(isExceptTextHave(TEXT_CHECK_NAME, name) == false)
            {
                sResultValue = WARNING_NAME_WRONG_INPUT
            }
        }
        return this
    }

    /**
     * Email 유효성 체크
     * @param email
     * @return
     */
    fun checkEmailData(email : String) : CheckUserInput
    {
        Log.f("email : $email")
        if(sResultValue != INPUT_SUCCESS)
        {
            return this
        }

        if(email == "")
        {
            sResultValue = WARNING_EMAIL_NOT_INPUT
        }
        else if(isExceptTextHave(TEXT_EMAIL, email) == false)
        {
            sResultValue = WARNING_EMAIL_WRONG_INPUT
        }
        return this
    }

    /**
     * Phone 유효성 체크
     * @param phone
     * @return
     */
    fun checkPhoneData(phone : String) : CheckUserInput
    {
        Log.f("phone : $phone")
        if(sResultValue != INPUT_SUCCESS)
        {
            return this
        }

        if(phone == "")
        {
            sResultValue = WARNING_PHONE_NOT_INPUT
        }
        else if(isExceptTextHave(TEXT_CHECK_PHONE, phone) == false)
        {
            sResultValue = WARNING_PHONE_WRONG_INPUT
        }
        return this
    }

    /**
     * 체크한 정보 값을 전달한다.
     * @return
     */
    fun getResultValue() : Int
    {
        Log.f("sResultValue : $sResultValue")
        return sResultValue
    }
}