package com.littlefox.app.foxschool.view.decoration;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.littlefox.app.foxschool.common.CommonUtils;
import com.littlefox.app.foxschool.common.Feature;


public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration
{
    private int spanCount;
    private int spacing;
    private Context mContext;
    public GridSpacingItemDecoration(Context context, int spanCount, int spacing)
    {
        mContext = context;
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view); // item position
        RecyclerView.ViewHolder holder =  (RecyclerView.ViewHolder) parent.getChildViewHolder(view);

        int column = 0;

        if(position < 0)
        {
            return;
        }

        column = position % spanCount;

        if(Feature.IS_TABLET)
        {
            setLocationGridItem(outRect, column, position);
        }
        else
        {
            setLocationGridItem(outRect,column);
        }

    }

    private void setLocationGridItem(Rect outRect, int column, int position)
    {
        outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
        outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

        if (position < spanCount) { // top edge
            outRect.top = 0;
        }
        // outRect.bottom = spacing; // item bottom
    }

    /**
     * GridView 각각의 Item을 왼쪽 오른쪽 균형있게 표시하려고 사용
     * @param outRect 그리는 Rect 영역
     * @param column 1 : 왼쪽 , 0 : 오른쪽
     */
    private void setLocationGridItem(Rect outRect, int column)
    {

        final int PADDING_24 = CommonUtils.getInstance(mContext).getPixel(24);
        final int PADDING_26 = CommonUtils.getInstance(mContext).getPixel(26);

        if (column == 1) // 오른쪽
        {
            outRect.left = PADDING_24 / 2;
            outRect.right = PADDING_26;
        }
        else if (column == 0) // 왼쪽
        {
            outRect.left = PADDING_26;
            outRect.right = PADDING_24 / 2;
        }
    }
}
