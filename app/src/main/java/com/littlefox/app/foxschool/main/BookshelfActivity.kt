package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.BookshelfContract
import com.littlefox.app.foxschool.main.presenter.BookshelfPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 책장 화면
 */
class BookshelfActivity : BaseActivity(), MessageHandlerCallback, BookshelfContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._backButtonRect)
    lateinit var _BackButtonRect : ImageView

    @BindView(R.id._backButton)
    lateinit var _BackButton : ImageView

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._detailInformationList)
    lateinit var _DetailInformationList : RecyclerView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : ScalableLayout

    @Nullable
    @BindView(R.id._fabToolbar)
    lateinit var _FabToolbarLayout : FABToolbarLayout

    @Nullable
    @BindView(R.id._floatingMenuButtonLayout)
    lateinit var _FloatingMenuButtonLayout : RelativeLayout

    @Nullable
    @BindView(R.id._floatingMenuButton)
    lateinit var _FloatingMenuButton : FloatingActionButton

    @BindView(R.id._floatingMenuBarLayout)
    lateinit var _FloatingMenuBarLayout : ScalableLayout

    @BindView(R.id._menuSelectAllImage)
    lateinit var _MenuSelectAllImage : ImageView

    @BindView(R.id._menuSelectAllText)
    lateinit var _MenuSelectAllText : TextView

    @BindView(R.id._menuSelectPlayImage)
    lateinit var _MenuSelectPlayImage : ImageView

    @BindView(R.id._menuSelectPlayText)
    lateinit var _MenuSelectPlayText : TextView

    @BindView(R.id._menuSelectCountText)
    lateinit var _MenuSelectCountText : TextView

    @BindView(R.id._menuRemoveBookshelfImage)
    lateinit var _MenuRemoveBookshelfImage : ImageView

    @BindView(R.id._menuRemoveBookshelfText)
    lateinit var _MenuRemoveBookshelfText : TextView

    @BindView(R.id._menuCancelImage)
    lateinit var _MenuCancelImage : ImageView

    @BindView(R.id._menuCancelText)
    lateinit var _MenuCancelText : TextView

    private lateinit var mBookshelfPresenter : BookshelfPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    private var isListSettingComplete : Boolean = false

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if (CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_bookshelf_detail_list_tablet)
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_bookshelf_detail_list)
        }

        ButterKnife.bind(this)
        mBookshelfPresenter = BookshelfPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mBookshelfPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mBookshelfPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mBookshelfPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** LifeCycle end **/

    /** Init **/
    override fun initView()
    {
        settingLayoutColor()
        _BackButton.visibility = View.VISIBLE
        _BackButtonRect.visibility = View.VISIBLE

        if(CommonUtils.getInstance(this).checkTablet)
        {
            val TABLET_LIST_WIDTH : Int = 960
            val params : LinearLayout.LayoutParams = _DetailInformationList.layoutParams as LinearLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_LIST_WIDTH)
            params.gravity = Gravity.CENTER_HORIZONTAL
            _DetailInformationList.layoutParams = params
        }
    }

    override fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(this).getTypefaceBold())
        _MenuSelectAllText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _MenuSelectPlayText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _MenuSelectCountText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _MenuRemoveBookshelfText.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _MenuCancelText.setTypeface(Font.getInstance(this).getTypefaceMedium())
    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        val statusBarColor : Int = CommonUtils.getInstance(this).getTopBarStatusBarColor()
        val backgroundColor : Int = CommonUtils.getInstance(this).getTopBarBackgroundColor()
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(statusBarColor))
        _TitleBaselayout.setBackgroundColor(resources.getColor(backgroundColor))
    }

    /**
     * 타이틀 설정
     */
    override fun setTitle(title : String?)
    {
        _TitleText.setText(title)
    }

    /**
     * 아이템 선택한 갯수에 따른 뷰 세팅
     */
    override fun setFloatingToolbarPlayCount(count : Int)
    {
        Log.f("count : $count")
        val isTablet : Boolean = CommonUtils.getInstance(this).checkTablet
        _MenuSelectCountText.visibility = View.VISIBLE

        if (count < 10)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_1)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(isTablet) 1562f else 410f,
                if(isTablet) 271f else 10f,
                if(isTablet) 30f else 40f,
                if(isTablet) 30f else 40f
            )
        }
        else if (count < 100)
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_2)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(isTablet) 1562f else 410f,
                if(isTablet) 271f else 10f,
                if(isTablet) 40f else 50f,
                if(isTablet) 30f else 40f
            )
        }
        else
        {
            _MenuSelectCountText.setBackgroundResource(R.drawable.count_3)
            _FloatingMenuBarLayout.moveChildView(
                _MenuSelectCountText,
                if(isTablet) 1562f else 410f,
                if(isTablet) 271f else 10f,
                if(isTablet) 50f else 60f,
                if(isTablet) 30f else 40f
            )
        }
        _MenuSelectCountText.setText(count.toString())
    }

    /**
     * 리스트뷰
     */
    override fun showBookshelfDetailListView(adapter : DetailListItemAdapter)
    {
        isListSettingComplete = true
        _DetailInformationList.visibility = View.VISIBLE

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        _DetailInformationList.layoutManager = linearLayoutManager

        val animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _DetailInformationList.layoutAnimation = animationController
        _DetailInformationList.adapter = adapter
    }

    /**
     * 하단 툴바 표시 (모바일)
     */
    override fun showFloatingToolbarLayout()
    {
        if(CommonUtils.getInstance(this).checkTablet) return

        if(_FabToolbarLayout.isToolbar == false)
        {
            _FabToolbarLayout.show()
        }
    }

    /**
     * 하단 툴바 숨김
     */
    override fun hideFloatingToolbarLayout()
    {
        if(CommonUtils.getInstance(this).checkTablet)
        {
            _MenuSelectCountText.visibility = View.GONE
            return
        }

        if(_FabToolbarLayout.isToolbar == true)
        {
            _MenuSelectCountText.visibility = View.GONE
            _FabToolbarLayout.hide()
        }
    }

    override fun showContentListLoading()
    {
        _LoadingProgressLayout.visibility = View.VISIBLE
    }

    override fun hideContentListLoading()
    {
        _LoadingProgressLayout.visibility = View.GONE
    }

    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(
            this,
            CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE)
        )
        mMaterialLoadingDialog?.show()
    }

    override fun hideLoading()
    {
        mMaterialLoadingDialog?.dismiss()
        mMaterialLoadingDialog = null
    }

    override fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    override fun handlerMessage(message : Message)
    {
        mBookshelfPresenter.sendMessageEvent(message)
    }

    @Optional
    @OnClick(
        R.id._backButtonRect, R.id._menuSelectAllImage, R.id._menuSelectAllText, R.id._menuSelectPlayImage,
        R.id._menuSelectPlayText, R.id._menuRemoveBookshelfImage , R.id._menuRemoveBookshelfText, R.id._menuCancelImage,
        R.id._menuCancelText, R.id._floatingMenuButton
    )
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
            R.id._menuSelectAllImage, R.id._menuSelectAllText ->
            {
                // 전체선택
                if (isListSettingComplete)
                {
                    mBookshelfPresenter.onClickSelectAll()
                }
            }
            R.id._menuSelectPlayImage, R.id._menuSelectPlayText ->
            {
                // 선택재생
                if(isListSettingComplete)
                {
                    mBookshelfPresenter.onClickSelectPlay()
                }
            }
            R.id._menuRemoveBookshelfImage, R.id._menuRemoveBookshelfText ->
            {
                // 삭제
                if(isListSettingComplete)
                {
                    mBookshelfPresenter.onClickRemoveBookshelf()
                }
            }
            R.id._menuCancelImage, R.id._menuCancelText ->
            {
                // 취소
                if(isListSettingComplete)
                {
                    _MenuSelectCountText.visibility = View.GONE
                    mBookshelfPresenter.onClickCancel()
                    if(CommonUtils.getInstance(this).checkTablet == false)
                    {
                        _FabToolbarLayout.hide()
                    }
                }
            }
            R.id._floatingMenuButton ->
            {
                // 플로팅 버튼
                if(isListSettingComplete)
                {
                    _FabToolbarLayout.show()
                }
            }
        }
    }
}