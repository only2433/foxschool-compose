package com.littlefox.app.foxschool.presentation.screen.intro.phone

import LogoFrameAnimationView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.enumerate.IntroViewMode
import com.littlefox.app.foxschool.enumerate.PasswordGuideType
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.IntroViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.intro.IntroEvent
import com.littlefox.app.foxschool.presentation.widget.BlueOutlinedButton
import com.littlefox.app.foxschool.presentation.widget.BlueRoundButton
import com.littlefox.app.foxschool.presentation.widget.IntroProgressBar
import com.littlefox.logmonitor.Log


@Composable
fun IntroScreenV(
    viewModel: IntroViewModel,
    onEvent : (IntroEvent) -> Unit
)
{
    val bottomTypeState by viewModel.bottomType.observeAsState(initial = IntroViewMode.DEFAULT)
    val percentProgressState by viewModel.progressPercent.observeAsState(initial = 0f)


    val context = LocalContext.current
    val density = LocalDensity.current
    val colorStart  = Color(0xFF47E1AD)
    val colorEnd    = Color(0xFF29C8E6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colorStart, colorEnd)
                )
            ),
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 798)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom

        )
        {
            Image(
                painter = painterResource(id = R.drawable.intro_logo),
                contentScale = ContentScale.Fit,
                contentDescription = "Logo",
                modifier = Modifier
                    .width(
                        getDp(pixel = 194)
                    )
                    .height(
                        getDp(pixel = 100)
                    )
            )
            Spacer(modifier = Modifier.height(getDp(pixel = 30)))
            Image(
                painter = painterResource(id = R.drawable.foxschool_logo),
                contentScale = ContentScale.Fit,
                contentDescription = "Text Logo",
                modifier = Modifier
                    .width(
                        getDp(pixel = 620)
                    )
                    .height(
                        getDp(pixel = 100)
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 524)
                )
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            when(bottomTypeState)
            {
                IntroViewMode.SELECT -> {
                    BuildSelectLayout(
                        onEvent
                    )
                }
                IntroViewMode.PROGRESS -> {
                    LogoFrameAnimationView()
                    IntroProgressBar(
                        percent = percentProgressState,
                        width = 888,
                        height = 50,
                        progressColor = colorResource(id = R.color.color_alpha_white))
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun BuildSelectLayout( event : (IntroEvent) -> Unit)
{
    Column( modifier = Modifier
        .fillMaxWidth()
        .height(
            getDp(pixel = 524)
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text(
            text = stringResource(id = R.string.message_intro_foxschool),
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = colorResource(id = R.color.color_ffffff),
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_bold
                    )
                )
            )
        )
        
        Spacer(modifier = Modifier.height(getDp(pixel = 20)))

        BlueOutlinedButton(
            text = stringResource(id = R.string.text_login),
            modifier = Modifier
                .width(
                    getDp(pixel = 788)
                )
                .height(
                    getDp(pixel = 120)
                )
        )
        {
            Log.i("onClickLogin Click")
            event(IntroEvent.onClickLogin)
        }

        Spacer(modifier = Modifier.height(getDp(pixel = 40)))

        BlueRoundButton(
            text = stringResource(id = R.string.text_foxschool_introduce),
            modifier = Modifier
                .width(
                    getDp(pixel = 788)
                )
                .height(
                    getDp(pixel = 120)
                )
        )
        {
            event(IntroEvent.onClickIntroduce)
        }


    }
}

