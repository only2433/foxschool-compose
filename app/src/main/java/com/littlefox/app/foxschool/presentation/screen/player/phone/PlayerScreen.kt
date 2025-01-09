package com.littlefox.app.foxschool.presentation.screen.player.phone

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Slider
import androidx.compose.material.SliderColors
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.littlefox.app.foxschool.presentation.viewmodel.PlayerViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.enumerate.DataType
import com.littlefox.app.foxschool.enumerate.MovieNavigationStatus
import com.littlefox.app.foxschool.enumerate.PlayerActionType
import com.littlefox.app.foxschool.`object`.data.player.PlayerEndViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.player.PlayerEvent
import com.littlefox.app.foxschool.presentation.widget.BuildPlayerListItem
import com.littlefox.app.foxschool.presentation.widget.BuildSpeedListItem
import com.littlefox.logmonitor.Log
import de.charlex.compose.material.HtmlText

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onEvent: (BaseEvent) -> Unit
) 
{
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val _captionText by viewModel.setCaptionText.observeAsState(initial = "")
    val _player by viewModel.player.observeAsState(initial = null)
    val _movieTitle by viewModel.setMovieTitle.observeAsState(initial = "")
    val _contentsList by viewModel.contentsList.observeAsState(initial = ArrayList<ContentsBaseResult>())
    val _currentPlayIndex by viewModel.currentPlayIndex.observeAsState(initial = 0)
    val _isSupportCaptionAndPage by viewModel.supportCaptionAndPage.observeAsState(initial = false)
    val _isPlayingMovie by viewModel.enablePlayMovie.observeAsState(initial = false)
    val _currentPlayProgress by viewModel.setSeekProgress.observeAsState(initial = 0)
    val _maxPlayProgress by viewModel.setMaxProgress.observeAsState(initial = 100)
    val _currentPlayTime by viewModel.setCurrentMovieTime.observeAsState(initial = "")
    val _maxPlayTime by viewModel.setRemainMovieTime.observeAsState(initial = "")
    val _navigationStatus by viewModel.movieNavigationStatus.observeAsState(initial = MovieNavigationStatus.NORMAL)
    val _isMovieLoading by viewModel.isMovieLoading.observeAsState(initial = false)
    val _isCompleteToReadyMovie by viewModel.isReadyToMovie.observeAsState(initial = false)
    val _endLayoutData by viewModel.settingPlayerEndView.observeAsState(initial = PlayerEndViewData())
    val _isShowEndLayout by viewModel.showPlayerEndView.observeAsState(initial = false)
    val _currentPlaySpeedIndex by viewModel.currentPlaySpeedIndex.observeAsState(initial = 0)

    var _enableCaption by  remember {
        mutableStateOf(
            CommonUtils.getInstance(context).getSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, DataType.TYPE_BOOLEAN) as Boolean
        )
    }
    LaunchedEffect(_enableCaption) {
        CommonUtils.getInstance(context).setSharedPreference(Common.PARAMS_IS_ENABLE_CAPTION, _enableCaption)
    }

    var _enablePageByPage by remember {
        mutableStateOf(
            CommonUtils.getInstance(context).getSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, DataType.TYPE_BOOLEAN) as Boolean
        )
    }
    LaunchedEffect(_enablePageByPage) {
        CommonUtils.getInstance(context).setSharedPreference(Common.PARAMS_IS_ENABLE_PAGE_BY_PAGE, _enablePageByPage)
    }


    var _isPlayListVisible by remember {
        mutableStateOf(false)
    }

    var _isPlaySpeedViewVisible by remember {
        mutableStateOf(false)
    }

    var _isMenuVisible by remember{
        mutableStateOf(false)
    }

    LaunchedEffect(_isCompleteToReadyMovie) {
        if(_isCompleteToReadyMovie == false)
        {
            _isMenuVisible = false
        }
    }

    LaunchedEffect(_isShowEndLayout) {
        if(_isShowEndLayout)
        {
            _isMenuVisible = false
            _isPlaySpeedViewVisible = false
            _isPlayListVisible = false
        }
    }

    when(configuration.orientation)
    {
        Configuration.ORIENTATION_PORTRAIT ->
        {
            BuildPortraitScreen(
                moviePlayer = _player,
                isCompleteToReadyMovie = _isCompleteToReadyMovie,
                isMovieLoading = _isMovieLoading,
                isPlayListVisible = _isPlayListVisible,
                isPlaySpeedViewVisible = _isPlaySpeedViewVisible,
                isShowEndLayout = _isShowEndLayout,
                isSupportCaptionAndPage = _isSupportCaptionAndPage,
                isPlayingMovie = _isPlayingMovie,
                isMenuVisible = _isMenuVisible,
                enableCaption = _enableCaption,
                enablePageByPage = _enablePageByPage,
                movieTitle = _movieTitle,
                captionText = _captionText,
                navigationStatus = _navigationStatus,
                currentPlayProgress = _currentPlayProgress.toFloat(),
                maxPlayProgress = _maxPlayProgress.toFloat(),
                currentPlayTime = _currentPlayTime,
                maxPlayTime = _maxPlayTime,
                endViewData = _endLayoutData,
                contentsList = _contentsList,
                currentPlayIndex = _currentPlayIndex,
                currentPlaySpeedIndex = _currentPlaySpeedIndex,
                onEvent = onEvent,
                onValueChange = { type, value ->
                    when(type)
                    {
                        PlayerActionType.MENU ->
                        {
                            _isMenuVisible = value
                        }
                        PlayerActionType.PLAY_LIST ->
                        {
                            _isPlayListVisible = value
                        }
                        PlayerActionType.SPEED_MENU ->
                        {
                            _isPlaySpeedViewVisible = value
                        }
                        PlayerActionType.CAPTION ->
                        {
                            _enableCaption = value
                        }
                        PlayerActionType.PAGE_BY_PAGE ->
                        {
                            _enablePageByPage = value
                        }
                    }
                }
            )
        }
        Configuration.ORIENTATION_LANDSCAPE ->
        {
            BuildLandscapeScreen(
                moviePlayer = _player,
                isCompleteToReadyMovie = _isCompleteToReadyMovie,
                isMovieLoading = _isMovieLoading,
                isPlayListVisible = _isPlayListVisible,
                isPlaySpeedViewVisible = _isPlaySpeedViewVisible,
                isShowEndLayout = _isShowEndLayout,
                isSupportCaptionAndPage = _isSupportCaptionAndPage,
                isPlayingMovie = _isPlayingMovie,
                isMenuVisible = _isMenuVisible,
                enableCaption = _enableCaption,
                enablePageByPage = _enablePageByPage,
                movieTitle = _movieTitle,
                captionText = _captionText,
                navigationStatus = _navigationStatus,
                currentPlayProgress = _currentPlayProgress.toFloat(),
                maxPlayProgress = _maxPlayProgress.toFloat(),
                currentPlayTime = _currentPlayTime,
                maxPlayTime = _maxPlayTime,
                endViewData = _endLayoutData,
                contentsList = _contentsList,
                currentPlayIndex = _currentPlayIndex,
                currentPlaySpeedIndex = _currentPlaySpeedIndex,
                onEvent = onEvent,
                onValueChange = { type, value ->
                    when(type)
                    {
                        PlayerActionType.MENU ->
                        {
                            _isMenuVisible = value
                        }
                        PlayerActionType.PLAY_LIST ->
                        {
                            _isPlayListVisible = value
                        }
                        PlayerActionType.SPEED_MENU ->
                        {
                            _isPlaySpeedViewVisible = value
                        }
                        PlayerActionType.CAPTION ->
                        {
                            _enableCaption = value
                        }
                        PlayerActionType.PAGE_BY_PAGE ->
                        {
                            _enablePageByPage = value
                        }
                    }
                }
            )
        }
        else -> {}
    }
}

@Composable
private fun BuildPortraitScreen(
    moviePlayer : SimpleExoPlayer?,
    isCompleteToReadyMovie : Boolean,
    isMovieLoading : Boolean,
    isPlayListVisible : Boolean,
    isPlaySpeedViewVisible : Boolean,
    isShowEndLayout : Boolean,
    isSupportCaptionAndPage : Boolean,
    isPlayingMovie: Boolean,
    isMenuVisible : Boolean,
    enableCaption : Boolean,
    enablePageByPage: Boolean,
    movieTitle: String,
    captionText : String,
    navigationStatus : MovieNavigationStatus,
    currentPlayProgress: Float,
    maxPlayProgress: Float,
    currentPlayTime: String,
    maxPlayTime: String,
    endViewData : PlayerEndViewData,
    contentsList: ArrayList<ContentsBaseResult>,
    currentPlayIndex: Int,
    currentPlaySpeedIndex: Int,
    onEvent: (BaseEvent) -> Unit,
    onValueChange: (PlayerActionType, Boolean) -> Unit,
)
{
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        if(isCompleteToReadyMovie == false)
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 602)
                    )
                    .background(
                        color = colorResource(id = R.color.color_000000)
                    )
            )
        }
        else
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 602)
                    )
            )
            {
                moviePlayer?.let {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = it
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 602)
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                if(isMovieLoading == false && isPlayListVisible == false && isPlaySpeedViewVisible == false && isShowEndLayout == false)
                                {
                                    onValueChange(PlayerActionType.MENU, !isMenuVisible)
                                }
                            })
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(
                            alignment = Alignment.BottomCenter
                        ),
                    visible = enableCaption,
                    enter = slideInVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        initialOffsetY = {
                            112
                        }
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        targetOffsetY = {
                            112
                        }
                    )
                )
                {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 112)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    getDp(pixel = 140)
                                )
                                .background(
                                    color = colorResource(id = R.color.color_alpha_07_black)
                                )
                        )

                        HtmlText(
                            text = captionText, style = TextStyle(
                                color = colorResource(id = R.color.color_ffffff),
                                fontSize = 12.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.pretendard_medium
                                    )
                                )
                            )
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .alpha(
                            0.5f
                        ),
                    visible = isMenuVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 602)
                            )
                            .background(
                                color = colorResource(id = R.color.color_000000)
                            )
                    )
                }

                AnimatedVisibility(
                    visible = isMenuVisible,
                    enter = slideInVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                        initialOffsetY = {
                            -112
                        }
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                        targetOffsetY = {
                            -112
                        }
                    )
                ) {
                    BuildTopViewPortrait(
                        isSupportCaptionAndPage = isSupportCaptionAndPage,
                        isCheckCaption = enableCaption,
                        onCheckCaption = {
                            onValueChange(PlayerActionType.CAPTION, !enableCaption)
                        },
                        onClickCloseButton = {
                            onEvent(
                                BaseEvent.onBackPressed
                            )
                        }
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(
                            Alignment.Center
                        ),
                    visible = isMenuVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    BuildPlayControllerView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 109)
                            ),
                        movieNavigationStatus = navigationStatus,
                        isPlaying = isPlayingMovie,
                        onClickPrev = {
                            onEvent(
                                PlayerEvent.onClickControllerPrev
                            )
                        },
                        onClickNext = {
                            onEvent(
                                PlayerEvent.onClickControllerNext
                            )
                        },
                        onClickPlay = {
                            onEvent(
                                PlayerEvent.onClickControllerPlay
                            )
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isShowEndLayout,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = tween(
                            durationMillis = 1200,
                            easing = FastOutSlowInEasing,
                        ),
                        initialOffsetY = {
                            -602
                        }
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = FastOutSlowInEasing
                        ),
                    )
                )
                {
                    BuildPlayerEndView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 602)
                            ),
                        data = endViewData,
                        onClickActionType = {type ->
                            onEvent(
                                PlayerEvent.onClickActionContentsType(type)
                            )
                        },
                        onClickClose = {
                            onEvent(
                                BaseEvent.onBackPressed
                            )
                        },
                        onClickReplay = {
                            onEvent(
                                PlayerEvent.onClickReplay
                            )
                        },
                        onClickNextMovie = {
                            onEvent(
                                PlayerEvent.onClickLoadNextMovie
                            )
                        })
                }
                
            }

        }

        BuildPlayListViewPortrait(
            modifier = Modifier
                .offset(
                    y = getDp(pixel = 602)
                ),
            dataList = contentsList,
            currentPlayIndex = currentPlayIndex,
            onSelectPlayItem = { index ->
                if(isMovieLoading == false)
                {
                    onEvent(
                        PlayerEvent.onSelectItem(index)
                    )
                }
            },
        )

        if(isCompleteToReadyMovie && isShowEndLayout == false)
        {
            BuildSeekViewPortrait(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(
                        y = getDp(pixel = 571)
                    ),
                progress = currentPlayProgress,
                maxProgress = maxPlayProgress,
                onStartTrackingTouch = {
                    onEvent(
                        PlayerEvent.onStartTrackingTouch
                    )
                },
                onStopTrackingTouch = { progress ->
                    onEvent(
                        PlayerEvent.onStopTrackingTouch(
                            progress.toInt()
                        )
                    )
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 602)
                )
        )
        {
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
                    visible = isMovieLoading, enter = fadeIn(), exit = fadeOut()
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
private fun BuildLandscapeScreen(
    moviePlayer : SimpleExoPlayer?,
    isCompleteToReadyMovie : Boolean,
    isMovieLoading : Boolean,
    isPlayListVisible : Boolean,
    isPlaySpeedViewVisible : Boolean,
    isShowEndLayout : Boolean,
    isSupportCaptionAndPage : Boolean,
    isPlayingMovie: Boolean,
    isMenuVisible : Boolean,
    enableCaption : Boolean,
    enablePageByPage: Boolean,
    movieTitle: String,
    captionText : String,
    navigationStatus : MovieNavigationStatus,
    currentPlayProgress: Float,
    maxPlayProgress: Float,
    currentPlayTime: String,
    maxPlayTime: String,
    endViewData : PlayerEndViewData,
    contentsList: ArrayList<ContentsBaseResult>,
    currentPlayIndex: Int,
    currentPlaySpeedIndex: Int,
    onEvent: (BaseEvent) -> Unit,
    onValueChange: (PlayerActionType, Boolean) -> Unit,
)
{
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        if(isCompleteToReadyMovie == false)
        {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(
                        color = colorResource(id = R.color.color_000000)
                    )
            )
        }
        else
        {
            moviePlayer?.let {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = it
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            if(isMovieLoading == false && isPlayListVisible == false && isPlaySpeedViewVisible == false && isShowEndLayout == false)
                            {
                                onValueChange(PlayerActionType.MENU, !isMenuVisible)
                            }
                        })
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(
                        alignment = Alignment.BottomCenter
                    ),
                visible = enableCaption,
                enter = slideInVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = {
                        140
                    }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetY = {
                        140
                    }
                )
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 140)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 140)
                            )
                            .background(
                                color = colorResource(id = R.color.color_alpha_07_black)
                            )
                    )

                    HtmlText(
                        text = captionText, style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.pretendard_medium
                                )
                            )
                        )
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier
                    .alpha(
                        0.5f
                    ),
                visible = isMenuVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = colorResource(id = R.color.color_000000)
                        )
                )
            }

            AnimatedVisibility(
                visible = isMenuVisible,
                enter = slideInVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                    initialOffsetY = {
                        -150
                    }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                    targetOffsetY = {
                        -150
                    }
                )
            ) {
                BuildTopView(
                    title = movieTitle,
                    isSupportCaptionAndPage = isSupportCaptionAndPage,
                    isCheckCaption = enableCaption,
                    isCheckPageByPage = enablePageByPage,
                    onCheckCaption = {
                        onValueChange(PlayerActionType.CAPTION, !enableCaption)
                    },
                    onCheckPageByPage = {
                        onValueChange(PlayerActionType.PAGE_BY_PAGE, !enablePageByPage)
                    },
                    onClickListButton = {
                        onValueChange(PlayerActionType.MENU, false)
                        onValueChange(PlayerActionType.PLAY_LIST, true)
                    },
                    onClickCloseButton = {
                        onEvent(
                            BaseEvent.onBackPressed
                        )
                    })

            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(
                        Alignment.Center
                    ),
                visible = isMenuVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                BuildPlayControllerView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 167)
                        ),
                    movieNavigationStatus = navigationStatus,
                    isPlaying = isPlayingMovie,
                    onClickPrev = {
                        onEvent(
                            PlayerEvent.onClickControllerPrev
                        )
                    },
                    onClickNext = {
                        onEvent(
                            PlayerEvent.onClickControllerNext
                        )
                    },
                    onClickPlay = {
                        onEvent(
                            PlayerEvent.onClickControllerPlay
                        )
                    }
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    ),
                visible = isMenuVisible,
                enter = slideInVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = {
                        200
                    }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetY = {
                        200
                    }
                )
            ) {
                BuildBottomInformationView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 234)
                        ),
                    progress = currentPlayProgress,
                    maxProgress = maxPlayProgress,
                    currentDuration = currentPlayTime,
                    totalDuration = maxPlayTime,
                    isRepeat = false,
                    onClickSpeedButton = {
                        onValueChange(PlayerActionType.MENU, false)
                        onValueChange(PlayerActionType.SPEED_MENU, true)
                    },
                    onStartTrackingTouch = {
                        onEvent(
                            PlayerEvent.onStartTrackingTouch
                        )
                    },
                    onStopTrackingTouch = { progress ->
                        onEvent(
                            PlayerEvent.onStopTrackingTouch(
                                progress.toInt()
                            )
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = isShowEndLayout,
                enter = fadeIn() + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing,
                    ),
                    initialOffsetY = {
                        -1000
                    }
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = FastOutSlowInEasing
                    ),
                )
            )
            {
                BuildPlayerEndView(
                    modifier = Modifier
                        .fillMaxSize(),
                    data = endViewData,
                    onClickActionType = {type ->
                        onEvent(
                            PlayerEvent.onClickActionContentsType(type)
                        )
                    },
                    onClickClose = {
                        onEvent(
                            BaseEvent.onBackPressed
                        )
                    },
                    onClickReplay = {
                        onEvent(
                            PlayerEvent.onClickReplay
                        )
                    },
                    onClickNextMovie = {
                        onEvent(
                            PlayerEvent.onClickLoadNextMovie
                        )
                    })
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(
                        alignment = Alignment.TopEnd
                    ),
                visible = isPlayListVisible,
                enter = slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetX = {
                        654
                    }
                ),
                exit = slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetX = {
                        654
                    }
                )
            )
            {
                BuildPlayListView(
                    dataList = contentsList,
                    currentPlayIndex = currentPlayIndex,
                    onSelectPlayItem = { index ->
                        if(isMovieLoading == false)
                        {
                            onEvent(
                                PlayerEvent.onSelectItem(index)
                            )
                        }
                    },
                    onClickClose = {
                        onValueChange(PlayerActionType.PLAY_LIST, false)
                        onValueChange(PlayerActionType.MENU, true)
                    }
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(
                        alignment = Alignment.TopEnd
                    ),
                visible = isPlaySpeedViewVisible,
                enter = slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetX = {
                        654
                    }
                ),
                exit = slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetX = {
                        654
                    }
                )
            )
            {
                BuildPlaySpeedColumnView(
                    currentIndex = currentPlaySpeedIndex,
                    onSelectSpeed = { index ->
                        onEvent(
                            PlayerEvent.onSelectSpeed(index)
                        )
                        onValueChange(PlayerActionType.SPEED_MENU, false)
                        onValueChange(PlayerActionType.MENU, true)
                    },
                    onClickClose = {
                        onValueChange(PlayerActionType.SPEED_MENU, false)
                        onValueChange(PlayerActionType.MENU, true)
                    }
                )
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
                visible = isMovieLoading, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.color_1aa3f8)
                )
            }
        }
    }
}



@Composable
private fun BuildTopView(
    title: String,
    isSupportCaptionAndPage: Boolean = false,
    isCheckCaption: Boolean = false,
    isCheckPageByPage: Boolean = false,
    onCheckCaption: () -> Unit,
    onCheckPageByPage: () -> Unit,
    onClickListButton: () -> Unit,
    onClickCloseButton: () -> Unit
)
{
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 150)
            )
            .padding(
                start = getDp(pixel = 50), end = getDp(pixel = 50)
            )
    ) {
        val (text, controller) = createRefs()
        Box(
            modifier = Modifier
                .height(
                    getDp(pixel = 150)
                )
                .constrainAs(text) {
                    start.linkTo(parent.start)
                    end.linkTo(controller.start)
                    width = Dimension.fillToConstraints
                },
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = title,
                style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff),
                    fontSize = 15.sp,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    )
                )
            )
        }

        Row(
            modifier = Modifier
                .width(
                    getDp(pixel = 630)
                )
                .height(
                    getDp(pixel = 150)
                )
                .constrainAs(controller) {
                    end.linkTo(parent.end)

                },
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            if(isSupportCaptionAndPage)
            {
                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 70)
                        )
                        .height(
                            getDp(pixel = 55)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = onCheckCaption
                        ),
                    painter = painterResource(
                        id = when(isCheckCaption)
                        {
                            true -> R.drawable.player__caption_on
                            false -> R.drawable.player__caption_off
                        }),
                    contentDescription = "Caption Image"
                )

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 75)
                        )
                )

                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 85)
                        )
                        .height(
                            getDp(pixel = 64)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = onCheckPageByPage
                        ),
                    painter = painterResource(
                        id = when(isCheckPageByPage)
                        {
                            true -> R.drawable.player__repeat_on
                            false -> R.drawable.player__repeat_off
                        }),
                    contentDescription = "Page By Page Image"
                )

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 75)
                        )
                )
            }

            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 62)
                    )
                    .height(
                        getDp(pixel = 52)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onClickListButton
                    ),
                painter = painterResource(id = R.drawable.player__list),
                contentDescription = "List Image"
            )

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 75)
                    )
            )

            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 58)
                    )
                    .height(
                        getDp(pixel = 58)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onClickCloseButton
                    ),
                painter = painterResource(id = R.drawable.player_btn_close),
                contentDescription = "List Image"
            )

        }
    }
}

@Composable
private fun BuildTopViewPortrait(
    isSupportCaptionAndPage: Boolean = false,
    isCheckCaption: Boolean = false,
    onCheckCaption: () -> Unit,
    onClickCloseButton: () -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 112)
            )
            .padding(
                start = getDp(pixel = 50), end = getDp(pixel = 50)
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        if(isSupportCaptionAndPage)
        {
            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 73)
                    )
                    .height(
                        getDp(pixel = 57)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onCheckCaption
                    ),
                painter = painterResource(
                    id = when(isCheckCaption)
                    {
                        true -> R.drawable.player__caption_on
                        false -> R.drawable.player__caption_off
                    }),
                contentDescription = "Caption Image"
            )

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 95)
                    )
            )
        }

        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 62)
                )
                .height(
                    getDp(pixel = 60)
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickCloseButton
                ),
            painter = painterResource(id = R.drawable.player_btn_close),
            contentDescription = "List Image"
        )
    }
}

/**
 * 플레이, 이전영상, 다음영상을 컨트롤 하는 뷰
 */
@Composable
private fun BuildPlayControllerView(
    modifier : Modifier,
    movieNavigationStatus : MovieNavigationStatus,
    isPlaying: Boolean,
    onClickPrev: () -> Unit,
    onClickNext: () -> Unit,
    onClickPlay: () -> Unit
)
{
    val configuration = LocalConfiguration.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(
            modifier = Modifier
                .width(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 49
                            else -> 68
                        }
                    )
                )
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 58
                            else -> 81
                        }
                    )
                )
                .alpha(
                    if(movieNavigationStatus == MovieNavigationStatus.BOTH_INVISIBLE || movieNavigationStatus == MovieNavigationStatus.PREV_BUTTON_INVISIBLE)
                    {
                        0f
                    } else
                    {
                        1f
                    }
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickPrev
                ),
            painter = painterResource(id = R.drawable.player__previous),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Prev Image",
        )
        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = when(configuration.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> 333
                        else -> 385
                    })
                )
        )
        Image(
            modifier = Modifier
                .width(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 98
                            else -> 147
                        }
                    )
                )
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 109
                            else -> 167
                        }
                    )
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickPlay
                ),
            painter = painterResource(
                id = when(isPlaying)
                {
                    true -> R.drawable.player__pause
                    false -> R.drawable.player__play
                }),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Play Image"
        )
        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = when(configuration.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> 333
                        else -> 385
                    })
                )
        )
        Image(
            modifier = Modifier
                .width(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 49
                            else -> 68
                        }
                    )
                )
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 58
                            else -> 81
                        }
                    )
                )
                .alpha(
                    if(movieNavigationStatus == MovieNavigationStatus.BOTH_INVISIBLE || movieNavigationStatus == MovieNavigationStatus.NEXT_BUTTON_INVISIBLE)
                    {
                        0f
                    } else
                    {
                        1f
                    }
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickNext
                ),
            painter = painterResource(id = R.drawable.player__next),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Play Image"
        )
    }
}

/**
 * Portrait 상태일땐, 하단 Information View 와 다르게 SeekView만 표시 되어야 한다.
 */
@Composable
private fun BuildSeekViewPortrait(
    modifier : Modifier,
    progress: Float,
    maxProgress: Float,
    onStartTrackingTouch : () -> Unit,
    onStopTrackingTouch: (Float) -> Unit
)
{
    var _seekProgress by remember {
        mutableStateOf(0f)
    }

    var _isDragging by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(progress) {
        _seekProgress = progress
    }

    Box(
        modifier = modifier
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 12)
                )
                .offset(
                    y = getDp(pixel = 25)
                )
        )
        {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(
                        getDp(pixel = 16)
                    )
                    .background(
                        color = colorResource(id = R.color.color_fff55a)
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(
                        getDp(pixel = 16)
                    )
                    .background(
                        color = colorResource(id = R.color.color_a0a0a0)
                    )
            )
        }
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 62)
                ),
            value = _seekProgress,
            valueRange = 0f..maxProgress,
            onValueChange = { value ->
                _seekProgress = value
                _isDragging = true
                if(_isDragging)
                {
                    onStartTrackingTouch
                }
            },
            onValueChangeFinished = {
                onStopTrackingTouch(_seekProgress)
                _isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.color_fff55a),
                activeTrackColor = colorResource(id = R.color.color_fff55a),
                inactiveTrackColor = colorResource(id = R.color.color_a0a0a0)
            )
        )
    }

}

/**
 * 하단에 배치되는 컨트롤러 븊. 현재/남은시간, 속도조절, Seeking 기능
 */
@Composable
private fun BuildBottomInformationView(
    modifier : Modifier,
    progress: Float,
    maxProgress: Float,
    isRepeat: Boolean,
    currentDuration: String = "",
    totalDuration: String = "",
    playSpeedText: String = "1x",
    onClickSpeedButton: () -> Unit,
    onStartTrackingTouch : () -> Unit,
    onStopTrackingTouch: (Float) -> Unit
)
{
    var _seekProgress by remember {
        mutableStateOf(0f)
    }

    var _isDragging by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(progress) {
        _seekProgress = progress
    }

    Row(
        modifier = modifier
            .padding(
                start = getDp(pixel = 50),
                end = getDp(pixel = 50)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 71)
                )
                .height(
                    getDp(pixel = 70)
                ), painter = painterResource(
                id = when(isRepeat)
                {
                    true -> R.drawable.player__replay_on
                    false -> R.drawable.player__replay_off
                }
            ), contentScale = ContentScale.Fit, contentDescription = "Repeat Icon"
        )
        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 52)
            )
        )

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 110)
                )
                .height(
                    getDp(pixel = 71)
                ), contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = currentDuration,
                style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_regular
                        )
                    )
                )
            )
        }

        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 20)
            )
        )
        Slider(
            modifier = Modifier
                .width(
                    getDp(pixel = 1310)
                )
                .height(
                    getDp(pixel = 62)
                ),
            value = _seekProgress,
            valueRange = 0f..maxProgress,
            onValueChange = { value ->
                _seekProgress = value
                _isDragging = true
                if(_isDragging)
                {
                    onStartTrackingTouch
                }
            },
            onValueChangeFinished = {
                onStopTrackingTouch(_seekProgress)
                _isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.color_fff55a),
                activeTrackColor = colorResource(id = R.color.color_ffffff),
                inactiveTrackColor = colorResource(id = R.color.color_a0a0a0)
            )
        )

        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 20)
            )
        )
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 110)
                )
                .height(
                    getDp(pixel = 71)
                ), contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = totalDuration,
                style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_regular
                        )
                    )
                )
            )
        }

        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 64)
            )
        )
        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 60)
                )
                .height(
                    getDp(pixel = 60)
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickSpeedButton
                ),
            painter = painterResource(id = R.drawable.player__speed),
            contentScale = ContentScale.Fit,
            contentDescription = "Speed Icon"
        )
        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 10)
            )
        )
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 110)
                )
                .height(
                    getDp(pixel = 71)
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = onClickSpeedButton
                ),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = playSpeedText,
                style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_regular
                        )
                    )
                )
            )
        }

        Spacer(
            modifier = Modifier.width(
                getDp(pixel = 10)
            )
        )

        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 62)
                )
                .height(
                    getDp(pixel = 51)
                ),
            painter = painterResource(id = R.drawable.btn_zoomout),
            contentScale = ContentScale.Fit,
            contentDescription = "Orientation Icon"
        )


    }
}

@Composable
private fun BuildPlayListViewPortrait(
    modifier : Modifier = Modifier,
    dataList: ArrayList<ContentsBaseResult>,
    currentPlayIndex : Int,
    onSelectPlayItem: (Int) -> Unit,
)
{
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_edeef2)
            )
    )
    {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 205)
                    )
                    .background(
                        color = colorResource(id = R.color.color_ffffff)
                    )
                    .padding(
                        start = getDp(pixel = 45), end = getDp(pixel = 45)
                    ),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 900)
                        )
                        .height(
                            getDp(pixel = 200)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = dataList[currentPlayIndex].getContentsName(),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_000000),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 92)
                        )
                        .height(
                            getDp(pixel = 125)
                        ),
                    painter = painterResource(id = R.drawable.icon_learning),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Option Icon"
                )
            }
            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 50)
                    )
            )
            LazyColumn {
                items(dataList.size){ index ->
                    BuildPlayerListItem(
                        data = dataList[index],
                        index = index,
                        currentPlayIndex = currentPlayIndex,
                        onSelectItem = {
                            Log.i("select index : ${index}")
                            onSelectPlayItem(index)
                        }
                    )
                }
            }
            
        }
    }
}

/**
 * 현재 재생 가능한 플레이 리스트를 보여줌
 */
@Composable
private fun BuildPlayListView(
    dataList: ArrayList<ContentsBaseResult>,
    currentPlayIndex : Int,
    onSelectPlayItem: (Int) -> Unit,
    onClickClose : () -> Unit
)
{
    Box(
        modifier = Modifier
            .width(
                getDp(pixel = 654)
            )
            .fillMaxHeight()
            .background(
                color = colorResource(id = R.color.color_alpha_07_black)
            )
    )
    {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 150)
                    )
                    .padding(
                        start = getDp(pixel = 50), end = getDp(pixel = 50)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_play_list_title),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_ffffff)
                    )
                )

                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 55)
                        )
                        .height(
                            getDp(pixel = 55)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = onClickClose
                        ),
                    painter = painterResource(id = R.drawable.player_btn_close),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Close Button"
                )
            }
            LazyColumn {
                items(dataList.size){ index ->
                    BuildPlayerListItem(
                        data = dataList[index],
                        index = index,
                        currentPlayIndex = currentPlayIndex,
                        onSelectItem = {
                            onSelectPlayItem(index)
                        }
                    )
                }
            }
        }

    }
}

/**
 * 속도 리스트 뷰
 */
@Composable
private fun BuildPlaySpeedColumnView(
    modifier : Modifier = Modifier,
    currentIndex: Int,
    onSelectSpeed: (Int) -> Unit,
    onClickClose: () -> Unit
)
{
    val context = LocalContext.current
    val speedTextArray = context.resources.getStringArray(R.array.text_list_speed)
    Column(
        modifier = modifier
            .width(
                getDp(pixel = 654)
            )
            .fillMaxHeight()
            .background(
                color = colorResource(id = R.color.color_alpha_07_black)
            )
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 150)
                )
                .padding(
                    start = getDp(pixel = 50), end = getDp(pixel = 50)
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.title_play_speed_title),
                style = TextStyle(
                    color = colorResource(id = R.color.color_ffffff)
                )
            )

            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 55)
                    )
                    .height(
                        getDp(pixel = 55)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onClickClose
                    ),
                painter = painterResource(id = R.drawable.player_btn_close),
                contentScale = ContentScale.Fit,
                contentDescription = "Close Button"
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 2)
                )
                .padding(
                    start = getDp(pixel = 39), end = getDp(pixel = 39)
                )
                .alpha(
                    0.3f
                )
        )
        {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 2)
                    )
                    .background(
                        color = colorResource(id = R.color.color_ffffff)
                    )
            )
        }
        
        Spacer(
            modifier = Modifier
                .height(
                    getDp(pixel = 20)
                )
        )

        for(i in speedTextArray.indices)
        {
            BuildSpeedListItem(
                index = i,
                currentSelectIndex = currentIndex,
                speedText = speedTextArray[i],
                onSelect = {
                    onSelectSpeed(i)
                }
            )
        }
    }
}

/**
 * 영상이 끝났을 때 화면에 표시 되는 뷰
 */
@Composable
private fun BuildPlayerEndView(
    modifier : Modifier = Modifier,
    data: PlayerEndViewData,
    onClickActionType: (ActionContentsType) -> Unit,
    onClickClose: () -> Unit,
    onClickReplay: () -> Unit,
    onClickNextMovie: () -> Unit
)
{
    val configuration = LocalConfiguration.current
    Box(
        modifier = modifier
    )
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = colorResource(id = R.color.color_alpha_aa_black)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 112
                            else -> 100
                        }
                    )
                )
                .padding(
                    end = getDp(pixel = 50)
                ),
            contentAlignment = Alignment.CenterEnd
        )
        {
            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 57)
                    )
                    .height(
                        getDp(pixel = 57)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onClickClose
                    ),
                painter = painterResource(id = R.drawable.player_btn_close),
                contentScale = ContentScale.Fit,
                contentDescription = "Close Icon"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 337
                            else -> 375
                        }
                    )
                )
                .align(
                    alignment = Alignment.Center
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .height(
                       getDp(
                           pixel = when(configuration.orientation)
                           {
                               Configuration.ORIENTATION_PORTRAIT -> 114
                               else -> 150
                           }
                       )
                   ),
                horizontalArrangement = Arrangement.Center
            )
            {
                if(data.isEbookAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.EBOOK)
                            }),
                        painter = painterResource(id = R.drawable.icon_ebook_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Ebook Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isVocabularyAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.VOCABULARY)
                            }),
                        painter = painterResource(id = R.drawable.icon_voca_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "VOCABULARY Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isQuizAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.QUIZ)
                            }),
                        painter = painterResource(id = R.drawable.icon_quiz_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "QUIZ Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isFlashcardAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.FLASHCARD)
                            }),
                        painter = painterResource(id = R.drawable.icon_flashcard_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "FLASHCARD Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isStarwordsAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.STARWORDS)
                            }),
                        painter = painterResource(id = R.drawable.icon_starwords_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "STARWORDS Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isCrosswordAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.CROSSWORD)
                            }),
                        painter = painterResource(id = R.drawable.icon_crossword_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "CROSSWORD Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )

                if(data.isTranslateAvailable)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 114
                                        else -> 150
                                    }
                                )
                            )
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = {
                                onClickActionType(ActionContentsType.TRANSLATE)
                            }),
                        painter = painterResource(id = R.drawable.icon_org_player),
                        contentScale = ContentScale.Fit,
                        contentDescription = "TRANSLATE Icon"
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 26
                                else -> 60
                            })
                        )
                )
            }
            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 75)
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(
                            pixel = when(configuration.orientation)
                            {
                                Configuration.ORIENTATION_PORTRAIT -> 120
                                else -> 150
                            }
                        )
                    ),
                horizontalArrangement = Arrangement.Center
            ) {   
                Box(
                    modifier = Modifier
                        .width(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 417
                                    else -> 497
                                }
                            )
                        )
                        .height(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 120
                                    else -> 150
                                }
                            )
                        )
                        .border(
                            width = getDp(pixel = 2),
                            color = colorResource(id = R.color.color_ffffff),
                            shape = RoundedCornerShape(
                                getDp(pixel = 80)
                            )
                        )
                        .clip(
                            RoundedCornerShape(
                                getDp(pixel = 80)
                            )
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = onClickReplay
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 51
                                        else -> 69
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 51
                                        else -> 69
                                    }
                                )
                            )
                            .offset(
                                x = getDp(pixel = 75)
                            ),
                        painter = painterResource(id = R.drawable.btn_icon_again),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Replay Icon"
                    )
                    Text(
                        modifier = Modifier
                            .offset(
                                x = getDp(pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 185
                                    else -> 225
                                })
                            ),
                        text = stringResource(id = R.string.text_replay),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_d6d6d6),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 20)
                        )
                )

                if(data.isNextButtonVisible)
                {
                    Box(
                        modifier = Modifier
                            .width(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 417
                                        else -> 497
                                    }
                                )
                            )
                            .height(
                                getDp(
                                    pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 120
                                        else -> 150
                                    }
                                )
                            )
                            .border(
                                width = getDp(pixel = 2),
                                color = colorResource(id = R.color.color_ffffff),
                                shape = RoundedCornerShape(
                                    getDp(pixel = 80)
                                )
                            )
                            .clip(
                                RoundedCornerShape(
                                    getDp(pixel = 80)
                                )
                            )
                            .clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null, onClick = onClickClose
                            ),
                        contentAlignment = Alignment.CenterStart
                    )
                    {
                        Image(
                            modifier = Modifier
                                .width(
                                    getDp(
                                        pixel = when(configuration.orientation)
                                        {
                                            Configuration.ORIENTATION_PORTRAIT -> 51
                                            else -> 69
                                        }
                                    )
                                )
                                .height(
                                    getDp(
                                        pixel = when(configuration.orientation)
                                        {
                                            Configuration.ORIENTATION_PORTRAIT -> 51
                                            else -> 69
                                        }
                                    )
                                )
                                .offset(
                                    x = getDp(pixel = 75)
                                ),
                            painter = painterResource(id = R.drawable.btn_icon_next_end),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Next Icon"
                        )
                        Text(
                            modifier = Modifier
                                .offset(
                                    x = getDp(pixel = when(configuration.orientation)
                                    {
                                        Configuration.ORIENTATION_PORTRAIT -> 185
                                        else -> 225
                                    })
                                ),
                            text = stringResource(id = R.string.text_next_video),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_d6d6d6),
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