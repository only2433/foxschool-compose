package com.littlefox.app.foxschool.main.webview.bridge

import android.content.Context
import android.view.Gravity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.dialog.TempleteAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.logmonitor.Log

open class BaseWebviewBridge
{
    private val DIALOG_TYPE_WEBVIEW_RESPONSE : Int = 10010

    private var mContext : Context
    private var _BaseLayout : CoordinatorLayout
    private var _TitleView : TextView? = null
    private lateinit var _WebView : WebView

    private var mTempleteAlertDialog : TempleteAlertDialog? = null

    constructor(context : Context, coordinatorLayout : CoordinatorLayout, titleView : TextView, webView : WebView)
    {
        mContext = context
        _BaseLayout = coordinatorLayout
        _TitleView = titleView
        _WebView = webView

        if(Feature.IS_WEBVIEW_DEBUGING)
        {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    constructor(context : Context, coordinatorLayout : CoordinatorLayout, webView : WebView)
    {
        mContext = context
        _BaseLayout = coordinatorLayout
        _TitleView = null
        _WebView = webView

        if(Feature.IS_WEBVIEW_DEBUGING)
        {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    @JavascriptInterface
    fun onInterfaceUpdateAccessToken(token : String)
    {
        Log.f("token : $token")
        _WebView.postDelayed(Runnable {
            CommonUtils.getInstance(mContext).setSharedPreference(Common.PARAMS_ACCESS_TOKEN, token)
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceExitView(message : String)
    {
        Log.f("message : $message")
        _WebView.postDelayed(Runnable {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
            (mContext as AppCompatActivity).finish()
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceExitView()
    {
        _WebView.postDelayed(
            Runnable {(mContext as AppCompatActivity).finish()},
            Common.DURATION_SHORTER
        )
    }

    @JavascriptInterface
    fun onInterfaceDuplicateLogin(message : String)
    {
        Log.f("message : $message")
        _WebView.postDelayed(Runnable {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
            IntentManagementFactory.getInstance().initAutoIntroSequence()
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceSuccessMessage(message : String?)
    {
        _WebView.postDelayed(Runnable {
            CommonUtils.getInstance(mContext).showSuccessSnackMessage(_BaseLayout, message!!)
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceErrorMessage(message : String?)
    {
        _WebView.postDelayed(Runnable {
            CommonUtils.getInstance(mContext).showErrorSnackMessage(_BaseLayout, message!!)
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceShowPopup(message : String)
    {
        Log.f("message : $message")
        _WebView.postDelayed(Runnable {showTempleteAlertDialog(message)}, Common.DURATION_SHORTER)
    }


    @JavascriptInterface
    fun onInterfaceSetTitle(title : String?)
    {
        _WebView.postDelayed(Runnable {
            try
            {
                _TitleView?.text = title
            } catch(e : NullPointerException) { }
        }, Common.DURATION_SHORTER)
    }

    @JavascriptInterface
    fun onInterfaceSaveLogMessage(message : String)
    {
        _WebView.postDelayed(Runnable {
            Log.f("WebView Error Log Message : $message")
            }, Common.DURATION_SHORTER
        )
    }

    private fun showTempleteAlertDialog(message : String)
    {
        mTempleteAlertDialog = TempleteAlertDialog(mContext)
        mTempleteAlertDialog?.setMessage(message)
        mTempleteAlertDialog?.setDialogEventType(DIALOG_TYPE_WEBVIEW_RESPONSE)
        mTempleteAlertDialog?.setButtonType(DialogButtonType.BUTTON_2)
        mTempleteAlertDialog?.setDialogListener(mDialogListener)
        mTempleteAlertDialog?.setGravity(Gravity.LEFT)
        mTempleteAlertDialog?.show()
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int) { }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            Log.f("messageType : $eventType, buttonType : $buttonType")
            if(eventType == DIALOG_TYPE_WEBVIEW_RESPONSE)
            {
                _WebView.postDelayed(Runnable {
                    if(buttonType == DialogButtonType.BUTTON_1)
                    {
                        Log.f("confirmPopup Y")
                        _WebView.loadUrl("javascript:confirmPopup('" + "N" + "')")
                    } else
                    {
                        Log.f("confirmPopup N")
                        _WebView.loadUrl("javascript:confirmPopup('" + "Y" + "')")
                    }
                }, Common.DURATION_SHORTER)
            }
        }
    }
}