package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.api.viewmodel.factory.TeacherHomeworkCheckingFactoryViewModel
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeacherHomeworkCheckingActivity : BaseActivity()
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

    @BindViews(R.id._homeworkEvalE0Button, R.id._homeworkEvalE1Button, R.id._homeworkEvalE2Button)
    lateinit var _HomeworkEvalButtonList : List<@JvmSuppressWildcards ImageView>

    @BindViews(R.id._homeworkEvalE0Image, R.id._homeworkEvalE1Image, R.id._homeworkEvalE2Image)
    lateinit var _HomeworkEvalImageList : List<@JvmSuppressWildcards ImageView>

    @BindViews(R.id._homeworkEvalE0Text, R.id._homeworkEvalE1Text, R.id._homeworkEvalE2Text)
    lateinit var _HomeworkEvalTextList : List<@JvmSuppressWildcards TextView>

    @BindView(R.id._homeworkTeacherComment)
    lateinit var _HomeworkTeacherComment : TextView

    @BindView(R.id._commentInputCountText)
    lateinit var _CommentInputCountText : TextView

    @BindView(R.id._commentEditText)
    lateinit var _CommentEditText : EditText

    @BindView(R.id._checkingRegisterButton)
    lateinit var _CheckingRegisterButton : TextView

    @BindView(R.id._checkingCancelButton)
    lateinit var _CheckingCancelButton : TextView

    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mSelected : Int = 1

    private val factoryViewModel : TeacherHomeworkCheckingFactoryViewModel by viewModels()
    
    /** LifeCycle **/
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            setContentView(R.layout.activity_homework_checking_tablet)
        }
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_homework_checking)
        }

        ButterKnife.bind(this)

        initView()
        initFont()
        setupObserverViewModel()

        factoryViewModel.init(this)
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
        _TitleText.text = resources.getString(R.string.text_homework_check)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE
        _CommentEditText.addTextChangedListener(mEditTextChangeListener)
    }

    override fun initFont()
    {
        _TitleText.typeface = Font.getInstance(this).getTypefaceBold()
        _HomeworkEvalTextList.forEach {
            it.setTypeface(Font.getInstance(this).getTypefaceMedium())
        }
        _HomeworkTeacherComment.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _CommentInputCountText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _CommentEditText.setTypeface(Font.getInstance(this).getTypefaceRegular())
        _CheckingRegisterButton.setTypeface(Font.getInstance(this).getTypefaceMedium())
        _CheckingCancelButton.setTypeface(Font.getInstance(this).getTypefaceMedium())
    }

    private fun settingLayoutColor()
    {
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(R.color.color_25b4cf))
        _TitleBaselayout.setBackgroundColor(resources.getColor(R.color.color_29c8e6))
    }

    override fun setupObserverViewModel()
    {
        factoryViewModel.isLoading.observe(this, Observer<Boolean> {loading ->
            if (loading)
            {
                showLoading()
            }
            else
            {
                hideLoading()
            }
        })

        factoryViewModel.toast.observe(this, Observer<String> {message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        factoryViewModel.successMessage.observe(this, Observer<String> {message ->
            CommonUtils.getInstance(this).showSuccessSnackMessage(_MainBaseLayout, message, Gravity.CENTER)
        })

        factoryViewModel.errorMessage.observe(this, Observer<String> {message ->
            CommonUtils.getInstance(this).showErrorSnackMessage(_MainBaseLayout, message, Gravity.CENTER)
        })

        factoryViewModel.settingBeforeData.observe(this, Observer<Pair<Int, String>> {pair ->
            setViewChecked(pair.first)
            if (pair.second != "") _CommentEditText.setText(pair.second)
            _CheckingRegisterButton.setText(resources.getString(R.string.text_change))
            setCommentCountText()
        })
    }
    /** Init end **/

    private fun setViewChecked(index : Int)
    {
        mSelected = index
        _HomeworkEvalButtonList.forEach {
            it.background = this.resources.getDrawable(R.drawable.check_off)
        }
        _HomeworkEvalButtonList[index].background = this.resources.getDrawable(R.drawable.check_on)
    }

    private fun setCommentCountText()
    {
        // 바이트 사이즈 구하기 위해 코멘트 바이트로 변경
        val inputByte = (_CommentEditText.text.toString()).toByteArray(charset("ms949"))
        val text = "${inputByte.size}/100 byte"
        _CommentInputCountText.text = text
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
                    val rect = Rect()
                    val coordinates = IntArray(2)
                    view.getLocationOnScreen(coordinates)
                    rect[coordinates[0], coordinates[1], coordinates[0] + view.width] =
                        coordinates[1] + view.height
                    val x = ev.x.toInt()
                    val y = ev.y.toInt()
                    if(rect.contains(x, y))
                    {
                        return consumed
                    }
                } else if(viewNew is EditText)
                {
                    Log.f("consumed : $consumed")
                    return consumed
                }
                CommonUtils.getInstance(this).hideKeyboard()
                viewNew.clearFocus()
                return consumed
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Optional
    @OnClick(R.id._closeButtonRect, R.id._checkingRegisterButton, R.id._checkingCancelButton,
        R.id._homeworkEvalE0Button, R.id._homeworkEvalE1Button, R.id._homeworkEvalE2Button,
        R.id._homeworkEvalE0Image, R.id._homeworkEvalE1Image, R.id._homeworkEvalE2Image,
        R.id._homeworkEvalE0Text, R.id._homeworkEvalE1Text, R.id._homeworkEvalE2Text,)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._closeButtonRect, R.id._checkingCancelButton ->
            {
                this.setResult(Activity.RESULT_CANCELED)
                this.finish()
            }

            R.id._checkingRegisterButton ->
            {
                // 평가 등록/수정
                factoryViewModel.onClickRegisterButton(mSelected, _CommentEditText.text.toString())
            }

            // [평가1] 참 잘했어요
            R.id._homeworkEvalE0Button,
            R.id._homeworkEvalE0Image,
            R.id._homeworkEvalE0Text->
            {
                setViewChecked(0)
            }

            // [평가2] 잘했어요
            R.id._homeworkEvalE1Button,
            R.id._homeworkEvalE1Image,
            R.id._homeworkEvalE1Text->
            {
                setViewChecked(1)
            }

            // [평가3] 좀 더 노력해요
            R.id._homeworkEvalE2Button,
            R.id._homeworkEvalE2Image,
            R.id._homeworkEvalE2Text->
            {
                setViewChecked(2)
            }
        }
    }

    /**
     * EditText TextChange Listener
     */
    private val mEditTextChangeListener = object : TextWatcher
    {
        override fun beforeTextChanged(s : CharSequence?, start : Int, count : Int, after : Int) { }

        override fun onTextChanged(text : CharSequence?, start : Int, before : Int, count : Int)
        {
            val byte = (text.toString()).toByteArray(charset("ms949"))
            if (byte.size > 100)
            {
                // 100바이트 이상 입력한 경우 텍스트 자르기
                val result = text?.dropLast(1)
                _CommentEditText.setText(result)
                _CommentEditText.setSelection(_CommentEditText.text.toString().length)
            }

            setCommentCountText() // 글자 byte 실시간 갱신
        }

        override fun afterTextChanged(text : Editable?) {}
    }
}