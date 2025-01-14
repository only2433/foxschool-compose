package com.littlefox.app.foxschool.presentation.screen.main.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.main.MainAction
import com.littlefox.app.foxschool.presentation.mvi.main.MainState
import com.littlefox.app.foxschool.presentation.viewmodel.MainViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
import com.littlefox.app.foxschool.presentation.widget.SeriesGridViewItem
import com.littlefox.logmonitor.Log



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubSongScreenV(
    state : MainState,
    onAction: (MainAction) -> Unit,
    scrollBehavior : TopAppBarScrollBehavior
)
{

    val _dataList by remember(state.songData) {
        derivedStateOf {
            state.songData.ifEmpty {
                emptyList()
            }
        }
    }

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

            if(_dataList.isNotEmpty())
            {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize())
                {
                    items(_dataList.size){index ->

                        if(index % 2 == 0)
                        {
                            SeriesGridViewItem(
                                modifier = Modifier
                                    .padding(
                                        start = getDp(pixel = 26),
                                        end = getDp(pixel = 12)
                                    ),
                                isVisibleLevel = false,
                                data = _dataList[index])
                            {
                                Log.i("item Click")
                                onAction(
                                    MainAction.ClickSongCategoriesItem(
                                        _dataList[index]
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
                                isVisibleLevel = false,
                                data = _dataList[index])
                            {
                                Log.i("item Click")
                                onAction(
                                    MainAction.ClickSongCategoriesItem(
                                        _dataList[index]
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