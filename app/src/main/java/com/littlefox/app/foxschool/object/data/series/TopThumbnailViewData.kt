package com.littlefox.app.foxschool.`object`.data.series

import com.littlefox.app.foxschool.enumerate.TransitionType

data class TopThumbnailViewData(
    val thumbnail : String = "",
    val titleColor : String = "",
    val transitionType : TransitionType = TransitionType.PAIR_IMAGE
)
