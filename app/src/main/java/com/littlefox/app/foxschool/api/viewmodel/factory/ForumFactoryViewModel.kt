package com.littlefox.app.foxschool.api.viewmodel.factory

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.api.base.BaseFactoryViewModel
import com.littlefox.app.foxschool.api.enumerate.RequestCode
import com.littlefox.app.foxschool.api.viewmodel.api.ForumApiViewModel
import com.littlefox.app.foxschool.api.viewmodel.fragment.ForumFragmentViewModel
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.ForumType
import com.littlefox.app.foxschool.fragment.ForumListFragment
import com.littlefox.app.foxschool.fragment.ForumWebviewFragment
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult
import com.littlefox.app.foxschool.viewmodel.base.SingleLiveEvent
import com.littlefox.logmonitor.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ForumFactoryViewModel @Inject constructor(private val apiViewModel : ForumApiViewModel) : BaseFactoryViewModel()
{
    companion object
    {
        const val MESSAGE_GO_TO_ARTICLE : Int           = 100
        private const val MAX_PER_PAGE_COUNT : Int      = 30
    }

    private val _initViewPager = SingleLiveEvent<MainFragmentSelectionPagerAdapter>()
    val initViewPager : LiveData<MainFragmentSelectionPagerAdapter> = _initViewPager

    private val _setCurrentPage = SingleLiveEvent<Int>()
    val setCurrentPage : LiveData<Int> = _setCurrentPage

    private val _setBackButton = SingleLiveEvent<Boolean>()
    val setBackButton : LiveData<Boolean> = _setBackButton

    private lateinit var mContext : Context
    private lateinit var mMainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter
    private var mForumBaseListResult : ForumBaseListResult? = null
    private var mRequestPagePosition : Int = 1
    private var mForumType : ForumType = ForumType.FAQ
    private lateinit var fragmentViewModel: ForumFragmentViewModel

    fun init(context : Context, type : ForumType)
    {
        mContext = context
        mForumType = type
        fragmentViewModel = ViewModelProvider(mContext as AppCompatActivity).get(
            ForumFragmentViewModel::class.java)
        Log.f("")
        mMainFragmentSelectionPagerAdapter = MainFragmentSelectionPagerAdapter((mContext as AppCompatActivity).supportFragmentManager).apply {
            addFragment(ForumListFragment.instance)
            addFragment(ForumWebviewFragment.instance)
        }
        _initViewPager.value = mMainFragmentSelectionPagerAdapter
        setupViewModelObserver()
        requestForumListAsync()
        fragmentViewModel.onSetForumType(mForumType)
    }

    override fun resume()
    {
        Log.f("")
    }

    override fun pause()
    {
        Log.f("")
    }

    override fun destroy()
    {
        Log.f("")
    }

    override fun setupViewModelObserver()
    {
        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.forumListData.collect { data ->
                data?.let { data ->
                    mForumBaseListResult = data
                    fragmentViewModel.onSettingForumList(data)
                }
            }
        }

        (mContext as AppCompatActivity).lifecycleScope.launchWhenResumed {
            apiViewModel.errorReport.collect { data ->
                data?.let {
                    val result = data.first
                    val code = data.second

                    Log.f("status : ${result.status}, message : ${result.message} , code : $code")
                    _toast.value = result.message
                    if(result.isDuplicateLogin)
                    {
                        //중복 로그인 시 재시작
                        (mContext as AppCompatActivity).finish()
                        IntentManagementFactory.getInstance().initAutoIntroSequence()
                    }
                    else if(result.isAuthenticationBroken)
                    {
                        Log.f("== isAuthenticationBroken ==")
                        (mContext as AppCompatActivity).finish()
                        IntentManagementFactory.getInstance().initScene()
                    }
                    else
                    {
                        (mContext as AppCompatActivity).onBackPressed()
                    }
                }
            }
        }
    }

    private fun requestForumListAsync()
    {
        mForumBaseListResult?.let {
            mRequestPagePosition = it.currentPageIndex + 1
        }
        Log.f("position : $mRequestPagePosition")

        val requestCode = if(mForumType == ForumType.FAQ)
        {
            RequestCode.CODE_FORUM_FAQ_LIST
        }
        else
        {
            RequestCode.CODE_FORUM_NEWS_LIST
        }

        apiViewModel.enqueueCommandStart(
            requestCode,
            MAX_PER_PAGE_COUNT,
            mRequestPagePosition
        )
    }

    fun onPageSelected(position : Int)
    {
        Log.f("position : $position")
        when(position)
        {
            Common.PAGE_FORUM_LIST ->
            {
                _setBackButton.value = false
            }
            Common.PAGE_FORUM_WEBVIEW ->
            {
                _setBackButton.value = true
                _isLoading.value = true
            }
        }
    }

    fun onRequestRefresh()
    {
        Log.f("requestRefresh")
        if(mForumBaseListResult?.isLastPage == true)
        {
            _successMessage.value = mContext.resources.getString(R.string.message_last_page)
            fragmentViewModel.onCancelRefresh()
        }
        else
        {
            requestForumListAsync()
        }
    }

    fun onShowWebView(articleID : String)
    {
        Log.f("showWebView articleID : $articleID")
        mMainFragmentSelectionPagerAdapter.setFragment(Common.PAGE_FORUM_WEBVIEW, ForumWebviewFragment.instance)
        mMainFragmentSelectionPagerAdapter.notifyDataSetChanged()

        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                delay(Common.DURATION_SHORT)
            }
            when(mForumType)
            {
                ForumType.FAQ ->
                {
                    fragmentViewModel.onSetArticleURL(Common.URL_FAQ_DETAIL + articleID)
                }
                ForumType.FOXSCHOOL_NEWS ->
                {
                    fragmentViewModel.onSetArticleURL(Common.URL_FOXSCHOOL_NEWS_DETAIL + articleID)
                }
            }

            _setCurrentPage.value = Common.PAGE_FORUM_WEBVIEW
        }
    }

    fun onPageLoadComplete()
    {
        Log.f("onPageLoadComplete")
        _isLoading.value = false
    }
}