package com.littlefox.app.foxschool.common

import android.content.Context
import android.content.res.Configuration
import android.view.OrientationEventListener
import com.littlefox.app.foxschool.common.listener.OrientationChangeListener

class OrientationManager  : OrientationEventListener
{
    private var previousAngle = 0
    var orientation = 0
    private var orientationChangeListener : OrientationChangeListener? = null
    private val mContext : Context

    constructor(context : Context) : super(context)
    {
        mContext = context
    }

    override fun onOrientationChanged(orientation : Int)
    {
        if(orientation == -1) return
        if(this.orientation == 0)
        {
            this.orientation = mContext.resources.configuration.orientation
            orientationChangeListener?.onOrientationChanged(this.orientation)
        }
        if(this.orientation == Configuration.ORIENTATION_LANDSCAPE && (previousAngle > 10 && orientation <= 10 || previousAngle < 350 && previousAngle > 270 && orientation >= 350))
        {
            orientationChangeListener?.onOrientationChanged(Configuration.ORIENTATION_PORTRAIT)
            this.orientation = Configuration.ORIENTATION_PORTRAIT
        }
        if(this.orientation == Configuration.ORIENTATION_PORTRAIT && (previousAngle < 90 && orientation >= 90 && orientation < 270 || previousAngle > 280 && orientation <= 280 && orientation > 180))
        {
            orientationChangeListener?.onOrientationChanged(Configuration.ORIENTATION_LANDSCAPE)
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        }
        previousAngle = orientation
    }

    fun setOrientationChangedListener(l : OrientationChangeListener?)
    {
        orientationChangeListener = l
    }



    companion object
    {
        private val TAG = OrientationManager::class.java.name
        private var instance : OrientationManager? = null
        fun getInstance(context : Context) : OrientationManager
        {
            if(instance == null)
            {
                instance = OrientationManager(context)
            }
            return instance!!
        }
    }
}