package com.littlefox.app.foxschool.main.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.littlefox.library.system.async.listener.AsyncListener;
import com.littlefox.logmonitor.Log;

import net.littlefox.lf_app_fragment.adapter.SeriesCardViewAdapter;
import net.littlefox.lf_app_fragment.adapter.listener.SeriesCardItemListener;
import net.littlefox.lf_app_fragment.common.Common;
import net.littlefox.lf_app_fragment.common.Feature;
import net.littlefox.lf_app_fragment.coroutine.StoryCategoryListInformationCoroutine;
import net.littlefox.lf_app_fragment.enumitem.ActivityMode;
import net.littlefox.lf_app_fragment.enumitem.AnimationMode;
import net.littlefox.lf_app_fragment.handler.WeakReferenceHandler;
import net.littlefox.lf_app_fragment.handler.callback.MessageHandlerCallback;
import net.littlefox.lf_app_fragment.main.contract.StoryCategoryListContract;
import net.littlefox.lf_app_fragment.management.IntentManagementFactory;
import net.littlefox.lf_app_fragment.object.result.base.BaseResult;
import net.littlefox.lf_app_fragment.object.result.base.StoryCategoryListBaseObject;
import net.littlefox.lf_app_fragment.object.result.common.SeriesBaseResult;
import net.littlefox.lf_app_fragment.object.result.common.SeriesInformationResult;
import net.littlefox.lf_app_fragment.object.result.detailListData.StoryCategoryListResult;

public class StoryCategoryListPresenter implements StoryCategoryListContract.Presenter
{
    private static final int MESSAGE_REQUEST_STORY_CATEGORY_LIST        = 100;
    private static final int MESSAGE_SET_STORY_CATEGORY_LIST            = 101;

    private Context mContext = null;
    private StoryCategoryListContract.View mStoryCategoryListContractView = null;
    private WeakReferenceHandler mMainHandler = null;
    private SeriesBaseResult mCurrentCategoryBaseData = null;
    private StoryCategoryListResult mStoryCategoryListResult = null;
    private SeriesCardViewAdapter mSeriesCardViewAdapter = null;
    private StoryCategoryListInformationCoroutine mStoryCategoryListInformationCoroutine = null;

    public StoryCategoryListPresenter(Context context)
    {
        mContext = context;
        mCurrentCategoryBaseData = ((AppCompatActivity)mContext).getIntent().getParcelableExtra(Common.INTENT_STORY_CATEGORY_DATA);
        mMainHandler = new WeakReferenceHandler((MessageHandlerCallback)mContext);
        mStoryCategoryListContractView = (StoryCategoryListContract.View)mContext;
        mStoryCategoryListContractView.initView();
        mStoryCategoryListContractView.initFont();
        mStoryCategoryListContractView.initTransition(mCurrentCategoryBaseData.getTransitionType());
        mStoryCategoryListContractView.setStatusBar(mCurrentCategoryBaseData.getStatusBarColor());

        if(Feature.IS_TABLET)
        {
            mStoryCategoryListContractView.settingTitleViewTablet(mCurrentCategoryBaseData.getSeriesName());
            mStoryCategoryListContractView.settingBackgroundViewTablet(mCurrentCategoryBaseData.getThumbnailUrl(), mCurrentCategoryBaseData.getTitleColor());
        }
        else
        {
            mStoryCategoryListContractView.settingTitleView(mCurrentCategoryBaseData.getSeriesName());
            mStoryCategoryListContractView.settingBackgroundView(mCurrentCategoryBaseData.getThumbnailUrl(), mCurrentCategoryBaseData.getTitleColor());
        }

        Log.f("onCreate");

        mStoryCategoryListContractView.showLoading();
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_STORY_CATEGORY_LIST, Common.DURATION_LONG);

    }

    @Override
    public void resume()
    {
        Log.f("");
        if(mSeriesCardViewAdapter != null)
        {
            mSeriesCardViewAdapter.setIndexImageVisible();
        }
    }

    @Override
    public void pause()
    {
        Log.f("");
    }

    @Override
    public void destroy()
    {
        Log.f("");
        if(mStoryCategoryListInformationCoroutine != null)
        {
            mStoryCategoryListInformationCoroutine.cancel();
            mStoryCategoryListInformationCoroutine = null;
        }

        mMainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void acvitityResult(int requestCode, int resultCode, Intent data)
    {

    }

    @Override
    public void sendMessageEvent(Message msg)
    {
        switch (msg.what)
        {
            case MESSAGE_REQUEST_STORY_CATEGORY_LIST:
                requestStoryCategoryListAsync();
                break;
            case MESSAGE_SET_STORY_CATEGORY_LIST:
                mStoryCategoryListContractView.showSeriesCountView(getTotalCategoryContentItemCount());
                initSeriesItemList();
                break;
        }
    }

    private int getTotalCategoryContentItemCount()
    {
        int result = 0;
        for(int i = 0; i < mStoryCategoryListResult.getInformationList().size(); i++)
        {
            result += mStoryCategoryListResult.getInformationList().get(i).getContentsCount();
        }

        return result;
    }

    private void requestStoryCategoryListAsync()
    {
        Log.f("");

        mStoryCategoryListInformationCoroutine = new StoryCategoryListInformationCoroutine(mContext);
        mStoryCategoryListInformationCoroutine.setData(mCurrentCategoryBaseData.getDisplayId());
        mStoryCategoryListInformationCoroutine.setAsyncListener(mAsyncListener);
        mStoryCategoryListInformationCoroutine.execute();
    }

    private void initSeriesItemList()
    {
        mSeriesCardViewAdapter = new SeriesCardViewAdapter(mContext, mStoryCategoryListResult.getInformationList());
        mSeriesCardViewAdapter.setSeriesCardItemListener(mSeriesCardItemListener);
        mStoryCategoryListContractView.showCategoryCardListView(mSeriesCardViewAdapter);
    }

    private AsyncListener mAsyncListener = new AsyncListener()
    {
        @Override
        public void onRunningStart(String code) {}

        @Override
        public void onRunningEnd(String code, Object object)
        {
            mStoryCategoryListContractView.hideLoading();
            BaseResult result = (BaseResult) object;
            Log.f("code : "+ code+", status : " +result.getStatus());
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                mStoryCategoryListResult = ((StoryCategoryListBaseObject)object).getData();
                mMainHandler.sendEmptyMessage(MESSAGE_SET_STORY_CATEGORY_LIST);
            }
            else
            {
                if (result.isAuthenticationBroken())
                {
                    Log.f("== isAuthenticationBroken ==");
                    ((AppCompatActivity) mContext).finish();
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();
                    IntentManagementFactory.getInstance().initScene();
                }
                else
                {
                    Toast.makeText(mContext, result.message, Toast.LENGTH_LONG).show();
                    ((AppCompatActivity) mContext).onBackPressed();
                }
            }
        }

        @Override
        public void onRunningCanceled(String code) {}

        @Override
        public void onRunningProgress(String code, Integer progress) {}

        @Override
        public void onRunningAdvanceInformation(String code, Object object) {}

        @Override
        public void onErrorListener(String code, String message) {}
    };

    private SeriesCardItemListener mSeriesCardItemListener = new SeriesCardItemListener()
    {
        @Override
        public void onClickItem(SeriesInformationResult seriesInformationResult, View selectView)
        {
            Pair<View, String> pair = new Pair<>(selectView, Common.STORY_DETAIL_LIST_HEADER_IMAGE);
            seriesInformationResult.setTransitionType(Common.TRANSITION_PAIR_IMAGE);
            seriesInformationResult.setSeriesType(Common.CONTENT_TYPE_STORY);
            IntentManagementFactory.getInstance()
                    .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                    .setData((SeriesBaseResult)seriesInformationResult)
                    .setViewPair(pair)
                    .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                    .startActivity();
        }
    };
}
