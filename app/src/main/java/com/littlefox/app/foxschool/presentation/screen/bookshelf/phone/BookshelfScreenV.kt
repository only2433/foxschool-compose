package com.littlefox.app.foxschool.presentation.screen.bookshelf.phone

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.littlefox.app.foxschool.presentation.viewmodel.BookshelfViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.viewmodel.bookshelf.BookshelfEvent
import com.littlefox.logmonitor.Log
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.widget.BuildBottomSelectBarLayout
import com.littlefox.app.foxschool.presentation.widget.BuildContentsListItem
import com.littlefox.app.foxschool.presentation.widget.TopBarBackLayout
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout

@Composable
fun BookshelfScreenV(
    viewModel : BookshelfViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val contentsList by viewModel.contentsList.observeAsState(
        initial = emptyList()
    )

    val seriesTitle by viewModel.setTitle.observeAsState(initial = "")
    val selectedItemCount by viewModel.itemSelectedCount.observeAsState(initial = 0)
    val isShowContentsLoading by viewModel.enableContentListLoading.observeAsState(initial = false)
    var isFabToolbarVisible by remember {
        mutableStateOf(false)
    }
    var shouldAnimate by remember {
        mutableStateOf(false)
    }

    val contentsSize = contentsList.size
    LaunchedEffect(contentsSize) {
        Log.i("------------- notify size : $contentsSize")
        shouldAnimate = true
    }

    LaunchedEffect(selectedItemCount) {

        Log.i("selectedItemCount : $selectedItemCount")
        if(selectedItemCount > 0)
        {
            isFabToolbarVisible = true
        }
        else
        {
            isFabToolbarVisible = false
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
        Column {
            TopBarBackLayout(
                title = seriesTitle,
                backgroundColor = colorResource(id = R.color.color_23cc8a)) {
                onEvent(
                    BaseEvent.onBackPressed
                )
            }

            AnimatedVisibility(
                visible = contentsList.size > 0 && shouldAnimate,
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
                            start = getDp(pixel = 20), end = getDp(pixel = 20)
                        )
                ) {
                    itemsIndexed(contentsList, key = {_, item -> item.id}){ index, item ->
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
                        }
                        BuildContentsListItem(
                            data = item,
                            onBackgroundClick = {
                                onEvent(
                                    BookshelfEvent.onSelectedItem(index)
                                )
                            },
                            onThumbnailClick = {
                                onEvent(
                                    BookshelfEvent.onClickThumbnail(item)
                                )
                            },
                            onOptionClick = {
                                onEvent(
                                    BookshelfEvent.onClickOption(item)
                                )
                            }
                        )
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
        )
        {
            AnimatedVisibility(
                visible = isShowContentsLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.color_1aa3f8)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = getDp(pixel = 40), bottom = getDp(pixel = 40)
                )
        )
        {
            AnimatedVisibility(
                visible = !isFabToolbarVisible,
                enter = slideInHorizontally(
                    initialOffsetX = {
                        200
                    },
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = {
                        200
                    },
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                FloatingActionButton(
                    onClick = {
                        isFabToolbarVisible = true
                    }
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
            isVisible = isFabToolbarVisible,
            isBookshelfMode = true,
            onClickAll = {
                onEvent(
                    BookshelfEvent.onClickSelectAll
                )
            },
            onClickPlay = {
                onEvent(
                    BookshelfEvent.onClickSelectPlay
                )
            },
            onClickBookshelf = {
                onEvent(
                    BookshelfEvent.onClickDeleteBookshelf
                )
            },
            onClickCancel = {
                onEvent(
                    BookshelfEvent.onClickCancel
                )
            }
        )
    }

}