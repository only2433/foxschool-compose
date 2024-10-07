package com.littlefox.app.foxschool.presentation.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Text

import androidx.compose.material3.rememberDrawerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.DrawerMenu
import com.littlefox.app.foxschool.presentation.common.getDp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DrawerMenuPhone(
    userName: String,
    userSchool: String,
    modifier : Modifier = Modifier,
    onClickItem: (DrawerMenu) -> Unit
)
{
    val seperateString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = 16.sp
            ))
            {
               append("$userName ")
            }
        withStyle(
            style = SpanStyle(
                fontSize = 13.sp,
            ))
        {
            append(userSchool)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    )
    {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 316)
                    )
                    .background(
                        color = colorResource(id = R.color.color_23cc8a)
                    )
            )
            {
                Column {
                    Spacer(modifier = Modifier
                        .height(
                            getDp(pixel = 40)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 807)
                            )
                            .height(
                                getDp(pixel = 90)
                            )
                            .padding(
                                horizontal = getDp(pixel = 80)
                            )
                    )
                    {
                        Text(
                            text = seperateString,
                            color = colorResource(id = R.color.color_ffffff)
                        )
                    }

                    Spacer(modifier = Modifier
                        .height(
                            getDp(pixel = 20)
                        )
                    )

                    GreenOutlineButton(
                        text = stringResource(id = R.string.text_my_info),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 267)
                            )
                            .height(
                                getDp(pixel = 107)
                            )
                            .offset(
                                x = getDp(pixel = 80)
                            )
                        ) {
                        onClickItem(DrawerMenu.MY_INFORMATION)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 280)
                    )
                    .background(
                        color = colorResource(id = R.color.color_edeef2)
                    ),
                contentAlignment = Alignment.Center
            )
            {
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 850)
                        )
                        .height(
                            getDp(pixel = 200)
                        )
                        .background(color = colorResource(id = R.color.color_ffffff))
                        .border(
                            width = getDp(pixel = 2),
                            color = colorResource(id = R.color.color_999999),
                            shape = RoundedCornerShape(
                                getDp(pixel = 10)
                            )
                        )
                        .clip(
                            shape = RoundedCornerShape(getDp(pixel = 10))
                        )
                )
                {
                    Row(
                    ){
                        Column(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = 282)
                                )
                                .height(
                                    getDp(pixel = 198)
                                )
                                .clickable(interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null, onClick = {
                                    onClickItem(DrawerMenu.STUDY_RECORD)
                                }),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.main_option_icon_1),
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 85)
                                    )
                                    .height(
                                        getDp(pixel = 76)
                                    ),
                                contentScale = ContentScale.Fit,
                                contentDescription = "학습 기록 아이콘"
                            )

                            Spacer(
                                modifier = Modifier.height(getDp(pixel = 10)))

                            Text(
                                text = stringResource(id = R.string.text_learning_log),
                                style = TextStyle(
                                    color = colorResource(id = R.color.color_444444),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily(
                                        Font(
                                            resId = R.font.roboto_regular
                                        )
                                    )
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = 2)
                                )
                                .fillMaxHeight()
                                .background(
                                    color = colorResource(id = R.color.color_999999)
                                ),
                        )

                        Column(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = 282)
                                )
                                .height(
                                    getDp(pixel = 198)
                                )
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    onClick = {
                                        onClickItem(DrawerMenu.RECORD_HISTORY)
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.main_option_icon_2),
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 85)
                                    )
                                    .height(
                                        getDp(pixel = 76)
                                    ),
                                contentScale = ContentScale.Fit,
                                contentDescription = "녹음 기록 아이콘"
                            )

                            Spacer(
                                modifier = Modifier.height(getDp(pixel = 10)))

                            Text(
                                text = stringResource(id = R.string.text_record_history),
                                style = TextStyle(
                                    color = colorResource(id = R.color.color_444444),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily(
                                        Font(
                                            resId = R.font.roboto_regular
                                        )
                                    )
                                )
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = 2)
                                )
                                .fillMaxHeight()
                                .background(
                                    color = colorResource(id = R.color.color_999999)
                                ),
                        )

                        Column(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = 282)
                                )
                                .height(
                                    getDp(pixel = 198)
                                )
                                .clickable(
                                    interactionSource = remember {
                                        MutableInteractionSource()
                                    },
                                    indication = null,
                                    onClick = {
                                        onClickItem(DrawerMenu.HOMEWORK_MANAGEMENT)
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.main_option_icon_3),
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 85)
                                    )
                                    .height(
                                        getDp(pixel = 76)
                                    ),
                                contentScale = ContentScale.Fit,
                                contentDescription = "숙제 관리 아이콘"
                            )
                            
                            Spacer(
                                modifier = Modifier.height(getDp(pixel = 10)))

                            Text(
                                text = stringResource(id = R.string.text_homework_manage),
                                style = TextStyle(
                                    color = colorResource(id = R.color.color_444444),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily(
                                        Font(
                                            resId = R.font.roboto_regular
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            }

            BuildMenuItemLayout() { menu ->
                onClickItem(menu)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 150)
                )
                .align(
                    alignment = Alignment.BottomCenter
                )
                .background(
                    color = colorResource(id = R.color.color_edeef2)
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = {
                        onClickItem(DrawerMenu.LOGOUT)
                    }
                ),
            contentAlignment = Alignment.Center
        )
        {
            Text(
                text = stringResource(id = R.string.text_logout),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                    fontSize = 16.sp
                ))

        }
    }
}

@Composable
private fun BuildMenuItemLayout(
    modifier : Modifier = Modifier,
    onClickItem: (DrawerMenu) -> Unit
)
{
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 45)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 191)
                ),
            horizontalArrangement = Arrangement.Center
        )
        {
            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            onClickItem(DrawerMenu.NEWS)
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_5),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "팍스스쿨 소식 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_foxschool_news),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            onClickItem(DrawerMenu.FAQ)
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_6),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "자주 묻는 질문 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_faqs),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            onClickItem(DrawerMenu.INQUIRY_ONE_TO_ONE)
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_7),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "1대1 문의하기 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_1_1_ask),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }
        }
        
        Spacer(
            modifier = Modifier
                .height(
                    getDp(pixel = 50)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 191)
                ),
            horizontalArrangement = Arrangement.Center
        )
        {
            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_8),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                onClickItem(DrawerMenu.APP_GUIDE)
                            }
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "앱 이용안내 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_about_app),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_9),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                onClickItem(DrawerMenu.TEACHER_MANUAL)
                            }
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "교사 메뉴얼 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_faqs),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 283)
                    )
                    .height(
                        getDp(pixel = 191)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            onClickItem(DrawerMenu.HOME_NEWSPAPER)
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_option_icon_10),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 76)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "가정 통신문 아이콘"
                )

                Spacer(modifier = Modifier.height(getDp(pixel = 10)))

                Text(
                    text = stringResource(id = R.string.text_home_newspaper),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}