package com.littlefox.app.foxschool.presentation.widget

import androidx.collection.emptyLongSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.enumerate.VocabularyBottomBarMenu
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.logmonitor.Log

@Composable
fun BuildVocabularyBarLayout(
    modifier : Modifier = Modifier,
    currentIntervalSecond: Int = 2,
    isVocabularyShelfMode: Boolean = false,
    isPlaying: Boolean = false,
    selectedItemCount: Int = 0,
    onClickMenu: (VocabularyBottomBarMenu) -> Unit,
)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 176)
                )
                .background(color = colorResource(id = R.color.color_29c8e6))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 176)
                )

        ) {
            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 216)
                    )
                    .height(
                        getDp(pixel = 176)
                    )
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = {
                        if(isPlaying == false) onClickMenu(VocabularyBottomBarMenu.INTERVAL)
                    })
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 90)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.bottom_interval),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Click Interval",
                        alpha = if(isPlaying) 0.2f else 1f
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 86)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        modifier = Modifier
                            .alpha(
                                alpha = if(isPlaying) 0.2f else 1f
                            ),
                        text = when(currentIntervalSecond)
                        {
                            0 -> stringResource(id = R.string.text_not_have_interval)
                            else -> "${currentIntervalSecond}초 간격"
                        },
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            ),
                        ),

                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 216)
                    )
                    .height(
                        getDp(pixel = 176)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            if(isPlaying == false)
                            {
                                if(selectedItemCount > 0)
                                {
                                    onClickMenu(VocabularyBottomBarMenu.SELECT_CLEAR)
                                }
                                else
                                {
                                    onClickMenu(VocabularyBottomBarMenu.SELECT_ALL)
                                }
                            }
                        }
                    )
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 90)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(
                            id = when(selectedItemCount > 0){
                                true -> R.drawable.bottom_close
                                false -> R.drawable.bottom_all
                            }),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Click Select",
                        alpha = if(isPlaying) 0.2f else 1f
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 86)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        modifier = Modifier
                            .alpha(
                                alpha = if(isPlaying) 0.2f else 1f
                            ),
                        text = stringResource(
                            id = when(selectedItemCount > 0){
                                true -> R.string.text_select_init
                                false -> R.string.text_select_all
                            }),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
            }


            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 216)
                    )
                    .height(
                        getDp(pixel = 176)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            if(isPlaying)
                            {
                                onClickMenu(VocabularyBottomBarMenu.SELECT_PLAY_STOP)
                            }
                            else
                            {
                                onClickMenu(VocabularyBottomBarMenu.SELECT_PLAY_START)
                            }

                        }
                    )
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 90)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = when(isPlaying)
                        {
                            true -> painterResource(id = R.drawable.bottom_stop)
                            false -> painterResource(id = R.drawable.bottom_play)
                        },
                        contentScale = ContentScale.Fit,
                        contentDescription = "Click Play",
                    )


                    if(selectedItemCount > 0 && isPlaying == false)
                    {
                        Box(
                            modifier = Modifier
                                .width(
                                    getDp(pixel = getSelectedItemWidth(selectedItemCount))
                                )
                                .height(
                                    getDp(pixel = 40)
                                )
                                .offset(
                                    x = getDp(pixel = getSelectedItemOffset(selectedItemCount))
                                )
                                .clip(
                                    shape = RoundedCornerShape(getDp(pixel = 20))
                                )
                                .background(
                                    color = colorResource(id = R.color.color_ffffff)
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                text = "$selectedItemCount",
                                style = TextStyle(
                                    color = colorResource(id = R.color.color_29c8e6),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily(
                                        Font(
                                            resId = R.font.roboto_regular
                                        )
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 86)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = when(isPlaying)
                        {
                            true -> stringResource(id = R.string.text_stop_play)
                            false -> stringResource(id = R.string.text_select_play)
                        },
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 216)
                    )
                    .height(
                        getDp(pixel = 176)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            onClickMenu(VocabularyBottomBarMenu.FLASHCARD)
                        }
                    )
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 90)
                        )
                        .alpha(
                            alpha = if(isPlaying) 0.2f else 1f
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.bottom_flashcard),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Click Flashcard"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 86)
                        )
                        .alpha(
                            alpha = if(isPlaying) 0.2f else 1f
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = stringResource(id = R.string.text_flashcards),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(
                        getDp(pixel = 216)
                    )
                    .height(
                        getDp(pixel = 176)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            if(isVocabularyShelfMode)
                            {
                                onClickMenu(VocabularyBottomBarMenu.MY_VOCABULARY_CONTENTS_DELETE)
                            }
                            else
                            {
                                onClickMenu(VocabularyBottomBarMenu.MY_VOCABULARY_CONTENTS_ADD)
                            }
                        }
                    )
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 90)
                        )
                        .alpha(
                            alpha = if(isPlaying) 0.2f else 1f
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(
                            id = when(isVocabularyShelfMode)
                            {
                                true -> R.drawable.bottom_delete
                                false -> R.drawable.bottom_voca
                            }),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Click Vocabulary ADD/DELETE"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 86)
                        )
                        .alpha(
                            alpha = if(isPlaying) 0.2f else 1f
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = stringResource(
                            id = when(isVocabularyShelfMode)
                            {
                                true -> R.string.text_delete
                                false -> R.string.text_add_vocabulary
                            }),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BuildBottomSelectBarLayout(
    modifier : Modifier = Modifier,
    isVisible: Boolean,
    isBookshelfMode: Boolean = false,
    selectedItemCount: Int = 0,
    onClickMenu: (ContentsListBottomBarMenu) -> Unit
)
{
    Log.i("selectedItemCount : $selectedItemCount")
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = {
                200
            },
            animationSpec = tween(
                durationMillis = Common.DURATION_NORMAL.toInt(),
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = {
                200
            },
            animationSpec = tween(
                durationMillis = Common.DURATION_NORMAL.toInt(),
                easing = FastOutSlowInEasing
            )
        )
    )
    {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 176)
                    )
                    .background(color = colorResource(id = R.color.color_29c8e6))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 176)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 270)
                        )
                        .height(
                            getDp(pixel = 176)
                        )
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            onClickMenu(ContentsListBottomBarMenu.SELECT_ALL)
                        }
                    )
                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 90)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.bottom_all),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Click All"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 86)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(
                            text = stringResource(id = R.string.text_select_all),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_ffffff),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 270)
                        )
                        .height(
                            getDp(pixel = 176)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                onClickMenu(ContentsListBottomBarMenu.SELECT_PLAY)
                            }
                        )
                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 90)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.bottom_play),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Click Play"
                        )

                        
                        if(selectedItemCount > 0)
                        {
                            Box(
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = getSelectedItemWidth(selectedItemCount))
                                    )
                                    .height(
                                        getDp(pixel = 40)
                                    )
                                    .offset(
                                        x = getDp(pixel = getSelectedItemOffset(selectedItemCount))
                                    )
                                    .clip(
                                        shape = RoundedCornerShape(getDp(pixel = 20))
                                    )
                                    .background(
                                        color = colorResource(id = R.color.color_ffffff)
                                    ),
                                contentAlignment = Alignment.Center
                            )
                            {
                                Text(
                                    text = "$selectedItemCount",
                                    style = TextStyle(
                                        color = colorResource(id = R.color.color_29c8e6),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily(
                                            Font(
                                                resId = R.font.roboto_regular
                                            )
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 86)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(
                            text = stringResource(id = R.string.text_select_play),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_ffffff),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 270)
                        )
                        .height(
                            getDp(pixel = 176)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                if(isBookshelfMode)
                                {
                                    onClickMenu(ContentsListBottomBarMenu.BOOKSHELF_DELETE)
                                }
                                else
                                {
                                    onClickMenu(ContentsListBottomBarMenu.BOOKSHELF_ADD)
                                }

                            }
                        )
                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 90)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(
                                id = when(isBookshelfMode){
                                    true -> R.drawable.bottom_delete
                                    false -> R.drawable.bottom_bookshelf
                                }),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Click Bookshelf"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 86)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(
                            text = stringResource(
                                id = when(isBookshelfMode){
                                    true -> R.string.text_delete
                                    false -> R.string.text_contain_bookshelf
                                }),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_ffffff),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 270)
                        )
                        .height(
                            getDp(pixel = 176)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                onClickMenu(ContentsListBottomBarMenu.CANCEL)
                            }
                        )
                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 90)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.bottom_close),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Click Cancel"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 86)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(
                            text = stringResource(id = R.string.text_cancel),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_ffffff),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun getSelectedItemWidth(count: Int) : Int
{
    if(count < 10)
    {
        return 40
    }
    else if(count >= 10 && count < 99)
    {
        return 50
    }
    else
    {
        return 60
    }
}

private fun getSelectedItemOffset(count: Int) : Int
{
    if(count < 10)
    {
        return 40
    }
    else if(count >= 10 && count < 99)
    {
        return 45
    }
    else
    {
        return 50
    }
}