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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer


import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.`object`.data.series.TopThumbnailViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.SeriesContentsListViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.series_contents_list.SeriesContentsListEvent
import com.littlefox.app.foxschool.presentation.widget.BuildContentsListItem
import com.littlefox.app.foxschool.presentation.widget.TopbarSeriesContentsLayout
import com.littlefox.logmonitor.Log
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ExperimentalToolbarApi
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalToolbarApi::class)
@Composable
fun SeriesContentsScreenV(
    viewModel : SeriesContentsListViewModel,
    onEvent: (SeriesContentsListEvent) -> Unit,
)
{
    val contentsList by viewModel.contentsList.collectAsState(initial = emptyList<ContentsBaseResult>())
    val showToolbarInformationView by viewModel.showToolbarInformationView.collectAsState(initial = false)
    val isShowContentsLoading by viewModel.isContentsLoading.collectAsState(initial = true)
    val seriesTitle by viewModel.seriesTitle.collectAsState(initial = "")
    val prepareData by viewModel.backgroundView.collectAsState(initial = TopThumbnailViewData())

    var isFabToolbarVisible by remember { //
        mutableStateOf(false)
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
                    title = seriesTitle,
                    background = prepareData.titleColor,
                    isShowSeriesInformation = showToolbarInformationView,
                    onTabBackButton = { /*TODO*/},
                    onTabSeriesInformationButton = {

                    })
                BuildCollapsibleImageHeader(thumbnailUrl = prepareData.thumbnail,
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
                        .parallax())
            })
        {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = getDp(pixel = 28),
                            end = getDp(pixel = 28),
                        )

                ) {

                    items(contentsList.size) {index ->
                        Column {

                            if(index == 0)
                            {
                                Spacer(
                                    modifier = Modifier.height(
                                            getDp(pixel = 20)
                                        )
                                )
                            }
                            BuildContentsListItem(data = contentsList[index],
                                itemColor = prepareData.titleColor,

                                onThumbnailClick = {

                                },
                                onOptionClick = {

                                })
                            Spacer(
                                modifier = Modifier.height(
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
                        visible = isShowContentsLoading, enter = fadeIn(), exit = fadeOut()
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
                visible = !isFabToolbarVisible,
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
                        isFabToolbarVisible = !isFabToolbarVisible
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
            isVisible = isFabToolbarVisible,
            onClickAll = {

            },
            onClickPlay = {

            },
            onClickBookshelf = {

            },
            onClickCancel = {
                isFabToolbarVisible = !isFabToolbarVisible
            }
        )
    }
}

@Composable
fun BuildCollapsibleImageHeader(
    thumbnailUrl : String,
    modifier : Modifier = Modifier
)
{
    Log.i("thumbnailUrl : $thumbnailUrl")
    Box(
        modifier = modifier
    )
    {
        // 여기에 썸네일을 세팅 하고 싶어.
        Image(
            painter = rememberAsyncImagePainter(thumbnailUrl),
            contentDescription = "Thumbnail Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun BuildBottomSelectBarLayout(
    modifier : Modifier = Modifier,
    isVisible: Boolean,
    onClickAll: () -> Unit,
    onClickPlay: () -> Unit,
    onClickBookshelf: () -> Unit,
    onClickCancel: () -> Unit
)
{
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
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onClickAll
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
                            onClick = onClickPlay
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
                            onClick = onClickBookshelf
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
                            painter = painterResource(id = R.drawable.bottom_bookshelf),
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
                            text = stringResource(id = R.string.text_contain_bookshelf),
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
                            onClick = onClickCancel
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

