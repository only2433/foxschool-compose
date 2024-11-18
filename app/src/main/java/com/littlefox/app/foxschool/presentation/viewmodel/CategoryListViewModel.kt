package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.CategoryListApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.ActivityMode
import com.littlefox.app.foxschool.enumerate.AnimationMode
import com.littlefox.app.foxschool.enumerate.TransitionType
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.`object`.result.story.StoryCategoryListResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.categoty_list.CategoryListEvent
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject


@HiltViewModel
class CategoryListViewModel @Inject constructor(private val apiViewModel : CategoryListApiViewModel) : BaseViewModel()
{
    private val _categoryList = SingleLiveEvent<ArrayList<SeriesInformationResult>>()
    val categoryList : LiveData<ArrayList<SeriesInformationResult>> get() = _categoryList

    private val _totalContentsCount = SingleLiveEvent<Int>()
    val totalContentsCount : LiveData<Int> get() = _totalContentsCount

    private val _categoryTitle = SingleLiveEvent<String>()
    val categoryTitle: LiveData<String> get() = _categoryTitle

    private val _statusBarColor = SingleLiveEvent<String>()
    val statusBarColor: LiveData<String> get() = _statusBarColor

    private val _backgroundViewData = SingleLiveEvent<TopThumbnailViewData>()
    val backgroundViewData: LiveData<TopThumbnailViewData> get() = _backgroundViewData

    private val _isContentsLoading = SingleLiveEvent<Boolean>()
    val isContentsLoading: LiveData<Boolean> get() = _isContentsLoading

    private lateinit var mContext : Context
    private lateinit var mCurrentCategoryBaseData : SeriesBaseResult
    private lateinit var mStoryCategoryListResult : StoryCategoryListResult
    private var mCategoryDataList : ArrayList<SeriesInformationResult> = ArrayList()

    override fun init(context : Context)
    {
        mContext = context
        mCurrentCategoryBaseData = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_STORY_CATEGORY_DATA)!!

        onHandleApiObserver()

        prepareUI()
        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                delay(Common.DURATION_NORMAL)
            }
            requestCategoryListAsync()
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        when(event)
        {
            is BaseEvent.onBackPressed ->
            {
                (mContext as AppCompatActivity).onBackPressed()
            }
            is CategoryListEvent.onClickContentsItem ->
            {
                startSeriesContentsActivity(event.data)
            }
        }
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.categoryListData.collect{ data ->
                    data?.let {
                        mStoryCategoryListResult = data
                        mCategoryDataList = mStoryCategoryListResult.getInformationList()

                        _isContentsLoading.value = false
                        viewModelScope.launch {
                            withContext(Dispatchers.Main)
                            {
                                delay(Common.DURATION_SHORTER)
                            }
                            _totalContentsCount.value = totalCategoryContentItemCount
                            _categoryList.value = mCategoryDataList
                        }
                    }
                }
            }
        }
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.errorReport.collect{ data ->
                    data?.let {
                        val result = data.first
                        val code = data.second
                        if(result.isDuplicateLogin)
                        {
                            (mContext as AppCompatActivity).finish()
                            _toast.value = result.message
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            (mContext as AppCompatActivity).finish()
                            _toast.value = result.message
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else
                        {
                            _toast.value = result.message
                            (mContext as AppCompatActivity).onBackPressed()
                        }
                    }
                }
            }
        }

    }

    override fun resume()
    {

    }

    override fun pause()
    {

    }

    override fun destroy()
    {

    }

    private fun prepareUI()
    {
        _categoryTitle.value = mCurrentCategoryBaseData.getSeriesName()
        _statusBarColor.value = mCurrentCategoryBaseData.statusBarColor
        val data = TopThumbnailViewData(
            thumbnail = mCurrentCategoryBaseData.getThumbnailUrl(),
            titleColor = mCurrentCategoryBaseData.titleColor,
            transitionType = mCurrentCategoryBaseData.getTransitionType()
        )

        _backgroundViewData.value = data
        _isContentsLoading.value = true
    }

    private fun requestCategoryListAsync()
    {
        apiViewModel.enqueueCommandStart(
            RequestCode.CODE_CATEGORY_LIST,
            mCurrentCategoryBaseData.getDisplayID()
        )
    }

    private val totalCategoryContentItemCount: Int
        get()
        {
           return mCategoryDataList.sumOf {
                it.getContentsCount()
            }
        }

    private fun startSeriesContentsActivity(data : SeriesBaseResult)
    {
        Log.i("")
        data.setTransitionType(TransitionType.PAIR_IMAGE)
        data.setSeriesType(Common.CONTENT_TYPE_STORY)
        IntentManagementFactory.getInstance()
            .readyActivityMode(ActivityMode.SERIES_DETAIL_LIST)
            .setData(data)
            .setAnimationMode(AnimationMode.NORMAL_ANIMATION)
            .startActivity()
    }
}