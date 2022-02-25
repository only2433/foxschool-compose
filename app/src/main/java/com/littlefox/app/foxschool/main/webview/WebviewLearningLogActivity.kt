package com.littlefox.app.foxschool.main.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.webview.bridge.BaseWebviewBridge
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

class WebviewLearningLogActivity : BaseActivity(), MessageHandlerCallback
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaseLayout : ScalableLayout

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

    @BindView(R.id._webview)
    lateinit var _WebView : WebView

    companion object
    {
        const val MESSAGE_SET_TITLE : Int = 10
        const val MESSAGE_PAGE_LOAD_ERROR : Int = 11
    }
    private var mCurrentURL : String = ""
    private var mLoadingDialog : MaterialLoadingDialog? = null
    private var mMainHandler : WeakReferenceHandler? = null

    /** ========== LifeCycle ========== */
    @SuppressLint("SourceLockedOrientationActivity")
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
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_webview)
        }
        ButterKnife.bind(this)
        mMainHandler = WeakReferenceHandler(this)

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
        settingLayoutColor()
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    private fun initText()
    {
        _TitleText.text = resources.getString(R.string.text_learning_log)
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
    }

    private fun initWebView()
    {
        showLoading()
        val extraHeaders : Map<String, String> = CommonUtils.getInstance(this).getHeaderInformation(true)
        _WebView.run {
            webViewClient = DataWebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(Common.URL_LEARNING_LOG, extraHeaders)
            addJavascriptInterface(
                DataInterfaceBridge(context, _MainBaseLayout, _TitleText, _WebView),
                Common.BRIDGE_NAME
            )
        }

    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _TitleBaseLayout.setBackgroundColor(resources.getColor(backgroundColor))
    }
    /** ========== Init end ========== */

    private fun setBackButton(isVisible : Boolean)
    {
        if(isVisible)
        {
            _BackButton.visibility = View.VISIBLE
            _BackButtonRect.visibility = View.VISIBLE
            _CloseButtonRect.visibility = View.GONE
            _CloseButton.visibility = View.GONE
        } else
        {
            _BackButton.visibility = View.GONE
            _BackButtonRect.visibility = View.GONE
            _CloseButtonRect.visibility = View.VISIBLE
            _CloseButton.visibility = View.VISIBLE
        }
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
        mLoadingDialog?.show()
    }

    private fun hideLoading()
    {
        if(mLoadingDialog != null)
        {
            mLoadingDialog?.dismiss()
            mLoadingDialog = null
        }
    }

    override fun handlerMessage(message : Message?)
    {
        when(message!!.what)
        {
            MESSAGE_SET_TITLE -> _TitleText.text = message.obj.toString()
            MESSAGE_PAGE_LOAD_ERROR ->
            {
                finish()
                Toast.makeText(WebviewLearningLogActivity@this,
                    resources.getString(R.string.message_webview_connect_error),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed()
    {
        if (Common.URL_LEARNING_LOG != mCurrentURL)
        {
            Log.f("")
            _WebView.postDelayed(Runnable {
               _WebView.loadUrl("javascript:backInterface()")
            }, Common.DURATION_SHORTER)
        }
        else
        {
            super.onBackPressed()
        }
    }

    @OnClick(R.id._backButtonRect, R.id._closeButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> onBackPressed()
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
            Log.f("url : $url")
            mCurrentURL = url

            if(url == Common.URL_LEARNING_LOG)
            {
                setBackButton(false)
            }
            else
            {
                setBackButton(true)
            }

            super.onPageFinished(view, url)
        }
    }

    internal inner class DataInterfaceBridge : BaseWebviewBridge
    {
        constructor(context: Context, coordinatorLayout : CoordinatorLayout, titleText : TextView, webView : WebView)
                : super(context, coordinatorLayout, titleText, webView)

        @JavascriptInterface
        fun onInterfaceGameLoadComplete()
        {
            _WebView.postDelayed({
                Log.f("----- Page Load Complete -----")
                hideLoading()
                mMainHandler!!.removeMessages(WebviewLearningLogActivity.MESSAGE_PAGE_LOAD_ERROR)
            }, Common.DURATION_SHORTER)
        }
    }
}