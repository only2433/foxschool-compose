package com.littlefox.app.foxschool.presentation.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import butterknife.OnClick
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp

import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor

@Composable
fun TopBarBackLayout(
    title: String,
    backgroundColor: Color,
    onBackEvent: () -> Unit
)
{
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 166)
            )
            .background(
                color = backgroundColor
            ),
    )
    {
        Box (
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.color_ffffff),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_bold
                        )
                    ),
                    textAlign = TextAlign.Center
                ),
            )
        }

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 166)
                )
                .height(
                    getDp(pixel = 166)
                )
                .align(Alignment.CenterStart) // Box의 오른쪽 끝에 정렬
                .padding(end = 10.dp)
                .clickable(
                    interactionSource = remember {MutableInteractionSource()},
                    indication = null, // 클릭 시 효과 제거
                    onClick = onBackEvent // 클릭 이벤트에 콜백 연결
                ),
        )
        {
            Image(
                painter = painterResource(id = R.drawable.top_pre),
                contentDescription = "Back Icon",
                modifier = Modifier
                    .width(
                        getDp(pixel = 48)
                    )
                    .height(
                        getDp(pixel = 48)
                    )
                    .align(Alignment.Center)

            )
        }
    }
}

@Composable
fun TopBarCloseLayout(
    modifier : Modifier = Modifier,
    height : Int = 166,
    title: String,
    backgroundColor : Color,
    onClickEvent: () -> Unit
)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = height)
            )
            .background(
                color = backgroundColor
            ),
    )
    {
        Box (
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.color_ffffff),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_bold
                        )
                    ),
                    textAlign = TextAlign.Center
                ),
            )
        }

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 166)
                )
                .height(
                    getDp(pixel = 166)
                )
                .align(Alignment.CenterEnd) // Box의 오른쪽 끝에 정렬
                .padding(end = 10.dp)
                .clickable(
                    interactionSource = remember {MutableInteractionSource()},
                    indication = null, // 클릭 시 효과 제거
                    onClick = onClickEvent // 클릭 이벤트에 콜백 연결
                ),
        )
        {
            Image(
                painter = painterResource(id = R.drawable.btn_close),
                contentDescription = "Close Icon",
                modifier = Modifier
                    .width(
                        getDp(pixel = 48)
                    )
                    .height(
                        getDp(pixel = 48)
                    )
                    .align(Alignment.Center)

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopbarMainLayout(
    title: String,
    scrollBehavior : TopAppBarScrollBehavior,
    onTabMenu: () -> Unit,
    onTabSearch: () -> Unit
)
{
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = colorResource(id = R.color.color_23cc8a),
        scrolledContainerColor = colorResource(id = R.color.color_23cc8a)
    )
    CenterAlignedTopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = {
            Box(
                modifier = Modifier
                    .height(
                        getDp(pixel = 166)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title, style = TextStyle(
                        color = colorResource(id = R.color.color_ffffff), fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_medium
                            )
                        ), fontSize = 16.sp
                    ), textAlign = TextAlign.Center
                )
            }
        },
        colors = colors,
        navigationIcon = {
            Box(
                modifier = Modifier
                    .height(
                        getDp(pixel = 166)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onTabMenu) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_menu),
                        contentDescription = "Menu Button",
                        tint = colorResource(id = R.color.color_ffffff)
                    )
                }
            }

        },
        actions = {
            Box(
                modifier = Modifier
                    .height(
                        getDp(pixel = 166)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onTabSearch) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_search),
                        contentDescription = "Search Button",
                        tint = colorResource(id = R.color.color_ffffff)

                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopbarSeriesContentsLayout(
    title : String,
    background: String,
    modifier : Modifier = Modifier,
    isShowSeriesInformation : Boolean,
    onTabBackButton: () -> Unit,
    onTabSeriesInformationButton: () -> Unit
)
{

    var backgroundColor: Color;
    try {
        backgroundColor = ComposeColor(AndroidColor.parseColor(background))
        // Use the color
    } catch (e: Exception) {
        // Handle invalid color, e.g., use a default color
        backgroundColor = Color.Transparent
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor
            )
    )
    {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (backButton, titleButton , infoButton) = createRefs()

            Image(
                modifier = Modifier
                    .constrainAs(backButton) {
                        start.linkTo(parent.start)

                    }
                    .width(
                        getDp(pixel = 144)
                    )
                    .height(
                        getDp(pixel = 138)
                    ),
                painter = painterResource(id = R.drawable.top_pre),
                contentScale = ContentScale.Inside,
                contentDescription = "Back Button"
            )

            Box(
                modifier = Modifier
                    .constrainAs(titleButton) {
                        start.linkTo(backButton.end, margin = 10.dp)
                    }
                    .width(
                        getDp(pixel = 736)
                    )
                    .height(
                        getDp(pixel = 144)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    )
                ))
            }

            if(isShowSeriesInformation)
            {
                Image(
                    modifier = Modifier
                        .constrainAs(infoButton) {
                            start.linkTo(titleButton.end, margin = 10.dp)
                        }
                        .width(
                            getDp(pixel = 144)
                        )
                        .height(
                            getDp(pixel = 138)
                        ),
                    painter = painterResource(id = R.drawable.info_icon),
                    contentScale = ContentScale.Inside,
                    contentDescription = "Back Button"
                )
            }
        }
    }
}
