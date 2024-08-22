package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp


@Composable
fun BlueOutlinedButton(text: String, modifier : Modifier = Modifier, onClick: () -> Unit )
{
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(
            getDp(pixel = 60)
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.color_ffffff),
            contentColor = colorResource(id = R.color.color_1aa3f8)
        )
    )
    {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                )
            )
        )
    }
}

@Composable
fun BlueRoundButton(text: String, modifier : Modifier = Modifier, onClick : () -> Unit)
{
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(
            getDp(pixel = 60)
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.color_3370de),
            contentColor = colorResource(id = R.color.color_ffffff)
        )
    )
    {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                )
            )
        )
    }
}