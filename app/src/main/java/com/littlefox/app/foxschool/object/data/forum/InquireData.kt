package com.littlefox.app.foxschool.`object`.data.forum

import com.littlefox.app.foxschool.enumerate.InquireType

class InquireData
{
    private var mInquireType : InquireType
    private var mInquireText : String
    private var mUserEmail : String

    constructor()
    {
        mInquireType = InquireType.ERROR
        mInquireText = ""
        mUserEmail = ""
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

    fun getInquireText() : String
    {
        return mInquireText
    }

    fun getUserEmail() : String
    {
        return mUserEmail
    }

}