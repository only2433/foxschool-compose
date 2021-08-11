package com.littlefox.app.foxschool.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.base.BaseActivity
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.enumerate.InputDataType
import com.littlefox.app.foxschool.main.contract.InquireContract
import com.littlefox.app.foxschool.main.presenter.InquirePresenter
import com.littlefox.library.system.handler.callback.MessageHandlerCallback
import com.littlefox.library.view.dialog.MaterialLoadingDialog
import com.ssomai.android.scalablelayout.ScalableLayout

/**
 * 1:1 문의하기 화면
 * @author 김태은
 */
class InquireActivity : BaseActivity(), MessageHandlerCallback, InquireContract.View
{
    @BindView(R.id._mainBaseLayout)
    lateinit var _MainBaseLayout : CoordinatorLayout

    @BindView(R.id._titleBaselayout)
    lateinit var _TitleBaselayout : ScalableLayout

    @BindView(R.id._titleText)
    lateinit var _TitleText : TextView

    @BindView(R.id._closeButtonRect)
    lateinit var _CloseButtonRect : ImageView

    @BindView(R.id._closeButton)
    lateinit var _CloseButton : ImageView

    @BindView(R.id._1on1ContentsLayout)
    lateinit var _1on1ContentsLayout : ScalableLayout

    @BindView(R.id._1on1EmailCheckText)
    lateinit var _1on1EmailCheckText : TextView

    @BindView(R.id._selectCategoryBg)
    lateinit var _SelectCategoryBg : ImageView

    @BindView(R.id._selectCategoryText)
    lateinit var _SelectCategoryText : TextView

    @BindView(R.id._selectCategoryButton)
    lateinit var _SelectCategoryButton : ImageView

    @BindView(R.id._inputEmailBg)
    lateinit var _InputEmailBg : ImageView

    @BindView(R.id._inputEmailEditText)
    lateinit var _InputEmailEditText : EditText

    @BindView(R.id._inputMessageBg)
    lateinit var _InputMessageBg : ImageView

    @BindView(R.id._inputMessageEditText)
    lateinit var _InputMessageEditText : EditText

    @BindView(R.id._1on1TipText)
    lateinit var _1on1TipText : TextView

    @BindView(R.id._registerButton)
    lateinit var _RegisterButton : TextView

    @BindView(R.id._sendEmailButton)
    lateinit var _SendEmailButton : TextView

    @BindView(R.id._1on1ReplyText)
    lateinit var _1on1ReplyText : TextView

    private lateinit var mInquirePresenter : InquirePresenter
    private var mMaterialLoadingDialog : MaterialLoadingDialog? = null

    /** ========== LifeCycle ========== */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        if(CommonUtils.getInstance(this).checkTablet)
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setContentView(R.layout.activity_inquire_tablet)
        }
        else
        {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContentView(R.layout.activity_inquire)
        }
        ButterKnife.bind(this)

        mInquirePresenter = InquirePresenter(this)
    }

    override fun onResume()
    {
        super.onResume()
        mInquirePresenter.resume()
    }

    override fun onPause()
    {
        super.onPause()
        mInquirePresenter.pause()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mInquirePresenter.destroy()
    }

    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }
    /** ========== LifeCycle ========== */

    /** ========== Init ========== */
    override fun initView()
    {
        settingLayoutColor()
        _TitleText.text = resources.getString(R.string.text_1_1_ask)
        _CloseButton.visibility = View.VISIBLE
        _CloseButtonRect.visibility = View.VISIBLE

        _InputEmailEditText.onFocusChangeListener = mEditInfoFocusListener
        _InputMessageEditText.onFocusChangeListener = mEditInfoFocusListener
    }

    override fun initFont()
    {
        _TitleText.setTypeface(Font.getInstance(this).getRobotoBold())
        _1on1EmailCheckText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _SelectCategoryText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _InputEmailEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _InputMessageEditText.setTypeface(Font.getInstance(this).getRobotoRegular())
        _1on1TipText.setTypeface(Font.getInstance(this).getRobotoMedium())
        _RegisterButton.setTypeface(Font.getInstance(this).getRobotoMedium())
        _SendEmailButton.setTypeface(Font.getInstance(this).getRobotoMedium())
        _1on1ReplyText.setTypeface(Font.getInstance(this).getRobotoMedium())
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

    /** ========== Init end ========== */

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
        mInquirePresenter.sendMessageEvent(message)
    }

    /**
     * 선택한 카테고리 텍스트 화면에 반영
     */
    override fun setInquireCategoryText(category : String)
    {
        _SelectCategoryText.setText(category)
        _SelectCategoryText.setTextColor(this.resources.getColor(R.color.color_444444))
    }

    /**
     * 사용자 이메일 데이터 화면에 세팅
     */
    override fun setUserEmail(email : String)
    {
        _InputEmailEditText.setText(email)
    }

    /**
     * 입력필드 에러 표시
     */
    override fun setInputError(type : InputDataType)
    {
        CommonUtils.getInstance(this).hideKeyboard()
        when(type)
        {
            InputDataType.NONE -> _SelectCategoryBg.setBackgroundResource(R.drawable.box_list_error)
            InputDataType.EMAIL -> _InputEmailBg.setBackgroundResource(R.drawable.box_list_error)
            InputDataType.MESSAGE -> _InputMessageBg.setBackgroundResource(R.drawable.box_list_error)
        }
    }

    @OnClick(R.id._1on1ContentsLayout, R.id._selectCategoryBg, R.id._selectCategoryText, R.id._registerButton, R.id._sendEmailButton, R.id._closeButtonRect)
    fun onClickView(view : View)
    {
        when(view.id)
        {
            R.id._1on1ContentsLayout -> CommonUtils.getInstance(this).hideKeyboard()
            R.id._selectCategoryBg, R.id._selectCategoryText ->
            {
                _SelectCategoryBg.setBackgroundResource(R.drawable.text_box)
                mInquirePresenter.onShowInquireCategoryDialog()
            }
            R.id._registerButton -> mInquirePresenter.onClickRegister(_InputEmailEditText.text.toString().trim(), _InputMessageEditText.text.toString())
            R.id._sendEmailButton -> mInquirePresenter.onClickSendToEmail(_InputMessageEditText.text.toString())
            R.id._closeButtonRect -> super.onBackPressed()
        }
    }

    /**
     * EditText TextChange Listener
     */
    private val mEditInfoFocusListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View?, hasFocus : Boolean)
        {
            when(view?.id)
            {
                // 이메일
                R.id._inputEmailEditText ->
                {
                    if (hasFocus)
                    {
                        _InputEmailBg.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputEmailBg.setBackgroundResource(R.drawable.text_box)
                        mInquirePresenter.checkEmailAvailable(_InputEmailEditText.text.toString().trim())
                    }
                }

                // 내용
                R.id._inputMessageEditText ->
                {
                    if (hasFocus)
                    {
                        _InputMessageBg.setBackgroundResource(R.drawable.text_box_b)
                    }
                    else
                    {
                        _InputMessageBg.setBackgroundResource(R.drawable.text_box)
                    }
                }
            }
        }
    }
}