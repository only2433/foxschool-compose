package com.littlefox.app.foxschool.presentation.common

import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.logmonitor.Log


@Composable
fun getDp(pixel : Int, isHeight : Boolean = false) : Dp
{
    val context = LocalContext.current
    var data: Dp = 0.dp
     with(LocalDensity.current){
        if(isHeight)
        {
            data = CommonUtils.getInstance(context).getHeightPixel(pixel).toDp()
        }
        else
        {
            data = CommonUtils.getInstance(context).getPixel(pixel).toDp()
        }
    }
    return data
}