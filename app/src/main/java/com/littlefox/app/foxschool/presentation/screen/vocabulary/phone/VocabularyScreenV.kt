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
import com.littlefox.app.foxschool.enumerate.VocabularyType
import com.littlefox.app.foxschool.presentation.viewmodel.VocabularyViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.logmonitor.Log
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.VocabularyTopBarMenu
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.vocabulary.VocabularyEvent
import com.littlefox.app.foxschool.presentation.widget.BuildVocabularyBarLayout
import com.littlefox.app.foxschool.presentation.widget.BuildVocabularyListItem
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun VocabularyScreenV(
    viewModel : VocabularyViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val _contentsList by viewModel.vocabularyContentsList.observeAsState(
        initial = EventWrapper(ArrayList())
    )
    val _selectItemCount by viewModel.itemSelectedCount.observeAsState(
        initial = EventWrapper(0)
    )
    val _intervalSecond by viewModel.intervalSecond.observeAsState(initial = 0)
    val _isSequencePlaying by viewModel.isPlayingStatus.observeAsState(initial = false)

    val _vocabularyTitle by viewModel.vocabularyTitle.observeAsState(initial = "")
    val _selectMenuData by viewModel.vocabularySelectType.observeAsState(initial = VocabularySelectData())
    val _isShowContentsLoading by viewModel.isContentsLoading.observeAsState(initial = false)
    val _vocabularyType by viewModel.vocabularyType.observeAsState(initial = VocabularyType.VOCABULARY_CONTENTS)
    val _currentPlayingIndex by viewModel.currentPlayingIndex.observeAsState(initial = 0)

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
    var _dataList by remember {
        mutableStateOf(ArrayList<VocabularyDataResult>())
    }
    var _itemCount by remember {
        mutableStateOf(0)
    }

    _contentsList.getContentIfNotHandled()?.let {
        Log.i("------------- notify size : ${it.size}")
        LaunchedEffect(_contentsList) {
            _isShouldAnimate = if(it.size > 0)
            {
                true
            }
            else
            {
                false
            }
        }
        _dataList = ArrayList()
        _dataList = it
    }

    _selectItemCount.getContentIfNotHandled()?.let {
        _isSelectedStatus = if(it > 0)
        {
            true
        }
        else
        {
            false
        }
        _itemCount = it
    }


    LaunchedEffect(_currentPlayingIndex) {
        Log.i("currentPlayingIndex : $_currentPlayingIndex")
        _coroutineScope.launch {
            _listState.animateScrollToItem(_currentPlayingIndex)
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
                title = _vocabularyTitle,
                backgroundColor = colorResource(id = R.color.color_23cc8a)) {
                onEvent(BaseEvent.onBackPressed)
            }
            BuildSelectTypeLayout(data = _selectMenuData) { selectMenu ->
                Log.i("selectMenu : $selectMenu")
                if(_isSequencePlaying == false)
                {
                    onEvent(
                        VocabularyEvent.onClickTopBarMenu(selectMenu)
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
                userScrollEnabled = !_isSequencePlaying
            ) {
                itemsIndexed(_dataList, key = {_, item -> item.getID()}){index, item ->
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
                            visible = _dataList.size > 0 && _isShouldAnimate,
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
                                type = _selectMenuData,
                                backgroundColor = if(_isSequencePlaying)
                                {
                                    if(_currentPlayingIndex == index)
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
                                    if(_isSequencePlaying == false)
                                    {
                                        onEvent(
                                            VocabularyEvent.onPlayContents(index)
                                        )
                                    }
                                },
                                onSelectItem = {
                                    if(_isSequencePlaying == false)
                                    {
                                        onEvent(
                                            VocabularyEvent.onSelectItem(index)
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
                visible = _isShowContentsLoading, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.color_1aa3f8)
                )
            }
        }

        BuildVocabularyBarLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            isVocabularyShelfMode = when(_vocabularyType)
            {
                VocabularyType.VOCABULARY_SHELF -> true
                VocabularyType.VOCABULARY_CONTENTS -> false
            },
            selectedItemCount = _itemCount,
            currentIntervalSecond = _intervalSecond,
            isPlaying = _isSequencePlaying,
            ) { menu ->
            onEvent(
                VocabularyEvent.onClickBottomBarMenu(menu)
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

