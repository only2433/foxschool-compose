package com.littlefox.app.foxschool.`object`.data.forum

import com.littlefox.app.foxschool.enumerate.InquireType

class InquireData
{
    private var mInquireType : InquireType
    private var mInquireText : String
    private var mUserEmail : String

    constructor(inquireType : InquireType, text : String, userEmail : String)
    {
        mInquireType = inquireType
        mInquireText = text
        mUserEmail = userEmail
    }

    fun getInquireType() : String
    {
        when(mInquireType)
        {
            InquireType.ERROR -> return "ERR"
            InquireType.STUDY -> return "STU"
            InquireType.ETC -> return "ETC"
        }
    }

    fun getInquireText() : String = mInquireText

    fun getUserEmail() : String = mUserEmail

}