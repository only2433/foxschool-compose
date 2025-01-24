package com.littlefox.app.foxschool.presentation.screen.category_list.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.widget.TopbarSeriesContentsLayout
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.mvi.category.CategoryListAction
import com.littlefox.app.foxschool.presentation.mvi.category.viewmodel.CategoryListViewModel
import com.littlefox.app.foxschool.presentation.widget.SeriesGridViewItem

@Composable
fun CategoryListScreenV(
    viewModel : CategoryListViewModel,
    onAction: (CategoryListAction) -> Unit
)
{
    val state by viewModel.state.collectAsStateWithLifecycle()
    var _shouldAnimate by remember {
        mutableStateOf( false)
    }

    LaunchedEffect(state.categoryList.size) {
        if(state.categoryList.size > 0)
        {
            _shouldAnimate = true
        }
    }

    val scaffoldState = rememberCollapsingToolbarScaffoldState()

    Box(modifier = Modifier.fillMaxSize())
    {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = scaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = {
                TopbarSeriesContentsLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 144)
                        )
                        .pin(),
                    title = state.title,
                    background = state.backgroundViewData.titleColor,
                    isShowSeriesInformation = false,
                    onTabBackButton = {},
                    onTabSeriesInformationButton = {}
                )
                BuildCollapsibleImageHeader(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(
                            getDp(pixel = 607)
                        )
                        .graphicsLayer {
                            alpha = if(scaffoldState.toolbarState.progress == 0f)
                            {
                                0f
                            } else
                            {
                                1f
                            }
                        }
                        .parallax(),
                    thumbnailUrl = state.backgroundViewData.thumbnail,
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorResource(id = R.color.color_edeef2)
                    )
            )
            {
                Column {
                    Spacer(
                        modifier = Modifier
                            .height(
                                getDp(pixel = 20)
                            )
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()

                    ) {
                        items(state.categoryList.size) { index ->
                            if(index % 2 == 0)
                            {
                                AnimatedVisibility(
                                    visible = state.categoryList.isNotEmpty() && _shouldAnimate,
                                    enter = slideInVertically(
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
                                    ),
                                ) {
                                    SeriesGridViewItem(
                                        modifier = Modifier
                                            .padding(
                                                start = getDp(pixel = 26),
                                                end = getDp(pixel = 12)
                                            ),
                                        data = state.categoryList[index],
                                        isVisibleLevel = false
                                    ) {
                                        onAction(
                                            CategoryListAction.ClickContentsItem(
                                                state.categoryList[index]
                                            )
                                        )
                                    }
                                }
                            }
                            else
                            {
                                AnimatedVisibility(
                                    visible = state.categoryList.isNotEmpty() && _shouldAnimate,
                                    enter = slideInVertically(
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
                                ) {
                                    SeriesGridViewItem(
                                        modifier = Modifier
                                            .padding(
                                                start = getDp(pixel = 12),
                                                end = getDp(pixel = 26)
                                            ),
                                        data = state.categoryList[index],
                                        isVisibleLevel = false
                                    ) {
                                        onAction(
                                            CategoryListAction.ClickContentsItem(
                                                state.categoryList[index]
                                            )
                                        )
                                    }
                                }
                            }
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
}

@Composable
fun BuildCollapsibleImageHeader(
    modifier : Modifier = Modifier,
    thumbnailUrl : String
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
