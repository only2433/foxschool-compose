package com.littlefox.app.foxschool.dialog


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Font
import com.littlefox.app.foxschool.dialog.listener.DialogListener
import com.littlefox.app.foxschool.enumerate.DialogButtonType

class TemplateAlertDialog(private val mContext : Context)
{
    protected var isCancelable : Boolean = true
    protected var isPasswordConfirm : Boolean = false // 비밀번호 확인 다이얼로그 플래그
    protected var mTitle = ""
    protected var mMessage = ""
    protected var mFirstButtonText : String? = null
    protected var mSecondButtonText : String? = null
    protected var mButtonCount = -1
    protected var mDialogEventType = -1
    protected var mIconResource = -1
    private lateinit var mDialogListener : DialogListener
    private lateinit var mAlertDialogBuilder : AlertDialog.Builder
    private val _BaseTitleLayout : LinearLayout? = null
    private val _TitleText : TextView? = null
    private val _ImageView : ImageView? = null
    private var _EditText : EditText? = null
    private var mGravityValue = -1

    /**
     * 리스너에서 전달해주는 Event 타입
     * @param type Event타입
     */
    fun setDialogEventType(type : Int)
    {
        mDialogEventType = type
    }

    fun setIconResource(icon : Int)
    {
        mIconResource = icon
    }

    fun setTitle(title : String)
    {
        mTitle = title
    }

    fun setMessage(message : String)
    {
        mMessage = message
    }

    fun setButtonType(buttonType : DialogButtonType)
    {
        if(buttonType == DialogButtonType.BUTTON_1)
        {
            setButtonText(mContext.resources.getString(R.string.text_confirm), "")
        }
        else
        {
            setButtonText(mContext.resources.getString(R.string.text_cancel), mContext.resources.getString(R.string.text_confirm))
        }
    }

    fun setButtonText(firstButtonText : String)
    {
        setButtonText(firstButtonText, "")
    }

    fun setButtonText(firstButtonText : String, secondButtonText : String)
    {
        mFirstButtonText = firstButtonText
        mSecondButtonText = secondButtonText
        if(secondButtonText == "")
        {
            mButtonCount = 1
        }
        else
        {
            mButtonCount = 2
        }
    }

    fun setGravity(gravity : Int)
    {
        mGravityValue = gravity
    }

    fun setCancelPossible(isCancelable : Boolean)
    {
        this.isCancelable = isCancelable
    }

    fun setDialogListener(dialogListener : DialogListener)
    {
        mDialogListener = dialogListener
    }

    /**
     * [비밀번호 확인 다이얼로그] EditText 표시 여부
     */
    fun setPasswordConfirmView(isPasswordConfirm : Boolean)
    {
        this.isPasswordConfirm = isPasswordConfirm
    }

    /**
     * [비밀번호 확인 다이얼로그] EditText 입력 값 내보내기
     */
    fun getPasswordInputData() : String
    {
        if (_EditText != null)
        {
            return _EditText!!.text.toString().trim()
        }
        return ""
    }

    fun show()
    {
        mAlertDialogBuilder = AlertDialog.Builder(mContext)
        if(mTitle != "")
        {
            mAlertDialogBuilder.setTitle(mTitle)
        }
        mAlertDialogBuilder.setMessage(mMessage)
        if(isCancelable)
        {
            mAlertDialogBuilder.setCancelable(true)
        }
        else
        {
            mAlertDialogBuilder.setCancelable(false)
        }

        if (isPasswordConfirm)
        {
            // [비밀번호 확인 다이얼로그] 뷰 세팅
            // TODO : 비밀번호 입력 다이얼로그 디자인이 나오면 픽셀값 확인 필요합니다.
            _EditText = EditText(mContext)
            val coordinatorLayout = CoordinatorLayout(mContext)
            val layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.marginStart = CommonUtils.getInstance(mContext).getPixel(50)
            layoutParams.marginEnd = CommonUtils.getInstance(mContext).getPixel(50)
            _EditText!!.layoutParams = layoutParams
            _EditText!!.inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            _EditText!!.height = CommonUtils.getInstance(mContext).getPixel(70)
            _EditText!!.setPadding(CommonUtils.getInstance(mContext).getPixel(30), 0, CommonUtils.getInstance(mContext).getPixel(30), 0)
            _EditText!!.setBackgroundResource(R.drawable.text_box)
            coordinatorLayout.addView(_EditText)
            mAlertDialogBuilder.setView(coordinatorLayout)
        }

        if(mIconResource != -1)
        {
            if(CommonUtils.getInstance(mContext).displayWidthPixel > Common.TARGET_PHONE_DISPLAY_WIDTH)
            {
                val bitmap : Bitmap = CommonUtils.getInstance(mContext).getBitmapFromDrawable(mContext.resources.getDrawable(mIconResource), CommonUtils.getInstance(mContext)!!.getPixel(60), CommonUtils.getInstance(mContext)!!.getPixel(60))
                val drawable : Drawable = CommonUtils.getInstance(mContext).getDrawableFromBitmap(bitmap)
                mAlertDialogBuilder.setIcon(drawable)
            }
            else
            {
                mAlertDialogBuilder.setIcon(mIconResource)
            }
        }
        mAlertDialogBuilder.setNegativeButton(mFirstButtonText, object : DialogInterface.OnClickListener
        {
            override fun onClick(dialog : DialogInterface, which : Int)
            {
                try
                {
                    if(mButtonCount == 1)
                    {
                        mDialogListener.onConfirmButtonClick(mDialogEventType)
                    }
                    else
                    {
                        mDialogListener.onChoiceButtonClick(DialogButtonType.BUTTON_1, mDialogEventType)
                    }
                }
                catch(e : NullPointerException)
                {
                }
            }
        })
        if(mButtonCount == 2)
        {
            mAlertDialogBuilder.setPositiveButton(mSecondButtonText, object : DialogInterface.OnClickListener
            {
                override fun onClick(dialog : DialogInterface, which : Int)
                {
                    try
                    {
                        mDialogListener.onChoiceButtonClick(DialogButtonType.BUTTON_2, mDialogEventType)
                    }
                    catch(e : NullPointerException) { }
                }
            })
        }


        val dialog = mAlertDialogBuilder.show()
        val messageText : TextView? = dialog.findViewById<View>(android.R.id.message) as TextView?
        messageText!!.setTypeface(Font.getInstance(mContext).getRobotoRegular())
        if(mGravityValue != -1)
        {
            messageText!!.setGravity(mGravityValue)
        }
        else
        {
            messageText!!.setGravity(Gravity.CENTER)
        }
        dialog.show()
    }

    companion object
    {
        /** 다이얼로그 이벤트를 굳이 받지않아도 되는 경우 사용  */
        const val DIALOG_EVENT_DEFAULT : Int    = 0
        const val MODE_TEXT : Int               = 0
        const val MODE_TITLE_HAVE_TEXT : Int    = 1
        const val TEXT_SIZE : Int               = 36
    }

    init
    {
        mTitle = ""
        mMessage = ""
    }
}