package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.vocabulary.VocabularySelectData
import com.littlefox.app.foxschool.adapter.VocabularyItemListAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font

import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.main.contract.VocabularyContract
import com.littlefox.app.foxschool.main.presenter.VocabularyPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.library.view.layoutmanager.LinearLayoutScrollerManager
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout


class VocabularyActivity : BaseActivity(), VocabularyContract.View, MessageHandlerCallback
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

    @BindView(R.id._checkAllIcon)
    lateinit var _CheckAllIcon : ImageView

    @BindView(R.id._checkAllText)
    lateinit var _CheckAllText : TextView

    @BindView(R.id._checkWordIcon)
    lateinit var _CheckWordIcon : ImageView

    @BindView(R.id._checkWordText)
    lateinit var _CheckWordText : TextView

    @BindView(R.id._checkMeaningIcon)
    lateinit var _CheckMeaningIcon : ImageView

    @BindView(R.id._checkMeaningText)
    lateinit var _CheckMeaningText : TextView

    @BindView(R.id._checkExampleIcon)
    lateinit var _CheckExampleIcon : ImageView

    @BindView(R.id._checkExampleText)
    lateinit var _CheckExampleText : TextView

    @BindView(R.id._wordItemList)
    lateinit var _WordItemList : RecyclerView

    @BindView(R.id._loadingProgressLayout)
    lateinit var _LoadingProgressLayout : ScalableLayout

    @BindView(R.id._bottomControlLayout)
    lateinit var _BottomControlLayout : ScalableLayout

    @BindView(R.id._bottomIntervalIcon)
    lateinit var _BottomIntervalIcon : ImageView

    @BindView(R.id._bottomIntervalText)
    lateinit var _BottomIntervalText : TextView

    @BindView(R.id._bottomSelectIcon)
    lateinit var _BottomSelectIcon : ImageView

    @BindView(R.id._bottomSelectText)
    lateinit var _BottomSelectText : TextView

    @BindView(R.id._bottomSelectCountText)
    lateinit var _BottomSelectCountText : TextView

    @BindView(R.id._bottomPlayIcon)
    lateinit var _BottomPlayIcon : ImageView

    @BindView(R.id._bottomPlayText)
    lateinit var _BottomPlayText : TextView

    @BindView(R.id._bottomWordsActionIcon)
    lateinit var _BottomWordsActionIcon : ImageView

    @BindView(R.id._bottomWordsActionText)
    lateinit var _BottomWordsActionText : TextView

    private lateinit var mVocabularyPresenter : VocabularyPresenter
    private var isItemSelected = false
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mVocabularyType : VocabularyType? = null
    private var mAnimationController : LayoutAnimationController? = null
    private var isScrollingDisable = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        if(Feature.IS_TABLET)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_vocabulary_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_vocabulary)
        }
        ButterKnife.bind(this)
        mVocabularyPresenter = VocabularyPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mVocabularyPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mVocabularyPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mVocabularyPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun initView()
    {
        CommonUtils.getInstance(this).setStatusBar(getResources().getColor(R.color.color_5c42a6))
        _BackButton.visibility = View.VISIBLE
        _BackButtonRect.visibility = View.VISIBLE
        _WordItemList.setLayoutManager(LinearLayoutScrollerManager(this))
        _WordItemList.addOnLayoutChangeListener(View.OnLayoutChangeListener {view, i, i1, i2, i3, i4, i5, i6, i7 -> mVocabularyPresenter.onListLayoutChangedComplete()})
        _WordItemList.setOnTouchListener(object : View.OnTouchListener
        {
            override fun onTouch(v : View, event : MotionEvent) : Boolean
            {
                return isScrollingDisable
            }
        })
        if(Feature.IS_TABLET)
        {
            val TABLET_LIST_WIDTH = 960
            val params : LinearLayout.LayoutParams = _WordItemList.getLayoutParams() as LinearLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_LIST_WIDTH)
            params.gravity = Gravity.CENTER_HORIZONTAL
            _WordItemList.setLayoutParams(params)
        }
        _TitleBaselayout.setBackgroundColor(getResources().getColor(R.color.color_8d65ff))
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
    }

    override fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(this).getRobotoBold())
        _CheckAllText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CheckWordText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CheckMeaningText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CheckExampleText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _BottomIntervalText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _BottomPlayText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _BottomSelectText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _BottomWordsActionText.setTypeface(Font.getInstance(this).getRobotoMedium())
    }

    override fun setTitle(title : String)
    {
        _TitleText.setText(title)
    }

    override fun showContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.VISIBLE)
        _WordItemList.setVisibility(View.GONE)
    }

    override fun hideContentListLoading()
    {
        _LoadingProgressLayout.setVisibility(View.GONE)
        _WordItemList.setVisibility(View.VISIBLE)
    }


    override fun showLoading()
    {
        mMaterialLoadingDialog = MaterialLoadingDialog(this, CommonUtils.getInstance(this).getPixel(Common.LOADING_DIALOG_SIZE))
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

    override fun setBottomWordsActionType(type : VocabularyType)
    {
        mVocabularyType = type
        if(type === VocabularyType.VOCABULARY_CONTENTS)
        {
            _BottomWordsActionIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_voca else R.drawable.bottom_voca)
            _BottomWordsActionText.setText(getResources().getString(R.string.text_add_vocabulary))
        }
        else if(type === VocabularyType.VOCABULARY_SHELF)
        {
            _BottomWordsActionIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_delete else R.drawable.bottom_delete)
            _BottomWordsActionText.setText(getResources().getString(R.string.text_delete))
        }
    }

    override fun setBottomIntervalValue(interval : Int)
    {
        if(interval == 0)
        {
            _BottomIntervalText.setText(getResources().getString(R.string.text_not_have_interval))
        }
        else
        {
            _BottomIntervalText.setText(java.lang.String.format(getResources().getString(R.string.text_sec_interval), interval))
        }
    }

    override fun setBottomPlayItemCount(count : Int)
    {
        Log.f("count : $count")
        if(count == 0)
        {
            _BottomSelectCountText.setVisibility(View.GONE)
            setBottomSelectStatusIcon(false)
            return
        }
        else
        {
            setBottomSelectStatusIcon(true)
            _BottomSelectCountText.setVisibility(View.VISIBLE)
        }
        if(count < 10)
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_1)
            _BottomControlLayout.moveChildView(_BottomSelectCountText,
                    if(Feature.IS_TABLET) 1562.0f else 680.0f,
                    if(Feature.IS_TABLET) 512.0f else 10.0f,
                    if(Feature.IS_TABLET) 30.0f else 40.0f,
                    if(Feature.IS_TABLET) 30.0f else 40.0f)
        }
        else if(count < 100)
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_2)
            _BottomControlLayout.moveChildView(_BottomSelectCountText,
                    if(Feature.IS_TABLET) 1562.0f else 680.0f,
                    if(Feature.IS_TABLET) 512.0f else 10.0f,
                    if(Feature.IS_TABLET) 40.0f else 50.0f,
                    if(Feature.IS_TABLET) 30.0f else 40.0f)
        }
        else
        {
            _BottomSelectCountText.setBackgroundResource(R.drawable.count_3)
            _BottomControlLayout.moveChildView(_BottomSelectCountText,
                    if(Feature.IS_TABLET) 1562.0f else 680.0f,
                    if(Feature.IS_TABLET) 512.0f else 10.0f,
                    if(Feature.IS_TABLET) 50.0f else 60.0f,
                    if(Feature.IS_TABLET) 30.0f else 40.0f)
        }
        _BottomSelectCountText.setText(count.toString())
    }

    override fun showListView(adapter : VocabularyItemListAdapter)
    {
        Log.f("")
        mAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _WordItemList.setLayoutAnimation(mAnimationController)
        _WordItemList.setAdapter(adapter)
    }

    override fun checkIconStatusMenu(vocabularySelectData : VocabularySelectData)
    {
        if(vocabularySelectData.isSelectAll())
        {
            Log.f("Check ALL")
            _CheckAllIcon!!.setImageResource(R.drawable.check_on)
            _CheckWordIcon!!.setImageResource(R.drawable.check_on)
            _CheckMeaningIcon!!.setImageResource(R.drawable.check_on)
            _CheckExampleIcon!!.setImageResource(R.drawable.check_on)
        }
        else
        {
            _CheckAllIcon!!.setImageResource(R.drawable.check_off)
            Log.f("Check word: " + vocabularySelectData.isSelectedWord())
            Log.f("Check meaning: " + vocabularySelectData.isSelectedMeaning())
            Log.f("Check example: " + vocabularySelectData.isSelectedExample())
            _CheckWordIcon.setImageResource(if(vocabularySelectData.isSelectedWord()) R.drawable.check_on else R.drawable.check_off)
            _CheckMeaningIcon.setImageResource(if(vocabularySelectData.isSelectedMeaning()) R.drawable.check_on else R.drawable.check_off)
            _CheckExampleIcon.setImageResource(if(vocabularySelectData.isSelectedExample()) R.drawable.check_on else R.drawable.check_off)
        }
    }

    override fun setBottomPlayStatus()
    {
        enableMenu(false)
        isScrollingDisable = true
        _BottomSelectCountText.setVisibility(View.INVISIBLE)
        _BottomPlayIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_stop else R.drawable.bottom_stop)
        _BottomPlayText.setText(getResources().getString(R.string.text_stop_play))
    }

    override fun setBottomStopStatus()
    {
        enableMenu(true)
        isScrollingDisable = false
        _BottomSelectCountText.setVisibility(View.VISIBLE)
        _BottomPlayIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_play else R.drawable.bottom_play)
        _BottomPlayText.setText(getResources().getString(R.string.text_select_play))
    }

    override fun scrollPosition(position : Int)
    {
        Log.f("position : $position")
        if(position == 0)
        {
            _WordItemList.scrollToPosition(0)
        }
        else
        {
            _WordItemList.smoothScrollToPosition(position)
        }
    }

    @OnClick(R.id._backButtonRect, R.id._checkAllIcon, R.id._checkAllText, R.id._checkWordIcon, R.id._checkWordText, R.id._checkMeaningIcon, R.id._checkMeaningText, R.id._checkExampleIcon, R.id._checkExampleText, R.id._bottomIntervalIcon, R.id._bottomIntervalText, R.id._bottomSelectIcon, R.id._bottomSelectText, R.id._bottomPlayIcon, R.id._bottomPlayText, R.id._bottomWordsActionIcon, R.id._bottomWordsActionText)
    fun onClickView(view : View)
    {
        if(_LoadingProgressLayout.getVisibility() == View.VISIBLE)
        {
            return
        }
        Log.f("view.getId() : " + view.id)
        when(view.id)
        {
            R.id._backButtonRect -> super.onBackPressed()
            R.id._checkAllIcon, R.id._checkAllText -> mVocabularyPresenter.onClickMenuSelectAll()
            R.id._checkWordIcon, R.id._checkWordText -> mVocabularyPresenter.onClickMenuWord()
            R.id._checkMeaningIcon, R.id._checkMeaningText -> mVocabularyPresenter.onClickMenuMeaning()
            R.id._checkExampleIcon, R.id._checkExampleText -> mVocabularyPresenter.onClickMenuExample()
            R.id._bottomIntervalIcon, R.id._bottomIntervalText -> mVocabularyPresenter.onClickBottomInterval()
            R.id._bottomWordsActionIcon, R.id._bottomWordsActionText -> if(mVocabularyType === VocabularyType.VOCABULARY_CONTENTS)
            {
                mVocabularyPresenter.onClickBottomPutInVocabularyShelf()
            }
            else
            {
                mVocabularyPresenter.onClickBottomDeleteInVocabularyShelf()
            }
            R.id._bottomSelectIcon, R.id._bottomSelectText -> if(isItemSelected)
            {
                mVocabularyPresenter.onClickBottomRemoveAll()
            }
            else
            {
                mVocabularyPresenter.onClickBottomSelectAll()
            }
            R.id._bottomPlayIcon, R.id._bottomPlayText -> mVocabularyPresenter.onClickBottomPlayAction()
        }
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mVocabularyPresenter.acvitityResult(requestCode, resultCode, data)
    }

    override fun handlerMessage(message : Message)
    {
        mVocabularyPresenter.sendMessageEvent(message)
    }

    private fun setBottomSelectStatusIcon(isItemSelected : Boolean)
    {
        Log.f("isItemSelected : $isItemSelected")
        this.isItemSelected = isItemSelected
        if(isItemSelected)
        {
            _BottomSelectText.setText(getResources().getString(R.string.text_select_init))
            _BottomSelectIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_close else R.drawable.bottom_close)
        }
        else
        {
            _BottomSelectText.setText(getResources().getString(R.string.text_select_all))
            _BottomSelectIcon.setImageResource(if(Feature.IS_TABLET) R.drawable.tablet_all else R.drawable.bottom_all)
        }
    }

    private fun enableMenu(isEnable : Boolean)
    {
        if(isEnable)
        {
            _BottomSelectText.setAlpha(1.0f)
            _BottomSelectIcon.alpha = 1.0f
            _BottomWordsActionIcon.alpha = 1.0f
            _BottomWordsActionText.setAlpha(1.0f)
            _BottomIntervalText.setAlpha(1.0f)
            _BottomIntervalIcon.alpha = 1.0f
        }
        else
        {
            _BottomSelectText.setAlpha(0.5f)
            _BottomSelectIcon.alpha = 0.5f
            _BottomWordsActionIcon.alpha = 0.5f
            _BottomWordsActionText.setAlpha(0.5f)
            _BottomIntervalText.setAlpha(0.5f)
            _BottomIntervalIcon.alpha = 0.5f
        }
        _BottomSelectText.setClickable(isEnable)
        _BottomSelectIcon.isClickable = isEnable
        _BottomWordsActionText.setClickable(isEnable)
        _BottomWordsActionIcon.isClickable = isEnable
        _BottomIntervalText.setClickable(isEnable)
        _BottomIntervalIcon.isClickable = isEnable
        _CheckAllText.setClickable(isEnable)
        _CheckAllIcon.isClickable = isEnable
        _CheckWordText.setClickable(isEnable)
        _CheckWordIcon.isClickable = isEnable
        _CheckMeaningText.setClickable(isEnable)
        _CheckMeaningIcon.isClickable = isEnable
        _CheckExampleText.setClickable(isEnable)
        _CheckExampleIcon.isClickable = isEnable
    }
}