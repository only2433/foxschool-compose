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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.ForumFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.fragment.ForumFragmentViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.logmonitor.Log
import dagger.hilt.android.AndroidEntryPoint

/**
 * [팍스스쿨 소식], [자주 묻는 질문] WebView Fragment
 */
@AndroidEntryPoint
class ForumWebviewFragment : Fragment()
{
    @BindView(R.id._webview)
    lateinit var _WebView : WebView

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder

    private val factoryViewModel : ForumFactoryViewModel by activityViewModels()
    private val fragementViewModel : ForumFragmentViewModel by activityViewModels()

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        val view : View = inflater.inflate(R.layout.fragment_forum_webview, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initDataObserver()
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

    override fun onDestroyView()
    {
        super.onDestroyView()
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
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initDataObserver()
    {
        fragementViewModel.articleUrlData.observe(viewLifecycleOwner) { articleID ->
            setData(articleID)
        }
    }
    /** ========== Init ========== */

    private fun setData(`object` : Any)
    {
        _WebView.setAlpha(0.0f)
        val ardicleUrl = `object` as String
        Log.f("URL : $ardicleUrl")
        val extraHeaders : Map<String, String> = CommonUtils.getInstance(mContext).getHeaderInformation(true)
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
            factoryViewModel.onPageLoadComplete()
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

            }, Common.DURATION_SHORTER)
        }
    }

    companion object
    {
        val instance : ForumWebviewFragment
            get() = ForumWebviewFragment()
    }
}