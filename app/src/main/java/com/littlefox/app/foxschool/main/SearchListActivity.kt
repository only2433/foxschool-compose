package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.Nullable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.*
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.adapter.DetailListItemAdapter
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.SearchListContract
import com.littlefox.app.foxschool.main.presenter.SearchListPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import com.ssomai.android.scalablelayout.ScalableLayout

class SearchListActivity : BaseActivity(), MessageHandlerCallback, SearchListContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._searchAllIcon)
    lateinit var _SearchAllIcon : ImageView

    @BindView(R.id._searchAllText)
    lateinit var _SearchAllText : TextView

    @BindView(R.id._searchStoryIcon)
    lateinit var _SearchStoryIcon : ImageView

    @BindView(R.id._searchStoryText)
    lateinit var _SearchStoryText : TextView

    @BindView(R.id._searchSongIcon)
    lateinit var _SearchSongIcon : ImageView

    @BindView(R.id._searchSongText)
    lateinit var _SearchSongText : TextView

    @BindView(R.id._searchEditBackgroundImage)
    lateinit var _SearchEditBackgroundImage : ImageView

    @BindView(R.id._searchCancelIcon)
    lateinit var _SearchCancelIcon : ImageView

    @BindView(R.id._searchSwipeRefreshLayout)
    lateinit var _SearchSwipeRefreshLayout : SwipyRefreshLayout

    @BindView(R.id._searchItemList)
    lateinit var _SearchItemListView : RecyclerView

    @Nullable
    @BindView(R.id._searchConfirmIcon)
    lateinit var _SearchConfirmIcon : ImageView

    @Nullable
    @BindView(R.id._searchConfirmTabletIcon)
    lateinit var _SearchConfirmTabletIcon : TextView

    @BindView(R.id._searchEditText)
    lateinit var _SearchEditText : EditText

    @BindView(R.id._progressWheelLayout)
    lateinit var _ProgressWheelLayout : ScalableLayout

    private lateinit var mSearchListPresenter : SearchListPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(Feature.IS_TABLET)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_search_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_search)
        }
        ButterKnife.bind(this)
        mSearchListPresenter = SearchListPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mSearchListPresenter.resume();
    }

    override fun onPause()
    {
        super.onPause()
        mSearchListPresenter.pause();
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mSearchListPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun initView()
    {
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.text_search)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE

        if(Feature.IS_TABLET)
        {
            val TABLET_LIST_WIDTH = 960
            val params = _SearchSwipeRefreshLayout.layoutParams as RelativeLayout.LayoutParams
            params.width = CommonUtils.getInstance(this).getPixel(TABLET_LIST_WIDTH)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            _SearchSwipeRefreshLayout.layoutParams = params
        }

        _SearchEditText.onFocusChangeListener = mEditFocusListener
        _SearchSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        _SearchEditText.setOnEditorActionListener(mEditorActionListener)
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _SearchAllText.typeface = Font.getInstance(this).getRobotoRegular()
        _SearchStoryText.typeface = Font.getInstance(this).getRobotoRegular()
        _SearchSongText.typeface = Font.getInstance(this).getRobotoRegular()
        _SearchEditText.typeface = Font.getInstance(this).getRobotoRegular()

        if(Feature.IS_TABLET)
        {
            _SearchConfirmTabletIcon.typeface = Font.getInstance(this).getRobotoMedium()
        }
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

    override fun handlerMessage(message : Message)
    {
        mSearchListPresenter.sendMessageEvent(message)
    }

    override fun showContentsListLoading()
    {
        if(_ProgressWheelLayout.visibility == View.GONE)
        {
            _ProgressWheelLayout.visibility = View.VISIBLE
            _SearchItemListView.visibility = View.INVISIBLE
        }
    }

    override fun hideContentsListLoading()
    {
        if(_ProgressWheelLayout.visibility == View.VISIBLE)
        {
            _ProgressWheelLayout.visibility = View.GONE
            _SearchItemListView.visibility = View.VISIBLE
        }
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

    /**
     * 리스트 표시 애니메이션
     */
    @SuppressLint("WrongConstant")
    override fun showSearchListView(adapter : DetailListItemAdapter)
    {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayout.VERTICAL
        _SearchItemListView.layoutManager = linearLayoutManager

        val animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.listview_layoutanimation)
        _SearchItemListView.layoutAnimation = animationController
        _SearchItemListView.adapter = adapter
    }

    /**
     * 리스트 새로고침 취소
     */
    override fun cancelRefreshView()
    {
        Log.f("")
        if(_SearchSwipeRefreshLayout.isRefreshing)
        {
            _SearchSwipeRefreshLayout.isRefreshing = false
        }
    }

    /**
     * 키보드 밖 영역 터치시 키보드 닫기 위한 이벤트
     */
    override fun dispatchTouchEvent(ev : MotionEvent) : Boolean
    {
        if(ev.action == MotionEvent.ACTION_UP)
        {
            val view = currentFocus

            if(view != null)
            {
                val consumed = super.dispatchTouchEvent(ev)

                val viewTmp = currentFocus
                val viewNew : View = viewTmp ?: view

                if(viewNew == view) {
                    val coordinates = IntArray(2)

                    view.getLocationOnScreen(coordinates)

                    val rect = Rect(coordinates[0], coordinates[1], coordinates[0] + view.width, coordinates[1] + view.height)

                    val x = ev.x.toInt()
                    val y = ev.y.toInt()

                    if(rect.contains(x, y)) {
                        return consumed
                    }
                }
                else if(viewNew is EditText) {
                    return consumed
                }
                CommonUtils.getInstance(this@SearchListActivity).hideKeyboard()
                viewNew.clearFocus()

                return consumed
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    @Optional
    @OnClick(
        R.id._closeButtonRect,
        R.id._searchAllRect,
        R.id._searchStoryRect,
        R.id._searchSongRect,
        R.id._searchConfirmIcon,
        R.id._searchConfirmTabletIcon,
        R.id._searchCancelIcon
    )
    fun onClickView(view: View)
    {
        when(view.id)
        {
            R.id._closeButtonRect ->
            {
                super.onBackPressed()
            }
            R.id._searchAllRect ->
            {
                switchSearchTypeIcon(Common.CONTENT_TYPE_ALL)
                mSearchListPresenter.onClickSearchType(Common.CONTENT_TYPE_ALL)
            }
            R.id._searchStoryRect ->
            {
                switchSearchTypeIcon(Common.CONTENT_TYPE_STORY)
                mSearchListPresenter.onClickSearchType(Common.CONTENT_TYPE_STORY)
            }
            R.id._searchSongRect ->
            {
                switchSearchTypeIcon(Common.CONTENT_TYPE_SONG)
                mSearchListPresenter.onClickSearchType(Common.CONTENT_TYPE_SONG)
            }
            R.id._searchCancelIcon ->
            {
                _SearchEditText.setText("")
            }
            R.id._searchConfirmIcon, R.id._searchConfirmTabletIcon ->
            {
                mSearchListPresenter.onClickSearchExecute(_SearchEditText.text.toString())
            }
        }
    }

    /**
     * 검색 체크 아이콘 변경
     */
    private fun switchSearchTypeIcon(type : String)
    {
        when(type)
        {
            Common.CONTENT_TYPE_ALL ->
            {
                _SearchAllIcon.setImageResource(R.drawable.check_on)
                _SearchStoryIcon.setImageResource(R.drawable.check_off)
                _SearchSongIcon.setImageResource(R.drawable.check_off)
            }
            Common.CONTENT_TYPE_STORY ->
            {
                _SearchAllIcon.setImageResource(R.drawable.check_off)
                _SearchStoryIcon.setImageResource(R.drawable.check_on)
                _SearchSongIcon.setImageResource(R.drawable.check_off)
            }
            Common.CONTENT_TYPE_SONG ->
            {
                _SearchAllIcon.setImageResource(R.drawable.check_off)
                _SearchStoryIcon.setImageResource(R.drawable.check_off)
                _SearchSongIcon.setImageResource(R.drawable.check_on)
            }
        }
    }

    /** ====================== set Listener ====================== */

    /**
     * 키보드 이벤트 리스너 (검색)
     */
    private val mEditorActionListener = object : TextView.OnEditorActionListener
    {
        override fun onEditorAction(textView : TextView, id : Int, keyEvent : KeyEvent?) : Boolean
        {
            when(id)
            {
                EditorInfo.IME_ACTION_SEARCH ->
                {
                    mSearchListPresenter.onClickSearchExecute(_SearchEditText.text.toString())
                    CommonUtils.getInstance(this@SearchListActivity).hideKeyboard()
                    _SearchEditText.clearFocus()
                }
            }
            return true
        }
    }

    /**
     * 검색영역 포커싱 이벤트 (백그라운드 이미지 변경용, 선택 시 파란색상)
     */
    private val mEditFocusListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View, hasFocus : Boolean)
        {
            Log.f("hasFocus : $hasFocus")
            when(view.id)
            {
                R.id._searchEditText ->
                    if(hasFocus)
                    {
                        if(Feature.IS_TABLET)
                        {
                            _SearchEditBackgroundImage.setBackgroundResource(R.drawable.text_box_b)
                        } else
                        {
                            _SearchEditBackgroundImage.setBackgroundResource(R.drawable.text_box_b_search)
                        }

                        _SearchEditText.isCursorVisible = true
                    }
                    else
                    {
                        if(Feature.IS_TABLET)
                        {
                            _SearchEditBackgroundImage.setBackgroundResource(R.drawable.box_list)
                        } else
                        {
                            _SearchEditBackgroundImage.setBackgroundResource(R.drawable.search_box1)
                        }

                        _SearchEditText.isCursorVisible = false
                    }
            }
        }
    }

    /**
     * 당겨서 재조회 이벤트 리스너
     */
    private val mOnRefreshListener = object : SwipyRefreshLayout.OnRefreshListener
    {
        override fun onRefresh(direction : SwipyRefreshLayoutDirection?)
        {
            Log.f("direction : $direction")
            mSearchListPresenter.requestRefresh()
        }
    }
}