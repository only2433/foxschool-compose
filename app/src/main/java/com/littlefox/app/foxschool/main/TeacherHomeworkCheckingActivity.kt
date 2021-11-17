package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.*
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.main.contract.TeacherHomeworkCheckingContract
import com.littlefox.app.foxschool.main.presenter.TeacherHomeworkCheckingPresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

class TeacherHomeworkCheckingActivity : BaseActivity(), MessageHandlerCallback, TeacherHomeworkCheckingContract.View
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

    private lateinit var mTeacherHomeworkCheckingPresenter : TeacherHomeworkCheckingPresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null
    private var mSelected : Int = 1
    
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
        mTeacherHomeworkCheckingPresenter = TeacherHomeworkCheckingPresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mTeacherHomeworkCheckingPresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mTeacherHomeworkCheckingPresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mTeacherHomeworkCheckingPresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        mTeacherHomeworkCheckingPresenter.activityResult(requestCode, resultCode, data)
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
        _TitleText.typeface = Font.getInstance(this).getRobotoBold()
        _HomeworkEvalTextList.forEach {
            it.setTypeface(Font.getInstance(this).getRobotoMedium())
        }
        _HomeworkTeacherComment.setTypeface(Font.getInstance(this).getRobotoMedium())
        _CommentInputCountText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CommentEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _CheckingRegisterButton.setTypeface(Font.getInstance(this).getRobotoMedium())
        _CheckingCancelButton.setTypeface(Font.getInstance(this).getRobotoMedium())
    }

    /**
     * 상단바 색상 설정
     */
    private fun settingLayoutColor()
    {
        CommonUtils.getInstance(this).setStatusBar(resources.getColor(R.color.color_25b4cf))
        _TitleBaselayout.setBackgroundColor(resources.getColor(R.color.color_29c8e6))
    }

    /** Init end **/

    /**
     * 기존에 입력되었던 데이터
     */
    override fun setBeforeData(index : Int, comment : String)
    {
        setViewChecked(index)
        if (comment != "") _CommentEditText.setText(comment)
        _CheckingRegisterButton.setText(resources.getString(R.string.text_change))
        setCommentCountText()
    }

    /**
     * 평가 버튼 선택 이미지 전환
     */
    private fun setViewChecked(index : Int)
    {
        mSelected = index
        _HomeworkEvalButtonList.forEach {
            it.background = this.resources.getDrawable(R.drawable.check_off)
        }
        _HomeworkEvalButtonList[index].background = this.resources.getDrawable(R.drawable.check_on)
    }

    /**
     * 코멘트 카운트 텍스트 설정
     */
    private fun setCommentCountText()
    {
        // 바이트 사이즈 구하기 위해 코멘트 바이트로 변경
        val inputByte = (_CommentEditText.text.toString()).toByteArray(charset("EUC-KR"))
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
        mTeacherHomeworkCheckingPresenter.sendMessageEvent(message)
    }

    @Optional
    @OnClick(R.id._homeworkCheckingLayout, R.id._closeButtonRect, R.id._checkingRegisterButton, R.id._checkingCancelButton,
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

            R.id._homeworkCheckingLayout -> CommonUtils.getInstance(this).hideKeyboard()

            R.id._checkingRegisterButton ->
            {
                // 평가 등록/수정
                mTeacherHomeworkCheckingPresenter.onClickRegisterButton(mSelected, _CommentEditText.text.toString())
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
            val byte = (text.toString()).toByteArray(charset("EUC-KR"))
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