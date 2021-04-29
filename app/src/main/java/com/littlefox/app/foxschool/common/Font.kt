package com.littlefox.app.foxschool.common

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Type

class Font (context : Context)
{
    /**
     * Noto sans KR bold
     */
    private var robotoBold : Typeface? = null
    /**
     * Noto sans KR regular
     */
    private var robotoRegular : Typeface? = null
    /**
     * Noto sans KR medium
     */
    private var robotoMedium : Typeface? = null
    private var robotoLight : Typeface? = null
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
        robotoBold = Typeface.createFromAsset(mgr, "fonts/Roboto-Bold.ttf")
        robotoRegular = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf")
        robotoMedium = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf")
        robotoLight = Typeface.createFromAsset(mgr, "fonts/Roboto-Light.ttf")
        gungsu = Typeface.createFromAsset(mgr, "fonts/gungsu.ttf")
        if(robotoBold == null)
        {
            robotoBold = Typeface.DEFAULT_BOLD
        }
        if(robotoRegular == null)
        {
            robotoRegular = Typeface.DEFAULT
        }
        if(robotoLight == null)
        {
            robotoLight = Typeface.DEFAULT
        }
        if(robotoMedium == null)
        {
            robotoMedium = Typeface.DEFAULT
        }
        if(gungsu == null)
        {
            gungsu = Typeface.DEFAULT
        }
    }

    fun getRobotoBold() : Typeface?
    {
        return robotoBold;
    }

    fun getRobotoRegular() : Typeface?
    {
        return robotoRegular;
    }

    fun getRobotoLight() : Typeface?
    {
        return robotoLight;
    }

    fun getRobotoMedium() : Typeface?
    {
        return robotoMedium;
    }

    fun getGungsu() : Typeface?
    {
        return gungsu;
    }
}