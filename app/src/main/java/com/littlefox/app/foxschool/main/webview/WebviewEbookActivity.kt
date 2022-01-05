package com.littlefox.app.foxschool.main.webview

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.webview.WebviewIntentParamsObject
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
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
import java.io.File
import java.util.ArrayList

class WebviewEbookActivity : BaseActivity()
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._webviewBaseLayout)
    lateinit var _WebviewBaseLayout : RelativeLayout

    @BindView(R.id._titleLayout)
    lateinit var _TitleLayout : ScalableLayout

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
    private var mMediaPlayer : MediaPlayer? = null
    private var mAudioAttributes : AudioAttributes? = null
    private lateinit var _WebView : WebView

    /** ========== LifeCycle ========== */
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        Log.f("")

        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_ebook_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            setContentView(R.layout.activity_ebook)
        }

        ButterKnife.bind(this)
        initView()
        initText()
    }

    override fun onResume()
    {
        Log.f("")
        super.onResume()

        initWebView()
    }

    override fun onPause()
    {
        Log.f("")
        super.onPause()

        releaseWebView()
        stopAudio()
    }

    override fun onDestroy()
    {
        Log.f("")
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
        _TitleText.text = resources.getString(R.string.text_ebook)
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
    }

    private fun initWebView()
    {
        showLoading()
        mWebviewIntentParamsObject = intent.getParcelableExtra(Common.INTENT_EBOOK_DATA)
        val extraHeaders = CommonUtils.getInstance(this).getHeaderInformation(true)

        var params : RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.BELOW, _TitleLayout.id)

        _WebView = WebView(this)
        _WebviewBaseLayout.addView(_WebView, params)

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
            DataInterfaceBridge(this, _MainBaseLayout, _TitleText, _WebView),
            Common.BRIDGE_NAME
        )
    }

    private fun releaseWebView()
    {
        _WebView.removeAllViews()
        _WebView.destroy()
        _WebviewBaseLayout.removeView(_WebView)
    }

    private fun startAudio(url : String)
    {
        Log.f("startAudio")
        if(mMediaPlayer != null)
        {
            mMediaPlayer?.reset()
        }
        else
        {
            mMediaPlayer = MediaPlayer()
        }
        try
        {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(mAudioAttributes == null)
                {
                    mAudioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                }
                mMediaPlayer?.setAudioAttributes(mAudioAttributes)
            }
            else
            {
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mMediaPlayer?.setDataSource(url)
            mMediaPlayer?.prepareAsync()
            mMediaPlayer?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener
            {
                override fun onPrepared(mediaPlayer : MediaPlayer)
                {
                    mMediaPlayer?.start()
                }
            })
        }
        catch(e : Exception)
        {
            Log.f("Exception : " + e.message)
        }
    }

    private fun stopAudio()
    {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mAudioAttributes = null
    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _TitleLayout.setBackgroundColor(resources.getColor(backgroundColor))
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

    internal inner class DataInterfaceBridge : BaseWebviewBridge
    {
        constructor(context: Context, coordinatorLayout : CoordinatorLayout, titleView : TextView , webView : WebView)
                : super(context, coordinatorLayout, titleView, webView)

        @JavascriptInterface
        fun onInterfacePlaySound(url : String)
        {
            _WebView.postDelayed({
                startAudio(url)
            }, Common.DURATION_SHORTER)
        }

        @JavascriptInterface
        fun onInterfaceStopSound()
        {
            _WebView.postDelayed({
                stopAudio()
            }, Common.DURATION_SHORTER)
        }

    }
}
