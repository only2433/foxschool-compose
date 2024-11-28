package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp

@Composable
fun BuildQuizTitleView(
    modifier : Modifier = Modifier,
    index: Int,
    title: String,
    enableSound: Boolean = true,
    onPlaySound: () -> Unit
)
{
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConstraintLayout(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = getDp(pixel = 80)
                )
        )
        {
            val (indexText, titleText)  = createRefs()
            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 100)
                    )
                    .height(
                        getDp(pixel = 138)
                    )
                    .constrainAs(indexText) {
                        start.linkTo(
                            parent.start
                        )
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "${index}. ",
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontSize = 15.sp,
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_bold
                            )
                        ),
                        textAlign = TextAlign.End
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 1380)
                    )
                    .height(
                        getDp(pixel = 138)
                    )
                    .constrainAs(titleText) {
                        start.linkTo(
                            indexText.end
                        )
                    },
                contentAlignment = Alignment.CenterStart
            )
            {
                Text(
                    text = title ?: "",
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontSize = 15.sp,
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        textAlign = TextAlign.Left
                    )
                )
            }
        }

        if(enableSound)
        {
            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 160)
                    )
                    .height(
                        getDp(pixel = 60)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onPlaySound
                    )
            )
            {
                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 60)
                        )
                        .height(
                            getDp(pixel = 60)
                        ),
                    painter = painterResource(id = R.drawable.icon_speaker),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Speaker Image"
                )
            }
        }
    }
}
