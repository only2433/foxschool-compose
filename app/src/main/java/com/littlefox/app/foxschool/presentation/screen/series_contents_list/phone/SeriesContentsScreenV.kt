package com.littlefox.app.foxschool.presentation.screen.series_contents_list.phone


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer


import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.SeriesContentsListViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
import com.littlefox.app.foxschool.presentation.widget.BuildBottomSelectBarLayout
import com.littlefox.app.foxschool.presentation.widget.BuildContentsListItem
import com.littlefox.app.foxschool.presentation.widget.TopbarSeriesContentsLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import kotlinx.coroutines.delay

@Composable
fun SeriesContentsScreenV(
    viewModel : SeriesContentsListViewModel,
    onEvent: (SeriesContentsListEvent) -> Unit,
)
{
    val _contentsList by viewModel.contentsList.observeAsState(
        initial = EventWrapper(ArrayList<ContentsBaseResult>())
    )
    val _selectedItemCount by viewModel.itemSelectedCount.observeAsState(
        initial = EventWrapper(0)
    )

    val _showToolbarInformationView by viewModel.showToolbarInformationView.observeAsState(initial = false)
    val _isShowContentsLoading by viewModel.isContentsLoading.observeAsState(initial = true)
    val _seriesTitle by viewModel.seriesTitle.observeAsState(initial = "")
    val _prepareData by viewModel.backgroundViewData.observeAsState(initial = TopThumbnailViewData())

    var _dataList by remember {
        mutableStateOf(ArrayList<ContentsBaseResult>())
    }
    var _itemCount by remember {
        mutableStateOf(0)
    }
    var _isFabToolbarVisible by remember { //
        mutableStateOf(false)
    }
    var _shouldAnimate by remember { mutableStateOf(false) }
    // contentsList의 사이즈가 변경될 때마다 애니메이션을 트리거

    _contentsList.getContentIfNotHandled()?.let {
        LaunchedEffect(_contentsList) {
            if(it.size > 0)
            {
                _shouldAnimate = true
            }
        }

        _dataList = ArrayList()
        _dataList = it
    }
    _selectedItemCount.getContentIfNotHandled()?.let {
        Log.i("count : $it")
        _itemCount = it
        _isFabToolbarVisible = if(it > 0) true else false
    }


    val scaffoldState = rememberCollapsingToolbarScaffoldState()

    Box(modifier = Modifier.fillMaxSize())
    {
        CollapsingToolbarScaffold(modifier = Modifier.fillMaxSize(), state = scaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed, toolbar = {
                TopbarSeriesContentsLayout(modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 144)
                    )
                    .pin(),
                    title = _seriesTitle,
                    background = _prepareData.titleColor,
                    isShowSeriesInformation = _showToolbarInformationView,
                    onTabBackButton = { /*TODO*/},
                    onTabSeriesInformationButton = {

                    })
                BuildCollapsibleImageHeader(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(
                            getDp(pixel = 607)
                        )
                        .graphicsLayer { // change alpha of Image as the toolbar expands
                            if(scaffoldState.toolbarState.progress == 0f)
                            {
                                alpha = 0f
                            } else
                            {
                                alpha = 1f
                            }

                        }
                        .parallax(),
                    thumbnailUrl = _prepareData.thumbnail,
                )
            })
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorResource(id = R.color.color_edeef2)
                    )
            )
            {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = getDp(pixel = 20), end = getDp(pixel = 20)
                        )
                )
                {
                    items(_dataList.size) {index ->
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
                                visible = _dataList.isNotEmpty() && _shouldAnimate,
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
                                    targetOffsetY = { 0 }
                                )
                            )
                            {
                                BuildContentsListItem(
                                    data = _dataList[index],
                                    itemIndexColor = _prepareData.titleColor,
                                    onBackgroundClick = {
                                        Log.i("onBackgroundClick : $index")
                                        onEvent(
                                            SeriesContentsListEvent.onSelectedItem(index)
                                        )
                                    },
                                    onThumbnailClick = {
                                        onEvent(
                                            SeriesContentsListEvent.onClickThumbnail(_dataList[index])
                                        )
                                    },
                                    onOptionClick = {
                                        onEvent(
                                            SeriesContentsListEvent.onClickOption(_dataList[index])
                                        )
                                    })
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
/*                AnimatedVisibility(
                    visible = _dataList.isNotEmpty() && _shouldAnimate,
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
                )
                {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = getDp(pixel = 28),
                                end = getDp(pixel = 28),
                            )

                    ) {
                        itemsIndexed(_dataList, key = {_, item -> item.id}) {index, item ->
                            Column {
                                if(index == 0)
                                {
                                    Spacer(
                                        modifier = Modifier.height(
                                            getDp(pixel = 20)
                                        )
                                    )
                                }
                                BuildContentsListItem(data = item,
                                    itemIndexColor = _prepareData.titleColor,
                                    onBackgroundClick = {
                                        Log.i("onBackgroundClick : $index")
                                        onEvent(
                                            SeriesContentsListEvent.onSelectedItem(index)
                                        )
                                    },
                                    onThumbnailClick = {
                                        onEvent(
                                            SeriesContentsListEvent.onClickThumbnail(item)
                                        )
                                    },
                                    onOptionClick = {
                                        onEvent(
                                            SeriesContentsListEvent.onClickOption(item)
                                        )
                                    })
                                Spacer(
                                    modifier = Modifier.height(
                                        getDp(pixel = 20)
                                    )
                                )
                            }
                        }
                    }

                }*/


                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 100)
                        )
                        .height(
                            getDp(pixel = 100)
                        )
                        .offset(
                            x = getDp(pixel = 490), y = getDp(pixel = 500)
                        )

                ) {
                    AnimatedVisibility(
                        visible = _isShowContentsLoading, enter = fadeIn(), exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.color_1aa3f8)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(
                end = getDp(pixel = 40), bottom = getDp(pixel = 40)
            )
        ) {
            AnimatedVisibility(
                visible = !_isFabToolbarVisible,
                enter = slideInHorizontally(
                    initialOffsetX = {
                        200
                    },
                    animationSpec = tween(
                        durationMillis = Common.DURATION_NORMAL.toInt(),
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = {
                        200
                    },
                    animationSpec = tween(
                        durationMillis = Common.DURATION_NORMAL.toInt(),
                        easing = FastOutSlowInEasing
                    )
                ),
            )
            {
                FloatingActionButton(
                    onClick = {
                        _isFabToolbarVisible = true
                    },

                    ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_plus),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 142)
                            )
                            .height(
                                getDp(pixel = 142)
                            ),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Float Button"
                    )
                }
            }
        }

        BuildBottomSelectBarLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            selectedItemCount = _itemCount,
            isVisible = _isFabToolbarVisible,
            onClickMenu = { menu ->
                onEvent(
                    SeriesContentsListEvent.onClickBottomBarMenu(menu)
                )
            }
        )
    }
}

@Composable
fun BuildCollapsibleImageHeader(
    modifier : Modifier = Modifier,
    thumbnailUrl : String,
)
{

    Box(
        modifier = modifier
    )
    {
        Image(
            painter = rememberAsyncImagePainter(thumbnailUrl),
            contentDescription = "Thumbnail Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}



