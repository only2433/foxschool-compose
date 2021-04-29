package com.littlefox.app.foxschool.adapter.listener

import com.littlefox.app.foxschool.adapter.listener.base.OnItemViewClickListener

interface PlayerEventListener : OnItemViewClickListener
{
    fun onClickOption(index : Int)
    fun onSelectSpeed(index : Int)
}