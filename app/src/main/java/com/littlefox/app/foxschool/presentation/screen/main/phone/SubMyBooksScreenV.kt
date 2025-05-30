package com.littlefox.app.foxschool.presentation.screen.main.phone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.BookType
import com.littlefox.app.foxschool.enumerate.SwitchButtonType
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.main.MyVocabularyResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.main.MainAction
import com.littlefox.app.foxschool.presentation.mvi.main.MainState
import com.littlefox.app.foxschool.presentation.widget.SwitchTextButton
import com.littlefox.logmonitor.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubMyBooksScreenV(
    state : MainState,
    onAction: (MainAction) -> Unit,
    scrollBehavior : TopAppBarScrollBehavior
)
{

    var _bookType by remember {
        mutableStateOf(BookType.BOOKSHELF)
    }
    var _currentListSize by remember {
        mutableStateOf(0)
    }
    val _bookshelfItemList = remember(state.myBooksData) {
        derivedStateOf {
            state.myBooksData.getBookShelvesList()
        }
    }
    val _vocabularyItemList = remember(state.myBooksData) {
        derivedStateOf {
            state.myBooksData.getVocabulariesList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentAlignment = Alignment.TopCenter
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 188)
                    )
                    .background(
                        color = colorResource(id = R.color.color_f5f5f5)
                    )
            ){
                SwitchTextButton(
                    firstText = stringResource(id = R.string.text_bookshelf),
                    secondText = stringResource(id = R.string.text_vocabulary)
                ) {switchButtonType ->

                    if(switchButtonType == SwitchButtonType.FIRST_ITEM)
                    {
                        _bookType = BookType.BOOKSHELF
                        _currentListSize = _bookshelfItemList.value.size
                    } else
                    {
                        _bookType = BookType.VOCABULARY
                        _currentListSize = _vocabularyItemList.value.size
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 2)
                    )
                    .background(color = colorResource(id = R.color.color_dbdada))
            )
            
            if(_bookType == BookType.BOOKSHELF)
            {
                Log.i("size : ${_bookshelfItemList.value.size}")

                if(_bookshelfItemList.value.isNotEmpty())
                {
                    LazyColumn{
                        itemsIndexed(_bookshelfItemList.value, key= {_, item -> item.getID()}){index, item ->
                            BuildBookshelfItem(
                                data = item,
                                onItemClick = {
                                    onAction(
                                        MainAction.EnterBookshelfList(index)
                                    )
                                },
                                onOptionClick = {
                                    onAction(
                                        MainAction.SettingBookshelf(item)
                                    )
                                }
                            )
                        }
                    }

                    if(_currentListSize < Common.MAX_BOOKSHELF_SIZE)
                    {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    getDp(pixel = 240)
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Image(
                                    modifier = Modifier
                                        .width(
                                            getDp(pixel = 87)
                                        )
                                        .height(
                                            getDp(pixel = 87)
                                        )
                                        .clickable(interactionSource = remember {
                                            MutableInteractionSource()
                                        }, indication = null, onClick = {
                                            onAction(MainAction.AddBookshelf)
                                        }),
                                    painter = painterResource(id = R.drawable.btn_add),
                                    contentScale = ContentScale.Fit,
                                    contentDescription = "추가 버튼"
                                )
                                Spacer(
                                    modifier = Modifier
                                        .height(
                                            getDp(pixel = 20)
                                        )
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            getDp(pixel = 70)
                                        ),
                                    text = stringResource(id = R.string.text_add_bookshelf),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        color = colorResource(id = R.color.color_b7b7b7),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(
                                            Font(
                                                resId = R.font.roboto_regular
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }
                }

            }
            else
            {
                if(_vocabularyItemList.value.isNotEmpty())
                {
                    LazyColumn{
                        itemsIndexed(_vocabularyItemList.value, key= {_, item -> item.getID()}){index, item ->
                            BuildMyVocabularyShelfItem(
                                data = item,
                                onItemClick = {
                                    onAction(
                                        MainAction.EnterVocabularyList(index)
                                    )
                                },
                                onOptionClick = {
                                    onAction(
                                        MainAction.SettingVocabulary(item)
                                    )
                                })
                        }
                    }

                    if(_currentListSize < Common.MAX_VOCABULARY_SIZE)
                    {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    getDp(pixel = 240)
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Image(
                                    modifier = Modifier
                                        .width(
                                            getDp(pixel = 87)
                                        )
                                        .height(
                                            getDp(pixel = 87)
                                        )
                                        .clickable(interactionSource = remember {
                                            MutableInteractionSource()
                                        }, indication = null, onClick = {
                                            onAction(MainAction.AddVocabulary)
                                        }),
                                    painter = painterResource(id = R.drawable.btn_add),
                                    contentScale = ContentScale.Fit,
                                    contentDescription = "추가 버튼"
                                )
                                Spacer(
                                    modifier = Modifier
                                        .height(
                                            getDp(pixel = 20)
                                        )
                                )
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(
                                            getDp(pixel = 70)
                                        ),
                                    text = stringResource(id = R.string.text_add_bookshelf),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        color = colorResource(id = R.color.color_b7b7b7),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(
                                            Font(
                                                resId = R.font.roboto_regular
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
    }
}



@Composable
fun BuildBookshelfItem(
    data: MyBookshelfResult,
    onItemClick: () -> Unit,
    onOptionClick: () -> Unit
)
{
    val context = LocalContext.current
    val color = CommonUtils.getInstance(context).getBookColorType(data.getColor())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize().height(
                    getDp(pixel = 170)
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()},
                    indication = null,
                    onClick = onItemClick
                ),
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start) {
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 95)
                        )
                )

                Box (
                    modifier = Modifier
                        .width(
                            getDp(pixel = 94)
                        )
                        .height(
                            getDp(pixel = 106)
                        ),
                    contentAlignment = Alignment.Center)
                {
                    Image(
                        painter = painterResource(
                            id = CommonUtils.getInstance(context).getBookResource(color)
                        ),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 94)
                            )
                            .height(
                                getDp(pixel = 106)
                            ),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Books Image"
                    )

                    Image(
                        painter = painterResource(id = R.drawable.icon_bookshelf),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 54)
                            )
                            .height(
                                getDp(pixel = 54)
                            )
                            .padding(
                                bottom = getDp(pixel = 15)
                            ),
                        contentScale = ContentScale.None,
                        contentDescription = "Star Image")
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 40)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 660)
                        )
                        .height(
                            getDp(pixel = 170)
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text(
                        text = "${data.getName()} (${data.getContentsCount()})",
                        style = TextStyle(
                            color = colorResource(id = R.color.color_444444),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_regular
                                )
                            )
                        )
                    )
                }

            }

            Box(
                modifier = Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(
                        end = getDp(pixel = 95)
                    )
                    .clickable(
                        interactionSource = remember { //
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onOptionClick
                    ),
                contentAlignment = Alignment.Center
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.icon_setting_g),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 63)
                        )
                        .height(
                            getDp(pixel = 63)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Setting Icon")
            }
        }
        
        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 1024)
                )
                .height(
                    getDp(pixel = 2)
                )
                .background(color = colorResource(id = R.color.color_dbdada))
        )
    }
    
    
}

@Composable
fun BuildMyVocabularyShelfItem(
    data: MyVocabularyResult,
    onItemClick: () -> Unit,
    onOptionClick: () -> Unit
)
{
    val context = LocalContext.current
    val color = CommonUtils.getInstance(context).getBookColorType(data.getColor())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .height(
                    getDp(pixel = 170)
                )
                .clickable(interactionSource = remember { //
                    MutableInteractionSource()
                },
                    indication = null,
                    onClick = onItemClick
                ),
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start) {
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 95)
                        )
                )

                Box (
                    modifier = Modifier
                        .width(
                            getDp(pixel = 94)
                        )
                        .height(
                            getDp(pixel = 106)
                        ),
                    contentAlignment = Alignment.Center)
                {
                    Image(
                        painter = painterResource(
                            id = CommonUtils.getInstance(context).getBookResource(color)
                        ),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 94)
                            )
                            .height(
                                getDp(pixel = 106)
                            ),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Books Image"
                    )

                    Image(
                        painter = painterResource(id = R.drawable.icon_voca),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 54)
                            )
                            .height(
                                getDp(pixel = 54)
                            )
                            .padding(
                                bottom = getDp(pixel = 15)
                            ),
                        contentScale = ContentScale.None,
                        contentDescription = "Voca Image")
                }

                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 40)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 660)
                        )
                        .height(
                            getDp(pixel = 170)
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text(
                        text = "${data.getName()} (${data.getWordCount()})",
                        style = TextStyle(
                            color = colorResource(id = R.color.color_444444),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_regular
                                )
                            )
                        )
                    )
                }

            }

            Box(
                modifier = Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(
                        end = getDp(pixel = 95)
                    )
                    .clickable(
                        interactionSource = remember { //
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onOptionClick
                    ),
                contentAlignment = Alignment.Center
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.icon_setting_g),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 63)
                        )
                        .height(
                            getDp(pixel = 63)
                        ),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Setting Icon")
            }
        }

        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 1024)
                )
                .height(
                    getDp(pixel = 2)
                )
                .background(color = colorResource(id = R.color.color_dbdada))
        )
    }
}

