package com.littlefox.app.foxschool.`object`.data.crashtics

import com.littlefox.app.foxschool.`object`.data.crashtics.base.BaseGenerator
import com.littlefox.app.foxschool.crashtics.CrashlyticsHelper
import java.util.*

class ErrorInAppBillingData : BaseGenerator
{
    constructor(id : String,
                deviceID : String,
                currencyCode : String,
                paymentAmount : String,
                transactionID : String,
                transactionTime : Long,
                receiptData : String,
                serverCode : Int,
                serverMessage : String,
                ex : Exception)
    {
        crashlyticsData = HashMap<String, String>()
        crashlyticsData.put("error_code", CrashlyticsHelper.ERROR_CODE_PAYMENT_INAPP.toString())
        crashlyticsData.put("id", id)
        crashlyticsData.put("device_id", deviceID)
        crashlyticsData.put("currency_code", currencyCode)
        crashlyticsData.put("payment_amount", paymentAmount)
        crashlyticsData.put("name", transactionID)
        crashlyticsData.put("transaction_time", transactionTime.toString())
        crashlyticsData.put("receipt_data", receiptData)
        crashlyticsData.put("server_code", serverCode.toString())
        crashlyticsData.put("server_message", serverMessage)
        exception = ex
    }
}