package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.presentation.common.getDp
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

@Composable
fun IntroProgressBar(
    percent: Float,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    progressColor: Color)
{
    var currentPercent by remember {
        mutableFloatStateOf(0f)
    }
    val animationPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(
            durationMillis = Common.DURATION_SHORT_LONG.toInt(),
            easing = FastOutSlowInEasing
        ), label = ""
    )

    LaunchedEffect(percent) {
        currentPercent = animationPercent
    }


    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(
                width = getDp(pixel = 2),
                color = progressColor,
                shape = RoundedCornerShape(
                    getDp(pixel = 25)
                )
            )
            .clip(RoundedCornerShape(getDp(pixel = 25)))
    )
    {
        Box(
            modifier = Modifier
                .width(width * animationPercent / 100)
                .height(height)
                .background(
                    color = progressColor,
                    shape = RoundedCornerShape(
                        getDp(pixel = 25)
                    )
                )
        )

        Text(
            text = "${animationPercent.toInt()}%",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = getDp(pixel = 10)),
            style = TextStyle(
                color = colorResource(id = R.color.color_ffffff),
                fontSize = 10.sp,
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_regular
                    )
                )
            )

        )
    }


}