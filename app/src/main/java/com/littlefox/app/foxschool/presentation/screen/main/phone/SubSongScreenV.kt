package com.littlefox.app.foxschool.presentation.screen.main.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.result.main.MainSongInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.MainViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
import com.littlefox.app.foxschool.presentation.widget.SeriesGridViewItem
import com.littlefox.logmonitor.Log



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubSongScreenV(
    viewModel : MainViewModel,
    onEvent: (MainEvent) -> Unit,
    scrollBehavior : TopAppBarScrollBehavior
)
{
    val viewModelDataList by viewModel.updateSongData.observeAsState(
        initial = emptyList()
    )
    val dataList by remember(viewModelDataList) {
        derivedStateOf {
            viewModelDataList.ifEmpty {
                emptyList()
            }
        }
    }

    Log.i("data update")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    )
    {
        Column {
            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 35)
                    )
            )

            if(dataList.isNotEmpty())
            {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize())
                {
                    items(dataList.size){ index ->

                        if(index % 2 == 0)
                        {
                            SeriesGridViewItem(
                                modifier = Modifier
                                    .padding(
                                        start = getDp(pixel = 26),
                                        end = getDp(pixel = 12)
                                    ),
                                isVisibleLevel = false,
                                data = dataList[index])
                            {
                                Log.i("item Click")
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
                                isVisibleLevel = false,
                                data = dataList[index])
                            {
                                Log.i("item Click")
                            }
                        }
                    }
                }
            }

        }
    }
}