package com.littlefox.app.foxschool.presentation.screen.main.phone

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.SeriesType
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.MainViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
import com.littlefox.app.foxschool.presentation.widget.SeriesGridViewItem
import com.littlefox.app.foxschool.presentation.widget.SwitchTextButton
import com.littlefox.logmonitor.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubStoryScreenV(
    viewModel : MainViewModel,
    onEvent: (MainEvent) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
)
{
    val mainStoryInformationResult by viewModel.updateStoryData.collectAsStateWithLifecycle(
        initialValue = MainStoryInformationResult()
    )

    // switchButtonType 상태를 관리
    var switchButtonType by remember { mutableStateOf(SwitchButtonType.FIRST_ITEM) }

    // derivedStateOf를 사용하여 dataList를 계산
    val dataList = remember(mainStoryInformationResult, switchButtonType) {
        derivedStateOf {
            if (switchButtonType == SwitchButtonType.FIRST_ITEM) {
                mainStoryInformationResult.getContentByLevelToList()
            } else {
                mainStoryInformationResult.getContentByCategoriesToList()
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var switchButtonType by remember { mutableStateOf(SwitchButtonType.FIRST_ITEM) }
            SwitchTextButton(
                firstText = stringResource(id = R.string.text_levels),
                secondText = stringResource(id = R.string.text_categories)) { type ->
                Log.i("button Click : $switchButtonType")
                switchButtonType = type
            }

            if(dataList.value.isNotEmpty())
            {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize(),

                    ) {

                    items(dataList.value.size){ index ->

                        if(index % 2 == 0)
                        {
                            SeriesGridViewItem(
                                modifier = Modifier
                                    .padding(
                                        start = getDp(pixel = 26),
                                        end = getDp(pixel = 12)
                                    ),
                                data = dataList.value[index])
                            {
                                Log.i("item Click")
                                onEvent(
                                    MainEvent.onClickStoryLevelsItem(
                                        dataList.value[index]
                                    )
                                )
                            }

                        }
                        else
                        {
                            SeriesGridViewItem(
                                modifier = Modifier
                                    .padding(
                                        start = getDp(pixel = 12),
                                        end = getDp(pixel = 26)
                                    ),
                                data = dataList.value[index])
                            {
                                Log.i("Item Click")
                                onEvent(
                                    MainEvent.onClickStoryLevelsItem(
                                        dataList.value[index]
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
