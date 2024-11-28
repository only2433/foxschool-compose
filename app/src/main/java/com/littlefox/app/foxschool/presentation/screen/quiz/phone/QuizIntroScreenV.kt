import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent
import com.littlefox.logmonitor.Log

@Composable
fun QuizIntroScreenV(
    viewModel: QuizViewModel,
    onEvent: (QuizEvent) -> Unit
) {
    val _titleData by viewModel.titleText.observeAsState(
        initial = Pair("", "")
    )
    val _loadingComplete by viewModel.loadingComplete.observeAsState(initial = false)

    LaunchedEffect(_loadingComplete) {
        Log.i("_loadingComplete : $_loadingComplete")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(
                    y = getDp(pixel = 60)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getDp(pixel = 270)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = _titleData.first,
                        style = TextStyle(
                            color = colorResource(id = R.color.color_3b3b3b),
                            fontSize = 30.sp,
                            fontFamily = FontFamily(
                                Font(resId = R.font.roboto_medium)
                            ),
                            textAlign = TextAlign.Center
                        )
                    )
                    if (_titleData.second != "") {
                        Spacer(
                            modifier = Modifier
                                .height(getDp(pixel = 10))
                        )
                        Text(
                            text = _titleData.second,
                            style = TextStyle(
                                color = colorResource(id = R.color.color_3b3b3b),
                                fontSize = 20.sp,
                                fontFamily = FontFamily(
                                    Font(resId = R.font.roboto_medium)
                                ),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .height(getDp(pixel = 20))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getDp(pixel = 334)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_quiz_main),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Title Image"
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 200)
                )
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = getDp(pixel = 60)
                ),
            contentAlignment = Alignment.Center
        ) {

            AnimatedVisibility(
                visible = _loadingComplete == false,
                enter = fadeIn(),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = Common.DURATION_SHORT.toInt()
                    )
                )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 100)
                        )
                        .height(
                            getDp(pixel = 100)
                        ),
                    color = colorResource(id = R.color.color_1aa3f8)
                )
            }



            AnimatedVisibility(
                visible = _loadingComplete == true,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = Common.DURATION_SHORTEST.toInt(),
                        delayMillis = Common.DURATION_SHORTEST.toInt()
                    )
                ),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 412)
                        )
                        .height(
                            getDp(pixel = 120)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()},
                            indication = null,
                            onClick = {
                                onEvent(
                                    QuizEvent.onClickNextQuiz
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.btn_quiz_start_n),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = "Button Image"
                    )
                    Text(
                        text = stringResource(id = R.string.text_start),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_ffffff),
                            fontSize = 15.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_bold
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}