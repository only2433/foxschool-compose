package com.littlefox.app.foxschool.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import net.littlefox.lf_app_fragment.common.Common

class NestedScrollingView : NestedScrollView {
    private var mState: Int = RecyclerView.SCROLL_STATE_IDLE

    interface NestedScrollViewScrollStateListener {
        fun onNestedScrollViewStateChanged(state: Int)
        fun onNestedScrollChanged(scrollX: Int,
                                  scrollY: Int,
                                  oldScrollX: Int,
                                  oldScrollY: Int)
    }

    private val mScrollEndCheckHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MESSAGE_STATE_SCROLL_END) {
                dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE)
            }
        }
    }

    fun setScrollListener(scrollListener: NestedScrollViewScrollStateListener?) {
        mScrollListener = scrollListener
    }

    private var mScrollListener: NestedScrollViewScrollStateListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun stopNestedScroll() {
        super.stopNestedScroll()
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return super.startNestedScroll(axes)
    }

    private fun dispatchScrollState(state: Int) {
        if (mScrollListener != null && mState != state) {
            mScrollListener!!.onNestedScrollViewStateChanged(state)
            mState = state
        }
    }

    protected override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        dispatchScrollState(RecyclerView.SCROLL_STATE_DRAGGING)
        mScrollEndCheckHandler.removeMessages(MESSAGE_STATE_SCROLL_END)
        mScrollEndCheckHandler.sendEmptyMessageDelayed(MESSAGE_STATE_SCROLL_END, Common.DURATION_SHORT)
        mScrollListener!!.onNestedScrollChanged(l, t, oldl, oldt)
        super.onScrollChanged(l, t, oldl, oldt)
    }

    companion object {
        private const val MESSAGE_STATE_SCROLL_END = 101
    }
}