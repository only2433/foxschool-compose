package com.littlefox.app.foxschool.adapter.listener

import android.view.View
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult

interface SeriesCardItemListener
{
    fun onClickItem(seriesInformationResult : SeriesInformationResult, selectView : View)
}