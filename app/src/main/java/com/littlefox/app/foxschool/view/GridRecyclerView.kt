package com.littlefox.app.foxschool.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.GridLayoutAnimationController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/*
* Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ /**
 * An extension of RecyclerView, focused more on resembling a GridView.
 * Unlike [RecyclerView], this view can handle
 * `<gridLayoutAnimation>` as long as you provide it a
 * [GridLayoutManager] in
 * `setLayoutManager(LayoutManager layout)`.
 *
 *
 * Created by Freddie (Musenkishi) Lust-Hed.
 */
class GridRecyclerView : RecyclerView
{
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {}

    override fun setLayoutManager(layout: LayoutManager?)
    {
        if (layout is GridLayoutManager) {
            super.setLayoutManager(layout)
        } else {
            throw ClassCastException("You should only use a GridLayoutManager with GridRecyclerView.")
        }
    }

    override fun attachLayoutAnimationParameters(child: View, params: ViewGroup.LayoutParams, index: Int, count: Int)
    {
        if (adapter != null && layoutManager is GridLayoutManager)
        {
            var animationParams = params.layoutAnimationParameters as GridLayoutAnimationController.AnimationParameters
            if (animationParams == null) {
                animationParams = GridLayoutAnimationController.AnimationParameters()
                params.layoutAnimationParameters = animationParams
            }
            val columns = (layoutManager as GridLayoutManager?)!!.spanCount
            animationParams.count = count
            animationParams.index = index
            animationParams.columnsCount = columns
            animationParams.rowsCount = count / columns
            val invertedIndex = count - 1 - index
            animationParams.column = columns - 1 - invertedIndex % columns
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns
        }
        else
        {
            super.attachLayoutAnimationParameters(child, params, index, count)
        }
    }
}