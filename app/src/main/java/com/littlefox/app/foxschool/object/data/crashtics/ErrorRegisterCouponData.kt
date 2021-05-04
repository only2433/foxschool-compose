package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import com.littlefox.app.foxschool.`object`.data.crashtics.base.ErrorRegisterData
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import java.util.*

class ErrorRegisterCouponData : BaseGenerator
{
    constructor(data : ErrorRegisterData, couponNumber : String, serverCode : Int, serverMessage : String, ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", CrashlyticsHelper.ERROR_CODE_PAYMENT_COUPON.toString())
        crashlyticsData.put("id", data.getID())
        crashlyticsData.put("name", data.getName())
        crashlyticsData.put("nickname", data.getNickName())
        if(data.getBirthYear() == "" == false)
        {
            crashlyticsData.put("birth_year", data.getBirthYear())
        }
        if(data.getMobile() == "" == false)
        {
            crashlyticsData.put("mobile", data.getMobile())
        }
        crashlyticsData.put("email_agree", data.isEmailAgree())
        crashlyticsData.put("sms_agree", data.isSmsAgree())
        crashlyticsData.put("gender", data.getGender())
        crashlyticsData.put("coupon_number", couponNumber)
        crashlyticsData.put("server_code", serverCode.toString())
        crashlyticsData.put("server_message", serverMessage)
        exception = ex
    }

}