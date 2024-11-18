package com.littlefox.app.foxschool.presentation.screen.search.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.littlefox.app.foxschool.presentation.viewmodel.SearchViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.search.SearchEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.SearchType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.widget.BuildPagingContentsListItem
import com.littlefox.app.foxschool.presentation.widget.SearchTextFieldLayout
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.logmonitor.Log

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onEvent: (BaseEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val _searchedItemList = viewModel.searchItemList.collectAsLazyPagingItems()
    val _isContentsLoading by viewModel.isContentsLoading.observeAsState(initial = false)
    var _searchType by remember { mutableStateOf(SearchType.ALL) }
    var _shouldAnimate by remember { mutableStateOf(false) }
    var _previousSearchText by remember { mutableStateOf("") }

    // searchedItemList의 itemCount가 변경될 때마다 애니메이션을 트리거
    LaunchedEffect(_searchedItemList.itemCount) {
        _shouldAnimate = true
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.color_edeef2))
            .addFocusCleaner(focusManager = focusManager)
    ) {
        Column {
            TopBarCloseLayout(
                title = stringResource(id = R.string.text_search),
                backgroundColor = colorResource(id = R.color.color_23cc8a)
            ) {
                onEvent(BaseEvent.onBackPressed)
            }

            BuildSelectTypeLayout(searchType = _searchType) {type ->

                _shouldAnimate = false
                _searchType = type
                onEvent(SearchEvent.onClickSearchType(type))
            }

            BuildSearchTextFieldLayout { searchText ->
                Log.i("Search Text: $searchText")


                _shouldAnimate = false
                focusManager.clearFocus()
                onEvent(SearchEvent.onClickSearchExecute(searchText))
            }

            if (_isContentsLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {


                AnimatedVisibility(
                    visible = _searchedItemList.itemCount > 0 && _shouldAnimate,
                    enter = slideInVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        initialOffsetY = { it }
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        targetOffsetY = { 0 }
                    )
                ){
                    LazyColumn(
                        modifier = Modifier
                            .padding(
                                start = getDp(pixel = 28),
                                end = getDp(pixel = 28)
                            )
                    ) {
                        items(_searchedItemList.itemCount) {index ->
                            val item = _searchedItemList[index]
                            item?.let {
                                Column {
                                    if (index == 0) {
                                        Spacer(modifier = Modifier.height(getDp(pixel = 20)))
                                    }

                                    BuildPagingContentsListItem(
                                        data = it,
                                        onOptionClick = {
                                            onEvent(
                                                SearchEvent.onClickOption(
                                                    ContentsBaseResult(item)
                                                )
                                            )
                                        },
                                        onThumbnailClick = {
                                            onEvent(
                                                SearchEvent.onClickThumbnail(
                                                    ContentsBaseResult(item)
                                                )
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(getDp(pixel = 20)))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 에러 상태 표시
        if (_searchedItemList.loadState.append is LoadState.Error) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error loading more items")
            }
        }
    }
}

@Composable
private fun BuildSelectTypeLayout(
    searchType : SearchType,
    onSelectItem: (SearchType) -> Unit
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
                    onSelectItem(SearchType.ALL)
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
                painter = if(searchType == SearchType.ALL) painterResource(id = R.drawable.check_on) else painterResource(id = R.drawable.check_off),
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
                    onSelectItem(SearchType.STORY)
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
                painter = if(searchType == SearchType.STORY) painterResource(id = R.drawable.check_on) else painterResource(id = R.drawable.check_off),
                contentScale = ContentScale.Fit,
                contentDescription = "스토리 선택 아이콘"
            )
            Text(
                modifier = Modifier
                    .width(
                        getDp(pixel = 150)
                    )
                    .offset(
                        x = getDp(pixel = 80)
                    ),
                text = stringResource(id = R.string.text_story),
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
                    onSelectItem(SearchType.SONG)
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
                painter = if(searchType == SearchType.SONG) painterResource(id = R.drawable.check_on) else painterResource(id = R.drawable.check_off),
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
                text = stringResource(id = R.string.text_song),
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

@Composable
private fun BuildSearchTextFieldLayout(
    onTextConfirmed: (String) -> Unit
)
{
    val searchText = remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 160)
            )
            .background(color = colorResource(id = R.color.color_ffffff))
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center)
        {
            SearchTextFieldLayout(
                text = searchText.value,
                width = 760,
                height = 120)
            {
                searchText.value = it
            }

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 120)
                    )
                    .height(
                        getDp(pixel = 120)
                    )
                    .clip(
                        shape = RoundedCornerShape(
                            topEnd = getDp(pixel = 20), bottomEnd = getDp(pixel = 20)
                        )
                    )
                    .background(color = colorResource(id = R.color.color_29c8e6))
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = {
                        onTextConfirmed(searchText.value)
                    }),
                contentAlignment = Alignment.Center
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.icon_search_3),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 58)
                        )
                        .height(
                            getDp(pixel = 60)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "검색 아이콘")
            }
        }
    }
}

private fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: () -> Unit = {}): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            doOnClear()
            focusManager.clearFocus()
        })
    }
}