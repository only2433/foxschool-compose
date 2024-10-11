package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.SeriesType
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.presentation.common.getDp

@Composable
fun LightBlueOutlinedButton(text: String, modifier : Modifier = Modifier, onClick: () -> Unit )
{
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(
            getDp(pixel = 60)
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.color_ffffff),
            contentColor = colorResource(id = R.color.color_26d0df)
        ),
        border = BorderStroke(
            color = colorResource(id = R.color.color_26d0df),
            width = getDp(pixel = 4)
        ),
        elevation = null
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
        ),
        border = BorderStroke(
            color = colorResource(id = R.color.color_1aa3f8),
            width = getDp(pixel = 4)
        ),
        elevation = null
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
fun GreenOutlineButton(
    text: String,
    modifier : Modifier = Modifier,
    fontSize : TextUnit = 14.sp,
    onClick: () -> Unit )
{
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(
            getDp(pixel = 60)
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.color_ffffff),
            contentColor = colorResource(id = R.color.color_23cc8a)
        ),
        border = BorderStroke(
            color = colorResource(id = R.color.color_23cc8a),
            width = getDp(pixel = 4)
        ),
        elevation = null

    )
    {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                ),
                fontSize = fontSize
            )
        )
    }
}

@Composable
fun LightBlueRoundButton(text: String, modifier : Modifier = Modifier, onClick : () -> Unit)
{
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(
            getDp(pixel = 60)
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.color_26d0df),
            contentColor = colorResource(id = R.color.color_ffffff)
        ),
        elevation = null
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
        ),
        elevation = null
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
fun SwitchTextButton(
    firstText: String,
    secondText: String,
    onClick: (SwitchButtonType) -> Unit,
)
{
    var selectButtonType by remember {
        mutableStateOf(SwitchButtonType.FIRST_ITEM)
    }

    val animationOffset by animateDpAsState(
        targetValue = if (selectButtonType == SwitchButtonType.FIRST_ITEM) getDp(pixel = 0) else getDp(pixel = 330),
        animationSpec = tween(
            durationMillis = Common.DURATION_SHORT_LONG.toInt(),
            easing = FastOutSlowInEasing
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(getDp(pixel = 178)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(getDp(pixel = 660))
                .height(getDp(pixel = 106))
                .clip(RoundedCornerShape(getDp(pixel = 60)))
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = getDp(pixel = 2),
                        color = colorResource(id = R.color.color_a0a0a0),
                        shape = RoundedCornerShape(getDp(pixel = 60))
                    )
                    .background(
                        color = colorResource(id = R.color.color_ffffff)
                    )
                    .clip(RoundedCornerShape(getDp(pixel = 60)))
            )



            // Text and click areas
            Row(modifier = Modifier.fillMaxSize()) {
                listOf(SwitchButtonType.FIRST_ITEM to firstText, SwitchButtonType.SECOND_ITEM to secondText).forEach {(type, buttonText) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(interactionSource = remember {MutableInteractionSource()},
                                indication = null,
                                onClick = {
                                    selectButtonType = type
                                    onClick(selectButtonType)
                                }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonText,
                            style = TextStyle(
                                color = if (selectButtonType == type)
                                    colorResource(id = R.color.color_26d0df)
                                else
                                    colorResource(id = R.color.color_a0a0a0),
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
                            )
                        )
                    }
                }
            }

            // Moving selection box
            Box(
                modifier = Modifier
                    .width(getDp(pixel = 330))
                    .height(getDp(pixel = 106))
                    .offset(x = animationOffset)
                    .border(
                        width = getDp(pixel = 4),
                        color = colorResource(id = R.color.color_26d0df),
                        shape = RoundedCornerShape(getDp(pixel = 60))
                    )
                    .clip(RoundedCornerShape(getDp(pixel = 60)))
            )
        }
    }
}