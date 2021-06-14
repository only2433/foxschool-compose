package com.littlefox.app.foxschool.view


import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.main.BannerInformationResult
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.view.listener.OnBannerClickListener
import com.littlefox.logmonitor.Log
import com.ssomai.android.scalablelayout.ScalableLayout
import java.util.*

class BannerView : RelativeLayout
{
    companion object {
        private const val MESSAGE_CHANGE_BANNER = 1001
        private const val DURATION_PAGE_CHANGE = 3000
        private const val DURATION_PAGE_ANIMATION = 300
        private const val DEFAULT_VIEW_PAGER_WIDTH = 940
        private const val DEFAULT_VIEW_PAGER_HEIGHT = 400
        private const val DEFAULT_INDICATOR_SIZE = 30
        private const val DEFAULT_INDICATOR_TOP = 370
        private const val TAG_INDICATOR = "indicator"
        private const val TAG_IMAGE = "image"
    }

    internal inner class PageChangeTask : TimerTask() {
        override fun run() {
            if (mCurrentPosition >= mBannerInformationList!!.size - 1) {
                mCurrentPosition = 0
            } else {
                mCurrentPosition++
            }
            mMainHandler.sendEmptyMessage(MESSAGE_CHANGE_BANNER)
        }
    }

    var mMainHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_CHANGE_BANNER -> {
                    notifyIndicator(mCurrentPosition)
                    _BannerViewSwitcher.showNext()
                }
            }
        }
    }

    /**
     * View의 각각의 정보
     */
    private var mBaseWidth = 0
    private var mBaseHeight = 0
    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mIndicatorSize = 0
    private var mIndicatorTop = 0
    private var mOnBannerClickListener: OnBannerClickListener? = null
    private var isIndicatorHave = false
    private var mCurrentPosition = 0
    private var mBannerInformationList: ArrayList<BannerInformationResult>? = null
    private lateinit var _BaseLayout: ScalableLayout
    private lateinit var _BannerViewSwitcher: ViewFlipper
    private lateinit var mContext: Context
    private lateinit var mLayoutInflator: LayoutInflater
    private var mDiffImageSizeInLayout = 0
    private var mPageChangeTimer: Timer? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
        val typedArray: TypedArray = mContext!!.obtainStyledAttributes(attrs, R.styleable.banner)
        setTypeArray(typedArray)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        val typedArray: TypedArray = mContext!!.obtainStyledAttributes(attrs, R.styleable.banner)
        setTypeArray(typedArray)
    }

    private fun initView(context: Context) {
        mContext = context
        mLayoutInflator = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = mLayoutInflator.inflate(R.layout.banner_view, this, false)
        addView(view)
        _BaseLayout = view.findViewById<View>(R.id._baseBannerLayout) as ScalableLayout
        _BannerViewSwitcher = view.findViewById<View>(R.id._bannerViewSwitcher) as ViewFlipper
    }

    /**
     * XML 에서 지정해둔 정보를 가져온다.
     * @param typedArray  XML 정보
     */
    private fun setTypeArray(typedArray: TypedArray) {
        mBaseWidth = typedArray.getInt(R.styleable.banner_base_width, 0)
        mBaseHeight = typedArray.getInt(R.styleable.banner_base_height, 0)
        mImageWidth = typedArray.getInt(R.styleable.banner_image_width, DEFAULT_VIEW_PAGER_WIDTH)
        mImageHeight = typedArray.getInt(R.styleable.banner_image_height, DEFAULT_VIEW_PAGER_HEIGHT)
        mIndicatorSize = typedArray.getInt(R.styleable.banner_indicator_size, DEFAULT_INDICATOR_SIZE)
        mIndicatorTop = typedArray.getInt(R.styleable.banner_indicator_top, DEFAULT_INDICATOR_TOP)
        typedArray.recycle()
        initBaseLayout()
    }

    private fun initBaseLayout() {
        if (mBaseWidth != 0 && mBaseHeight != 0) _BaseLayout.setScaleSize(mBaseWidth.toFloat(), mBaseHeight.toFloat())
        if (mImageWidth != 0 && mImageHeight != 0) {
            _BaseLayout.moveChildView(
                    _BannerViewSwitcher, (
                    (mBaseWidth - mImageWidth) / 2).toFloat(), (
                    (mBaseHeight - mImageHeight) / 2).toFloat(),
                    mImageWidth.toFloat(),
                    mImageHeight
                            .toFloat())
        }
    }

    private fun initImageAddLayoutParams() {
        _BannerViewSwitcher.setInAnimation(mContext, android.R.anim.fade_in)
        _BannerViewSwitcher.setOutAnimation(mContext, android.R.anim.fade_out)
        if (isIndicatorHave) {
            for (i in mBannerInformationList!!.indices) {
                val imageView = ImageView(mContext)
                (mContext as AppCompatActivity?)!!.runOnUiThread(Runnable {
                    Glide.with(mContext)
                            .load(mBannerInformationList!![i].getImageUrl())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView)
                })
                imageView.setOnClickListener { v ->
                    Log.i("getTag : " + v.tag.toString())
                    mOnBannerClickListener?.onBannerSelectItem(i)
                }
                _BannerViewSwitcher.addView(imageView)
            }
        }
    }

    /**
     * 배너 이미지 사이즈 중앙에 배너 인디케이터를 위치 시키는 메소드
     */
    private fun initIndicatorLayoutParams() {
        mDiffImageSizeInLayout = ((_BaseLayout.getScaleWidth() - mImageWidth) / 2).toInt()
        val DEFAULT_INDICATOR_MARGIN_LEFT: Int = CommonUtils.getInstance(mContext).getPixel(34)
        val centerLocation = if (mBannerInformationList!!.size > 1) (mImageWidth - (mIndicatorSize * mBannerInformationList!!.size + DEFAULT_INDICATOR_MARGIN_LEFT * (mBannerInformationList!!.size - 1))) / 2 + mDiffImageSizeInLayout else (mImageWidth - mIndicatorSize) / 2 + mDiffImageSizeInLayout
        if (isIndicatorHave) {
            for (i in mBannerInformationList!!.indices) {
                val imageView = ImageView(mContext)
                if (i == 0) {
                    imageView.setImageResource(R.drawable.banner_ball_on)
                } else {
                    imageView.setImageResource(R.drawable.banner_ball_off)
                }
                imageView.tag = TAG_INDICATOR + i
                _BaseLayout.addView(imageView, (centerLocation + DEFAULT_INDICATOR_MARGIN_LEFT * i + mIndicatorSize * i).toFloat(), mIndicatorTop.toFloat(), mIndicatorSize.toFloat(), mIndicatorSize.toFloat())
            }
        }
    }

    /**
     * 화면에 보이는 영역의 배너 포지션에 인디케이터를 표시하여 갱신한다.
     * @param position 현재 보이는 index
     */
    private fun notifyIndicator(position: Int) {
        for (i in 0 until _BaseLayout.getChildCount()) {
            val view: View = _BaseLayout.getChildAt(i)
            if (view.tag.toString().contains(TAG_INDICATOR)) {
                if (view.tag == TAG_INDICATOR + position) {
                    (view as ImageView).setImageResource(R.drawable.banner_ball_on)
                } else {
                    (view as ImageView).setImageResource(R.drawable.banner_ball_off)
                }
            }
        }
    }

    private fun getPositionBanner(tag: String): Int {
        for (i in 0 until _BaseLayout.getChildCount()) {
            val view: View = _BaseLayout.getChildAt(i)
            if (view.tag.toString().contains(TAG_INDICATOR)) {
                if (view.tag == tag) {
                    return i
                }
            }
        }
        return 0
    }

    private fun enablePageChangeTimer(isEnable: Boolean) {
        if (isEnable) {
            mPageChangeTimer = Timer()
            mPageChangeTimer!!.schedule(PageChangeTask(), DURATION_PAGE_CHANGE.toLong(), DURATION_PAGE_CHANGE.toLong())
        } else {
            if (mPageChangeTimer != null) {
                mPageChangeTimer!!.cancel()
                mPageChangeTimer = null
            }
        }
    }

    fun setBannerInformation(list: ArrayList<BannerInformationResult>?) {
        mBannerInformationList = list
        if (mBannerInformationList!!.size != 0) {
            isIndicatorHave = true
        }
        initImageAddLayoutParams()
        initIndicatorLayoutParams()
    }

    fun startBanner() {
        if (mBannerInformationList != null && mBannerInformationList!!.size > 1) {
            enablePageChangeTimer(true)
        }
    }

    fun releaseBanner() {
        if (mBannerInformationList != null && mBannerInformationList!!.size > 1) {
            enablePageChangeTimer(false)
        }
    }

    fun setOnBannerClickListener(onBannerClickListener: OnBannerClickListener?)
    {
        mOnBannerClickListener = onBannerClickListener
    }


}