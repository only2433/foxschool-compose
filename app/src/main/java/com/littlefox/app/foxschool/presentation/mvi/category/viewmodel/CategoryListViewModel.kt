package com.littlefox.app.foxschool.presentation.mvi.category.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
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
import com.littlefox.app.foxschool.presentation.mvi.base.Action
import com.littlefox.app.foxschool.presentation.mvi.base.BaseMVIViewModel
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import com.littlefox.app.foxschool.presentation.mvi.category.CategoryListAction
import com.littlefox.app.foxschool.presentation.mvi.category.CategoryListEvent
import com.littlefox.app.foxschool.presentation.mvi.category.CategoryListSideEffect
import com.littlefox.app.foxschool.presentation.mvi.category.CategoryListState
import com.littlefox.logmonitor.Log

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(private val apiViewModel : CategoryListApiViewModel): BaseMVIViewModel<CategoryListState, CategoryListEvent, SideEffect>(
    CategoryListState()
)
{
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

    override fun resume() {}

    override fun pause() {}

    override fun destroy() {}

    override fun onBackPressed()
    {
        (mContext as AppCompatActivity).finish()
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.CREATED) {
                apiViewModel.categoryListData.collect{ data ->
                    data?.let {
                        mStoryCategoryListResult = data
                        mCategoryDataList = mStoryCategoryListResult.getInformationList()
                        postEvent(
                            CategoryListEvent.EnableContentsLoading(false)
                        )
                        viewModelScope.launch {
                            withContext(Dispatchers.Main)
                            {
                                delay(Common.DURATION_SHORTER)
                            }
                            postEvent(
                                CategoryListEvent.SetTotalContentsCount(totalCategoryContentItemCount),
                                CategoryListEvent.NotifyContentsList(mCategoryDataList)
                            )
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
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initAutoIntroSequence()
                            }
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                                IntentManagementFactory.getInstance().initScene()
                            }
                        }
                        else
                        {
                            postSideEffect(
                                SideEffect.ShowToast(result.message)
                            )
                            viewModelScope.launch {
                                withContext(Dispatchers.IO)
                                {
                                    delay(Common.DURATION_SHORT)
                                }
                                (mContext as AppCompatActivity).finish()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onHandleAction(action : Action)
    {
        when(action)
        {
            is CategoryListAction.ClickContentsItem ->
            {
                startSeriesContentsActivity(action.data)
            }
        }
    }

    override suspend fun reduceState(current : CategoryListState, event : CategoryListEvent) : CategoryListState
    {
        return when(event)
        {
            is CategoryListEvent.NotifyContentsList ->
            {
                current.copy(
                    categoryList = event.contentsList
                )
            }
            is CategoryListEvent.SetTotalContentsCount ->
            {
                current.copy(
                    contentsCount = event.count
                )
            }
            is CategoryListEvent.SetTitle ->
            {
                current.copy(
                    title = event.title
                )
            }
            is CategoryListEvent.SettingBackground ->
            {
                current.copy(
                    backgroundViewData = event.data
                )
            }
            is CategoryListEvent.EnableContentsLoading ->
            {
                current.copy(
                    isContentsLoading = event.isLoading
                )
            }

        }
    }

    private fun prepareUI()
    {
        postSideEffect(
            CategoryListSideEffect.SetStatusBarColor(
                mCurrentCategoryBaseData.statusBarColor
            )
        )
        postEvent(
            CategoryListEvent.SetTitle(
                mCurrentCategoryBaseData.getSeriesName()
            ),
        )
        val data = TopThumbnailViewData(
            thumbnail = mCurrentCategoryBaseData.getThumbnailUrl(),
            titleColor = mCurrentCategoryBaseData.titleColor,
            transitionType = mCurrentCategoryBaseData.getTransitionType()
        )
        postEvent(
            CategoryListEvent.SettingBackground(data),
            CategoryListEvent.EnableContentsLoading(true)
        )
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