package com.littlefox.app.foxschool.main.presenter

import android.content.Context
import android.content.Intent
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.ForumListBaseObject
import com.littlefox.app.foxschool.`object`.result.base.BaseResult
import com.littlefox.app.foxschool.adapter.MainFragmentSelectionPagerAdapter
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.coroutine.ForumCoroutine
import com.littlefox.app.foxschool.fragment.ForumListFragment
import com.littlefox.app.foxschool.fragment.ForumWebviewFragment
import com.littlefox.app.foxschool.main.contract.ForumContract
import com.littlefox.app.foxschool.management.IntentManagementFactory
import com.littlefox.app.foxschool.viewmodel.ForumFragmentObserver
import com.littlefox.app.foxschool.viewmodel.ForumPresenterObserver
import com.littlefox.library.system.async.listener.AsyncListener
import com.littlefox.library.system.handler.WeakReferenceHandler
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.logmonitor.Log

class FoxSchoolNewsPresenter : ForumContract.Presenter
{
    companion object
    {
        private const val MESSAGE_GO_TO_ARTICLE : Int   = 100
        private const val MAX_PER_PAGE_COUNT : Int      = 30
    }

    private lateinit var mContext : Context
    private lateinit var mForumContractView : ForumContract.View
    private lateinit var mMainFragmentSelectionPagerAdapter : MainFragmentSelectionPagerAdapter
    private lateinit var mMainHandler : WeakReferenceHandler

    private lateinit var mForumPresenterObserver : ForumPresenterObserver
    private lateinit var mForumFragmentObserver : ForumFragmentObserver

    private var mSchoolNewsListCoroutine : ForumCoroutine? = null
    private var mFoxSchoolNewsBaseObject : ForumListBaseObject? = null
    private var mRequestPagePosition : Int = 1

    constructor(context : Context)
    {
        mContext = context
        mForumContractView = mContext as ForumContract.View
        mMainHandler = WeakReferenceHandler(mContext as MessageHandlerCallback)
        mForumContractView.initView()
        mForumContractView.initFont()
        Log.f("onCreate")
        init()
    }

    private fun init()
    {
        Log.f("")

        mMainFragmentSelectionPagerAdapter = MainFragmentSelectionPagerAdapter((mContext as AppCompatActivity).supportFragmentManager)
        mMainFragmentSelectionPagerAdapter.addFragment(ForumListFragment.instance)
        mMainFragmentSelectionPagerAdapter.addFragment(ForumWebviewFragment.instance)
        mForumContractView.initViewPager(mMainFragmentSelectionPagerAdapter)
        requestNewsListAsync()

        mForumFragmentObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(ForumFragmentObserver::class.java)
        mForumPresenterObserver = ViewModelProviders.of(mContext as AppCompatActivity).get(ForumPresenterObserver::class.java)
        mForumPresenterObserver.setForumType(Common.FORUM_TYPE_FOXSCHOOL_NEWS)
        setupForumFragmentListener()
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
        mSchoolNewsListCoroutine?.cancel()
        mSchoolNewsListCoroutine = null
        mMainHandler.removeCallbacksAndMessages(null)
    }

    override fun acvitityResult(requestCode : Int, resultCode : Int, data : Intent?) { }

    override fun onPageSelected(position : Int)
    {
        Log.f("position : $position")
        when(position)
        {
            Common.PAGE_FORUM_LIST -> mForumContractView.setBackButton(false)
            Common.PAGE_FORUM_WEBVIEW ->
            {
                mForumContractView.setBackButton(true)
                mForumContractView.showLoading()
            }
        }
    }

    override fun sendMessageEvent(msg : Message)
    {
        when(msg.what)
        {
            MESSAGE_GO_TO_ARTICLE ->
            {
                mForumPresenterObserver.onSetArticleUrl(msg.obj as String)
                mForumContractView.setCurrentViewPage(Common.PAGE_FORUM_WEBVIEW)
            }
        }
    }

    /**
     * 팍스스쿨 뉴스 리스트 요청
     */
    private fun requestNewsListAsync()
    {
        if(mFoxSchoolNewsBaseObject != null)
        {
            mRequestPagePosition = mFoxSchoolNewsBaseObject!!.getData().currentPageIndex + 1
        }
        Log.f("position : $mRequestPagePosition")
        mSchoolNewsListCoroutine = ForumCoroutine(mContext)
        mSchoolNewsListCoroutine!!.setData(
            Common.API_FOXSCHOOL_NEWS,
            mRequestPagePosition,
            MAX_PER_PAGE_COUNT
        )
        mSchoolNewsListCoroutine!!.asyncListener = mAsyncListener
        mSchoolNewsListCoroutine!!.execute()
    }

    private fun setupForumFragmentListener()
    {
        // 당겨서 재조회 요청
        mForumFragmentObserver.refreshData.observe((mContext as AppCompatActivity), Observer {
            Log.f("requestRefresh")
            if(mFoxSchoolNewsBaseObject!!.getData().isLastPage)
            {
                mForumContractView.showErrorMessage(mContext.resources.getString(R.string.message_last_page))
                mForumPresenterObserver.onCancelRefreshData()
            }
            else
            {
                requestNewsListAsync()
            }
        })

        // 리스트 클릭 (상세화면으로 이동)
        mForumFragmentObserver.webviewIDData.observe((mContext as AppCompatActivity), Observer<String> { articleID ->
            Log.f("showWebView articleID : $articleID")
            mMainFragmentSelectionPagerAdapter.setFragment(Common.PAGE_FORUM_WEBVIEW, ForumWebviewFragment.instance)
            mMainFragmentSelectionPagerAdapter.notifyDataSetChanged()

            val message = Message.obtain()
            message.what = MESSAGE_GO_TO_ARTICLE
            message.obj = Common.URL_FOXSCHOOL_NEWS_DETAIL + articleID
            mMainHandler.sendMessageDelayed(message, Common.DURATION_SHORT)
        })

        // 페이지 로딩 완료 (로딩 다이얼로그 닫기)
        mForumFragmentObserver.pageLoadData.observe((mContext as AppCompatActivity), Observer<Boolean?> {
            Log.f("onPageLoadComplete")
            mForumContractView.hideLoading()
        })
    }

    /**
     * 통신 Response Listener
     */
    private val mAsyncListener : AsyncListener = object : AsyncListener
    {
        override fun onRunningStart(code : String) { }

        override fun onRunningEnd(code : String, `object` : Any)
        {
            val result : BaseResult = `object` as BaseResult
            Log.f("code : " + code + ", status : " + result.getStatus())
            if(result.getStatus() == BaseResult.SUCCESS_CODE_OK)
            {
                if(code == Common.COROUTINE_CODE_FORUM)
                {
                    mFoxSchoolNewsBaseObject = `object` as ForumListBaseObject
                    mForumPresenterObserver.onSettingForumList(mFoxSchoolNewsBaseObject)
                }
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

        override fun onRunningCanceled(code : String) { }

        override fun onRunningProgress(code : String, progress : Int) { }

        override fun onRunningAdvanceInformation(code : String, `object` : Any) { }

        override fun onErrorListener(code : String, message : String) { }
    }
}