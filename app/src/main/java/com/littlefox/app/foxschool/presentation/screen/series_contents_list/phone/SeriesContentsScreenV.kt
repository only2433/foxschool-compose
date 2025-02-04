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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.ContentsListBottomBarMenu
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.SeriesContentsListAction
import com.littlefox.app.foxschool.presentation.mvi.series_contents_list.viewmodel.SeriesContentsListViewModel
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
    onAction: (SeriesContentsListAction) -> Unit,
)
{
    val state by viewModel.state.collectAsStateWithLifecycle()

    var _isFabToolbarVisible by remember { //
        mutableStateOf(false)
    }
    var _shouldAnimate by remember {
        mutableStateOf(false)
    }

    // contentsList의 사이즈가 변경될 때마다 애니메이션을 트리거
    LaunchedEffect(state.contentsList) {
        if(state.contentsList.size > 0)
        {
            _shouldAnimate = true
        }
    }

    // selectItemCount 사이즈가 변경될 때마다 애니메이션을 트리거
    LaunchedEffect(state.selectItemCount) {
        Log.i("state.selectItemCount : $state.selectItemCount")
        _isFabToolbarVisible = if(state.selectItemCount > 0) true else false
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
                    title = state.title,
                    background = state.backgroundViewData.titleColor,
                    isShowSeriesInformation = state.isShowInformationTooltip,
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
                    thumbnailUrl = state.backgroundViewData.thumbnail,
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
                    items(state.contentsList.size) {index ->
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
                                visible = state.contentsList.isNotEmpty() && _shouldAnimate,
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
                                    data = state.contentsList[index],
                                    itemIndexColor = state.backgroundViewData.titleColor,
                                    onBackgroundClick = {
                                        Log.i("onBackgroundClick : $index")
                                        onAction(
                                            SeriesContentsListAction.SelectedItem(index)
                                        )
                                    },
                                    onThumbnailClick = {
                                        onAction(
                                            SeriesContentsListAction.ClickThumbnail(
                                                state.contentsList[index]
                                            )
                                        )
                                    },
                                    onOptionClick = {
                                        onAction(
                                            SeriesContentsListAction.ClickOption(
                                                state.contentsList[index]
                                            )
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
                        visible = state.isContentsLoading, enter = fadeIn(), exit = fadeOut()
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
            selectedItemCount = state.selectItemCount,
            isVisible = _isFabToolbarVisible,
            onClickMenu = { menu ->
                onAction(
                    SeriesContentsListAction.ClickBottomBarMenu(menu)
                )
                if(menu == ContentsListBottomBarMenu.CANCEL)
                {
                    _isFabToolbarVisible = false
                }
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



