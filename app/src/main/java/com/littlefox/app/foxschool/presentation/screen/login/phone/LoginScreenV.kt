package com.littlefox.app.foxschool.presentation.screen.login.phone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.login.UserLoginData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.login.LoginAction
import com.littlefox.app.foxschool.presentation.mvi.login.viewmodel.LoginViewModel
import com.littlefox.app.foxschool.presentation.widget.BlueRoundButton
import com.littlefox.app.foxschool.presentation.widget.PrefixIconTextFieldLayout
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.logmonitor.Log



@Composable
fun LoginScreenV(
    viewModel: LoginViewModel,
    onAction: (LoginAction) -> Unit
)
{
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val _schoolText = remember {
        mutableStateOf("")
    }
    val _idText = remember {
        mutableStateOf("")
    }
    val _passwordText = remember {
        mutableStateOf("")
    }
    val _selectSchoolCode = remember {
        mutableStateOf("")
    }
    val _isAutoLoginCheck = remember {
        mutableStateOf(false)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_edeef2)
            )
            .addFocusCleaner(focusManager = focusManager),
        contentAlignment = Alignment.TopCenter

    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            TopBarCloseLayout(
                title = stringResource(id = R.string.text_login),
                backgroundColor = colorResource(id = R.color.color_23cc8a))
            {
                Log.i("Close Button Click")

            }

            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 50)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 1020)
                    )
                    .height(
                        getDp(pixel = 1000)
                    )
                    .border(
                        width = getDp(pixel = 2),
                        color = colorResource(id = R.color.color_999999),
                        shape = RoundedCornerShape(
                            getDp(pixel = 20)
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            getDp(pixel = 20)
                        )
                    )
                    .background(
                        color = colorResource(id = R.color.color_ffffff)
                    )
                    .padding(
                        horizontal = getDp(pixel = 60)
                    )
            )
            {
                Column {

                    ConstraintLayout (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                getDp(pixel = 650)
                            )
                    )
                    {
                        val (loginTextField, spacer, checkBox) = createRefs()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .constrainAs(loginTextField) {
                                    top.linkTo(
                                        parent.top
                                    )
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            Spacer(
                                modifier = Modifier
                                    .height(
                                        getDp(pixel = 80)
                                    )
                            )

                            PrefixIconTextFieldLayout(
                                text = _schoolText.value,
                                icon = painterResource(id = R.drawable.icon_search_2),
                                hintText = stringResource(id = R.string.text_school_search),
                                isBottomRounded = state.schoolList.isEmpty(),
                                width = 888,
                                height = 120)
                            {
                                _schoolText.value = it
                                onAction(
                                    LoginAction.InputSchoolNameChanged(it)
                                )
                            }

                            Spacer(
                                modifier = Modifier
                                    .height(
                                        getDp(pixel = 30)
                                    )
                            )


                            PrefixIconTextFieldLayout(
                                text = _idText.value,
                                icon = painterResource(id = R.drawable.icon_id),
                                hintText = stringResource(id = R.string.text_id_input),
                                width = 888,
                                height = 120)
                            {
                                _idText.value = it
                            }

                            Spacer(
                                modifier = Modifier
                                    .height(
                                        getDp(pixel = 30)
                                    )
                            )

                            PrefixIconTextFieldLayout(
                                text = _passwordText.value,
                                icon = painterResource(id = R.drawable.icon_lock),
                                hintText = stringResource(id = R.string.text_password_input),
                                visualTransformation = PasswordVisualTransformation(),
                                width = 888,
                                height = 120)
                            {
                                _passwordText.value = it
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(
                                    getDp(pixel = 20)
                                )
                                .constrainAs(spacer) {
                                    top.linkTo(
                                        loginTextField.bottom
                                    )

                                }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    getDp(pixel = 100)
                                )
                                .constrainAs(checkBox) {
                                    top.linkTo(
                                        spacer.bottom
                                    )
                                },
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Image(
                                painter = if(_isAutoLoginCheck.value)
                                {
                                    painterResource(id = R.drawable.radio_on)
                                }
                                else
                                {
                                    painterResource(id = R.drawable.radio_off)
                                },
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 100)
                                    )
                                    .height(
                                        getDp(pixel = 100)
                                    )
                                    .clickable(interactionSource = remember {
                                        MutableInteractionSource()
                                    }, indication = null, // 클릭 시 효과 제거
                                        onClick = {
                                            _isAutoLoginCheck.value = !_isAutoLoginCheck.value
                                            onAction(
                                                LoginAction.CheckAutoLogin(
                                                    _isAutoLoginCheck.value
                                                )
                                            )
                                        }),
                                contentScale = ContentScale.Inside,
                                contentDescription = "Auto Login CheckBox"

                            )

                            Spacer(
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 20)
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .width(
                                        getDp(pixel = 300)
                                    )
                                    .height(
                                        getDp(pixel = 100)
                                    )
                            )
                            {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    text = stringResource(id = R.string.text_auto_login),
                                    style = TextStyle(
                                        color = colorResource(id = R.color.color_444444),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(
                                            Font(
                                                resId = R.font.roboto_medium
                                            )
                                        ),
                                        textAlign = TextAlign.Start
                                    )
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier
                            .height(
                                getDp(pixel = 40)
                            )
                    )

                    BlueRoundButton(
                        text = stringResource(id = R.string.text_login),
                        modifier = Modifier
                            .width(
                                getDp(pixel = 880)
                            )
                            .height(
                                getDp(pixel = 120)
                            )
                    )
                    {
                        Log.i("Login Button Click")
                        focusManager.clearFocus()
                        onAction(
                            LoginAction.ClickLogin(
                                UserLoginData(
                                    userId = _idText.value,
                                    password = _passwordText.value,
                                    schoolCode = _selectSchoolCode.value
                                )
                            )
                        )

                    }

                    Spacer(
                        modifier = Modifier
                            .height(
                                getDp(pixel = 40)
                            )
                    )
                    BuildFindsLayout(onAction)
                }
                if(state.schoolList.isNotEmpty())
                {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = getDp(pixel = 6), y = getDp(pixel = 200)
                            )
                            .width(
                                getDp(pixel = 888)
                            )
                            .height(
                                getDp(pixel = 400)
                            )
                            .background(
                                color = colorResource(id = R.color.color_ffffff)
                            )
                            .border(
                                width = getDp(pixel = 2),
                                color = colorResource(id = R.color.color_999999),
                                shape = RoundedCornerShape(
                                    bottomStart = getDp(pixel = 20), bottomEnd = getDp(pixel = 20)
                                )
                            )
                            .clip(
                                RoundedCornerShape(
                                    getDp(pixel = 20)
                                )
                            )
                            

                    )
                    {
                        LazyColumn(
                            modifier = Modifier
                                .padding(
                                    top = getDp(pixel = 30),
                                    start = getDp(pixel = 110)
                                )
                        )
                        {
                            items(state.schoolList) {item ->
                                Text(
                                    text = item.getSchoolName(),
                                    modifier = Modifier
                                        .clickable {
                                            onAction(
                                                LoginAction.SelectSchoolName
                                            )
                                            _schoolText.value = item.getSchoolName()
                                            _selectSchoolCode.value = item.getSchoolID()
                                        },
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
            }

        }
        BuildBottomInformationLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }

}


@Composable
private fun BuildFindsLayout(
    onAction : (LoginAction) -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 240)
                )
                .height(
                    getDp(pixel = 90)
                )
                .clickable(interactionSource = remember { //
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onAction(
                        LoginAction.ClickFindID
                    )
                })
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = stringResource(id = R.string.text_find_login),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_regular
                        )
                    ),
                    fontSize = 12.sp,
                ),
                textAlign = TextAlign.Start
            )
        }

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 1)
                )
                .height(
                    getDp(pixel = 40)
                )
                .background(
                    color = colorResource(id = R.color.color_444444)
                )
        )

        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 80)
                )
        )

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 200)
                )
                .height(
                    getDp(pixel = 90)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource ()
                },
                    indication = null,
                    onClick = {
                        onAction(
                            LoginAction.ClickFindPassword
                        )
                })
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = stringResource(id = R.string.text_find_passoword),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_regular
                        )
                    ),
                    fontSize = 12.sp,
                ),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun BuildBottomInformationLayout(
    modifier : Modifier = Modifier
)
{
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 254)
            )
            .padding(
                bottom = getDp(pixel = 40)
            ),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = stringResource(id = R.string.message_sign_only_web),
            modifier = Modifier
                .width(
                    getDp(pixel = 1020)
                )
                .height(
                    getDp(pixel = 50)
                ),
            style = TextStyle(
                color = colorResource(id = R.color.color_000000),
                fontSize = 13.sp,
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                ),
                textAlign = TextAlign.Center
            ),
        )

        Spacer(
            modifier = Modifier
                .height(
                    getDp(pixel = 50)
                )
        )

        Text(
            text = stringResource(id = R.string.message_company_information_from_login),
            modifier = Modifier
                .width(
                    getDp(pixel = 1020)
                )
                .height(
                    getDp(pixel = 120)
                ),
            style = TextStyle(
                color = colorResource(id = R.color.color_666666),
                fontSize = 13.sp,
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                ),
                textAlign = TextAlign.Center
            ),
        )

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