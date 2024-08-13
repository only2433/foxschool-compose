package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import com.littlefox.app.foxschool.api.viewmodel.factory.BookshelfFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.BottomContentItemOptionDialog
import com.littlefox.app.foxschool.dialog.TemplateAlertDialog
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.dialog.listener.ItemOptionListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType
import com.littlefox.app.foxschool.enumerate.ResultLauncherCode
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint

/**
 * 책장 화면
 */
@AndroidEntryPoint
class BookshelfActivity : BaseActivity()
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

    @JvmField
    @BindView(R.id._fabToolbar)
    var _FabToolbarLayout : FABToolbarLayout? = null

    @JvmField
    @BindView(R.id._floatingMenuButtonLayout)
    var _FloatingMenuButtonLayout : RelativeLayout? = null

    @JvmField
    @BindView(R.id._floatingMenuButton)
    var _FloatingMenuButton : FloatingActionButton? = null

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

    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var isListSettingComplete : Boolean = false
    private lateinit var mTemplateAlertDialog : TemplateAlertDialog
    private var mBottomContentItemOptionDialog: BottomContentItemOptionDialog? = null

    private val factoryViewModel : BookshelfFactoryViewModel by viewModels()

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if (CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_bookshelf_detail_list_tablet)
        } else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_bookshelf_detail_list)
        }

        ButterKnife.bind(this)

        initView()
        initFont()
        setupObserverViewModel()
        factoryViewModel.init(this)
        factoryViewModel.onAddResultLaunchers(mBookAddActivityResult)
    }

    override fun onResume()
    {
        super.onResume()
        factoryViewModel.resume()
    }

    override fun onPause()
    {
        super.onPause()
        factoryViewModel.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        factoryViewModel.destroy()
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

    override fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this){ loading ->
            if(loading)
            {
                showLoading()
            } else
            {
                hideLoading()
            }
        }

        factoryViewModel.toast.observe(this){ message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        factoryViewModel.successMessage.observe(this){ message ->
            showSuccessMessage(message)
        }

        factoryViewModel.errorMessage.observe(this){ message ->
            showErrorMessage(message)
        }

        factoryViewModel.setTitle.observe(this){ title ->
            setTitle(title)
        }

        factoryViewModel.showBookshelfDetailListView.observe(this){ adapter ->
            showBookshelfDetailListView(adapter)
        }

        factoryViewModel.enableContentListLoading.observe(this){ enable ->
            if(enable)
            {
                showContentListLoading()
            }
            else
            {
                hideContentListLoading()
            }
        }

        factoryViewModel.enableFloatingToolbarLayout.observe(this){ enable ->
            if(enable)
            {
                showFloatingToolbarLayout()
            }
            else
            {
                hideFloatingToolbarLayout()
            }
        }

        factoryViewModel.setFloatingToolbarPlayCount.observe(this){ count ->
            setFloatingToolbarPlayCount(count)
        }

        factoryViewModel.dialogBottomOption.observe(this){ data ->
            showBottomBookshelfItemDialog(data)
        }

        factoryViewModel.dialogBookshelfContentsDelete.observe(this){
            showBookshelfContentsDeleteDialog()
        }

        factoryViewModel.dialogWarningRecordPermission.observe(this){
            showChangeRecordPermissionDialog()
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

    /**
     * 타이틀 설정
     */
    fun setTitle(title : String?)
    {
        _TitleText.setText(title)
    }

    /**
     * 아이템 선택한 갯수에 따른 뷰 세팅
     */
    fun setFloatingToolbarPlayCount(count : Int)
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
    fun showBookshelfDetailListView(adapter : DetailListItemAdapter)
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
    fun showFloatingToolbarLayout()
    {
        if(CommonUtils.getInstance(this).checkTablet) return

        if(_FabToolbarLayout?.isToolbar == false)
        {
            _FabToolbarLayout?.show()
        }
    }

    /**
     * 하단 툴바 숨김
     */
    fun hideFloatingToolbarLayout()
    {
        if(CommonUtils.getInstance(this).checkTablet)
        {
            _MenuSelectCountText.visibility = View.GONE
            return
        }

        if(_FabToolbarLayout?.isToolbar == true)
        {
            _MenuSelectCountText.visibility = View.GONE
            _FabToolbarLayout?.hide()
        }
    }

    /**
     * ================ 다이얼로그 ================
     */
    private fun showBottomBookshelfItemDialog(data : ContentsBaseResult)
    {
        mBottomContentItemOptionDialog = BottomContentItemOptionDialog(this, data)
        mBottomContentItemOptionDialog!!
            .setDeleteMode()
            .setFullName()
            .setItemOptionListener(mItemOptionListener)
            .setView()
        mBottomContentItemOptionDialog!!.show()
    }

    private fun showBookshelfContentsDeleteDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_question_delete_contents_in_bookshelf))
            setButtonType(DialogButtonType.BUTTON_2)
            setDialogEventType(BookshelfFactoryViewModel.DIALOG_EVENT_DELETE_BOOKSHELF_CONTENTS)
            setDialogListener(mDialogListener)
            show()
        }
    }

    /**
     * 마이크 권한 허용 요청 다이얼로그
     * - 녹음기 기능 사용을 위해
     */
    private fun showChangeRecordPermissionDialog()
    {
        mTemplateAlertDialog = TemplateAlertDialog(this).apply {
            setMessage(resources.getString(R.string.message_record_permission))
            setDialogEventType(BookshelfFactoryViewModel.DIALOG_EVENT_WARNING_RECORD_PERMISSION)
            setButtonType(DialogButtonType.BUTTON_2)
            setButtonText(
                resources.getString(R.string.text_cancel),
                resources.getString(R.string.text_change_permission))
            setDialogListener(mDialogListener)
            show()
        }
    }

    fun showContentListLoading()
    {
        _LoadingProgressLayout.visibility = View.VISIBLE
    }

    fun hideContentListLoading()
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

    fun showSuccessMessage(message : String)
    {
        CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message)
    }

    fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
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
                    factoryViewModel.onClickSelectAll()
                }
            }
            R.id._menuSelectPlayImage, R.id._menuSelectPlayText ->
            {
                // 선택재생
                if(isListSettingComplete)
                {
                    factoryViewModel.onClickSelectPlay()
                }
            }
            R.id._menuRemoveBookshelfImage, R.id._menuRemoveBookshelfText ->
            {
                // 삭제
                if(isListSettingComplete)
                {
                    factoryViewModel.onClickRemoveBookshelf()
                }
            }
            R.id._menuCancelImage, R.id._menuCancelText ->
            {
                // 취소
                if(isListSettingComplete)
                {
                    _MenuSelectCountText.visibility = View.GONE
                    factoryViewModel.onClickCancel()
                    if(CommonUtils.getInstance(this).checkTablet == false)
                    {
                        _FabToolbarLayout?.hide()
                    }
                }
            }
            R.id._floatingMenuButton ->
            {
                // 플로팅 버튼
                if(isListSettingComplete)
                {
                    _FabToolbarLayout?.show()
                }
            }
        }
    }

    private val mBookAddActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if(result.resultCode == RESULT_OK)
        {
            factoryViewModel.onActivityResult(intent = result.data)
        }
    }

    private val mItemOptionListener : ItemOptionListener = object : ItemOptionListener
    {
        override fun onClickQuiz()
        {
            factoryViewModel.onClickQuizButton()
        }

        override fun onClickTranslate()
        {
            factoryViewModel.onClickTranslateButton()
        }

        override fun onClickVocabulary()
        {
            factoryViewModel.onClickVocabularyButton()
        }

        override fun onClickBookshelf()
        {
            factoryViewModel.onClickBookshelfButton()
        }

        override fun onClickEbook()
        {
            factoryViewModel.onClickEbookButton()
        }

        override fun onClickGameStarwords()
        {
            factoryViewModel.onClickStarwordsButton()
        }

        override fun onClickGameCrossword()
        {
            factoryViewModel.onClickCrosswordButton()
        }

        override fun onClickFlashCard()
        {
            factoryViewModel.onClickFlashcardButton()
        }

        override fun onClickRecordPlayer()
        {
            factoryViewModel.onClickRecordPlayerButton()
        }
    }

    private val mDialogListener : DialogListener = object : DialogListener
    {
        override fun onConfirmButtonClick(eventType : Int)
        {
            factoryViewModel.onDialogClick(eventType)
        }

        override fun onChoiceButtonClick(buttonType : DialogButtonType, eventType : Int)
        {
            factoryViewModel.onDialogChoiceClick(buttonType, eventType)
        }
    }
}