package com.littlefox.app.foxschool.common

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import java.lang.reflect.Type

class Font (context : Context)
{
    private var typeFaceBold : Typeface? = null
    private var typeFaceMedium : Typeface? = null
    private var typeFaceRegular : Typeface? = null
    private var typeFaceLight : Typeface? = null
    private var gungsu : Typeface? = null

    companion object
    {
        private var _self : Font? = null
        fun getInstance(context : Context?) : Font
        {
            if(_self == null)
            {
                _self = Font(context!!)
            }
            return _self!!
        }
    }

    init
    {
        val mgr = context.assets

        // Android 12 (SDK 31) 부터는 pretendard 폰트 사용
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
        {
            typeFaceBold = Typeface.createFromAsset(mgr, "fonts/Pretendard-Bold.ttf")
            typeFaceMedium = Typeface.createFromAsset(mgr, "fonts/Pretendard-Medium.ttf")
            typeFaceRegular = Typeface.createFromAsset(mgr, "fonts/Pretendard-Regular.ttf")
            typeFaceLight = Typeface.createFromAsset(mgr, "fonts/Pretendard-Light.ttf")
        }
        else
        {
            typeFaceBold = Typeface.createFromAsset(mgr, "fonts/Roboto-Bold.ttf")
            typeFaceMedium = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf")
            typeFaceRegular = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf")
            typeFaceLight = Typeface.createFromAsset(mgr, "fonts/Roboto-Light.ttf")
        }
        gungsu = Typeface.createFromAsset(mgr, "fonts/gungsu.ttf")

        if(typeFaceBold == null)
        {
            typeFaceBold = Typeface.DEFAULT_BOLD
        }
        if(typeFaceMedium == null)
        {
            typeFaceMedium = Typeface.DEFAULT
        }
        if(typeFaceRegular == null)
        {
            typeFaceRegular = Typeface.DEFAULT
        }
        if(typeFaceLight == null)
        {
            typeFaceLight = Typeface.DEFAULT
        }
        if(gungsu == null)
        {
            gungsu = Typeface.DEFAULT
        }
    }

    fun getTypefaceBold() : Typeface?
    {
        return typeFaceBold
    }

    fun getTypefaceMedium() : Typeface?
    {
        return typeFaceMedium
    }

    fun getTypefaceRegular() : Typeface?
    {
        return typeFaceRegular
    }

    fun getRobotoLight() : Typeface?
    {
        return typeFaceLight
    }

    fun getGungsu() : Typeface?
    {
        return gungsu;
    }
}