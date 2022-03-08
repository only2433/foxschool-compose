package com.littlefox.app.foxschool.common

import java.util.*

/**
 * 웹에서 가입한 회원의 정보에 따라 간다.
 */
object LittlefoxLocale
{
    private var mCurrentLocale : String = ""


    fun setLocale(locale : String)
    {
        if(Locale.KOREA.toString().contains(locale))
        {
            mCurrentLocale = Locale.KOREA.toString()
        }
        else if(Locale.JAPAN.toString().contains(locale))
        {
            mCurrentLocale = Locale.JAPAN.toString()
        }
        else if(Locale.SIMPLIFIED_CHINESE.toString().contains(locale))
        {
            mCurrentLocale = Locale.SIMPLIFIED_CHINESE.toString()
        }
        else if("zh_HK".contains(locale) || Locale.TRADITIONAL_CHINESE.toString().contains(locale))
        {
            mCurrentLocale = Locale.TRADITIONAL_CHINESE.toString()
        }
        else
        {
            mCurrentLocale = Locale.ENGLISH.toString()
        }
    }

    fun getCurrentLocale() : String
    {
        return mCurrentLocale
    }

    fun getDeviceLocale() : String
    {
        return Locale.getDefault().toString()
    }
}