package com.littlefox.app.foxschool.main.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.webkit.JavascriptInterface
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
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.webview.bridge.BaseWebviewBridge
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 1:1문의하기
 */
class Webview1On1AskActivity : BaseActivity()
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

    private var mLoadingDialog : MaterialLoadingDialog? = null
    private var mCurrentURL = ""

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
        _TitleText.text = resources.getString(R.string.text_1_1_ask)
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
    }

    private fun initWebView()
    {
        showLoading()
        val extraHeaders = CommonUtils.getInstance(this).getHeaderInformation(true)
        _WebView.webViewClient = DataWebViewClient()
        _WebView.settings.javaScriptEnabled = true
        _WebView.loadUrl(Common.URL_1_ON_1_ASK, extraHeaders)
        _WebView.addJavascriptInterface(
            DataInterfaceBridge(this, _MainBaseLayout, _TitleText, _WebView),
            Common.BRIDGE_NAME
        )
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

    override fun onBackPressed()
    {
        if (Common.URL_1_ON_1_ASK != mCurrentURL)
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

    internal inner class DataInterfaceBridge : BaseWebviewBridge
    {
        constructor(context: Context, coordinatorLayout : CoordinatorLayout, titleView : TextView, webView : WebView) :
                super(context, coordinatorLayout, titleView, webView)

        @JavascriptInterface
        fun onInterfaceAskToEmail(companyEmail : String)
        {
            Log.f("companyEmail : $companyEmail")
            _WebView.postDelayed(Runnable {
                CommonUtils.getInstance(this@Webview1On1AskActivity).inquireForDeveloper(companyEmail)
            }, Common.DURATION_SHORTER)
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

            Log.f("url : $url")
            mCurrentURL = url

            if(url == Common.URL_1_ON_1_ASK)
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
}