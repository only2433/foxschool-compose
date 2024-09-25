package com.littlefox.app.foxschool.presentation.common

import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.littlefox.app.foxschool.common.CommonUtils


@Composable
fun getDp(pixel : Int, isHeight : Boolean = false) : Dp
{
    val context = LocalContext.current
    return with(LocalDensity.current){

        if(isHeight)
        {
            CommonUtils.getInstance(context).getHeightPixel(pixel).toDp()
        }
        else
        {
            CommonUtils.getInstance(context).getPixel(pixel).toDp()
        }

    }
}