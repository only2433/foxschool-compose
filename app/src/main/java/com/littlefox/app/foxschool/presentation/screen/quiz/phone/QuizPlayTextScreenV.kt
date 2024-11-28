package com.littlefox.app.foxschool.presentation.screen.quiz.phone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizUserInteractionData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.widget.BuildQuizTitleView
import com.littlefox.app.foxschool.presentation.widget.PressedTextButton

@Composable
fun QuizPlayTextScreenV(
    quizType: String,
    onEvent: (QuizEvent) -> Unit,
    data: QuizTextData
)
{
    var _isQuestionEnd by remember {
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
                title = if(quizType == Common.QUIZ_CODE_PHONICS_SOUND_TEXT)
                {
                    stringResource(id = R.string.message_sound_text_title)
                }
                else
                {
                    data.getTitle()
                },
                enableSound = if(quizType == Common.QUIZ_CODE_TEXT)
                {
                    false
                }
                else
                {
                    true
                },
                onPlaySound = {
                    onEvent(
                        QuizEvent.onClickQuizPlaySound
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
                isSelectEnd = _isQuestionEnd,
                onSelectItem = { position ->

                    val isCorrect = data.getExampleList()[position].isAnswer()
                    if(_isQuestionEnd)
                    {
                        return@BuildContentsView
                    }
                    _isQuestionEnd = true

                    val result = if(quizType == Common.QUIZ_CODE_PHONICS_SOUND_TEXT)
                    {
                        sendUserSelectSoundTextInformation(
                            data = data,
                            isCorrect,
                            selectIndex = position
                        )
                    }
                    else
                    {
                        sendUserSelectTextInformation(
                            data = data,
                            isCorrect,
                            selectIndex = position
                        )
                    }

                    onEvent(
                        QuizEvent.onSelectedUserAnswer(result)
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
                ) {
                    if(_isQuestionEnd)
                    {
                        onEvent(
                            QuizEvent.onClickNextQuiz
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
    data: QuizTextData,
    isSelectEnd: Boolean,
    onSelectItem: (Int) -> Unit
)
{
    val indexListIconList = intArrayOf(
        R.drawable.icon_index_1,
        R.drawable.icon_index_2,
        R.drawable.icon_index_3,
        R.drawable.icon_index_4
    )
    val marginTop = if(data.getExampleList().size <= 3) 20 else 10

    var correctAnswerIndex = 0
    for(index in data.getExampleList().indices)
    {
        if(data.getExampleList()[index].isAnswer())
        {
            correctAnswerIndex = index
        }
    }

    var userSelectIndex by remember {
        mutableStateOf(-1)
    }
    val exampleList by remember {
        mutableStateOf(data.getExampleList())
    }


    LazyColumn(
        modifier = modifier
            .offset(
                y =  getDp(pixel = 50)
            )
    )
    {
        items(exampleList.size){ index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 110)
                    ),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 119)
                        )
                )
                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 50)
                        )
                        .height(
                            getDp(pixel = 50)
                        )
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            if(isSelectEnd == false)
                            {
                                userSelectIndex = index
                                onSelectItem(index)
                            }
                        }),
                    painter = painterResource(
                        id = if(userSelectIndex != -1)
                        {
                            if(userSelectIndex == index)
                            {
                                R.drawable.icon_check_on
                            }
                            else
                            {
                                if(exampleList[index].isAnswer())
                                {
                                    R.drawable.icon_check_answer
                                }
                                else
                                {
                                    R.drawable.icon_check_off
                                }
                            }
                        }
                        else
                        {
                            R.drawable.icon_check_off
                        }
                    ),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "Check Icon"
                )
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 34)
                        )
                )
                Image(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 50)
                        )
                        .height(
                            getDp(pixel = 50)
                        ),
                    painter = painterResource(id = indexListIconList[index]),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "Index Icon")
                Spacer(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 17)
                        )
                )
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 1400)
                        )
                        .height(
                            getDp(pixel = 110)
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text(
                        text = exampleList[index].getExampleText(),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_444444),
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
            
            Spacer(
                modifier = Modifier
                    .height(
                        getDp(pixel = marginTop)
                    )
            )
        }
    }
}

/**
 * 선택한 텍스트 문제 정보를 전달
 * @param data
 * @param isCorrect
 * @param selectIndex
 */
private fun sendUserSelectTextInformation(
    data: QuizTextData,
    isCorrect : Boolean,
    selectIndex : Int) : QuizUserInteractionData
{
    val questionSequence = data.getRecordQuizIndex().toString()
    return QuizUserInteractionData(
        isCorrect,
        questionSequence,
        data.getRecordCorrectIndex(),
        data.getExampleList()[selectIndex].getExampleIndex().toString()
    )
}

/**
 * 선택한 텍스트 문제 정보를 전달
 *
 * @param isCorrect
 * @param selectIndex
 */
private fun sendUserSelectSoundTextInformation(
    data: QuizTextData,
    isCorrect : Boolean,
    selectIndex : Int) : QuizUserInteractionData
{
    var questionSequence = ""
    for(i in 0 until data.getExampleList().size)
    {
        if(i == data.getExampleList().size - 1)
        {
            questionSequence += data.getExampleList()[i]!!.getExampleIndex().toString()
        }
        else
        {
            questionSequence += data.getExampleList()[i]!!.getExampleIndex().toString() + ","
        }
    }

    return QuizUserInteractionData(
        isCorrect,
        questionSequence,
        data.getAnswerDataIndex(),
        data.getExampleList()[selectIndex].getExampleIndex().toString()
    )
}