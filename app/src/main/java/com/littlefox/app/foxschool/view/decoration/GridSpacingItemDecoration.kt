package com.littlefox.app.foxschool.view.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.common.Feature

class GridSpacingItemDecoration : ItemDecoration
{
    private var mContext : Context
    private var spanCount : Int = 0
    private var spacing : Int = 0
    private var isPaddingDisable : Boolean = false

    constructor(context : Context, spanCount : Int, spacing : Int, isPaddingDisable : Boolean)
    {
        mContext  = context
        this.spanCount = spanCount
        this.spacing = spacing
        this.isPaddingDisable = isPaddingDisable
    }
    override fun getItemOffsets(outRect : Rect, view : View, parent : RecyclerView, state : RecyclerView.State)
    {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view) // item position
        val holder = parent.getChildViewHolder(view) as RecyclerView.ViewHolder
        var column = 0
        if(position < 0)
        {
            return
        }
        column = position % spanCount

        if(isPaddingDisable)
        {
            setLocationGridItem(outRect, column, position)
        } else
        {
            setLocationGridItem(outRect, column)
        }
    }

    private fun setLocationGridItem(outRect : Rect, column : Int, position : Int)
    {
        outRect.left =
            spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
        outRect.right =
            (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
        if(position < spanCount)
        { // top edge
            outRect.top = 0
        } // outRect.bottom = spacing; // item bottom
    }

    /**
     * GridView 각각의 Item을 왼쪽 오른쪽 균형있게 표시하려고 사용
     * @param outRect 그리는 Rect 영역
     * @param column 1 : 왼쪽 , 0 : 오른쪽
     */
    private fun setLocationGridItem(outRect : Rect, column : Int)
    {
        val PADDING_24 = CommonUtils.getInstance(mContext).getPixel(24)
        val PADDING_26 = CommonUtils.getInstance(mContext).getPixel(26)
        if(column == 1) // 오른쪽
        {
            outRect.left = PADDING_24 / 2
            outRect.right = PADDING_26
        } else if(column == 0) // 왼쪽
        {
            outRect.left = PADDING_26
            outRect.right = PADDING_24 / 2
        }
    }
}