import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import com.littlefox.app.foxschool.presentation.screen.main.phone.SubMyBooksScreenV
import com.littlefox.app.foxschool.presentation.screen.main.phone.SubSongScreenV
import com.littlefox.app.foxschool.presentation.screen.main.phone.SubStoryScreenV


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

import com.littlefox.app.foxschool.presentation.viewmodel.MainViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.main.MainEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.data.main.TabItemData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.presentation.widget.DrawerMenuPhone
import com.littlefox.app.foxschool.presentation.widget.TopbarMainLayout
import com.littlefox.logmonitor.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenV(
    viewModel: MainViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val tabs = listOf(
        TabItemData("Story", painterResource(id = R.drawable.gnb_icon02), colorResource(id = R.color.color_23cc8a)),
        TabItemData("Song", painterResource(id = R.drawable.gnb_icon03), colorResource(id = R.color.color_23cc8a)),
        TabItemData("My Books", painterResource(id = R.drawable.gnb_icon04), colorResource(id = R.color.color_23cc8a))
    )

    val scrollBehavior =  TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pagerState = rememberPagerState(pageCount = {3})
    val drawerControllerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if(drawerControllerState.isOpen)
        {
            coroutineScope.launch {
                drawerControllerState.close()
            }
        }
        else
        {
            onEvent(
                BaseEvent.onBackPressed
            )

        }
    }


    ModalNavigationDrawer(
        drawerState = drawerControllerState,

        gesturesEnabled = false,

        drawerContent = {
            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 920)
                    )
                    .background(color = colorResource(id = R.color.color_ffffff))
                    .fillMaxHeight()
            )
            {
                DrawerMenuPhone(
                    userName = "정재현",
                    userSchool = "남원 고등학교") { menu ->

                    coroutineScope.launch {
                        drawerControllerState.close()
                        withContext(Dispatchers.IO)
                        {
                            delay(Common.DURATION_SHORTER)
                        }
                        onEvent(
                            MainEvent.onClickDrawerItem(menu)
                        )
                    }
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                Column {
                    Box {
                        TopbarMainLayout(
                            title = "팍스 스쿨",
                            scrollBehavior = scrollBehavior,
                            onTabMenu = {
                                Log.i("onTabMenu Click")
                                coroutineScope.launch {
                                    drawerControllerState.open()
                                }
                            },
                            onTabSearch = {
                                Log.i("onTabSearch Click")
                            },
                        )
                    }
                    TabRow(selectedTabIndex = pagerState.currentPage,
                        containerColor = colorResource(id = R.color.color_edeef2),
                        indicator = {position ->
                            SecondaryIndicator(
                                Modifier
                                    .tabIndicatorOffset(position[pagerState.currentPage])
                                    .fillMaxWidth()
                                    .height(getDp(pixel = 6)),
                                color = colorResource(id = R.color.color_23cc8a)
                            )
                        }) {
                        tabs.forEachIndexed {index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = index, animationSpec = tween(
                                                durationMillis = Common.DURATION_NORMAL.toInt(),
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if(pagerState.currentPage == index) tab.color else colorResource(
                                            id = R.color.color_444444
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            },
                modifier = Modifier.fillMaxSize(),
                content = { it ->
                HorizontalPager(
                    pageSize = PageSize.Fill,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) { page ->
                    TabContent(page, viewModel, onEvent, scrollBehavior)
                }
            }
            )
        }
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabContent(page: Int, viewModel: MainViewModel, onEvent: (MainEvent) -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    when (page) {
        0 -> SubStoryScreenV(viewModel, onEvent, scrollBehavior)
        1 -> SubSongScreenV(viewModel, onEvent, scrollBehavior)
        2 -> SubMyBooksScreenV(viewModel, onEvent, scrollBehavior)
    }
}






