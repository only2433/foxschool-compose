package com.littlefox.app.foxschool.main.webview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.webview.bridge.BaseWebviewBridge
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import java.io.File

class WebviewEbookActivity : BaseActivity()
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._webview)
    lateinit var _WebView : WebView

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    private var mLoadingDialog : MaterialLoadingDialog? = null
    private var mWebviewIntentParamsObject : WebviewIntentParamsObject? = null

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        Log.f("")

        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_webview_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            setContentView(R.layout.activity_webview)
        }

        ButterKnife.bind(this)
        initView()
        initText()
        initWebView()
    }

    override fun onResume()
    {
        Log.f("")
        super.onResume()
        _WebView.onResume()
    }

    override fun onPause()
    {
        Log.f("")
        super.onPause()
        _WebView.onPause()
    }

    override fun onDestroy()
    {
        Log.f("")
        _WebView.loadUrl("about:blink")
        _WebView.removeAllViews()
        _WebView.destroy()
        super.onDestroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** ========== LifeCycle end ========== */

    /** ========== Init ========== */
    private fun initView()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    private fun initText()
    {
        _TitleText.text = resources.getString(R.string.text_ebook)
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
    }


    private fun initWebView()
    {
        showLoading()
        mWebviewIntentParamsObject = intent.getParcelableExtra(Common.INTENT_EBOOK_DATA)
        val extraHeaders = CommonUtils.getInstance(this).getHeaderInformation(true)
        _WebView.webViewClient = DataWebViewClient()
        _WebView.settings.javaScriptEnabled = true

        if(mWebviewIntentParamsObject!!.getHomeworkNumber() != 0)
        {
            _WebView.loadUrl(
                "${Common.URL_EBOOK}${mWebviewIntentParamsObject!!.getContentID()}${File.separator}${mWebviewIntentParamsObject!!.getHomeworkNumber()}",
                extraHeaders)
        }
        else
        {
            _WebView.loadUrl(
                "${Common.URL_EBOOK}${mWebviewIntentParamsObject!!.getContentID()}", extraHeaders)
        }

        _WebView.addJavascriptInterface(
            BaseWebviewBridge(this, _MainBaseLayout, _TitleText, _WebView),
            Common.BRIDGE_NAME
        )
    }

    private fun showLoading()
    {
        if(mLoadingDialog == null)
        {
            mLoadingDialog = MaterialLoadingDialog(
                this,
                CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
            )
        }
        mLoadingDialog!!.show()
    }

    private fun hideLoading()
    {
        if(mLoadingDialog != null)
        {
            mLoadingDialog!!.dismiss()
            mLoadingDialog = null
        }
    }

    @OnClick(R.id._closeButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
        }
    }

    internal inner class DataWebViewClient : WebViewClient()
    {
        override fun shouldOverrideUrlLoading(view : WebView, request : WebResourceRequest) : Boolean
        {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view : WebView, url : String)
        {
            hideLoading()
            super.onPageFinished(view, url)
            Log.f("url : $url")
        }
    }


}
