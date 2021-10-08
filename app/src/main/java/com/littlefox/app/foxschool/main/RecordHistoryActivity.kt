package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.RecordHistoryListAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.RecordHistoryContract
import com.littlefox.app.foxschool.main.presenter.RecordHistoryPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 녹음기록 화면
 * @author 김태은
 */
class RecordHistoryActivity : BaseActivity(), MessageHandlerCallback, RecordHistoryContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout
    
    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._recordContentsLayout)
    lateinit var _RecordContentsLayout : ScalableLayout

    @BindView(R.id._recordHistoryInfoText)
    lateinit var _RecordHistoryInfoText : TextView
    
    @BindView(R.id._recordHistoryListView)
    lateinit var _RecordHistoryListView : RecyclerView

    @BindView(R.id._recordHistoryEmptyLayout)
    lateinit var _RecordHistoryEmptyLayout : RelativeLayout

    @BindView(R.id._recordHistoryEmptyText)
    lateinit var _RecordHistoryEmptyText : TextView

    private lateinit var mRecordHistoryPresenter : RecordHistoryPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_record_history_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_record_history)
        }

        ButterKnife.bind(this)
        mRecordHistoryPresenter = RecordHistoryPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mRecordHistoryPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mRecordHistoryPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mRecordHistoryPresenter.destroy()
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
        _TitleText.text = resources.getString(R.string.text_record_history)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _RecordHistoryInfoText.typeface = Font.getInstance(this).getRobotoRegular()
        _RecordHistoryEmptyText.typeface = Font.getInstance(this).getRobotoRegular()
    }

    /** Init end **/

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

    override fun showRecordHistoryListView(adapter : RecordHistoryListAdapter)
    {
        _RecordHistoryEmptyLayout.visibility = View.GONE
        _RecordHistoryListView.visibility = View.VISIBLE

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        _RecordHistoryListView.layoutManager = linearLayoutManager

        val animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _RecordHistoryListView.layoutAnimation = animationController
        _RecordHistoryListView.adapter = adapter
    }

    override fun showRecordHistoryEmptyMessage()
    {
        _RecordHistoryEmptyLayout.visibility = View.VISIBLE
        _RecordHistoryListView.visibility = View.GONE
    }

    override fun handlerMessage(message : Message)
    {
        mRecordHistoryPresenter.sendMessageEvent(message)
    }

    @Optional
    @OnClick(R.id._closeButtonRect)
    fun onClickView(view: View)
    {
        when(view.id)
        {
            R.id._closeButtonRect -> super.onBackPressed()
        }
    }
}