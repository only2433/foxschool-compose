package com.littlefox.app.foxschool.view

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.TextView

class ProgressBarAnimation : Animation
{
    companion object
    {
        private const val WAVE_LEVEL_UNIT = 100
    }

    private lateinit var progressBar : ProgressBar
    private lateinit var progressText : TextView
    private var from : Float = 0f
    private var to : Float = 0f


    constructor(progressBar : ProgressBar, progressText : TextView, from : Float, to : Float) : super()
    {

        this.progressBar = progressBar;
        this.progressText = progressText;
        this.from = from;
        this.to = to;
    }

    override fun applyTransformation(interpolatedTime : Float, t : Transformation)
    {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
        progressText.setText((value.toInt()).toString() + "%")
    }


}