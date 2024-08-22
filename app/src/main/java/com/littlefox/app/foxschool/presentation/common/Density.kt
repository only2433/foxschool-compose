package com.littlefox.app.foxschool.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.littlefox.app.foxschool.common.CommonUtils


@Composable
fun getDp(pixel : Int) : Dp
{
    val context = LocalContext.current
    return with(LocalDensity.current){
        CommonUtils.getInstance(context).getPixel(pixel).toDp()
    }
}