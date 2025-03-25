package com.littlefox.app.foxschool.presentation.screen.quiz.phone

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizAction
import com.littlefox.app.foxschool.presentation.widget.BuildQuizTitleView
import com.littlefox.app.foxschool.presentation.widget.PressedTextButton


@Composable
fun QuizPlayPictureScreenV(
    onAction: (QuizAction) -> Unit,
    data: QuizPictureData
)
{
    var isQuestionEnd by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_ffffff)
            )
            .offset(
                y = getDp(pixel = 110)
            )
    )
    {
        Column(
            modifier = Modifier
                .wrapContentHeight()
        ) {

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 2)
                    )
                    .background(
                        color = colorResource(id = R.color.color_ceddec)
                    )
            )

            BuildQuizTitleView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 138)
                    )
                    .background(
                        color = colorResource(id = R.color.color_eff4f6)
                    ),
                index = data.getQuizIndex(),
                title = data.getTitle(),
                onPlaySound = {
                    onAction(
                        QuizAction.ClickQuizPlaySound
                    )
                }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 2)
                    )
                    .background(
                        color = colorResource(id = R.color.color_ceddec)
                    )
            )

            BuildContentsView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 500)
                    ),
                data = data,
                onSelectItem = { position ->

                    if(isQuestionEnd)
                    {
                        return@BuildContentsView
                    }
                    isQuestionEnd = true

                    var result = sendUserSelectPictureInformation(
                        data = data,
                        position
                    )
                    onAction(
                        QuizAction.SelectUserAnswer(result)
                    )
                }
            )

            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = 50)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 110)
                    ),
                contentAlignment = Alignment.Center
            )
            {
                PressedTextButton(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 323)
                        )
                        .height(
                            getDp(pixel = 92)
                        ),
                    normalImageID = R.drawable.btn_quiz_n,
                    pressedImageID = R.drawable.btn_quiz_o,
                    text = stringResource(id = R.string.text_next)
                )
                {
                    if(isQuestionEnd)
                    {
                        onAction(
                            QuizAction.ClickNextQuiz
                        )
                    }
                }
                
            }
        }
    }
}


@Composable
private fun BuildContentsView(
    modifier : Modifier,
    data : QuizPictureData,
    onSelectItem: (Int) -> Unit
)
{
    Row(
        modifier = modifier
            .offset(
                y = getDp(pixel = 60)
            ),
        horizontalArrangement = Arrangement.Center
    )
    {
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 545)
                )
                .height(
                    getDp(pixel = 410)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(0)
                })
        )
        {
            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 545)
                    )
                    .height(
                        getDp(pixel = 410)
                    ),
                bitmap = data.getImageInformationList()[0].getImage().asImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = ""
            )
        }

        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 60)
                )
        )

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 545)
                )
                .height(
                    getDp(pixel = 410)
                )
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    onSelectItem(1)
                })
        )
        {
            Image(
                modifier = Modifier
                    .width(
                        getDp(pixel = 545)
                    )
                    .height(
                        getDp(pixel = 410)
                    ),
                bitmap = data.getImageInformationList()[1].getImage().asImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = ""
            )
        }
    }
}

/**
 * 선택한 이미지 문제 정보를 전달
 * @param isCorrect
 */
private fun sendUserSelectPictureInformation(data : QuizPictureData ,  selectPosition: Int) : QuizUserInteractionData
{
    var questionSequence = ""
    var incorrectIndexString = ""

    for(i in 0 until data.getImageInformationList().size)
    {
        questionSequence += if(i == data.getImageInformationList().size - 1)
        {
            data.getImageInformationList()[i].getIndex().toString()
        } else
        {
            data.getImageInformationList()[i].getIndex().toString() + ","
        }
    }

    if(data.getImageInformationList()[selectPosition].isAnswer())
    {
        return QuizUserInteractionData(
            true,
            questionSequence,
            data.getRecordQuizCorrectIndex(),
            data.getRecordQuizCorrectIndex().toString()
        )
    }
    else
    {
        incorrectIndexString = if (data.getRecordQuizCorrectIndex() == data.getRecordQuizInCorrectIndex())
        {
            data.getRecordQuizInCorrectIndex().toString() + "r"
        } else
        {
            data.getRecordQuizInCorrectIndex().toString()
        }

        return QuizUserInteractionData(
            false,
            questionSequence,
            data.getRecordQuizCorrectIndex(),
            incorrectIndexString
        )
    }
}