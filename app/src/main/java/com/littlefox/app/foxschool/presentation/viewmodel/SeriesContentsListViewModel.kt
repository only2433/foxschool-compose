package com.littlefox.app.foxschool.presentation.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.api.base.BaseApiViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.MainApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.api.SeriesContentsListApiViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils

import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.data.series.SeriesViewData
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.content.DetailItemInformationResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseViewModel
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class SeriesContentsListViewModel @Inject constructor(private val apiViewModel : SeriesContentsListApiViewModel) : BaseViewModel()
{

    private val _isContentsLoading = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isContentsLoading : SharedFlow<Boolean> = _isContentsLoading

    private val _contentsList = MutableSharedFlow<ArrayList<ContentsBaseResult>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val contentsList: SharedFlow<ArrayList<ContentsBaseResult>> = _contentsList

    private val _isSingleSeries = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val isSingleSeries: SharedFlow<Boolean> = _isSingleSeries

    private val _showToolbarInformationView = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val showToolbarInformationView: SharedFlow<Boolean> = _showToolbarInformationView

    private val _seriesTitle = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val seriesTitle: SharedFlow<String> = _seriesTitle

    private val _backgroundViewData = MutableSharedFlow<TopThumbnailViewData>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val backgroundView: SharedFlow<TopThumbnailViewData> = _backgroundViewData

    private val _seriesDataView = MutableSharedFlow<SeriesViewData>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val seriesViewData: SharedFlow<SeriesViewData> = _seriesDataView

    private val _statusBarColor = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST

    )
    val statusBarColor:SharedFlow<String> = _statusBarColor


    private var mCurrentContentsItemList : ArrayList<ContentsBaseResult> = ArrayList<ContentsBaseResult>()
    private var isStillSeries : Boolean = false
    private lateinit var mCurrentSeriesBaseResult : SeriesBaseResult
    private lateinit var mDetailItemInformationResult : DetailItemInformationResult
    private lateinit var mContext : Context


    override fun init(context : Context)
    {
        mContext = context
        mCurrentSeriesBaseResult = (mContext as AppCompatActivity).intent.getParcelableExtra(Common.INTENT_STORY_SERIES_DATA)!!
        onHandleApiObserver()

        prepareUI()
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                delay(Common.DURATION_LONG)
            }
            requestDetailInformation()
        }

    }

    private fun prepareUI()
    {
        viewModelScope.launch {
            _seriesTitle.emit(mCurrentSeriesBaseResult.getSeriesName())
        }

        viewModelScope.launch {
            _statusBarColor.emit(mCurrentSeriesBaseResult.statusBarColor)
        }

        val data = TopThumbnailViewData(
            thumbnail = mCurrentSeriesBaseResult.getThumbnailUrl(),
            titleColor = mCurrentSeriesBaseResult.titleColor,
            transitionType = mCurrentSeriesBaseResult.getTransitionType()
            )

        viewModelScope.launch {
            _backgroundViewData.emit(data)
        }
        viewModelScope.launch {
            _isContentsLoading.emit(true)
        }
    }

    override fun onHandleViewEvent(event : BaseEvent)
    {
        TODO("Not yet implemented")
    }

    override fun onHandleApiObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED) {
                apiViewModel.isLoading.collect { data ->
                    data?.let {
                        if (data.first == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                        {
                            if(data.second)
                            {
                                Log.i("isLoading = true")
                                _isLoading.emit(true)
                            }
                            else
                            {
                                Log.i("isLoading = false")
                                _isLoading.emit(false)
                            }
                        }
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.storyContentsListData.collect { data ->
                    data?.let {
                        settingDetailView(data)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.songContentsListData.collect { data ->
                    data?.let {
                        settingDetailView(data)
                    }
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launch {
            (mContext as AppCompatActivity).repeatOnLifecycle(Lifecycle.State.RESUMED){
                apiViewModel.errorReport.collect { data ->
                    data?.let {
                        val result = data.first
                        val code = data.second

                        if(result.isDuplicateLogin)
                        {
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initAutoIntroSequence()
                        }
                        else if(result.isAuthenticationBroken)
                        {
                            (mContext as AppCompatActivity).finish()
                            viewModelScope.launch {
                                _toast.emit(result.message)
                            }
                            IntentManagementFactory.getInstance().initScene()
                        }
                        else
                        {
                            if(code == RequestCode.CODE_CONTENTS_STORY_LIST ||
                                code == RequestCode.CODE_CONTENTS_SONG_LIST)
                            {
                                viewModelScope.launch {
                                    _toast.emit(result.message)
                                }
                                (mContext as AppCompatActivity).onBackPressed()
                            }
                            else if(code == RequestCode.CODE_BOOKSHELF_CONTENTS_ADD)
                            {
                                viewModelScope.launch {
                                    _isLoading.emit(false)
                                }

                                viewModelScope.launch {
                                    withContext(Dispatchers.IO)
                                    {
                                        delay(Common.DURATION_SHORT)
                                    }
                                    _errorMessage.emit(result.message)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    override fun resume()
    {
        TODO("Not yet implemented")
    }

    override fun pause()
    {
        TODO("Not yet implemented")
    }

    override fun destroy()
    {
        TODO("Not yet implemented")
    }

    private fun requestDetailInformation()
    {
        if(mCurrentSeriesBaseResult.getSeriesType() == Common.CONTENT_TYPE_STORY)
        {
            apiViewModel.enqueueCommandStart(
                RequestCode.CODE_CONTENTS_STORY_LIST,
                mCurrentSeriesBaseResult.getDisplayID()
            )
        }
        else
        {
            apiViewModel.enqueueCommandStart(
                RequestCode.CODE_CONTENTS_SONG_LIST,
                mCurrentSeriesBaseResult.getDisplayID()
            )
        }

    }

    private val lastStudyMovieIndex : Int
        get()
        {
            var result = 0
            for(i in 0 until mDetailItemInformationResult.getContentsList().size)
            {
                if(mDetailItemInformationResult.lastStudyContentID == mDetailItemInformationResult.getContentsList()[i].getID())
                {
                    if(mDetailItemInformationResult.isStillOnSeries)
                    {
                        result = mDetailItemInformationResult.getContentsList().size - i
                    }
                    else
                    {
                        result = i + 1
                    }
                }
            }
            return result
        }

    private fun settingDetailView(data : DetailItemInformationResult)
    {
        mDetailItemInformationResult = data
        var viewData: SeriesViewData

        if(mDetailItemInformationResult.isSingleSeries == false
            && mCurrentSeriesBaseResult.getSeriesType().equals(Common.CONTENT_TYPE_STORY))
        {
            viewModelScope.launch {
                _showToolbarInformationView.emit(true)
            }
        }

        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            viewData = SeriesViewData(
                type = mCurrentSeriesBaseResult.getSeriesType(),
                seriesLevel = mDetailItemInformationResult.seriesLevel,
                contentsSize = mDetailItemInformationResult.getContentsList().size,
                category = mCurrentSeriesBaseResult.categoryData,
                isSingleSeries = mDetailItemInformationResult.isSingleSeries,
                arLevel = mDetailItemInformationResult.seriesARLevel,
                introduction = mCurrentSeriesBaseResult.introduction
            )
        }
        else
        {
            viewData = SeriesViewData(
                type = mCurrentSeriesBaseResult.getSeriesType(),
                seriesLevel = mDetailItemInformationResult.seriesLevel,
                contentsSize = mDetailItemInformationResult.getContentsList().size,
                isSingleSeries = mDetailItemInformationResult.isSingleSeries,
                arLevel = mDetailItemInformationResult.seriesARLevel,
            )
        }

        viewModelScope.launch {
            _seriesDataView.emit(viewData)
        }

        viewModelScope.launch {
            _isSingleSeries.emit(mDetailItemInformationResult.isSingleSeries)
        }

        if(mDetailItemInformationResult.seriesID != "")
        {
            if(!mDetailItemInformationResult.isSingleSeries && mDetailItemInformationResult.isSingleSeries)
            {
                isStillSeries = true
            }
        }

        viewModelScope.launch {
            _isContentsLoading.emit(false)
        }

        initContentsList()
    }

    private fun initContentsList()
    {
        mCurrentContentsItemList.clear()
        mCurrentContentsItemList = mDetailItemInformationResult.getContentsList()
        for(i in mCurrentContentsItemList.indices)
        {
            val index = i + 1
            mCurrentContentsItemList[i].setIndex(index)
        }

        if(isStillSeries)
        {
            mCurrentContentsItemList.reverse()
        }

        viewModelScope.launch {
            _contentsList.emit(mCurrentContentsItemList)
        }

        checkLastWatchContents()
    }

    private fun checkLastWatchContents()
    {
        if(mDetailItemInformationResult.lastStudyContentID != "")
        {
            val loginInformationResult : LoginInformationResult =
                CommonUtils.getInstance(mContext).getPreferenceObject(Common.PARAMS_USER_API_INFORMATION, LoginInformationResult::class.java) as LoginInformationResult
            val resultIndex = lastStudyMovieIndex
            viewModelScope.launch {
                _toast.emit(
                    "${loginInformationResult.getUserInformation().getName()}님은 현재 $resultIndex 까지 학습 했어요."
                )
            }
        }
    }
}