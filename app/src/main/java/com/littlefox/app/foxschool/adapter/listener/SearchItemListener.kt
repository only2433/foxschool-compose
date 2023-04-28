package com.littlefox.app.foxschool.adapter.listener

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult

interface SearchItemListener
{
    fun onItemClickThumbnail(item : ContentsBaseResult)
    fun onItemClickOption(item : ContentsBaseResult)
}