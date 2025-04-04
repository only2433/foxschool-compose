package com.littlefox.app.foxschool.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.ForumListBaseObject
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseResult
import com.littlefox.app.foxschool.adapter.ForumListAdapter
import com.littlefox.app.foxschool.adapter.ForumListPagingAdapter
import com.littlefox.app.foxschool.adapter.listener.ForumItemListener
import com.littlefox.app.foxschool.api.viewmodel.factory.ForumFactoryViewModel
import com.littlefox.app.foxschool.api.viewmodel.fragment.ForumFragmentViewModel
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ForumType
import com.littlefox.app.foxschool.`object`.result.forum.ForumBaseListResult
import com.littlefox.logmonitor.Log
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job

/**
 * [팍스스쿨 소식], [자주 묻는 질문] List Fragment
 */
@AndroidEntryPoint
class ForumListFragment : Fragment()
{
    @BindView(R.id._forumSwipeRefreshLayout)
    lateinit var _ForumSwipeRefreshLayout : SwipyRefreshLayout

    @BindView(R.id._forumListView)
    lateinit var _ForumListView : RecyclerView

    @BindView(R.id._progressWheelLayout)
    lateinit var _ProgressWheelLayout: ScalableLayout

    companion object
    {
        val instance : ForumListFragment
            get() = ForumListFragment()
    }

    private var mJob: Job? = null

    private lateinit var mContext : Context
    private lateinit var mUnbinder : Unbinder
    private var mForumListAdapter : ForumListAdapter? = null
    private lateinit var mForumListPagingAdapter : ForumListPagingAdapter
    private val mTotalDataList : ArrayList<ForumBaseResult> = ArrayList<ForumBaseResult>()

    private var mForumType : ForumType = ForumType.FOXSCHOOL_NEWS

    private val factoryViewModel : ForumFactoryViewModel by activityViewModels()
    private val framentVieModel : ForumFragmentViewModel by activityViewModels()

    /** ========== LifeCycle ========== */
    override fun onAttach(context : Context)
    {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View?
    {
        Log.f("")
        val view : View
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            view = inflater.inflate(R.layout.fragment_forum_list_tablet, container, false)
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_forum_list, container, false)
        }
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initForumListView()
        setupObserverViewModel()
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

    override fun onDestroyView()
    {
        super.onDestroyView()
        mForumListAdapter = null
        mUnbinder.unbind()
    }

    override fun onDestroy()
    {
        Log.f("")
        super.onDestroy()
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    private fun initView()
    {

        _ForumSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        if(CommonUtils.getInstance(mContext).checkTablet)
        {
            val TABLET_LIST_WIDTH = 960
            val params : RelativeLayout.LayoutParams = _ForumSwipeRefreshLayout.getLayoutParams() as RelativeLayout.LayoutParams
            params.width = CommonUtils.getInstance(mContext).getPixel(TABLET_LIST_WIDTH)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            _ForumSwipeRefreshLayout.setLayoutParams(params)
        }
        _ProgressWheelLayout.visibility = View.VISIBLE
    }

   private fun initRecyclerView()
    {
        if(mForumListAdapter == null)
        {
            mForumListAdapter = ForumListAdapter(mContext, mForumType)
            mForumListAdapter?.setData(mTotalDataList)
            mForumListAdapter?.setOnItemViewClickListener(mForumListItemListener)

            val linearLayoutManager = LinearLayoutManager(mContext)
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
            _ForumListView.setLayoutManager(linearLayoutManager)

            val animationController : LayoutAnimationController = AnimationUtils.loadLayoutAnimation(mContext, R.anim.listview_layoutanimation)
            _ForumListView.setLayoutAnimation(animationController)
            _ForumListView.setAdapter(mForumListAdapter)
        }
        else
        {
            Log.f("mTextNormalItemListAdapter  notifyDataSetChanged")
            mForumListAdapter?.notifyDataSetChanged()
        }
    }

    private fun initForumListView()
    {
        mForumListPagingAdapter = ForumListPagingAdapter(mContext ,mForumType)
        mForumListPagingAdapter.setOnItemViewClickListener(mForumListItemListener)
        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        _ForumListView.setLayoutManager(linearLayoutManager)
        _ForumListView.setAdapter(mForumListPagingAdapter)

        val animationController : LayoutAnimationController = AnimationUtils.loadLayoutAnimation(mContext, R.anim.listview_layoutanimation)
        _ForumListView.layoutAnimation = animationController
        _ForumListView.scheduleLayoutAnimation()

    }

    /*private fun getForumList()
    {
        mJob?.cancel()
        mJob = lifecycleScope.launch {
            viewModel.getPagingData().collectLatest {
                mForumListPagingAdapter.submitData(it)
            }
        }
    }*/

    /** ========== Init ========== */
    private fun setupObserverViewModel()
    {

        framentVieModel.forumTypeData.observe(viewLifecycleOwner) { type ->
            mForumType = type
        }

        framentVieModel.forumListData.observe(viewLifecycleOwner) { data ->
            setData(data as ForumBaseListResult)
        }

        framentVieModel.cancelRefreshData.observe(viewLifecycleOwner) { enable ->
            cancelRefreshData()
        }
    }

    private fun setData(result : ForumBaseListResult)
    {
        Log.f("setData size : " + result.getNewsList().size)
        if(_ForumSwipeRefreshLayout.isRefreshing())
        {
            _ForumSwipeRefreshLayout.setRefreshing(false)
        }
        if(_ProgressWheelLayout.getVisibility() == View.VISIBLE)
        {
            _ProgressWheelLayout.setVisibility(View.GONE)
        }
        mTotalDataList.addAll(result.getNewsList())
        initRecyclerView()
    }

    /**
     * 재조회 취소
     */
    private fun cancelRefreshData()
    {
        Log.f("")
        if(_ForumSwipeRefreshLayout.isRefreshing())
        {
            _ForumSwipeRefreshLayout.setRefreshing(false)
        }
    }

    /**
     * 하단 당겨서 조회 리스너
     */
    private val mOnRefreshListener : SwipyRefreshLayout.OnRefreshListener = object : SwipyRefreshLayout.OnRefreshListener
    {
        override fun onRefresh(direction : SwipyRefreshLayoutDirection)
        {
            Log.f("direction : $direction")
            /**
             * 메인으로 전달하여 API 통신 시도
             */
            factoryViewModel.onRequestRefresh()

            if(_ForumSwipeRefreshLayout.isRefreshing())
            {
                _ForumSwipeRefreshLayout.setRefreshing(false)
            }

        }
    }

    /**
     * 리스트 클릭 이벤트 리스너
     */
    private val mForumListItemListener : ForumItemListener = object : ForumItemListener
    {
        override fun onItemClick(articleId : String)
        {
            factoryViewModel.onShowWebView(articleId)
        }
    }
}