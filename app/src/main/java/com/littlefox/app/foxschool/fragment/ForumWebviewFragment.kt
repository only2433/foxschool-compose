package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.viewmodel.CommunicateFragmentObserver
import com.littlefox.app.foxschool.viewmodel.NewsCommunicatePresenterObserver
import com.littlefox.logmonitor.Log

class ForumWebviewFragment : Fragment()
{
    @BindView(R.id._webview)
    lateinit var _WebView : WebView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private lateinit var mNewsCommunicatePresenterObserver : NewsCommunicatePresenterObserver
    private lateinit var mCommunicateFragmentObserver : CommunicateFragmentObserver
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initDataObserver()
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        val view : View = inflater.inflate(R.layout.fragment_forum_webview, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        return view
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
        if(_WebView != null)
        {
            _WebView.loadUrl("about:blink")
            _WebView.removeAllViews()
            _WebView.destroy()
        }
        super.onDestroy()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun initDataObserver()
    {
        mCommunicateFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(CommunicateFragmentObserver::class.java)
        mNewsCommunicatePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(NewsCommunicatePresenterObserver::class.java)
        mNewsCommunicatePresenterObserver.articleIDData.observe(this, Observer<String> {articleID ->
            if(viewLifecycleOwner.lifecycle.currentState != Lifecycle.State.CREATED)
            {
                setData(articleID)
            }
        })
    }

    private fun setData(`object` : Any)
    {
        _WebView.setAlpha(0.0f)
        val ardicleUrl = `object` as String
        Log.f("URL : $ardicleUrl")
        val extraHeaders : Map<String, String> = CommonUtils.getInstance(mContext).getHeaderInformation(false)
        _WebView.setWebViewClient(DataWebViewClient())
        _WebView.getSettings().setJavaScriptEnabled(true)
        _WebView.loadUrl(ardicleUrl, extraHeaders)
        _WebView.addJavascriptInterface(WebViewJavabridge(), Common.BRIDGE_NAME)
        if(Feature.IS_WEBVIEW_DEBUGING)
        {
            WebView.setWebContentsDebuggingEnabled(true)
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
            mCommunicateFragmentObserver.onPageLoadComplete()
            _WebView.setAlpha(1.0f)
            super.onPageFinished(view, url)
        }
    }

    internal inner class WebViewJavabridge
    {
        @JavascriptInterface
        fun onInterfaceGoExternalLink(url : String)
        {
            _WebView.postDelayed(Runnable {
                Log.f("url : $url")
                CommonUtils.getInstance(mContext).startLinkMove(url)
            }, Common.DURATION_SHORTER)
        }

        @JavascriptInterface
        fun onInterfaceShowSeries(seriesID : String)
        {
            _WebView.postDelayed(Runnable {
                Log.f("seriesID : $seriesID")
                mCommunicateFragmentObserver.onSeriesShow(seriesID)
            }, Common.DURATION_SHORTER)
        }
    }

    companion object
    {
        val instance : ForumWebviewFragment
            get() = ForumWebviewFragment()
    }
}