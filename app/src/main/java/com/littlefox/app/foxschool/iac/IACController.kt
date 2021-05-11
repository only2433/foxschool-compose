package com.littlefox.app.foxschool.iac

import com.littlefox.app.foxschool.`object`.data.iac.AwakeItemData
import com.littlefox.app.foxschool.common.Common
import com.littlefox.logmonitor.Log

import java.text.SimpleDateFormat
import java.util.*

/**
 * 다시보지 않기를 한 IAC 아이템을 특정 조건이 되었을 때 다시 보이게 하기 위해 만든 오브젝트
 * @author 정재현
 */
class IACController
{
    private var mSaveIACInformation : AwakeItemData? = null
    private var isIACAwake = false
    private var isPositiveButtonClick = false

    init
    {
        mSaveIACInformation = null
    }

    /**
     * 현재 서버의 IAC 정보를 넘겨주고 로컬 IAC와 비교를 통해 정보를 전달한다. 로컬 정보가 없을땐 서버정보의 IAC로 세팅한다.
     * @param serverIACInformation
     * @return
     */
    fun isAwake(serverIACInformation : AwakeItemData) : Boolean
    {
        if(mSaveIACInformation == null)
        {
            Log.f("SaveIACInformation == null")
            isPositiveButtonClick = false
            isIACAwake = true
            mSaveIACInformation = serverIACInformation
        }
        else if(mSaveIACInformation?.iacCode != serverIACInformation.iacCode)
        {
            Log.f("iacCode != serverIac")
            isPositiveButtonClick = false
            isIACAwake = true
            mSaveIACInformation = serverIACInformation
        } else
        {
            awakeIACItem()
        }
        return isIACVisible()
    }

    /**
     * 링크이동을 클릭했을 경우 IAC는 어떠한 상황이든 노출이 안된다.
     */
    fun setPositiveButtonClick()
    {
        isPositiveButtonClick = true
    }

    /**
     * 닫기나 보지 않기를 클릭했을 때 세팅
     */
    fun setCloseButtonClick()
    {
        isIACAwake = false
    }

    /**
     * IAC를 보여줘야하는 지에 대한 구분을 하는 메소드
     * @return
     */
    fun isIACVisible() : Boolean
    {
        return isIACAwake;
    }

    /**
     * IAC Information을 업데이트한다.
     * @param awakeItemData
     */
    fun setSaveIACInformation(awakeItemData : AwakeItemData?)
    {
        mSaveIACInformation = awakeItemData
    }

    /**
     * 특정 상황이 되었을 때, IAC를 보여줘야하는 지에 대한 부분을 체크하는 메소드
     */
    private fun awakeIACItem()
    {
        val currentTime = System.currentTimeMillis()
        val CurDateFormat = SimpleDateFormat("yyyyMMdd")
        val date : Date
        val saveDate : Date
        if(isPositiveButtonClick == true)
        {
            isIACAwake = false
            return
        }
        if(mSaveIACInformation?.iacType.equals(Common.IAC_AWAKE_CODE_ALWAYS_VISIBLE))
        {
            Log.f("ALWAYS_VISIBLE")
            isIACAwake = true
        }
        else if(mSaveIACInformation?.iacType.equals(Common.IAC_AWAKE_CODE_SPECIAL_DATE_VISIBLE))
        {
            Log.f("SPECIAL_DATE_VISIBLE")
            date = Date(currentTime)
            saveDate = Date(mSaveIACInformation?.iacCloseTime)
            val currentDateFormat = CurDateFormat.format(date)
            val savedDateFormat = CurDateFormat.format(saveDate)
            Log.f(
                "currentDateFormat : " + Integer.valueOf(currentDateFormat) + ", savedDeteFormat : " + Integer.valueOf(
                    savedDateFormat
                ) + ", latingDate : " + mSaveIACInformation?.latingDate
            )
            if(Integer.valueOf(currentDateFormat) - Integer.valueOf(savedDateFormat) >= mSaveIACInformation?.latingDate)
            {
                isIACAwake = true
            }
        } else if(mSaveIACInformation?.iacType.equals(Common.IAC_AWAKE_CODE_ONCE_VISIBLE))
        {
            Log.f("ONCE_VISIBLE")
            isIACAwake = false
        }
    }


}