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
import androidx.compose.runtime.mutableIntStateOf
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
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.widget.BuildBottomSelectBarLayout
import com.littlefox.app.foxschool.presentation.widget.BuildContentsListItem
import com.littlefox.app.foxschool.presentation.widget.TopBarBackLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper

@Composable
fun BookshelfScreenV(
    viewModel : BookshelfViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val _contentsList by viewModel.contentsList.observeAsState(
        initial = EventWrapper(ArrayList())
    )
    val _selectedItemCount by viewModel.itemSelectedCount.observeAsState(
        initial = EventWrapper(0)
    )
    val _seriesTitle by viewModel.setTitle.observeAsState(initial = "")
    val _isShowContentsLoading by viewModel.enableContentListLoading.observeAsState(initial = false)

    var _dataList by remember {
        mutableStateOf(ArrayList<ContentsBaseResult>())
    }
    var _itemCount by remember {
        mutableIntStateOf(0)
    }
    var _isFabToolbarVisible by remember {
        mutableStateOf(false)
    }
    var _isShouldAnimate by remember {
        mutableStateOf(false)
    }


    _contentsList.getContentIfNotHandled()?.let {
        Log.i("------------- notify size : ${it.size}")
        LaunchedEffect(_contentsList) {
            if( it.size > 0)
            {
                _isShouldAnimate = true
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
                title = _seriesTitle,
                backgroundColor = colorResource(id = R.color.color_23cc8a)) {
                onEvent(
                    BaseEvent.onBackPressed
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = getDp(pixel = 20), end = getDp(pixel = 20)
                    )
            ) {
                itemsIndexed(_dataList, key = {_, item -> item.id}){index, item ->
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
                                targetOffsetY = { 0 }
                            )
                        )
                        {
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
        )
        {
            AnimatedVisibility(
                visible = _isShowContentsLoading,
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
                visible = !_isFabToolbarVisible,
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
                        _isFabToolbarVisible = true
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
            isVisible = _isFabToolbarVisible,
            selectedItemCount = _itemCount,
            isBookshelfMode = true,
            onClickMenu = { menu ->
                onEvent(
                    BookshelfEvent.onClickBottomBarMenu(menu)
                )
            }
        )
    }

}