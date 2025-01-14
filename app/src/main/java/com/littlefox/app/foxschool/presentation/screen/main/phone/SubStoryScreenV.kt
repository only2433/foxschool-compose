package com.littlefox.app.foxschool.presentation.screen.main.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.main.MainAction
import com.littlefox.app.foxschool.presentation.mvi.main.MainState
import com.littlefox.app.foxschool.presentation.mvi.main.main.MainViewModel
import com.littlefox.app.foxschool.presentation.widget.SeriesGridViewItem
import com.littlefox.app.foxschool.presentation.widget.SwitchTextButton
import com.littlefox.logmonitor.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubStoryScreenV(
    state : MainState,
    onAction: (MainAction) -> Unit,
    scrollBehavior : TopAppBarScrollBehavior
)
{

    // switchButtonType 상태를 관리
    var _switchButtonType by remember {
        mutableStateOf(SwitchButtonType.FIRST_ITEM)
    }
    // derivedStateOf를 사용하여 dataList를 계산
    val _dataList = remember(state.storyData, _switchButtonType) {
        derivedStateOf {
            if (_switchButtonType == SwitchButtonType.FIRST_ITEM) {
                state.storyData.getContentByLevelToList()
            } else {
                state.storyData.getContentByCategoriesToList()
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

            SwitchTextButton(
                firstText = stringResource(id = R.string.text_levels),
                secondText = stringResource(id = R.string.text_categories)) { type ->
                Log.i("button Click : $_switchButtonType")
                _switchButtonType = type
            }

            if(_dataList.value.isNotEmpty())
            {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize())
                {
                    items(_dataList.value.size){index ->

                        if(index % 2 == 0)
                        {
                            SeriesGridViewItem(
                                modifier = Modifier
                                    .padding(
                                        start = getDp(pixel = 26),
                                        end = getDp(pixel = 12)
                                    ),
                                data = _dataList.value[index])
                            {
                                Log.i("item Click")
                                if(_switchButtonType == SwitchButtonType.FIRST_ITEM)
                                {
                                    onAction(
                                        MainAction.ClickStoryLevelsItem(
                                            _dataList.value[index]
                                        )
                                    )
                                }
                                else
                                {
                                    onAction(
                                        MainAction.ClickStoryCategoriesItem(
                                            _dataList.value[index]
                                        )
                                    )
                                }

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
                                data = _dataList.value[index])
                            {
                                Log.i("Item Click")
                                if(_switchButtonType == SwitchButtonType.FIRST_ITEM)
                                {
                                    onAction(
                                        MainAction.ClickStoryLevelsItem(
                                            _dataList.value[index]
                                        )
                                    )
                                }
                                else
                                {
                                    onAction(
                                        MainAction.ClickStoryCategoriesItem(
                                            _dataList.value[index]
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
}
