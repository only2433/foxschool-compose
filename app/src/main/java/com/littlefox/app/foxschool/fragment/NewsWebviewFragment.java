package com.littlefox.app.foxschool.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.littlefox.logmonitor.Log;

import net.littlefox.lf_app_fragment.R;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.CommonUtils;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.object.viewModel.CommunicateFragmentObserver;
import net.littlefox.lf_app_fragment.object.viewModel.NewsCommunicatePresenterObserver;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class  NewsWebviewFragment extends Fragment
{
    @BindView(R.id._webview)
    WebView _WebView;

    private Context mContext = null;

    private Unbinder mUnbinder;
    private NewsCommunicatePresenterObserver mNewsCommunicatePresenterObserver;
    private CommunicateFragmentObserver mCommunicateFragmentObserver;

    public static NewsWebviewFragment getInstance()
    {
        return new NewsWebviewFragment();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initDataObserver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.f("");
        View view = inflater.inflate(R.layout.fragment_news_webview, container, false);
        mUnbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onResume()
    {
        Log.f("");
        super.onResume();
        _WebView.onResume();
    }

    @Override
    public void onPause()
    {
        Log.f("");
        super.onPause();
        _WebView.onPause();
    }

    @Override
    public void onDestroy()
    {
        Log.f("");
        if(_WebView != null)
        {
            _WebView.loadUrl("about:blink");
            _WebView.removeAllViews();
            _WebView.destroy();
        }

        super.onDestroy();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    private void initDataObserver()
    {
        mCommunicateFragmentObserver = ViewModelProviders.of((AppCompatActivity)mContext).get(CommunicateFragmentObserver.class);
        mNewsCommunicatePresenterObserver = ViewModelProviders.of((AppCompatActivity)mContext).get(NewsCommunicatePresenterObserver.class);
        mNewsCommunicatePresenterObserver.articleIDData.observe(this, new Observer<String>()
        {
            @Override
            public void onChanged(String articleID)
            {
                if(getViewLifecycleOwner().getLifecycle().getCurrentState() != Lifecycle.State.CREATED)
                {
                    setData(articleID);
                }
            }
        });

    }

    private void setData(Object object)
    {
        _WebView.setAlpha(0.0f);
        String ardicleUrl= (String)object;
        Log.f("URL : "+ ardicleUrl);
        Map<String, String> extraHeaders = CommonUtils.getInstance(mContext).getHeaderInformation(false);
        _WebView.setWebViewClient(new DataWebViewClient());
        _WebView.getSettings().setJavaScriptEnabled(true);
        _WebView.loadUrl(ardicleUrl ,extraHeaders);

        _WebView.addJavascriptInterface(new WebViewJavabridge(), Common.BRIDGE_NAME);
        if(Feature.IS_WEBVIEW_DEBUGING)
        {
            _WebView.setWebContentsDebuggingEnabled(true);
        }
    }


    class DataWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
        {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            mCommunicateFragmentObserver.onPageLoadComplete();
            _WebView.setAlpha(1.0f);
            super.onPageFinished(view, url);
        }
    }

    class WebViewJavabridge
    {
        @JavascriptInterface
        public void onInterfaceGoExternalLink(final String url)
        {
            _WebView.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Log.f("url : "+ url);
                    CommonUtils.getInstance(mContext).startLinkMove(url);
                }
            }, Common.DURATION_SHORTER);
        }

        @JavascriptInterface
        public void onInterfaceShowSeries(final String seriesID)
        {
            _WebView.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Log.f("seriesID : "+seriesID);
                    mCommunicateFragmentObserver.onSeriesShow(seriesID);
                }
            }, Common.DURATION_SHORTER);
        }
    }
}
