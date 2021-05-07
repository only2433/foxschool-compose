package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.NewsListBaseObject
import com.littlefox.app.foxschool.`object`.result.news.ForumBaseResult
import com.littlefox.app.foxschool.adapter.NewsListAdapter
import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.viewmodel.CommunicateFragmentObserver
import com.littlefox.app.foxschool.viewmodel.NewsCommunicatePresenterObserver
import com.littlefox.logmonitor.Log
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.ssomai.android.scalablelayout.ScalableLayout

import java.util.ArrayList

class NewsListFragment : Fragment()
{
    @BindView(R.id._newsSwipeRefreshLayout)
    lateinit var _NewsSwipeRefreshLayout : SwipyRefreshLayout

    @BindView(R.id._newsListView)
    lateinit var _NewsListView : RecyclerView

    @BindView(R.id._progressWheelLayout)
    lateinit var _ProgressWheelLayout : ScalableLayout

    companion object
    {
        val instance : NewsListFragment
            get() = NewsListFragment()
    }

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private var mNewsListAdapter : NewsListAdapter? = null
    private val mTotalNewsDataList : ArrayList<ForumBaseResult> = ArrayList<ForumBaseResult>()
    private lateinit var mCommunicateFragmentObserver : CommunicateFragmentObserver
    private lateinit var mNewsCommunicatePresenterObserver : NewsCommunicatePresenterObserver
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initDataObserver()
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        val view : View
        if(Feature.IS_TABLET)
        {
            view = inflater.inflate(R.layout.fragment_news_list, container, false)
        } else
        {
            view = inflater.inflate(R.layout.fragment_news_list_tablet, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onResume()
    {
        Log.f("")
        super.onResume()
    }

    override fun onPause()
    {
        Log.f("")
        super.onPause()
    }

    override fun onDestroy()
    {
        Log.f("")
        super.onDestroy()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        mNewsListAdapter = null
        mUnbinder.unbind()
    }

    private fun initView()
    {
        _ProgressWheelLayout.setVisibility(View.VISIBLE)
        _NewsSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        if(Feature.IS_TABLET)
        {
            val TABLET_LIST_WIDTH = 960
            val params : RelativeLayout.LayoutParams = _NewsSwipeRefreshLayout.getLayoutParams() as RelativeLayout.LayoutParams
            params.width = CommonUtils.getInstance(mContext).getPixel(TABLET_LIST_WIDTH)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            _NewsSwipeRefreshLayout.setLayoutParams(params)
        }
    }

    private fun initDataObserver()
    {
        mCommunicateFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(CommunicateFragmentObserver::class.java)
        mNewsCommunicatePresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(NewsCommunicatePresenterObserver::class.java)
        mNewsCommunicatePresenterObserver.settingNewsListData.observe(mContext as AppCompatActivity,
            Observer<Any> {newsListBaseObject ->
                if(viewLifecycleOwner.lifecycle.currentState != Lifecycle.State.CREATED)
                {
                    setData(newsListBaseObject as NewsListBaseObject)
                }
            })
        mNewsCommunicatePresenterObserver.cancelRefreshData.observe(mContext as AppCompatActivity, Observer<Boolean?> {
            if(viewLifecycleOwner.lifecycle.currentState != Lifecycle.State.CREATED)
            {
                cancelRefreshData()
            }
        })
    }

    private fun setData(result : NewsListBaseObject)
    {
        Log.f("setData size : " + result.getNewsList().size)
        if(_NewsSwipeRefreshLayout.isRefreshing())
        {
            _NewsSwipeRefreshLayout.setRefreshing(false)
        }
        if(_ProgressWheelLayout.getVisibility() == View.VISIBLE)
        {
            _ProgressWheelLayout.setVisibility(View.GONE)
        }
        mTotalNewsDataList.addAll(result.getNewsList())
        initRecyclerView()
    }

    private fun cancelRefreshData()
    {
        Log.f("")
        if(_NewsSwipeRefreshLayout.isRefreshing())
        {
            _NewsSwipeRefreshLayout.setRefreshing(false)
        }
    }

    private fun initRecyclerView()
    {
        if(mNewsListAdapter == null)
        {
            mNewsListAdapter = NewsListAdapter(mContext)
            mNewsListAdapter?.setData(mTotalNewsDataList)
            mNewsListAdapter?.setOnItemViewClickListener(mNewsListItemListener)
            val linearLayoutManager = LinearLayoutManager(mContext)
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
            _NewsListView.setLayoutManager(linearLayoutManager)
            val animationController : LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(mContext, R.anim.listview_layoutanimation)
            _NewsListView.setLayoutAnimation(animationController)
            _NewsListView.setAdapter(mNewsListAdapter)
        } else
        {
            Log.f("mTextNormalItemListAdapter  notifyDataSetChanged")
            mNewsListAdapter?.notifyDataSetChanged()
        }
    }

    private val mOnRefreshListener : SwipyRefreshLayout.OnRefreshListener =
        object : SwipyRefreshLayout.OnRefreshListener
        {
            override fun onRefresh(direction : SwipyRefreshLayoutDirection)
            {
                Log.f("direction : $direction")
                /**
                 * 메인으로 전달하여 API 통신 시도
                 */
                mCommunicateFragmentObserver.onRequestRefresh()
            }
        }
    private val mNewsListItemListener : OnItemViewClickListener = object : OnItemViewClickListener
    {
        override fun onItemClick(position : Int)
        {
            mCommunicateFragmentObserver.onShowWebView(mTotalNewsDataList[position].getForumId())
        }
    }
}