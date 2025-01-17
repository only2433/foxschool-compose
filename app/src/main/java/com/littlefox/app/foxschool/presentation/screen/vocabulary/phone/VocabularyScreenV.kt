package com.littlefox.app.foxschool.presentation.screen.vocabulary.phone

import VocabularySelectData
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column



import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.logmonitor.Log
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.VocabularyTopBarMenu
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.vocabulary.VocabularyAction
import com.littlefox.app.foxschool.presentation.mvi.vocabulary.viewmodel.VocabularyViewModel
import com.littlefox.app.foxschool.presentation.widget.BuildVocabularyBarLayout
import com.littlefox.app.foxschool.presentation.widget.BuildVocabularyListItem
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun VocabularyScreenV(
    viewModel : VocabularyViewModel,
    onAction: (VocabularyAction) -> Unit
)
{
    val state by viewModel.state.collectAsStateWithLifecycle()
    val _listState = remember {
        LazyListState()
    }
    val _coroutineScope = rememberCoroutineScope()
    var _isShouldAnimate by remember {
        mutableStateOf(false)
    }
    var _isSelectedStatus by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(state.contentsList) {
        _isShouldAnimate = if(state.contentsList.size > 0)
        {
            true
        }
        else
        {
            false
        }
    }

    LaunchedEffect(state.currentPlayingIndex) {
        Log.i("currentPlayingIndex : $state.currentPlayingIndex")
        _coroutineScope.launch {
            _listState.animateScrollToItem(state.currentPlayingIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_edeef2)
            )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = getDp(pixel = 176)
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBarCloseLayout(
                title = state.title,
                backgroundColor = colorResource(id = R.color.color_23cc8a))
            {
                viewModel.onBackPressed()
            }
            BuildSelectTypeLayout(data = state.studyTypeData) { selectMenu ->
                Log.i("selectMenu : $selectMenu")
                if(state.isPlayingStatus == false)
                {
                    onAction(
                        VocabularyAction.ClickTopBarMenu(
                            selectMenu
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = getDp(pixel = 20), end = getDp(pixel = 20)
                    ),
                state = _listState,
                userScrollEnabled = !state.isPlayingStatus
            ) {
                itemsIndexed(state.contentsList, key = {_, item -> item.getID()}){index, item ->
                    Column {
                        if(index == 0)
                        {
                            Spacer(
                                modifier = Modifier
                                    .height(
                                        getDp(pixel = 20)
                                    )
                            )
                        }

                        AnimatedVisibility(
                            visible = state.contentsList.size > 0 && _isShouldAnimate,
                            enter = fadeIn() + slideInVertically(
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing,
                                    delayMillis = index * 50
                                ),
                                initialOffsetY = {
                                    1000
                                }
                            ),
                            exit = slideOutVertically(
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                                targetOffsetY = {0}
                            )
                        )
                        {
                            BuildVocabularyListItem(
                                data = item,
                                type = state.studyTypeData,
                                backgroundColor = if(state.isPlayingStatus)
                                {
                                    if(state.currentPlayingIndex == index)
                                    {
                                        colorResource(id = R.color.color_fffca0)
                                    }
                                    else
                                    {
                                        colorResource(id = R.color.color_d9dede)
                                    }
                                }else
                                {
                                    if(item.isSelected())
                                    {
                                        colorResource(id = R.color.color_d9dede)
                                    }
                                    else
                                    {
                                        colorResource(id = R.color.color_ffffff)
                                    }
                                },
                                onPlayItem = {
                                    if(state.isPlayingStatus == false)
                                    {
                                        onAction(
                                            VocabularyAction.PlayContents(index)
                                        )
                                    }
                                },
                                onSelectItem = {
                                    if(state.isPlayingStatus == false)
                                    {
                                        onAction(
                                            VocabularyAction.SelectItem(index)
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .height(
                                    getDp(pixel = 20)
                                )
                        )
                    }
                }
            }

        }

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 100)
                )
                .height(
                    getDp(pixel = 100)
                )
                .align(Alignment.Center)
        ) {
            AnimatedVisibility(
                visible = state.isContentsLoading, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.color_1aa3f8)
                )
            }
        }

        BuildVocabularyBarLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            isVocabularyShelfMode = when(state.vocabularyType)
            {
                VocabularyType.VOCABULARY_SHELF -> true
                VocabularyType.VOCABULARY_CONTENTS -> false
            },
            selectedItemCount = state.selectCount,
            currentIntervalSecond = state.intervalSecond,
            isPlaying = state.isPlayingStatus,) { menu ->
                onAction(
                    VocabularyAction.ClickBottomBarMenu(menu)
                )
        }
    }
}

@Composable
private fun BuildSelectTypeLayout(
    data : VocabularySelectData,
    onSelectItem: (VocabularyTopBarMenu) -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 130)
            )
            .background(colorResource(id = R.color.color_ffffff)),
        horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 230)
                )
                .height(
                    getDp(pixel = 130)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(VocabularyTopBarMenu.ALL)
                }),
            contentAlignment = Alignment.CenterStart // 왼쪽 가운데 정렬
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
                painter = when(data.isCheckAll())
                {
                    true -> painterResource(id = R.drawable.check_on)
                    false -> painterResource(id = R.drawable.check_off)
                },
                contentScale = ContentScale.Fit,
                contentDescription = "전체 선택 아이콘"
            )
            Text(
                modifier = Modifier
                    .width(
                        getDp(pixel = 150)
                    )
                    .offset(
                        x = getDp(pixel = 80)
                    ),
                text = stringResource(id = R.string.text_all),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                    fontSize = 14.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.width(getDp(pixel = 20)))

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 230)
                )
                .height(
                    getDp(pixel = 130)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(VocabularyTopBarMenu.WORD)
                }),
            contentAlignment = Alignment.CenterStart // 왼쪽 가운데 정렬
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
                painter = when(data.isSelectedWord)
                {
                    true -> painterResource(id = R.drawable.check_on)
                    false -> painterResource(id = R.drawable.check_off)
                },
                contentScale = ContentScale.Fit,
                contentDescription = "단어 선택 아이콘"
            )
            Text(
                modifier = Modifier
                    .width(
                        getDp(pixel = 150)
                    )
                    .offset(
                        x = getDp(pixel = 80)
                    ),
                text = stringResource(id = R.string.text_words),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                    fontSize = 14.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.width(getDp(pixel = 20)))

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 230)
                )
                .height(
                    getDp(pixel = 130)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(VocabularyTopBarMenu.MEANING)
                }),
            contentAlignment = Alignment.CenterStart // 왼쪽 가운데 정렬
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
                painter = when(data.isSelectedMeaning)
                {
                    true -> painterResource(id = R.drawable.check_on)
                    false -> painterResource(id = R.drawable.check_off)
                },
                contentScale = ContentScale.Fit,
                contentDescription = "송 선택 아이콘"
            )
            Text(
                modifier = Modifier
                    .width(
                        getDp(pixel = 150)
                    )
                    .offset(
                        x = getDp(pixel = 80)
                    ),
                text = stringResource(id = R.string.text_meaning),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                    fontSize = 14.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.width(getDp(pixel = 20)))

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 230)
                )
                .height(
                    getDp(pixel = 130)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(VocabularyTopBarMenu.EXAMPLE)
                }),
            contentAlignment = Alignment.CenterStart // 왼쪽 가운데 정렬
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
                painter = when(data.isSelectedExample)
                {
                    true -> painterResource(id = R.drawable.check_on)
                    false -> painterResource(id = R.drawable.check_off)
                },
                contentScale = ContentScale.Fit,
                contentDescription = "예문 선택 아이콘"
            )
            Text(
                modifier = Modifier
                    .width(
                        getDp(pixel = 150)
                    )
                    .offset(
                        x = getDp(pixel = 80)
                    ),
                text = stringResource(id = R.string.text_example),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                    fontSize = 14.sp,
                ),
            )
        }

    }

}

