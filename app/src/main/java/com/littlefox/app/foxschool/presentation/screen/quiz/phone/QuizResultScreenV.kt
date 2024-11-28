package com.littlefox.app.foxschool.presentation.screen.quiz.phone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.littlefox.app.foxschool.`object`.data.quiz.QuizResultViewData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.CommonUtils
import com.littlefox.app.foxschool.enumerate.Grade
import com.littlefox.app.foxschool.presentation.widget.PressedTextButton
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log

@Composable
fun QuizResultScreenV(
    resultData : String,
    onEvent: (QuizEvent) -> Unit
)
{
    var _quizPlayCount by remember {
        mutableStateOf(0)
    }
    var _quizCorrectCount by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(resultData) {
        if(resultData.equals("") == false)
        {
            val list = resultData.split(":")
            _quizPlayCount = list[0].toInt()
            _quizCorrectCount = list[1].toInt()

            Log.i("------- _quizPlayCount : $_quizPlayCount , _quizCorrectCount : $_quizCorrectCount")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 343)
                    )
            )
            {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            getDp(pixel = 276)
                        ),
                    painter = painterResource(id = R.drawable.img_quiz_result_bg),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "Top Background Image")

                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 774)
                        )
                        .height(
                            getDp(pixel = 322)
                        )
                        .align(
                            Alignment.TopCenter
                        )
                        .offset(
                            y = getDp(pixel = 21)
                        ),
                    painter = painterResource(id = setResultTitleText(
                        quizCount = _quizPlayCount,
                        correctCount = _quizCorrectCount
                    )),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Grade Icon"
                )
            }

            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 50)
                    )
            )

            BuildUserGradeView(
                quizCount = _quizPlayCount,
                correctCount = _quizCorrectCount
            )

            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 100)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            )
            {
                PressedTextButton(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 323)
                        )
                        .height(
                            getDp(pixel = 92)
                        )
                    ,
                    normalImageID = R.drawable.btn_quiz_n,
                    pressedImageID = R.drawable.btn_quiz_o,
                    text = stringResource(id = R.string.text_savescore)
                ) {
                    onEvent(
                        QuizEvent.onClickSaveStudyInformation
                    )
                }

                Spacer(
                    modifier = Modifier.width(
                        getDp(pixel = 20)
                    )
                )

                PressedTextButton(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 323)
                        )
                        .height(
                            getDp(pixel = 92)
                        )
                    ,
                    normalImageID = R.drawable.btn_quiz_n,
                    pressedImageID = R.drawable.btn_quiz_o,
                    text = stringResource(id = R.string.text_tryagain)
                ) {
                    onEvent(
                        QuizEvent.onClickReplay
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildUserGradeView(
    quizCount: Int,
    correctCount: Int
)
{
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 270)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 937)
                )
                .height(
                    getDp(pixel = 127)
                )
                .clip(
                    RoundedCornerShape(
                        getDp(pixel = 10)
                    )
                )
                .background(
                    color = colorResource(id = R.color.color_dbdada)
                )
                .padding(
                    start = getDp(pixel = 69), end = getDp(pixel = 69)
                )

        )
        {
            Row {
                ConstraintLayout (
                    modifier = Modifier
                        .weight(1f)

                ){
                    val (correctIcon, text) = createRefs()

                    Box(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 64)
                            )
                            .height(
                                getDp(pixel = 127)
                            )
                            .constrainAs(correctIcon) {
                                start.linkTo(parent.start)
                            },
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.ic_result_correct),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Correct Icon"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 220)
                            )
                            .height(
                                getDp(pixel = 127)
                            )
                            .constrainAs(text) {
                                start.linkTo(correctIcon.end)
                            }
                            .offset(
                                x = getDp(pixel = 20)
                            ),
                        contentAlignment = Alignment.CenterStart
                    )
                    {
                        Text(
                            text = stringResource(id = R.string.text_correct),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_3b3b3b),
                                fontSize = 15.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 100)
                        )
                        .height(
                            getDp(pixel = 127)
                        ),
                    contentAlignment = Alignment.CenterEnd
                )
                {
                    Text(
                        text = "$correctCount",
                        style = TextStyle(
                            color = colorResource(id = R.color.color_4568d8),
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
        Spacer(
            modifier = Modifier
                .height(
                    getDp(pixel = 16)
                )
        )

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 937)
                )
                .height(
                    getDp(pixel = 127)
                )
                .clip(
                    RoundedCornerShape(
                        getDp(pixel = 10)
                    )
                )
                .background(
                    color = colorResource(id = R.color.color_dbdada)
                )
                .padding(
                    start = getDp(pixel = 69), end = getDp(pixel = 69)
                )

        )
        {
            Row {
                ConstraintLayout (
                    modifier = Modifier
                        .weight(1f)

                ){
                    val (correctIcon, text) = createRefs()

                    Box(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 64)
                            )
                            .height(
                                getDp(pixel = 127)
                            )
                            .constrainAs(correctIcon) {
                                start.linkTo(parent.start)
                            },
                        contentAlignment = Alignment.Center
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.ic_result_incorrect),
                            contentScale = ContentScale.Fit,
                            contentDescription = "Correct Icon"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 220)
                            )
                            .height(
                                getDp(pixel = 127)
                            )
                            .constrainAs(text) {
                                start.linkTo(correctIcon.end)
                            }
                            .offset(
                                x = getDp(pixel = 20)
                            ),
                        contentAlignment = Alignment.CenterStart
                    )
                    {
                        Text(
                            text = stringResource(id = R.string.text_incorrect),
                            style = TextStyle(
                                color = colorResource(id = R.color.color_3b3b3b),
                                fontSize = 15.sp,
                                fontFamily = FontFamily(
                                    Font(
                                        resId = R.font.roboto_medium
                                    )
                                )
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 100)
                        )
                        .height(
                            getDp(pixel = 127)
                        ),
                    contentAlignment = Alignment.CenterEnd
                )
                {
                    Text(
                        text = "${quizCount - correctCount}",
                        style = TextStyle(
                            color = colorResource(id = R.color.color_757575),
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

/** 결과 상단 타이틀 이미지 */
@Composable
private fun setResultTitleText(quizCount : Int, correctCount : Int) : Int
{
    val localContext = LocalContext.current
    return when(CommonUtils.getInstance(localContext).getMyGrade(quizCount, correctCount))
    {
        Grade.EXCELLENT -> R.drawable.img_excellent
        Grade.VERYGOOD -> R.drawable.img_very_good
        Grade.GOODS -> R.drawable.img_good
        Grade.POOL -> R.drawable.img_try_again
    }
}