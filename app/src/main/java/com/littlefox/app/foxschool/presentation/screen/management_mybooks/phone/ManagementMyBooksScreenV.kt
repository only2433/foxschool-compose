package com.littlefox.app.foxschool.presentation.screen.management_mybooks.phone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.presentation.viewmodel.ManagementMyBooksViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.MyBooksType
import com.littlefox.app.foxschool.`object`.data.bookshelf.ManagementBooksData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.manage_mybooks.ManagementMyBooksEvent
import com.littlefox.app.foxschool.presentation.widget.DeleteIconTextFieldLayout
import com.littlefox.app.foxschool.presentation.widget.LightBlueOutlinedButton
import com.littlefox.app.foxschool.presentation.widget.LightBlueRoundButton
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout

@Composable
fun ManagementMyBooksScreenV(
    viewModel : ManagementMyBooksViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val focusManager = LocalFocusManager.current
    val _managementData by viewModel.managementBooksData.observeAsState(initial = null)
    val _nameText = remember {
        mutableStateOf("")
    }
    var _currentSelectColorIndex = remember {
        mutableStateOf(0)
    }

    LaunchedEffect(_managementData) {
        _managementData?.let {
            _currentSelectColorIndex.value = getBookIndexFromColor(it.getColor())
            _nameText.value = it.getName()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_ffffff)
            )
            .addFocusCleaner(focusManager)
    )
    {
        Column{

            TopBarCloseLayout(
                title = getTitleText(_managementData),
                backgroundColor = colorResource(id = R.color.color_23cc8a)) {
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 114)
                    )
                    .background(color = colorResource(id = R.color.color_edeef2)),
                contentAlignment = Alignment.CenterStart
            )
            {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(
                            x = getDp(pixel = 42)
                        ),
                    text = getMessageText(data = _managementData),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_regular
                            )
                        ),
                        fontSize = 14.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 233)
                    ),
                contentAlignment = Alignment.Center
            )
            {
                DeleteIconTextFieldLayout(
                    text = _nameText.value,
                    hintText = stringResource(id = R.string.message_edit_maximum_text),
                    width = 884,
                    height = 120,
                    onTextChange = { value ->
                        _nameText.value = value
                    },
                    onClickDelete = {
                        _nameText.value = ""
                    }
                )
            }

            BuildBookItemsLayout(
                selectedIndex = _currentSelectColorIndex.value,
                onValueChange = {
                    _currentSelectColorIndex.value = it

                    onEvent(
                        ManagementMyBooksEvent.onSelectBooksItem(
                            getBookColorFromIndex(it)
                        )
                    )
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LightBlueRoundButton(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 880)
                        )
                        .height(
                            getDp(pixel = 120)
                        ), text = stringResource(id = R.string.text_save)
                ) {
                    onEvent(
                        ManagementMyBooksEvent.onSelectSaveButton(_nameText.value)
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 20)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LightBlueOutlinedButton(modifier = Modifier
                    .width(
                        getDp(pixel = 880)
                    )
                    .height(
                        getDp(pixel = 120)
                    ), text = _managementData?.let {
                    when
                    {
                        it.getName().isNotEmpty() -> stringResource(id = R.string.text_delete)
                        else -> stringResource(id = R.string.text_cancel)
                    }
                } ?: "") {
                    onEvent(
                        ManagementMyBooksEvent.onCancelDeleteButton
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildBookItemsLayout(
    selectedIndex : Int,
    onValueChange : (Int) -> Unit
)
{
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 166)
            ),
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center){
            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_01),
                isCheckVisible = selectedIndex == 0
            ){
                onValueChange(0)
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 40)
                    )
            )

            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_02),
                isCheckVisible = selectedIndex == 1
            ){
                onValueChange(1)
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 40)
                    )
            )

            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_03),
                isCheckVisible = selectedIndex == 2
            ){
                onValueChange(2)
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 40)
                    )
            )

            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_04),
                isCheckVisible = selectedIndex == 3
            ){
                onValueChange(3)
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 40)
                    )
            )

            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_05),
                isCheckVisible = selectedIndex == 4
            ){
                onValueChange(4)
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 40)
                    )
            )

            BuildBookIcon(
                icon = painterResource(id = R.drawable.bookshelf_06),
                isCheckVisible = selectedIndex == 5
            ){
                onValueChange(5)
            }

        }
    }
}

@Composable
private fun BuildBookIcon(
    icon: Painter,
    isCheckVisible: Boolean,
    onClickCheck: () -> Unit
)
{
    Box(
        modifier = Modifier
            .width(
                getDp(pixel = 94)
            )
            .height(
                getDp(pixel = 106)
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = onClickCheck
            ),
        contentAlignment = Alignment.Center
    )
    {
        Image(
            painter = icon,
            contentScale = ContentScale.Fit,
            contentDescription = "Red"
        )

        if(isCheckVisible)
        {
            Image(
                modifier = Modifier
                    .padding(
                        start = getDp(pixel = 8),
                        bottom = getDp(pixel = 14)
                    ),
                painter = painterResource(id = R.drawable.icon_select),
                contentScale = ContentScale.Fit,
                contentDescription = "Select Icon"
            )
        }
    }
}

@Composable
private fun getTitleText(data: ManagementBooksData?) : String
{
    data?.let {
        when(data.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD ->  return stringResource(id = R.string.text_add_bookshelf)
            MyBooksType.VOCABULARY_ADD -> return stringResource(id = R.string.text_add_vocabulary)
            MyBooksType.BOOKSHELF_MODIFY -> return stringResource(id = R.string.text_manage_bookshelf)
            MyBooksType.VOCABULARY_MODIFY -> return stringResource(id = R.string.text_manage_vocabulary)
        }
    }
    return  ""
}

@Composable
private fun getMessageText(data: ManagementBooksData?) : String
{
    data?.let {
        when(data.getBooksType())
        {
            MyBooksType.BOOKSHELF_ADD ->  return stringResource(id = R.string.message_maximum_bookshelf)
            MyBooksType.VOCABULARY_ADD -> return stringResource(id = R.string.message_maximum_vocabulary)
            MyBooksType.BOOKSHELF_MODIFY -> return stringResource(id = R.string.message_sorting_manage_bookshelf)
            MyBooksType.VOCABULARY_MODIFY -> return stringResource(id = R.string.message_sorting_manage_vocabulary)
        }
    }
    return ""
}

private fun getBookIndexFromColor(color: String) : Int
{
    return when(color)
    {
        "red" -> 0
        "orange" -> 1
        "green" -> 2
        "blue" -> 3
        "purple" -> 4
        "pink" -> 5
        else -> 0
    }
}

private fun getBookColorFromIndex(index : Int) : String
{
    return when(index)
    {
        0 -> "red"
        1 -> "orange"
        2 -> "green"
        3 -> "blue"
        4 -> "purple"
        5 -> "pink"
        else -> "red"
    }
}

private fun Modifier.addFocusCleaner(focusManager: FocusManager, doOnClear: () -> Unit = {}): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            doOnClear()
            focusManager.clearFocus()
        })
    }
}