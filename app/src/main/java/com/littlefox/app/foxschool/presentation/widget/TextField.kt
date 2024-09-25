package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp

@Composable
fun IconTextFieldLayout(
    text: String,
    icon: Painter,
    hintText: String,
    width: Int,
    height: Int,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isBottomRounded: Boolean = true,
    onTextChange: (String) -> Unit)
{
    val isFocused = remember {
        mutableStateOf(false)
    }
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        visualTransformation = visualTransformation,
        modifier = Modifier
            .width(getDp(pixel = width))
            .height(getDp(pixel = height))
            .border(
                width = getDp(pixel = 2),
                color = colorResource(id = R.color.color_999999),
                shape = if(isBottomRounded)
                {
                    RoundedCornerShape(getDp(pixel = 20))
                }
                else
                {
                    RoundedCornerShape(
                        topStart = getDp(pixel = 20),
                        topEnd = getDp(pixel = 20)
                    )
                }
            )
            .background(color = colorResource(id = R.color.color_ffffff))
            .onFocusChanged {focusState ->
                isFocused.value = focusState.isFocused
            }
            .focusRequester(focusRequester)
            .padding(horizontal = getDp(pixel = 30)), // Adjust padding as needed
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_light))
        ),
        decorationBox = @Composable { innerTextField ->
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 10)
                        )
                )
                Icon(
                    painter = icon,
                    contentDescription = "Icon",
                    modifier = Modifier
                        .width(
                            getDp(pixel = 50)
                        )
                        .height(
                            getDp(pixel = 50)
                        ),
                    tint = if(isFocused.value)
                    {
                        colorResource(id = R.color.color_23cc8a)
                    }
                    else
                    {
                        colorResource(id = R.color.color_999999)
                    }
                )
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 20)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = width - 200)
                        )
                        .height(
                            getDp(pixel = height)
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if(text.isEmpty())
                    {
                        Text(
                            text = hintText,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(resId = R.font.roboto_light)),
                                color = Color.Gray
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                        )
                    }
                    innerTextField() // This is where the actual text field is rendered
                    // If you want to include a leading icon:

                }
            }
        }
    )
}