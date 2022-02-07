package com.littlefox.app.foxschool.common

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.*
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.google.gson.Gson
import com.littlefox.app.foxschool.BuildConfig
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.login.StudentSectionResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.base.MainApplication
import com.littlefox.app.foxschool.enumerate.*
import com.littlefox.library.view.`object`.DisPlayMetricsObject
import com.littlefox.logmonitor.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by 정재현 on 2015-07-07.
 */
class CommonUtils
{
    companion object
    {
        var sCommonUtils : CommonUtils? = null
        lateinit var sContext : Context
        fun getInstance(context : Context) : CommonUtils
        {
            if(sCommonUtils == null)
            {
                sCommonUtils = CommonUtils()
            }
            sContext = context
            return sCommonUtils!!
        }
    }

    fun getDateTime(timeMs : Long) : String
    {
        val date = Date(timeMs)
        return SimpleDateFormat("MM/dd/yyyy").format(date)
    }

    val todayDate : String
        get()
        {
            val calendar = Calendar.getInstance()
            return SimpleDateFormat("yyyy.MM.dd").format(calendar.time)
        }

    fun getTodayYear(timeMs : Long) : String
    {
        val date = Date(timeMs)
        return SimpleDateFormat("yyyy").format(date)
    }

    /**
     * 밀리세컨드를 시간 String  으로 리턴한다.
     * @param timeMs 밀리세컨드
     * @return 시간 String Ex) HH:MM:TT
     */
    fun getMillisecondTime(timeMs : Long) : String
    {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if(hours > 0)
        {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else
        {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    fun getMillisecond(second : Int) : Long
    {
        return (second * Common.SECOND).toLong()
    }

    /**
     * 세컨드를 시간 String  으로 리턴한다.
     * @param time 밀리세컨드
     * @return 시간 String Ex) HH:MM:TT
     */
    fun getSecondTime(time : Int) : String
    {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val seconds = time % 60
        val minutes = time / 60 % 60
        val hours = time / 3600
        mFormatBuilder.setLength(0)
        return if(hours > 0)
        {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else
        {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }


    fun getTodayDateText() : String
    {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyy.MM.dd").format(calendar.time)
    }

    /**
     * 숙제 날짜 변환
     * - 형식 : MM.dd
     * - MM과 dd가 0으로 시작하는 경우 앞의 0 제거
     * (Ex) 09.05 -> 9.5
     */
    fun getHomeworkDateText(date : String) : String
    {
        val fullDate : Date = SimpleDateFormat("yyyy-MM-dd").parse(date)
        return SimpleDateFormat("M.d").format(fullDate)
    }

    /**
     * 학습일시 날짜 변환
     * - 형식 : YYYY.MM.dd 오전/오후 h:mm
     * - MM, dd, hh가 0으로 시작하는 경우 앞의 0 제거
     * - 모바일인 경우 오전/오후 부터 한 줄 띄워서 표시
     */
    fun getStudyCompleteDateText(date : String) : String
    {
        val fullDate : Date = SimpleDateFormat("yyyy.MM.dd hh:mm").parse(date)

        if (checkTablet)
        {
            return SimpleDateFormat("yyyy.M.d a h:mm").format(fullDate)
        }
        else
        {
            return SimpleDateFormat("yyyy.M.d \na h:mm").format(fullDate)
        }
    }

    fun showDeviceInfo()
    {
        Log.f("BRAND : " + Build.BRAND)
        Log.f("DEVICE : " + Build.DEVICE)
        Log.f("MODEL : " + Build.MODEL)
        Log.f("VERSION SDK : " + Build.VERSION.SDK_INT)
        Log.f("APP VERSION : " + getPackageVersionName(Common.PACKAGE_NAME))
        Log.f("Device ID : $secureDeviceID")
        Log.f("WIDTH PIXEL : " + MainApplication.sDisPlayMetrics!!.widthPixels + ", HEIGHT PIXEL : " + MainApplication.sDisPlayMetrics!!.heightPixels)
        Log.f("Language : " + Locale.getDefault().toString())
        Log.f("Available Storage : $availableStorageSize")
        Log.f("checkTabletDeviceWithProperties : " + checkTabletDeviceWithProperties())
        Log.f("checkTabletDeviceWithUserAgent : " + checkTabletDeviceWithUserAgent(sContext))
    }

    fun initFeature()
    {
        if(isTabletModel)
        {
            setSharedPreference(Common.PARAMS_CHECK_TABLET, true)
            Log.f("IS_TABLET TRUE")
        }
        else
        {
            setSharedPreference(Common.PARAMS_CHECK_TABLET, false)
            Log.f("IS_TABLET FALSE")
        }

        if(isHaveNavigationBar())
        {
            Log.f("HAVE NAVIGATION BAR")
            Feature.HAVE_NAVIGATION_BAR = true
        } else
        {
            Log.f("NOT NAVIGATION BAR")
            Feature.HAVE_NAVIGATION_BAR = false
        }
        if(isDisplayMinimumSize)
        {
            Log.f("MINIMUM DISPLAY SIZE")
            Feature.IS_MINIMUM_DISPLAY_SIZE = true
        } else
        {
            Log.f("SUITABLE DISPLAY SIZE")
            Feature.IS_MINIMUM_DISPLAY_SIZE = false
        }

        if(checkTablet)
        {
            Log.f("비율 : " + displayWidthPixel.toFloat() / displayHeightPixel.toFloat())
        }
        else
        {
            Log.f("비율 : " + displayHeightPixel.toFloat() / displayWidthPixel.toFloat())

        }

        if(checkTablet)
        {
            val currentDisplayRadio : Float = (displayWidthPixel.toFloat() / displayHeightPixel.toFloat())
            if(currentDisplayRadio < Common.MINIMUM_TABLET_DISPLAY_RADIO)
            {
                Log.f("TABLET = 4 : 3 비율 ")
                setSharedPreference(Common.PARAMS_CHECK_TABLET_DEVICE_RADIO, DisplayTabletType.RADIO_4_3.toString())
            } else
            {
                Log.f("TABLET = 16 : 9 비율 ")
                setSharedPreference(Common.PARAMS_CHECK_TABLET_DEVICE_RADIO, DisplayTabletType.DEFAULT.toString())
            }
        }
        else
        {
            val currentDisplayRadio : Float = (displayHeightPixel.toFloat() / displayWidthPixel.toFloat())


            if(currentDisplayRadio > Common.PHONE_DISPLAY_RADIO_FLIP)
            {
                setSharedPreference(Common.PARAMS_CHECK_PHONE_DEVICE_RADIO, DisplayPhoneType.RADIO_FLIP.toString())
            }
            else if(currentDisplayRadio > Common.PHONE_DISPLAY_RADIO_20_9)
            {
                Log.f("PHONE = 20 : 9 비율 ")
                setSharedPreference(Common.PARAMS_CHECK_PHONE_DEVICE_RADIO, DisplayPhoneType.RADIO_20_9.toString())
            }
            else
            {
                Log.f("PHONE = 16 : 9 비율 ")
                setSharedPreference(Common.PARAMS_CHECK_PHONE_DEVICE_RADIO, DisplayPhoneType.DEFAULT.toString())
            }
        }

        if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
        {
            Feature.IS_SUPPORT_LITTLEFOX_CLASS = true
        } else
        {
            Feature.IS_SUPPORT_LITTLEFOX_CLASS = false
        }

    }

    fun getPhoneDisplayRadio(): DisplayPhoneType
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        val value : String = pref.getString(Common.PARAMS_CHECK_PHONE_DEVICE_RADIO, DisplayPhoneType.DEFAULT.toString()).toString()
        return DisplayPhoneType.toDisplayPhoneType(value)
    }

    fun getTabletDisplayRadio() : DisplayTabletType
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        val value : String = pref.getString(Common.PARAMS_CHECK_TABLET_DEVICE_RADIO, DisplayTabletType.DEFAULT.toString()).toString()
        return DisplayTabletType.toDisplayTabletType(value)
    }

    /**
     * 방어코드 가끔 OS 결함으로 width , height 가 잘못들어올때 방어코드 처리
     */
    /**
     * <pre>
     * Window 의 정보를 얻어온다.
    </pre> *
     *
     * @return
     */
    fun windowInfo()
    {
        var width = 0
        var height = 0
        if(MainApplication.sDisPlayMetrics == null)
        {
            MainApplication.sDisPlayMetrics = DisplayMetrics()
        }
        (sContext as Activity).windowManager.defaultDisplay.getMetrics(MainApplication.sDisPlayMetrics)
        width = MainApplication.sDisPlayMetrics!!.widthPixels
        height = MainApplication.sDisPlayMetrics!!.heightPixels
        /**
         * 방어코드 가끔 OS 결함으로 width , height 가 잘못들어올때 방어코드 처리
         */
        if(isTabletModel)
        {
            if(MainApplication.sDisPlayMetrics!!.widthPixels < MainApplication.sDisPlayMetrics!!.heightPixels)
            {
                MainApplication.sDisPlayMetrics!!.widthPixels = height
                MainApplication.sDisPlayMetrics!!.heightPixels = width
            }
        } else
        {
            if(MainApplication.sDisPlayMetrics!!.widthPixels > MainApplication.sDisPlayMetrics!!.heightPixels)
            {
                MainApplication.sDisPlayMetrics!!.widthPixels = height
                MainApplication.sDisPlayMetrics!!.heightPixels = width
            }
        }
        val `object` = DisPlayMetricsObject(
            MainApplication.sDisPlayMetrics!!.widthPixels.toFloat(),
            MainApplication.sDisPlayMetrics!!.heightPixels.toFloat()
        )
        setPreferenceObject(Common.PARAMS_DISPLAY_METRICS, `object`)
    }

    /**
     * 1080 * 1920  기준으로 멀티 해상도의 픽셀을 계산한다. Tablet 은 1920 * 1200
     * @param value 1080 * 1920  의 픽셀
     * @return
     */
    fun getPixel(value : Int) : Int
    {
        try
        {
            if(MainApplication.sDisplayFactor == 0.0f)
            {
                if(checkTablet)
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.widthPixels / 1920.0f
                else
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.widthPixels / 1080.0f
            }
        } catch(e : NullPointerException)
        {
            val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                Common.PARAMS_DISPLAY_METRICS,
                DisPlayMetricsObject::class.java
            ) as DisPlayMetricsObject?
            if(checkTablet)
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.widthPixel / 1920.0f
            else
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.widthPixel / 1080.0f
        }
        return (value * MainApplication.sDisplayFactor).toInt()
    }

    /**
     * 1080 * 1920  기준으로 멀티 해상도의 픽셀을 계산한다. Tablet 은 1920 * 1200
     * @param value 1080 * 1920  의 픽셀
     * @return
     */
    fun getPixel(value : Float) : Float
    {
        try
        {
            if(MainApplication.sDisplayFactor == 0.0f)
            {
                if(checkTablet)
                {
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.widthPixels / 1920.0f
                }
                else
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.widthPixels / 1080.0f
            }
        } catch(e : NullPointerException)
        {
            val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                Common.PARAMS_DISPLAY_METRICS,
                DisPlayMetricsObject::class.java
            ) as DisPlayMetricsObject?
            if(checkTablet)
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.widthPixel / 1920.0f
            else
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.widthPixel / 1080.0f
        }
        return value * MainApplication.sDisplayFactor
    }

    /**
     * 1080 * 1920 기준으로 멀티 해상도의 픽셀을 계산한다. Tablet 은 1920 * 1200
     * @param value 1080 * 1920 의 픽셀
     * @return
     */
    fun getHeightPixel(value : Int) : Int
    {
        try
        {
            if(MainApplication.sDisplayFactor == 0.0f)
            {
                if(checkTablet)
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.heightPixels / 1200.0f
                else
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.heightPixels / 1920.0f
            }
        } catch(e : NullPointerException)
        {
            val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                Common.PARAMS_DISPLAY_METRICS,
                DisPlayMetricsObject::class.java
            ) as DisPlayMetricsObject?
            if(checkTablet)
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.heightPixel / 1200.0f
            else
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.heightPixel / 1920.0f
        }
        return (value * MainApplication.sDisplayFactor).toInt()
    }

    /**
     * 1080 * 1920기준으로 멀티 해상도의 픽셀을 계산한다.
     * @param value 1080 * 1920 의 픽셀
     * @return
     */
    fun getHeightPixel(value : Float) : Float
    {
        try
        {
            if(MainApplication.sDisplayFactor == 0.0f)
            {
                if(checkTablet)
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.heightPixels / 1200.0f
                else
                    MainApplication.sDisplayFactor = MainApplication.sDisPlayMetrics!!.heightPixels / 1920.0f
            }
        } catch(e : NullPointerException)
        {
            val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                Common.PARAMS_DISPLAY_METRICS,
                DisPlayMetricsObject::class.java
            ) as DisPlayMetricsObject?
            if(checkTablet)
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.heightPixel / 1200.0f
            else
                MainApplication.sDisplayFactor = disPlayMetricsObject!!.heightPixel / 1080.0f
        }
        return value * MainApplication.sDisplayFactor
    }

    /**
     * 최소 지원 해상도를 리턴
     * @return
     */
    val minDisplayWidth : Float
        get() = 1080.0f

    /**
     * 저장한 프리퍼런스를 불러온다.
     * @param key  해당 값의 키값
     * @param type 데이터의 타입
     * @return
     */
    fun getSharedPreference(key : String?, type : DataType) : Any
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        when(type)
        {
            DataType.TYPE_BOOLEAN -> return pref.getBoolean(key, false)
            DataType.TYPE_INTEGER -> return pref.getInt(key, -1)
            DataType.TYPE_STRING -> return pref.getString(key, "").toString()
        }
        return pref.getBoolean(key, false)
    }

    fun getSharedPreferenceString(key : String, defaultValue : String) : String
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        return pref.getString(key, defaultValue).toString()
    }

    fun getSharedPreferenceInteger(key : String, defaultValue : Int) : Int
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        return pref.getInt(key, defaultValue)
    }

    /**
     * 해당 프리퍼런스를 저장한다.
     * @param key 해당 값의 키값
     * @param object 저장할 데이터
     */
    fun setSharedPreference(key : String, data : Any)
    {
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        val editor : SharedPreferences.Editor = pref.edit()
        if(data is Boolean)
        {
            editor.putBoolean(key, (data as Boolean))
        } else if(data is Int)
        {
            editor.putInt(key, (data as Int))
        } else if(data is String)
        {
            editor.putString(key, data as String)
        }
        editor.commit()
    }

    /**
     * 현재 디스플레이의 가로 해상도를 리턴
     * @return
     */
    val displayWidthPixel : Int
        get()
        {
            if(MainApplication.sDisPlayMetrics == null)
            {
                val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                    Common.PARAMS_DISPLAY_METRICS,
                    DisPlayMetricsObject::class.java
                ) as DisPlayMetricsObject?
                if(disPlayMetricsObject != null)
                {
                    return disPlayMetricsObject.widthPixel.toInt()
                } else
                {
                    return 0
                }
            }
            return MainApplication.sDisPlayMetrics!!.widthPixels
        }

    /**
     * 현재 디스플레이의 세로 해상도를 리턴
     * @return
     */
    val displayHeightPixel : Int
        get()
        {
            if(MainApplication.sDisPlayMetrics == null)
            {
                val disPlayMetricsObject : DisPlayMetricsObject? = getPreferenceObject(
                    Common.PARAMS_DISPLAY_METRICS,
                    DisPlayMetricsObject::class.java
                ) as DisPlayMetricsObject?
                if(disPlayMetricsObject != null)
                {
                    return disPlayMetricsObject.heightPixel.toInt()
                } else
                {
                    return 0
                }
            }
            return MainApplication.sDisPlayMetrics!!.heightPixels
        }

    /**
     * 해상도가 특정 이하의 해상도인지 확인하는 메소드
     * @return TRUE : Minimum 보다 이하 , FALSE : Minimum 보다 이상
     */
    val isDisplayMinimumSize : Boolean
        get()
        {
            Log.i("CommonUtils.getDisplayWidthPixel(context) : $displayWidthPixel")
            if(minDisplayWidth > displayWidthPixel)
            {
                return true
            } else
            {
                return false
            }
        }

    /**
     * 현재 모델이 타블릿인지 아닌지 확인
     * @return
     */
    val isTabletModel : Boolean
        get()
        {
            return checkTabletDeviceWithScreenSize(sContext)
                    && checkTabletDeviceWithProperties()
        }

    val checkTablet : Boolean
        get()
        {
            return getSharedPreference(Common.PARAMS_CHECK_TABLET, DataType.TYPE_BOOLEAN) as Boolean
        }

    private fun checkTabletDeviceWithScreenSize(context : Context) : Boolean
    {
        val device_large =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        Log.f("device_large : $device_large")
        if(device_large)
        {
            val metrics = DisplayMetrics()
            val activity = context as Activity
            activity!!.windowManager.defaultDisplay.getMetrics(metrics)
            Log.f("metrics.densityDpi : " + metrics.densityDpi)
            if(metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH || metrics.densityDpi == DisplayMetrics.DENSITY_TV || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH || metrics.densityDpi == 136 || metrics.densityDpi == 180 || metrics.densityDpi == 240 || metrics.densityDpi == 300 || metrics.densityDpi == 306 || metrics.densityDpi == 330 || metrics.densityDpi == DisplayMetrics.DENSITY_280 || metrics.densityDpi == DisplayMetrics.DENSITY_340 || metrics.densityDpi == DisplayMetrics.DENSITY_360 || metrics.densityDpi == 372 || metrics.densityDpi == DisplayMetrics.DENSITY_420 || metrics.densityDpi == DisplayMetrics.DENSITY_XXHIGH)
            {
                return true
            }
        }
        return false
    }

    private fun checkTabletDeviceWithProperties() : Boolean
    {
        var isTablet = false
        isTablet = try
        {
            val ism = Runtime.getRuntime().exec("getprop ro.build.characteristics").inputStream
            val bts = ByteArray(1024)
            ism.read(bts)
            ism.close()
            String(bts).toLowerCase().contains("tablet")
        } catch(t : Throwable)
        {
            t.printStackTrace()
            return false
        }
        if(Build.MODEL == "iPlay_20")
        {
            isTablet = true
        }
        return isTablet
    }

    private fun checkTabletDeviceWithUserAgent(context : Context) : Boolean
    {
        try
        {
            var webView : WebView? = WebView(context)
            val ua : String = webView?.getSettings()?.getUserAgentString().toString()
            webView = null
            if(ua.contains("Mobile Safari") && Build.MODEL == "BTV-W09" == false && Build.MODEL == "BTV-DL09" == false && Build.MODEL == "SHT-W09" == false && Build.MODEL == "SM-T380" == false && Build.MODEL == "SM-T385K" == false && Build.MODEL == "M40" == false && Build.MODEL == "SM-T295N" == false)
            {
                Log.f("Mobile Safari")
                return false
            } else
            {
                Log.f("Tablet Safari")
                return true
            }
        } catch(e : Exception)
        {
            return false
        }
    }

    /**
     * 패키지버젼코드 확인
     *
     * @return 패키지 버젼 코드
     */
    val packageVersionCode : Int
        get()
        {
            var result = -1
            try
            {
                val pi : PackageInfo? =
                    sContext.packageManager.getPackageInfo(Common.PACKAGE_NAME, 0)
                if(pi != null)
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    {
                        result = pi.longVersionCode.toInt()
                    } else
                    {
                        result = pi.versionCode
                    }
                }
            } catch(ex : Exception)
            {
                Log.f("getPackageVersionCode Error : " + ex.message)
            }
            return result
        }

    /**
     * 패키지 버전 네임 확인
     * @return 패키지 버전 네임
     */
    fun getPackageVersionName(packageName : String) : String
    {
        var result = ""
        try
        {
            val pi : PackageInfo? = sContext.packageManager.getPackageInfo(packageName, 0)
            if(pi != null) result = pi.versionName
        } catch(ex : Exception)
        {
            Log.f("getPackageVersionName Error : " + ex.message)
        }
        return result
    }

    /**
     * 인스톨 되어있나 검색한다.
     * @param packageName 해당 패키지 명
     * @return
     */
    fun isInstalledPackage(packageName : String) : Boolean
    {
        var result = true
        try
        {
            val intent : Intent? = sContext.packageManager.getLaunchIntentForPackage(packageName)
            if(intent == null)
            {
                result = false
            }
        } catch(e : Exception)
        {
            result = false
        }
        return result
    }

    /**
     * 기기 고유 코드를 전달한다.
     * @return
     */
    val secureDeviceID : String
        get() = Settings.Secure.getString(sContext.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp : Float) : Float
    {
        val resources = sContext.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px : Float) : Float
    {
        val resources = sContext.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi / 160f)
    }

    val availableStorageSize : Long
        get()
        {
            val stat = StatFs(Environment.getDataDirectory().path)
            val result : Long
            result = stat.getAvailableBlocksLong() * stat.getBlockSizeLong()
            return result / (1024 * 1024)
        }

    /**
     * 비디오파일이 다운로드된 Internal Storage폴더의 사이즈를 리턴
     */
    val sizeVideoFileStorage : Long
        get()
        {
            var totalSize : Long = 0
            val appBaseFolder : File = File(Common.PATH_APP_ROOT)
            for(f in appBaseFolder.listFiles())
            {
                totalSize += if(f.isDirectory)
                {
                    val dirSize = browseFiles(f)
                    dirSize
                } else
                {
                    f.length()
                }
            }
            Log.f("App uses $totalSize total bytes")
            return totalSize / (1024 * 1024)
        }

    private fun browseFiles(dir : File) : Long
    {
        var dirSize : Long = 0
        for(f in dir.listFiles())
        {
            dirSize += f.length()
            if(f.isDirectory)
            {
                dirSize += browseFiles(f)
            }
        }
        return dirSize
    }

    fun getDrawableResourceFromString(context : Context, name : String) : Int
    {
        return context.resources.getIdentifier(
            name,
            "drawable",
            context.applicationContext.packageName
        )
    }

    fun getBitmapFromDrawable(mDrawable : Drawable, width : Int, height : Int) : Bitmap
    {
        val bitmap : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        mDrawable.setBounds(0, 0, width, height)
        mDrawable.draw(canvas)
        return bitmap
    }

    fun getRoundedCornerBitmap(bitmap : Bitmap) : Bitmap
    {
        val output : Bitmap =
            Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        val rectF = RectF(rect)
        val roundPx = 12f
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun getRoundedCornerRect(width : Int, height : Int, color : Int) : Drawable
    {
        val output : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, width, height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, getPixel(20).toFloat(), getPixel(20).toFloat(), paint)
        return getDrawableFromBitmap(output)
    }

    fun getDrawableFromBitmap(mBitmap : Bitmap?) : Drawable
    {
        return BitmapDrawable(sContext.resources, mBitmap)
    }

    fun getScaledDrawable(width : Int, height : Int, drawable : Int) : Drawable
    {
        var bitmap : Bitmap = BitmapFactory.decodeResource(sContext.resources, drawable)
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return getDrawableFromBitmap(bitmap)
    }

    /**
     * 앱버젼이 같은지 확인
     * @param appVersion 서버의 버젼
     * @return TRUE : 현재 로컬버젼과 같다.  FALSE : 현재 로컬버젼과 다르다.
     */
    fun isAppVersionEqual(appVersion : String) : Boolean
    {
        return appVersion == getPackageVersionName(Common.PACKAGE_NAME)
    }

    /**
     * 현재 버젼과 업데이트 되는 버젼을 비교하기 위해
     * 이전에 기억하는 팝업 정보도 삭제한다.
     * @return TRUE : 현재 버젼과 업데이트 되는 버젼과 같다.
     *
     * FALSE : 현재 버젼과 업데이트 되는 버젼과 다르다.
     */
    fun verifyCurrentVersionCode() : Boolean
    {
        val registerVersion =
            getSharedPreference(Common.PARAMS_REGISTER_APP_VERSION, DataType.TYPE_INTEGER) as Int
        val currentVersion = packageVersionCode
        Log.i("registerVersion : $registerVersion, currentVersion : $currentVersion")
        if(currentVersion != registerVersion)
        {
            setSharedPreference(Common.PARAMS_REGISTER_APP_VERSION, currentVersion)
            return false
        }
        return true
    }

    /**
     * 오브젝트 클래스를 불러오는 프리퍼런스
     * @param key 키값
     * @param className 클래스 네임
     * @return
     */
    fun getPreferenceObject(key : String?, className : Class<*>?) : Any?
    {
        var result : Any? = null
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        val loadObjectString : String = pref.getString(key, "").toString()
        if(loadObjectString == "" == false)
        {
            result = Gson().fromJson<Any>(loadObjectString, className)
        }
        return result
    }

    /**
     * 오브젝트 클래스를 저장하는 프리퍼런스
     * @param key 키값
     * @param object 저장할 오브젝트
     */
    fun setPreferenceObject(key : String?, `object` : Any?)
    {
        var saveObjectString = ""
        val pref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext)
        val editor : SharedPreferences.Editor = pref.edit()
        if(`object` != null)
        {
            saveObjectString = Gson().toJson(`object`)
        }
        editor.putString(key, saveObjectString)
        editor.commit()
    }

    fun getTranslateYAnimation(duration : Long, fromYValue : Float, toYValue : Float) : Animation?
    {
        return getTranslateYAnimation(duration, fromYValue, toYValue, null)
    }

    fun getTranslateYAnimation(
        duration : Long,
        fromYValue : Float,
        toYValue : Float,
        interpolator : Interpolator?
    ) : Animation?
    {
        var anim : Animation? = null
        anim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.ABSOLUTE,
            fromYValue,
            Animation.ABSOLUTE,
            toYValue
        )
        anim.setDuration(duration)
        anim.setFillAfter(true)
        if(interpolator != null)
        {
            anim.setInterpolator(interpolator)
        }
        return anim
    }

    fun getTranslateXAnimation(duration : Long, fromXValue : Float, toXValue : Float) : Animation?
    {
        return getTranslateXAnimation(duration, fromXValue, toXValue, null)
    }

    fun getTranslateXAnimation(
        duration : Long,
        fromXValue : Float,
        toXValue : Float,
        interpolator : Interpolator?
    ) : Animation?
    {
        var anim : Animation? = null
        anim = TranslateAnimation(
            Animation.ABSOLUTE,
            fromXValue,
            Animation.ABSOLUTE,
            toXValue,
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.0f
        )
        anim.setDuration(duration)
        anim.setFillAfter(true)
        if(interpolator != null)
        {
            anim.setInterpolator(interpolator)
        }
        return anim
    }

    fun getAlphaAnimation(duration : Long, fromValue : Float, toValue : Float) : Animation?
    {
        var anim : Animation? = null
        anim = AlphaAnimation(fromValue, toValue)
        anim.setDuration(duration)
        anim.setFillAfter(true)
        return anim
    }

    fun getRotateAnimation(duration : Long, fromValue : Float, toValue : Float) : Animation?
    {
        var anim : Animation? = null
        anim = RotateAnimation(
            fromValue,
            toValue,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.setDuration(duration)
        anim.setFillAfter(true) // FillAfter: true 상태여야 애니메이션을 멈췄을 때 진행하던 위치에서 멈춤 (일시정지를 위함)
        anim.setInterpolator(LinearInterpolator())
        return anim
    }

    fun setStatusBar(color : Int)
    {
        val window = (sContext as Activity).window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    fun showErrorSnackMessage(coordinatorLayout : CoordinatorLayout, message : String)
    {
        showErrorSnackMessage(
            coordinatorLayout,
            message,
            sContext.resources.getColor(R.color.color_fff348),
            R.drawable.snackbar_warning,
            Gravity.CENTER
        )
    }

    fun showSuccessSnackMessage(coordinatorLayout : CoordinatorLayout, message : String)
    {
        showSnackMessage(
            coordinatorLayout,
            message,
            sContext.resources.getColor(R.color.color_57e2ff),
            Gravity.CENTER
        )
    }

    fun showErrorSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : String,
        gravity : Int
    )
    {
        showErrorSnackMessage(
            coordinatorLayout,
            message,
            sContext.resources.getColor(R.color.color_fff348),
            R.drawable.snackbar_warning,
            gravity
        )
    }

    fun showSuccessSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : String,
        gravity : Int
    )
    {
        showSnackMessage(
            coordinatorLayout,
            message,
            sContext.resources.getColor(R.color.color_57e2ff),
            gravity
        )
    }

    @JvmOverloads
    fun showSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : String,
        color : Int,
        gravity : Int = -1
    )
    {
        Log.f("gravity : $gravity")
        val snackbar : Snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
        val view : View = snackbar.getView()
        val textView : TextView = view.findViewById<View>(R.id.snackbar_text) as TextView
        if(gravity != -1)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY)
            }
            textView.setGravity(gravity)
        }
        textView.maxLines = 3
        textView.typeface = Font.getInstance(sContext).getRobotoMedium()
        textView.setTextColor(color)
        snackbar.show()
    }

    fun showErrorSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : String,
        color : Int,
        icon : Int,
        gravity : Int = -1
    )
    {
        val snackbar = Snackbar.make(coordinatorLayout, " $message", Snackbar.LENGTH_LONG)

        val view : View = snackbar.view
        val textView = view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        (textView.parent as SnackbarContentLayout).gravity = Gravity.CENTER

        if(gravity != -1)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                textView.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
            }
            textView.gravity = Gravity.CENTER
        }
        textView.maxLines = 3
        textView.typeface = Font.getInstance(sContext).getRobotoMedium()
        textView.setTextColor(color)
        snackbar.show()
    }

    fun showSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : Array<String>,
        color : IntArray
    )
    {
        var beforeCount = 0
        var messageText : String = ""
        val spannableStringBuilder : SpannableStringBuilder
        for(s in message)
        {
            messageText += s
        }
        spannableStringBuilder = SpannableStringBuilder(messageText)
        for(i in message.indices)
        {
            var currentCount = 0
            for(j in 0 until i + 1)
            {
                currentCount += message[j].length
            }
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(color[i]),
                beforeCount,
                currentCount,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            beforeCount = currentCount
        }
        val snackbar : Snackbar =
            Snackbar.make(coordinatorLayout, messageText, Snackbar.LENGTH_SHORT)
        val view : View = snackbar.getView()
        val textView : TextView = view.findViewById<View>(R.id.snackbar_text) as TextView
        textView.typeface = Font.getInstance(sContext).getRobotoMedium()
        textView.setText(spannableStringBuilder)
        snackbar.show()
    }

    fun showSnackMessage(
        coordinatorLayout : CoordinatorLayout,
        message : Array<String>,
        color : IntArray,
        listener : View.OnClickListener?
    )
    {
        var beforeCount = 0
        var messageText : String = ""
        val spannableStringBuilder : SpannableStringBuilder
        for(s in message)
        {
            messageText += s
        }
        spannableStringBuilder = SpannableStringBuilder(messageText)
        for(i in message.indices)
        {
            var currentCount = 0
            for(j in 0 until i + 1)
            {
                currentCount += message[j].length
            }
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(color[i]),
                beforeCount,
                currentCount,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            beforeCount = currentCount
        }
        val snackbar : Snackbar =
            Snackbar.make(coordinatorLayout, messageText, Snackbar.LENGTH_SHORT)
        val view : View = snackbar.getView()
        val textView : TextView = view.findViewById<View>(R.id.snackbar_text) as TextView
        textView.typeface = Font.getInstance(sContext).getRobotoMedium()
        textView.setText(spannableStringBuilder)
        textView.setOnClickListener(listener)
        snackbar.show()
    }

    /**
     * 플레이 시간으로 프리뷰 시간을 구하는 메소드
     * @param totalPlayTime 토탈 플레이 시간
     * @return
     */
    fun getPreviewTime(totalPlayTime : Int) : Int
    {
        val MIN_PREVIEW_TIME = 10
        val MAX_PREVIEW_TIME = 60
        val TERM_PREVIEW_TIME = 10
        val MIN_TOTAL_PLAY_TIME = 40
        val MAX_TOTAL_PLAY_TIME = 120
        val TERM_PLAY_TIME = 20
        var result = 0
        var count = 0
        while(result == 0)
        {
            if(totalPlayTime < MIN_TOTAL_PLAY_TIME + count * TERM_PLAY_TIME)
            {
                result = MIN_PREVIEW_TIME + TERM_PREVIEW_TIME * count
            } else if(totalPlayTime >= MAX_TOTAL_PLAY_TIME)
            {
                result = MAX_PREVIEW_TIME
            }
            count++
        }
        return result
    }

    /**
     * 30일이 지났는 지 체크
     * @param subscribeEndMiliseconds 해당 시간
     * @return TRUE: 30일 이 넘음 , FALSE : 30일이 지나지 않음
     */
    fun isOverPayDay(subscribeEndMiliseconds : Long) : Boolean
    {
        Log.f("Today : " + getDateTime(System.currentTimeMillis()))
        Log.f("Pay End Day : " + getDateTime(subscribeEndMiliseconds))
        if(System.currentTimeMillis() >= subscribeEndMiliseconds)
        {
            return true
        } else
        {
            return false
        }
    }

    fun getAdded31Days(currentSubscribeMilliseconds : Long) : Long
    {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentSubscribeMilliseconds
        calendar.add(Calendar.DATE, 31)
        return calendar.timeInMillis
    }

    fun getAdded1year(currentSubscribeMilliseconds : Long) : Long
    {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentSubscribeMilliseconds
        calendar.add(Calendar.YEAR, 1)
        return calendar.timeInMillis
    }


    fun getLastStudyDate(timeInfo : String?) : String
    {
        var date : Date? = null
        val dateFormat : DateFormat
        val calendar = Calendar.getInstance()
        dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val resultFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        try
        {
            date = dateFormat.parse(timeInfo)
        } catch(e : ParseException)
        {
            e.printStackTrace()
        }
        return resultFormat.format(date)
    }

    fun getMillisecondFromDate(dateText : String?) : Long
    {
        val format = SimpleDateFormat("yyyy-MM-dd")
        var date : Date? = null
        try
        {
            date = format.parse(dateText)
        } catch(e : ParseException)
        {
        }
        return date!!.time
    }

    // 두 날짜 사이의 기간
    fun getTerm(startDate : String, endDate : String) : Int
    {
        val startDateMilli = getMillisecondFromDate(startDate)
        val endDateMilli = getMillisecondFromDate(endDate)
        if (startDateMilli < endDateMilli)
        {
            val termMilli = (endDateMilli - startDateMilli)
            val termDate = (termMilli / (24 * 60 * 60 * Common.SECOND)).toInt()
            return termDate
        }
        return 0
    }

    fun startLinkMove(link : String?)
    {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(link))
        sContext.startActivity(intent)
    }

    /**
     * 소프트 네비게이션 바가 있는지 체크
     * @return
     */
    fun isHaveNavigationBar() : Boolean
    {
        val appUsableSize = getAppUsableScreenSize()
        val realScreenSize = getRealScreenSize()
        if(appUsableSize.y < realScreenSize.y)
        {
            return true
        } else
        {
            return false
        }
    }

    /**
     * 네이게이션바 사이즈를 리턴한다.
     * @return
     */
    fun getNavigationBarSize() : Point
    {
        val appUsableSize = getAppUsableScreenSize()
        val realScreenSize = getRealScreenSize()

        // avigation bar on the right
        if(appUsableSize.x < realScreenSize.x)
        {
            return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
        } // navigation bar at the bottom
        return if(appUsableSize.y < realScreenSize.y)
        {
            Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
        } else Point() // navigation bar is not present
    }

    fun getAppUsableScreenSize() : Point
    {
        val windowManager : WindowManager =
            sContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display : Display = windowManager.getDefaultDisplay()
        val size = Point()
        display.getSize(size)
        return size
    }

    fun getRealScreenSize() : Point
    {
        val windowManager : WindowManager =
            sContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display : Display = windowManager.getDefaultDisplay()
        val size = Point()
        display.getRealSize(size)
        return size
    }

    /**
     * 1:1문의 메일 보내기
     */
    fun inquireForDeveloper(sendUrl : String?, message : String? = "")
    {
        var userID = ""
        val userLoginData : UserLoginData? = getPreferenceObject(Common.PARAMS_USER_LOGIN, UserLoginData::class.java) as UserLoginData?

        if(userLoginData != null)
        {
            userID = userLoginData.userID
            Log.f("User ID : $userID")
        }
        else
        {
            userID = "FREE USER"
        }

        val i : Intent
        val strTitle = sContext.resources.getString(R.string.app_name)
        var text = "[${Build.BRAND}] Model: ${Build.MODEL}, OS: ${Build.VERSION.RELEASE}, Ver: ${getPackageVersionName(Common.PACKAGE_NAME)}, ID : $userID"
        if (message != null) text += "\n\n $message" // 사용자가 입력한 메세지가 있는 경우 기존 포맷 다음에 메세지 추가

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            i = Intent(Intent.ACTION_SEND)
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(sendUrl))
            i.putExtra(Intent.EXTRA_TEXT, text)
            val file = File(Log.getLogfilePath())
            val uri : Uri = FileProvider.getUriForFile(sContext, BuildConfig.APPLICATION_ID, file)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            i.setDataAndType(uri, sContext.contentResolver.getType(uri))
            i.putExtra(Intent.EXTRA_STREAM, uri)
        }
        else
        {
            i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", sendUrl, null))
            i.putExtra(Intent.EXTRA_TEXT, text)
            val uri = Uri.parse("file://" + Log.getLogfilePath())
            i.putExtra(Intent.EXTRA_STREAM, uri)
        }

        sContext.startActivity(Intent.createChooser(i, strTitle))
    }

    fun getDayNumberSuffixToEN(day : Int) : String
    {
        if(day >= 11 && day <= 13)
        {
            return "th"
        } else when(day % 10)
        {
            1 -> return "st"
            2 -> return "nd"
            3 -> return "rd"
            else -> return "th"
        }
    }

    val availableSelectYears : Array<String?>
        get()
        {
            val MAX_TERM_YEAR = 100
            val availableYears = arrayOfNulls<String>(MAX_TERM_YEAR)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val startYear = calendar[Calendar.YEAR] - (MAX_TERM_YEAR - 1)
            val endYear = calendar[Calendar.YEAR]
            Log.i("startYear : " + startYear + " , Current Year : " + calendar[Calendar.YEAR])
            var count = 0
            for(i in startYear..endYear)
            {
                availableYears[count] = i.toString()
                count++
            }
            Log.i("size : " + availableYears.size)
            return availableYears
        }

    /**
     * 배경색의 뷰를 원 모양의 애니메이션으로 나타나게 동작시킨다.
     * @param view 해당 뷰
     * @param color 배경색
     * @param positionX 시작 위치 X
     * @param positionY 시작 위치 Y
     * @param duration 애니메이션 시간
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAnimateReveal(
        view : View,
        color : Int,
        positionX : Int,
        positionY : Int,
        duration : Int
    )
    {
        showAnimateReveal(view, color, positionX, positionY, false, duration)
    }

    /**
     * 배경색의 뷰를 원 모양의 애니메이션으로 나타나게 동작시킨다.
     * @param view 해당 뷰
     * @param color 배경색
     * @param positionX 시작 위치 X
     * @param positionY 시작 위치 Y
     * @param isAlphaAnimation 알파 애니메이션을 적용할 것인지의 여부
     * @param duration 애니메이션 시간
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAnimateReveal(
        view : View,
        color : Int,
        positionX : Int,
        positionY : Int,
        isAlphaAnimation : Boolean,
        duration : Int
    )
    {
        val finalRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
        val animaterSet = AnimatorSet()
        val revealAnimation : Animator =
            ViewAnimationUtils.createCircularReveal(view, positionX, positionY, 0f, finalRadius)
        view.setBackgroundColor(ContextCompat.getColor(sContext, color))
        if(isAlphaAnimation)
        {
            val alphaAnimation : Animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animaterSet.playTogether(revealAnimation, alphaAnimation)
        }
        animaterSet.setDuration(duration.toLong())
        animaterSet.setInterpolator(AccelerateInterpolator())
        animaterSet.start()
    }

    /**
     * 배경색의 뷰를 원 모양의 애니메이션으로 사라지게 동작시킨다.
     * @param view 해당 뷰
     * @param color 배경색
     * @param positionX 시작 위치 X
     * @param positionY 시작 위치 Y
     * @param duration 애니메이션 시간
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hideAnimateReveal(
        view : View,
        color : Int,
        positionX : Int,
        positionY : Int,
        duration : Int
    )
    {
        hideAnimateReveal(view, color, positionX, positionY, false, duration)
    }

    /**
     * 배경색의 뷰를 원 모양의 애니메이션으로 사라지게 동작시킨다.
     * @param view 해당 뷰
     * @param color 배경색
     * @param positionX 시작 위치 X
     * @param positionY 시작 위치 Y
     * @param isAlphaAnimation 알파 애니메이션을 적용할 것인지의 여부
     * @param duration 애니메이션 시간
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hideAnimateReveal(
        view : View,
        color : Int,
        positionX : Int,
        positionY : Int,
        isAlphaAnimation : Boolean,
        duration : Int
    )
    {
        val initialRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
        val animaterSet = AnimatorSet()
        val revealAnimation : Animator =
            ViewAnimationUtils.createCircularReveal(view, positionX, positionY, initialRadius, 0f)
        view.setBackgroundColor(ContextCompat.getColor(sContext, color))
        if(isAlphaAnimation)
        {
            val alphaAnimation : Animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            animaterSet.playTogether(revealAnimation, alphaAnimation)
        }
        animaterSet.setDuration(duration.toLong())
        animaterSet.setInterpolator(AccelerateInterpolator())
        animaterSet.start()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestPermission(permissionList : ArrayList<String>, requestCode : Int)
    {
        val unAuthorizeList = ArrayList<String>()
        for(i in permissionList.indices)
        {
            if(ContextCompat.checkSelfPermission(
                    sContext,
                    permissionList[i]
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                unAuthorizeList.add(permissionList[i])
            }
        }
        if(unAuthorizeList.size > 0)
        {
            var unAuthorizePermissions : Array<String?>? = arrayOfNulls(unAuthorizeList.size)
            unAuthorizePermissions = unAuthorizeList.toArray(unAuthorizePermissions)
            (sContext as AppCompatActivity?)!!.requestPermissions(
                unAuthorizePermissions,
                requestCode
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getUnAuthorizePermissionList(permissionList : ArrayList<String>) : ArrayList<String>
    {
        val unAuthorizeList = ArrayList<String>()
        for(i in permissionList.indices)
        {
            if(sContext!!.checkSelfPermission(permissionList[i]) != PackageManager.PERMISSION_GRANTED)
            {
                unAuthorizeList.add(permissionList[i])
            }
        }
        return unAuthorizeList
    }

    /**
     * 오디오 녹을을 위한 퍼미션 체크
     */
    fun checkRecordPermission() : Boolean
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (sContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                return false
            }
            return true
        }
        else
        {
            return true
        }
    }

    /**
     * 그레이 처리된 이미지를 원본 이미지로 변경
     * @param view 그레이 이미지
     */
    fun setOriginalImageFromGray(view : ImageView)
    {
        view.colorFilter = null
        view.imageAlpha = 255
    }

    /**
     * 원래의 이미지에서 그레이 처리된 이미지로 변경
     * @param view 원본 이미지
     */
    fun setGrayImageFromOriginal(view : ImageView)
    {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val colorMatrixColorFilter = ColorMatrixColorFilter(matrix)
        view.setColorFilter(colorMatrixColorFilter)
        view.imageAlpha = 128
    }

    /**
     * 나의 학습 의 평점을 알아오는 메소드
     * @param quizCount 퀴즈의 갯수
     * @param correctCount 정답을 맞춘 개수
     * @return 나의 평점
     */
    fun getMyGrade(quizCount : Int, correctCount : Int) : Grade
    {
        var isGradeVeryGood = false
        if(quizCount == correctCount)
        {
            return Grade.EXCELLENT
        }
        if(quizCount < 6)
        {
            if(quizCount - 1 == correctCount) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(quizCount - 2 <= correctCount) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        } else if(quizCount < 11)
        {
            if(quizCount - 1 == correctCount) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(quizCount - 3 <= correctCount) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        } else if(quizCount < 15)
        {
            if(correctCount >= 8) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(correctCount >= 6) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        } else if(quizCount < 17)
        {
            if(correctCount >= 13) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(correctCount >= 11) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        } else if(quizCount < 20)
        {
            if(correctCount >= 15) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(correctCount >= 13) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        } else
        {
            if(correctCount >= 18) isGradeVeryGood = true
            else isGradeVeryGood = false
            if(isGradeVeryGood == false)
            {
                if(correctCount >= 16) return Grade.GOODS
                else return Grade.POOL
            } else return Grade.VERYGOOD
        }
    }

    fun getBookColorType(color : String) : BookColor
    {
        if(color == "red")
        {
            return BookColor.RED
        } else if(color == "orange")
        {
            return BookColor.ORANGE
        } else if(color == "green")
        {
            return BookColor.GREEN
        } else if(color == "blue")
        {
            return BookColor.BLUE
        } else if(color == "purple")
        {
            return BookColor.PURPLE
        } else if(color == "pink")
        {
            return BookColor.PINK
        }
        return BookColor.RED
    }

    fun getBookResource(color : BookColor?) : Int
    {
        when(color)
        {
            BookColor.RED -> return R.drawable.bookshelf_01
            BookColor.ORANGE -> return R.drawable.bookshelf_02
            BookColor.GREEN -> return R.drawable.bookshelf_03
            BookColor.BLUE -> return R.drawable.bookshelf_04
            BookColor.PURPLE -> return R.drawable.bookshelf_05
            BookColor.PINK -> return R.drawable.bookshelf_06
            else -> return R.drawable.bookshelf_01
        }
    }

    fun getBookColorString(color : BookColor?) : String
    {
        when(color)
        {
            BookColor.RED -> return "red"
            BookColor.ORANGE -> return "orange"
            BookColor.GREEN -> return "green"
            BookColor.BLUE -> return "blue"
            BookColor.PURPLE -> return "purple"
            BookColor.PINK -> return "pink"
            else -> return "red"
        }
    }

    fun hideKeyboard()
    {
        Log.f("")
        val inputMethodManager = sContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager //Find the currently focused view, so we can grab the correct window token from it.
        var view : View? = (sContext as AppCompatActivity).getCurrentFocus() //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null)
        {
            view = View(sContext)
        } else
        {
            view.clearFocus()
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * EditText 강제포커싱 할 때 키보드 표시
     */
    fun showKeyboard(view : EditText)
    {
        Log.f("")
        view.requestFocus()
        val inputMethodManager = sContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }


    fun loadMainData() : MainInformationResult
    {
        return getPreferenceObject(
            Common.PARAMS_FILE_MAIN_INFO,
            MainInformationResult::class.java
        ) as MainInformationResult
    }

    fun saveMainData(data : MainInformationResult)
    {
        setPreferenceObject(Common.PARAMS_FILE_MAIN_INFO, data)
    }

    /**
     * 최대 사이즈를 정해놓은 리스트에 아이템을 Push . 최대 개수가 넘게 되면 제일 뒤에 있는 아이템을 pop 한다.
     * @param data 데이터 객체
     * @param list 데이터를 담고 있는 리스트
     * @param maxSize 최대 사이즈
     * @return 결과 리스트
     */
    fun pushData(data : Any?, list : ArrayList<Any?>, maxSize : Int) : ArrayList<Any?>
    {
        if(list.size >= maxSize)
        {
            list.removeAt(list.size - 1)
        }
        list.add(0, data)
        return list
    }

    fun splitWordsIntoStringsThatFit(
        source : String,
        maxWidthPx : Float,
        paint : Paint
    ) : List<String>
    {
        val result = ArrayList<String>()
        val currentLine = ArrayList<String>()
        val sources = source.split("\\s").toTypedArray()
        for(chunk in sources)
        {
            if(paint.measureText(chunk) < maxWidthPx)
            {
                processFitChunk(maxWidthPx, paint, result, currentLine, chunk)
            } else
            { //the chunk is too big, split it.
                val splitChunk = splitIntoStringsThatFit(chunk, maxWidthPx, paint)
                for(chunkChunk in splitChunk)
                {
                    processFitChunk(maxWidthPx, paint, result, currentLine, chunkChunk)
                }
            }
        }
        if(!currentLine.isEmpty())
        {
            result.add(TextUtils.join(" ", currentLine))
        }
        return result
    }

    /**
     * Splits a string to multiple strings each of which does not exceed the width
     * of maxWidthPx.
     */
    private fun splitIntoStringsThatFit(
        source : String,
        maxWidthPx : Float,
        paint : Paint
    ) : List<String>
    {
        if(TextUtils.isEmpty(source) || paint.measureText(source) <= maxWidthPx)
        {
            return Arrays.asList(source)
        }
        val result = ArrayList<String>()
        var start = 0
        for(i in 1..source.length)
        {
            val substr = source.substring(start, i)
            if(paint.measureText(substr) >= maxWidthPx)
            { //this one doesn't fit, take the previous one which fits
                val fits = source.substring(start, i - 1)
                result.add(fits)
                start = i - 1
            }
            if(i == source.length)
            {
                val fits = source.substring(start, i)
                result.add(fits)
            }
        }
        return result
    }

    /**
     * Processes the chunk which does not exceed maxWidth.
     */
    private fun processFitChunk(
        maxWidth : Float,
        paint : Paint,
        result : ArrayList<String>,
        currentLine : ArrayList<String>,
        chunk : String
    )
    {
        currentLine.add(chunk)
        val currentLineStr : String = TextUtils.join(" ", currentLine)
        if(paint.measureText(currentLineStr) >= maxWidth)
        { //remove chunk
            currentLine.removeAt(currentLine.size - 1)
            result.add(TextUtils.join(" ", currentLine))
            currentLine.clear() //ok because chunk fits
            currentLine.add(chunk)
        }
    }

    /**
     * HTML로 되어있는 DATA를 TAG를 제거한 String  값을 리턴한다.
     * @param html 해당 HTML 데이터
     * @return 제거한 String
     */
    fun removeHtmlTag(html : String) : String
    {
        var html = html
        try
        {
            html = html.replace("<(.*?)\\>".toRegex(), " ")
            html = html.replace("<(.*?)\\\n".toRegex(), " ")
            html = html.replaceFirst("(.*?)\\>".toRegex(), " ")
            html = html.replace("&nbsp;".toRegex(), " ")
            html = html.replace("&amp;".toRegex(), " ")
        } catch(e : NullPointerException)
        {
            return ""
        }
        return html
    }

    /**
     * 웹뷰에서 사용하는 Default Header 정보
     * @param needToken Token이 필요한지의 여부
     * @return 헤더
     */
    fun getHeaderInformation(needToken : Boolean) : Map<String, String>
    {
        var token : String = ""
        val deviceType : String =
            if(checkTablet) Common.DEVICE_TYPE_TABLET else Common.DEVICE_TYPE_PHONE
        val result : MutableMap<String, String> = HashMap()
        if(needToken)
        {
            token = "Bearer " + getSharedPreference(Common.PARAMS_ACCESS_TOKEN, DataType.TYPE_STRING) as String
            result["Authorization"] = token
        }
        result["api-locale"] = Locale.getDefault().toString()
        result["api-user-agent"] =
            Common.HTTP_HEADER_APP_NAME.toString() + ":" + deviceType + File.separator + getPackageVersionName(
                Common.PACKAGE_NAME
            ) + File.separator + Build.MODEL + File.separator + Common.HTTP_HEADER_ANDROID + ":" + Build.VERSION.RELEASE
        return result
    }

    /**
     * 단어장의 컨텐츠 뷰 사이즈를 리턴한다. ( 텍스트 라인 개수에 따라 라인 변형 )
     * @param lineCount 텍스트 라인 개수
     * @return 컨텐츠 뷰 사이즈
     */
    fun getVocabularyContentViewSize(lineCount : Int) : Int
    {
        when(lineCount)
        {
            1, 2 ->
                if(checkTablet)
                    return getPixel(122)
                else
                    return getPixel(174)
            3 ->
                if(checkTablet)
                    return getPixel(160)
                else
                    return getPixel(174)
            4 ->
                if(checkTablet)
                    return getPixel(196)
                else
                    return getPixel(230)

            5 ->
                if(checkTablet)
                    return getPixel(232)
                else
                    return getPixel(280)

            6 ->
                if(checkTablet)
                    return getPixel(268)
                else
                    return getPixel(330)
            else ->
                if(checkTablet)
                    return getPixel(304)
                else
                    return getPixel(380)
        }
    }

    fun getRecordHistoryContentSize(lineCount : Int) : Int
    {
        when(lineCount)
        {
            4 ->
                if(checkTablet)
                    return getPixel(158)
                else
                    return getPixel(226)
            5 ->
                if(checkTablet)
                    return getPixel(198)
                else
                    return getPixel(282)
            else ->
                if(checkTablet)
                    return getPixel(118)
                else
                    return getPixel(170)
        }
    }

    fun getDecimalNumber(count : Int) : String
    {
        val df = DecimalFormat("##,###")
        return df.format(count.toLong())
    }

    fun getCountryAddLabel(label : String) : String
    {
        var result = ""
        if(Locale.getDefault().toString().contains(Locale.KOREA.toString()))
        {
            result = "KO_$label"
        } else if(Locale.getDefault().toString().contains(Locale.JAPAN.toString()))
        {
            result = "JP_$label"
        } else if(Locale.getDefault().toString().contains(Locale.SIMPLIFIED_CHINESE.toString()))
        {
            result = "CN_$label"
        } else if(Locale.getDefault().toString().contains(Locale.TRADITIONAL_CHINESE.toString()))
        {
            result = "TW_$label"
        } else
        {
            result = "EN_$label"
        }
        return result
    }

    fun getDateTitle(index : Int) : String
    {
        when(index)
        {
            0 -> return "월요일"
            1 -> return "화요일"
            2 -> return "수요일"
            3 -> return "목요일"
            else -> return "금요일"
        }
    }

    /**
     * 화면에 보일 컨텐츠 이름을 리턴한다. 서브네임이 있을 경우엔 시리즈 명과 같이 노출
     * @param data 컨텐츠 데이터
     * @return 컨텐츠 네임
     */
    fun getContentsName(data : ContentsBaseResult) : String
    {
        var result : String? = ""
        if(data.getSubName().equals(""))
        {
            result = data.getName()
        } else
        {
            result = data.getName() + ": " + data.getSubName()
        }
        return result
    }

    /**
     * 단어장은 서브네임이 있을 경우엔 서브네임을 타이틀로, 없을 경우 컨텐츠 네임으로 보여준다.
     * @param data 컨텐츠 데이터
     * @return 컨텐츠 네임
     */
    fun getVocabularyTitleName(data : ContentsBaseResult) : String
    {
        var result : String = ""
        if(data.getSubName().equals(""))
        {
            result = data.getName()
        } else
        {
            result = data.getSubName()
        }
        return result
    }

    /**
     * 총 플레이타임의 80퍼센트를 봐야 학습 기록을 저장하기위해 총시간에서 80퍼센트를 계산
     * @param duration 학습 가능 플레이 타임
     * @return
     */
    fun getStudyCompleteTime(duration : Int) : Int
    {
        return (duration * 0.8).toInt()
    }

    /**
     * 양끝 공백 제거 메소드
     * @param text 원본 텍스트
     * @return
     */
    fun getReplaceBothEndTrim(text : String) : String
    {
        return text.replace("(^\\p{Z}+|\\p{Z}+$)".toRegex(), "")
    }

    /**
     * 입력한 숫자 데이터에 하이픈을 입력해주는 메소드
     * @param data 입력한 숫자 데이터
     * @return
     */
    fun getPhoneTypeNumber(data : String) : String
    {
        var result = ""
        if(data.length == 8)
        {
            result = data.replaceFirst("^([0-9]{4})([0-9]{4})$".toRegex(), "$1-$2")
        } else if(data.length == 12)
        {
            result = data.replaceFirst("(^[0-9]{4})([0-9]{4})([0-9]{4})$".toRegex(), "$1-$2-$3")
        }
        result = data.replaceFirst("(^02|[0-9]{3})([0-9]{3,4})([0-9]{4})$".toRegex(), "$1-$2-$3")
        return result
    }


    fun getGenderItemList(isNoneNecessary : Boolean) : Array<String?>
    {
        val result : Array<String?>
        val data : Array<String>
        if(isNoneNecessary)
        {
            result = arrayOfNulls(3)
            data = sContext!!.resources.getStringArray(R.array.text_list_gender)
            for(i in result.indices)
            {
                if(i == 0)
                {
                    result[i] = sContext!!.resources.getString(R.string.text_no_select)
                } else
                {
                    result[i] = data[i - 1]
                }
            }
            return result
        } else
        {
            result = sContext.resources.getStringArray(R.array.text_list_gender)
            return result
        }
    }


    fun getYearItemList(isNoneNecessary : Boolean) : Array<String?>
    {
        var maxYear : Int = Integer.valueOf(getTodayYear(System.currentTimeMillis()))
        val itemListSize = maxYear - (maxYear - 100) + 1
        Log.f("maxYear : " + maxYear + ", minYear : " + (maxYear - 100 + 1) + ", itemListSize : " + itemListSize)
        val result = arrayOfNulls<String>(itemListSize)
        for(i in 0 until itemListSize)
        {
            if(isNoneNecessary)
            {
                if(i == 0)
                {
                    result[i] = sContext!!.resources.getString(R.string.text_no_select)
                } else
                {
                    result[i] = maxYear--.toString()
                }
            } else
            {
                result[i] = maxYear--.toString()
            }
        }
        return result
    }

    fun saveBitmapToFileCache(bitmap : Bitmap, filename : String?)
    {
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileCacheItem = File(file, filename)
        var out : OutputStream? = null
        try
        {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch(e : Exception)
        {
            e.printStackTrace()
        } finally
        {
            try
            {
                out?.close()
            } catch(e : IOException)
            {
                e.printStackTrace()
            }
        }
    }


    fun getCurrentPercent(currentDuration : Int, maxDuration : Int) : Int
    {
        if(currentDuration >= maxDuration)
        {
            return 100
        }
        val resultData = currentDuration.toFloat() / maxDuration.toFloat() * 100f
        return resultData.toInt()
    }

    /**
     * 특정 숫자를 제외한 랜덤 숫자를 부여한다.
     * @param maxCount 0~ maxCount
     * @param exceptNumber 제외할 숫자
     * @return
     */
    fun getRandomNumber(maxCount : Int, exceptNumber : Int) : Int
    {
        var seedNum = -1
        val rand = Random(System.nanoTime())
        do
        {
            seedNum = rand.nextInt(maxCount)
        } while(seedNum == exceptNumber)
        return seedNum
    }

    /**
     * 최대 숫자 내에 랜덤 숫자를 부여한다.
     * @param maxCount 0~ maxCount
     * @return
     */
    fun getRandomNumber(maxCount : Int) : Int
    {
        var seedNum = -1
        val rand = Random(System.nanoTime())
        seedNum = rand.nextInt(maxCount)
        return seedNum
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users to download the APK from the Google Play Store or enable it in the device's
     * system settings.
     */
    fun checkPlayServices() : Boolean
    {
        val googleApiAvailability : GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode : Int = googleApiAvailability.isGooglePlayServicesAvailable(sContext)
        when(resultCode)
        {
            ConnectionResult.SUCCESS -> return true
            ConnectionResult.SERVICE_DISABLED, ConnectionResult.SERVICE_INVALID, ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ->
            {
                val dialog : Dialog =
                    googleApiAvailability.getErrorDialog(sContext as Activity?, resultCode, 0)
                dialog.setOnCancelListener(object : DialogInterface.OnCancelListener
                {
                    override fun onCancel(dialogInterface : DialogInterface)
                    {
                        (sContext as Activity).finish()
                    }
                })
                dialog.show()
            }
        }
        return false
    }

    val isTeacherMode : Boolean
        get()
        {
            return getSharedPreference(Common.PARAMS_IS_TEACHER_MODE, DataType.TYPE_BOOLEAN) as Boolean
        }

    fun getTopBarStatusBarColor() : Int
    {
        if(isTeacherMode)
        {
            return R.color.color_25b4cf
        }
        else
        {
            return R.color.color_1fb77c
        }
    }

    fun getTopBarBackgroundColor() : Int
    {
        if(isTeacherMode)
        {
            return R.color.color_29c8e6
        }
        else
        {
            return R.color.color_23cc8a
        }
    }

    fun getTopBarIndicatorColor() : Int
    {
        if(checkTablet)
        {
            return R.color.color_fff55a
        }

        if(isTeacherMode)
        {
            return R.color.color_29c8e6
        }
        else
        {
            return R.color.color_23cc8a
        }
    }

    /**
     * 생체인증 가능한 기기인지 체크
     */
    fun checkCanBioLogin() : BioCheckResultType
    {
        // 생체인증 기능은 M(23)부터 가능
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            val biometricManager = BiometricManager.from(sContext)
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> return BioCheckResultType.BIO_SUCCESS
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> return BioCheckResultType.BIO_CANT_USE_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> return BioCheckResultType.BIO_UNABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BioCheckResultType.BIO_NONE
            }
        }

        return BioCheckResultType.BIO_CANT_USE_HARDWARE
    }

    fun setTextByHtmlType(baseView : View, itemList : HashMap<Int, String>)
    {
        val entries : Set<Map.Entry<Int, String>> = itemList.entries

        for((key, text) in entries)
        {
            val textview = baseView.findViewById<View>(key) as TextView
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                textview.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else
            {
                textview.text = Html.fromHtml(text)
            }
        }
    }

    /**
     * 달력 색 Bar 이미지 가져오기
     */
    fun getCalendarBarImage(color : String, calType : CalendarImageType) : Drawable
    {
        var imageType = "one"
        when(calType)
        {
            CalendarImageType.ONE_DAY -> imageType = "one"
            CalendarImageType.SEVERAL_DAY_START -> imageType = "start"
            CalendarImageType.SEVERAL_DAY_CENTER -> imageType = "center"
            CalendarImageType.SEVERAL_DAY_END -> imageType = "end"
        }

        // 색 바 이미지 파일명 가져오기
        var filename = "icon_calendar_${color}_${imageType}"
        if (checkTablet) filename += "_tablet"

        val id = sContext.resources.getIdentifier(filename, "drawable", sContext.packageName) // 아이디 가져오기

        var drawable : Drawable? = null
        try {
            drawable = sContext.resources.getDrawable(id)
        } catch(e : Exception) {
            // 파일이 없는경우 디폴트 : 회색 1칸짜리 이미지
            if (drawable == null) sContext.resources.getDrawable(R.drawable.icon_calendar_gray_one)
        }
        return drawable!!
    }

    /**
     * 숙제 선생님 평가 이미지 가져오기
     */
    fun getHomeworkEvalImage(eval : String) : Drawable?
    {
        when(eval)
        {
            "E0" -> return sContext.resources.getDrawable(R.drawable.icon_stamp_e0)
            "E1" -> return sContext.resources.getDrawable(R.drawable.icon_stamp_e1)
            "E2" -> return sContext.resources.getDrawable(R.drawable.icon_stamp_e2)
            else -> return null
        }
    }

    /**
     * 숙제 선생님 평가 문구 가져오기
     */
    fun getHomeworkEvalText(eval : String) : String
    {
        when(eval)
        {
            "E0" -> return sContext.resources.getString(R.string.text_homework_excellent)
            "E1" -> return sContext.resources.getString(R.string.text_homework_good)
            "E2" -> return sContext.resources.getString(R.string.text_homework_try_again)
            else -> return ""
        }
    }

    /**
     * 숙제 종류에 따른 bar 이미지
     */
    fun getHomeworkTypeImage(type : HomeworkType, isEnable : Boolean = false) : Drawable
    {
        var filename : String = ""
        when(type)
        {
            HomeworkType.ANIMATION -> filename = "icon_homework_animation"
            HomeworkType.EBOOK -> filename = "icon_homework_ebook"
            HomeworkType.QUIZ -> filename = "icon_homework_quiz"
            HomeworkType.CROSSWORD -> filename = "icon_homework_crossword"
            HomeworkType.STARWORDS -> filename = "icon_homework_starwords"
            HomeworkType.RECORDER -> filename = "icon_homework_recorder"
        }

        if (isEnable)
        {
            filename += "_off"
        }
        else
        {
            filename += "_on"
        }

        val id = sContext.resources.getIdentifier(filename, "drawable", sContext.packageName) // 아이디 가져오기
        var drawable : Drawable? = null
        try {
            drawable = sContext.resources.getDrawable(id)
        } catch(e : Exception) {
            // 파일이 없는경우 디폴트 : 애니메이션 ON
            if (drawable == null) sContext.resources.getDrawable(R.drawable.icon_homework_animation_on)
        }
        return drawable!!
    }

    /**
     * 컨텐츠 종류에 따른 이미지
     */
    fun getContentTypeImage(type : ContentType) : Drawable
    {
        when (type)
        {
            ContentType.STORY -> return sContext.resources.getDrawable(R.drawable.icon_story)
            ContentType.SONG -> return sContext.resources.getDrawable(R.drawable.icon_song)
            else -> return sContext.resources.getDrawable(R.drawable.icon_story)
        }
    }

    fun downloadFileToExternalPublicDir(link : String, filename : String)
    {
        val request : DownloadManager.Request = DownloadManager.Request(Uri.parse(link))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        val downloadManager : DownloadManager = sContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    fun getClassName(studentSectionResult : StudentSectionResult) : String
    {
        if (studentSectionResult.isHaveClass() == false)
        {
            // 반 배정이 되지 않은 학생 : (미배정)
            return sContext.getString(R.string.text_student_class_unassigned)
        }
        else if (studentSectionResult.getGrade() > 0)
        {
            // 학년 정보 있는 경우 : %d학년 %s반
            val front = String.format(sContext.resources.getString(R.string.text_student_class_grade), studentSectionResult.getGrade())
            val end = String.format(sContext.resources.getString(R.string.text_student_class_class), studentSectionResult.getClassName())
            return "$front $end"
        }
        else
        {
            // 학년 정보가 없는 유치원, 어린이집은 학년 표기 없이 반명만 노출 : %s반
            return String.format(sContext.resources.getString(R.string.text_student_class_class), studentSectionResult.getClassName())
        }
    }
}