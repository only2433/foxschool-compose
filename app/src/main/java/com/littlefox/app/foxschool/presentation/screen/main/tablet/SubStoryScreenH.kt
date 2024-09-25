package com.littlefox.app.foxschool.presentation.screen.main.tablet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.screen.main.phone.SubStoryScreenV

@Composable
fun SubStoryScreenV()
{
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = "내 동화",
            style = TextStyle(
                color = colorResource(id = R.color.color_000000),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                )
            )
        )
    }
}