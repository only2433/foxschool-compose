package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.littlefox.app.foxschool.`object`.result.StoryCategoryListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.`object`.result.story.StoryCategoryListResult
import com.littlefox.app.foxschool.adapter.SeriesCardViewAdapter
import com.littlefox.app.foxschool.adapter.listener.SeriesCardItemListener
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.coroutine.StoryCategoryListInformationCoroutine
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.main.contract.StoryCategoryListContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log
import java.util.ArrayList


class StoryCategoryListPresenter : StoryCategoryListContract.Presenter
{
    companion object
    {
        private const val MESSAGE_REQUEST_STORY_CATEGORY_LIST : Int     = 100
        private const val MESSAGE_SET_STORY_CATEGORY_LIST : Int         = 101
    }

    private lateinit var mContext : Context
    private lateinit var mMainHandler : WeakReferenceHandler
    private lateinit var mStoryCategoryListContractView : StoryCategoryListContract.View
    private lateinit var mCurrentCategoryBaseData : SeriesBaseResult
    private lateinit var mStoryCategoryListResult : StoryCategoryListResult
    private var mSeriesCardViewAdapter : SeriesCardViewAdapter? = null
    private var mStoryCategoryListInformationCoroutine : StoryCategoryListInformationCoroutine? = null

    constructor(context : Context)
    {
        mContext = context
        mCurrentCategoryBaseData = (mContext as AppCompatActivity).getIntent().getParcelableExtra(Common.INTENT_STORY_CATEGORY_DATA)!!
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mStoryCategoryListContractView = (mContext as StoryCategoryListContract.View).apply {
            initView()
            initFont()
            initTransition(mCurrentCategoryBaseData.getTransitionType())
            setStatusBar(mCurrentCategoryBaseData.statusBarColor)
            if(CommonUtils.getInstance(mContext).checkTablet)
            {
                settingTitleViewTablet(mCurrentCategoryBaseData.getSeriesName())
                settingBackgroundViewTablet(mCurrentCategoryBaseData.getThumbnailUrl(), mCurrentCategoryBaseData.titleColor)
            } else
            {
                settingTitleView(mCurrentCategoryBaseData.getSeriesName())
                settingBackgroundView(mCurrentCategoryBaseData.getThumbnailUrl(), mCurrentCategoryBaseData.titleColor)
            }
            Log.f("onCreate")
            showLoading()
        }
        mMainHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_STORY_CATEGORY_LIST, Common.DURATION_LONG)
    }

    override fun resume()
    {
        Log.f("")
        mSeriesCardViewAdapter?.setIndexImageVisible()
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
        mStoryCategoryListInformationCoroutine?.cancel()
        mStoryCategoryListInformationCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun activityResult(requestCode : Int, resultCode : Int, data : Intent?) {}

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_REQUEST_STORY_CATEGORY_LIST -> requestStoryCategoryListAsync()
            MESSAGE_SET_STORY_CATEGORY_LIST ->
            {
                mStoryCategoryListContractView.showSeriesCountView(totalCategoryContentItemCount)
                initSeriesItemList()
            }
        }
    }

    private val totalCategoryContentItemCount : Int
        get()
        {
            var result = 0
            for(i in 0 until mStoryCategoryListResult.getInformationList().size)
            {
                result += mStoryCategoryListResult.getInformationList().get(i).getContentsCount()
            }
            return result
        }

    private fun requestStoryCategoryListAsync()
    {
        Log.f("")
        mStoryCategoryListInformationCoroutine = StoryCategoryListInformationCoroutine(mContext).apply {
            setData(mCurrentCategoryBaseData.getDisplayID())
            asyncListener = mAsyncListener
            execute()
        }
    }

    private fun initSeriesItemList()
    {
        mSeriesCardViewAdapter = SeriesCardViewAdapter(mContext, mStoryCategoryListResult.getInformationList())
        mSeriesCardViewAdapter!!.setSeriesCardItemListener(mSeriesCardItemListener)
        mStoryCategoryListContractView.showCategoryCardListView(mSeriesCardViewAdapter!!)
    }

    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) {}

        override fun onRunningEnd(code : String, `object` : Any)
        {
            mStoryCategoryListContractView.hideLoading()
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() === BaseResult.SUCCESS_CODE_OK)
            {
                mStoryCategoryListResult = (`object` as StoryCategoryListBaseObject).getData()
                mMainHandler.sendEmptyMessage(MESSAGE_SET_STORY_CATEGORY_LIST)
            } else
            {
                if(result.isAuthenticationBroken)
                {
                    Log.f("== isAuthenticationBroken ==")
                    (mContext as AppCompatActivity).finish()
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    IntentManagementFactory.getInstance().initScene()
                } else
                {
                    Toast.makeText(mContext, result.getMessage(), Toast.LENGTH_LONG).show()
                    (mContext as AppCompatActivity).onBackPressed()
                }
            }
        }

        override fun onRunningCanceled(code : String) {}

        override fun onRunningProgress(code : String, progress : Int) {}

        override fun onRunningAdvanceInformation(code : String, `object` : Any) {}

        override fun onErrorListener(code : String, message : String) {}
    }
    private val mSeriesCardItemListener : SeriesCardItemListener = object : SeriesCardItemListener
    {
        override fun onClickItem(seriesInformationResult : SeriesInformationResult, selectView : View)
        {
            val pair = Pair<View, String>(selectView, Common.STORY_DETAIL_LIST_HEADER_IMAGE)
            seriesInformationResult.setDisplayId(seriesInformationResult.getDisplayID())
            seriesInformationResult.setContentsName(seriesInformationResult.getSeriesName())
            seriesInformationResult.setTransitionType(TransitionType.PAIR_IMAGE)
            seriesInformationResult.setSeriesType(Common.CONTENT_TYPE_STORY)
            IntentManagementFactory.getInstance()
                .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
                .setData(seriesInformationResult as SeriesBaseResult)
                .setViewPair(pair)
                .setAnimationMode(AnimationMode.METERIAL_ANIMATION)
                .startActivity()
        }
    }

}