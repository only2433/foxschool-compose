package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.BookColor
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.main.contract.ManagementItemMyBooksContract
import com.littlefox.app.foxschool.main.presenter.ManagementItemMyBooksPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 책장/단어장 관리 화면
 */
class ManagementMyBooksActivity : BaseActivity(), MessageHandlerCallback, ManagementItemMyBooksContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaseLayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleTextView : TextView

    @BindView(R.id._messageText)
    lateinit var _MessageText : TextView

    @BindView(R.id._saveButton)
    lateinit var _SaveButton : TextView

    @BindView(R.id._cancelActionButton)
    lateinit var _CancelActionButton : TextView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._nameEditBackground)
    lateinit var _NameEditBackground : ImageView

    @BindView(R.id._nameEditText)
    lateinit var _NameEditText : EditText

    @BindViews(
        R.id._bookIndex0Select,
        R.id._bookIndex1Select,
        R.id._bookIndex2Select,
        R.id._bookIndex3Select,
        R.id._bookIndex4Select,
        R.id._bookIndex5Select
    )
    lateinit var _BookIndexSelectList : List<@JvmSuppressWildcards ImageView>

    @BindView(R.id._bookIndex0Image)
    lateinit var _BookIndex0Image : ImageView

    @BindView(R.id._bookIndex1Image)
    lateinit var _BookIndex1Image : ImageView

    @BindView(R.id._bookIndex2Image)
    lateinit var _BookIndex2Image : ImageView

    @BindView(R.id._bookIndex3Image)
    lateinit var _BookIndex3Image : ImageView

    @BindView(R.id._bookIndex4Image)
    lateinit var _BookIndex4Image : ImageView

    @BindView(R.id._bookIndex5Image)
    lateinit var _BookIndex5Image : ImageView

    private lateinit var mManagementItemMyBooksPresenter : ManagementItemMyBooksPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_manage_mybooks_tablet)
        } else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_manage_mybooks)
        }
        ButterKnife.bind(this)
        mManagementItemMyBooksPresenter = ManagementItemMyBooksPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mManagementItemMyBooksPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mManagementItemMyBooksPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mManagementItemMyBooksPresenter.destroy()
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
        _TitleTextView.text = resources.getString(R.string.text_add_bookshelf)
        _NameEditText.onFocusChangeListener = mEditFocusListener
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
        settingLayoutColor()
    }

    override fun initFont()
    {
        _TitleTextView.setTypeface(Font.getInstance(this).getRobotoBold())
        _MessageText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _NameEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _SaveButton.setTypeface(Font.getInstance(this).getRobotoMedium())
        _CancelActionButton.setTypeface(Font.getInstance(this).getRobotoMedium())
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
     * 선택된 책 색상 설정
     */
    override fun settingBookColor(color : BookColor)
    {
        when(color)
        {
            BookColor.RED -> checkBookIndex(0)
            BookColor.ORANGE -> checkBookIndex(1)
            BookColor.GREEN -> checkBookIndex(2)
            BookColor.BLUE -> checkBookIndex(3)
            BookColor.PURPLE -> checkBookIndex(4)
            BookColor.PINK -> checkBookIndex(5)
        }
    }

    /**
     * 화면 종류에 따른 뷰 설정
     * - 타이틀, 메세지 변경
     */
    override fun settingLayoutView(type : MyBooksType)
    {
        when(type)
        {
            MyBooksType.BOOKSHELF_ADD ->
            {
                _TitleTextView.text = resources.getString(R.string.text_add_bookshelf)
                _MessageText.text = resources.getString(R.string.message_maximum_bookshelf)
            }
            MyBooksType.BOOKSHELF_MODIFY ->
            {
                _TitleTextView.text = resources.getString(R.string.text_manage_bookshelf)
                _MessageText.text = resources.getString(R.string.message_sorting_manage_bookshelf)
            }
            MyBooksType.VOCABULARY_ADD ->
            {
                _TitleTextView.text = resources.getString(R.string.text_add_vocabulary)
                _MessageText.text = resources.getString(R.string.message_maximum_vocabulary)
            }
            MyBooksType.VOCABULARY_MODIFY ->
            {
                _TitleTextView.text = resources.getString(R.string.text_manage_vocabulary)
                _MessageText.text = resources.getString(R.string.message_sorting_manage_vocabulary)
            }
        }
    }

    /**
     * 책 이름 설정
     */
    override fun setBooksName(name : String)
    {
        Log.f("BooksName : $name")
        _NameEditText.setText(name)
    }

    /**
     * 취소/삭제 버튼 설정
     */
    override fun setCancelButtonAction(isDeleteAvailable : Boolean)
    {
        if(isDeleteAvailable)
        {
            _CancelActionButton.text = resources.getString(R.string.text_delete)
        } else
        {
            _CancelActionButton.text = resources.getString(R.string.text_cancel)
        }
    }

    /**
     * 책 체크 이미지 표시/숨김
     */
    private fun checkBookIndex(index : Int)
    {
        Log.i("index : $index")
        for(i in _BookIndexSelectList.indices)
        {
            if (i == index)
            {
                _BookIndexSelectList[i].visibility = View.VISIBLE
            }
            else
            {
                _BookIndexSelectList[i].visibility = View.GONE
            }
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
        if(mMaterialLoadingDialog != null)
        {
            mMaterialLoadingDialog?.dismiss()
            mMaterialLoadingDialog = null
        }
    }

    override fun showSuccessMessage(message : String) { }

    override fun showErrorMessage(message : String)
    {
        CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message)
    }

    override fun handlerMessage(message : Message?)
    {
        mManagementItemMyBooksPresenter.sendMessageEvent(message!!)
    }

    /**
     * 키보드 닫기 처리
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

                if(viewNew == view)
                {
                    val coordinates = IntArray(2)

                    view.getLocationOnScreen(coordinates)

                    val rect = Rect(coordinates[0], coordinates[1], coordinates[0] + view.width, coordinates[1] + view.height)

                    val x = ev.x.toInt()
                    val y = ev.y.toInt()

                    if(rect.contains(x, y))
                    {
                        return consumed
                    }
                }
                else if(viewNew is EditText)
                {
                    return consumed
                }
                CommonUtils.getInstance(this@ManagementMyBooksActivity).hideKeyboard()
                viewNew.clearFocus()

                return consumed
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @OnClick(
        R.id._closeButtonRect, R.id._saveButton, R.id._cancelActionButton, R.id._deleteEditButton,
        R.id._bookIndex0Image, R.id._bookIndex1Image, R.id._bookIndex2Image, R.id._bookIndex3Image, R.id._bookIndex4Image, R.id._bookIndex5Image
    )
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._saveButton ->
            {
                Log.i("")
                CommonUtils.getInstance(this).hideKeyboard()
                mManagementItemMyBooksPresenter.onSelectSaveButton(_NameEditText.text.toString())
            }
            R.id._cancelActionButton -> mManagementItemMyBooksPresenter.onCancelActionButton()
            R.id._closeButtonRect -> mManagementItemMyBooksPresenter.onSelectCloseButton()
            R.id._deleteEditButton -> _NameEditText.setText("")
            R.id._bookIndex0Image ->
            {
                checkBookIndex(0)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.RED)
            }
            R.id._bookIndex1Image ->
            {
                checkBookIndex(1)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.ORANGE)
            }
            R.id._bookIndex2Image ->
            {
                checkBookIndex(2)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.GREEN)
            }
            R.id._bookIndex3Image ->
            {
                checkBookIndex(3)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.BLUE)
            }
            R.id._bookIndex4Image ->
            {
                checkBookIndex(4)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.PURPLE)
            }
            R.id._bookIndex5Image ->
            {
                checkBookIndex(5)
                mManagementItemMyBooksPresenter.onSelectBooksItem(BookColor.PINK)
            }
        }
    }

    private val mEditFocusListener = OnFocusChangeListener {view, hasFocus ->
        Log.f("hasFocus : $hasFocus")
        when(view.id)
        {
            R.id._nameEditText -> try
            {
                if(hasFocus)
                {
                    _NameEditBackground.setBackgroundResource(R.drawable.text_box_b)
                    _NameEditText.isCursorVisible = true
                }
                else
                {
                    _NameEditBackground.setBackgroundResource(R.drawable.box_list)
                    _NameEditText.isCursorVisible = false
                }
            } catch(e : Exception) { }
        }
    }
}