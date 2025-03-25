package com.littlefox.app.foxschool.presentation.screen.quiz.phone

import QuizIntroScreenV
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.enumerate.QuizAnswerViewType

import com.littlefox.app.foxschool.`object`.data.quiz.QuizPhonicsTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTypeData
import com.littlefox.app.foxschool.presentation.mvi.quiz.QuizAction
import com.littlefox.app.foxschool.presentation.mvi.quiz.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log

@Composable
fun QuizScreenV(
    viewModel : QuizViewModel,
    onAction: (QuizAction) -> Unit
)
{
    val state by viewModel.state.collectAsStateWithLifecycle()

    val _pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {state.viewPageCount}
    )

    LaunchedEffect(state.currentPage) {
        Log.i("_setCurrentPage : $state.currentPage")
        _pagerState.animateScrollToPage(
            page = state.currentPage,
            animationSpec = tween(
                durationMillis = Common.DURATION_NORMAL.toInt(),
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(_pagerState.currentPage) {
        Log.i("currentPage : ${_pagerState.currentPage}")
        onAction(
            QuizAction.PageSelected
        )
    }

    var _isVisibleAnswerView by remember {
        mutableStateOf(false)
    }

    var _isCorrectQuestion by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(state.answerViewType) {
        Log.i("_checkAnswerView View : $state.answerViewType")

        when(state.answerViewType)
        {
            QuizAnswerViewType.SHOW_CORRECT ->
            {
                _isCorrectQuestion = true
                _isVisibleAnswerView = true
            }
            QuizAnswerViewType.SHOW_INCORRECT ->
            {
                _isCorrectQuestion = false
                _isVisibleAnswerView = true
            }
            QuizAnswerViewType.HIDE ->
            {
                _isVisibleAnswerView = false
            }
        }
    }

    var _pictureQuizList by remember {
        mutableStateOf(arrayOf<QuizPictureData>())
    }

    var _textQuizList by remember {
        mutableStateOf(arrayOf<QuizTextData>())
    }

    var _phonicsQuizLista by remember {
        mutableStateOf(arrayOf<QuizPhonicsTextData>())
    }

    LaunchedEffect(state.quizPlayData) {
        when(state.quizPlayData)
        {
            is QuizTypeData.Picture ->
            {
                _pictureQuizList = (state.quizPlayData as QuizTypeData.Picture).list.toTypedArray()
                Log.i("Quiz Picture size: ${_pictureQuizList.size}")
            }
            is QuizTypeData.Text ->
            {
                _textQuizList = (state.quizPlayData as QuizTypeData.Text).list.toTypedArray()
                Log.i("Quiz Text size: ${_textQuizList.size}")
            }
            is QuizTypeData.SoundText ->
            {
                _textQuizList = (state.quizPlayData as QuizTypeData.SoundText).list.toTypedArray()
                Log.i("Quiz Sound Text size: ${_textQuizList.size}")
            }
            is QuizTypeData.Phonics ->
            {
                _phonicsQuizLista = (state.quizPlayData as QuizTypeData.Phonics).list.toTypedArray()
                Log.i("Quiz Phonics Text size: ${_phonicsQuizLista.size}")
            }
            else -> {}
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.color_ffffff)
            )
    )
    {


        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopBarCloseLayout(
                height = 110,
                title = stringResource(id = R.string.title_quiz),
                backgroundColor = colorResource(id = R.color.color_23cc8a))
            {
                viewModel.onBackPressed()
            }
            HorizontalPager(
                state = _pagerState,
                pageSize = PageSize.Fill,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->

                when(page)
                {
                    0 ->
                    {
                        QuizIntroScreenV(
                            state = state,
                            onAction = onAction)

                    }
                    state.viewPageCount - 1 ->
                    {
                        QuizResultScreenV(
                            resultData = state.resultData,
                            onAction = onAction)
                    }
                    else ->
                    {
                        when(state.quizPlayData)
                        {
                            is QuizTypeData.Picture ->
                            {
                                QuizPlayPictureScreenV(
                                    onAction = onAction,
                                    data = _pictureQuizList[page - 1]
                                )
                            }
                            is QuizTypeData.Text ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_TEXT,
                                    onAction = onAction,
                                    data = _textQuizList[page -1])
                            }
                            is QuizTypeData.SoundText ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_SOUND_TEXT,
                                    onAction = onAction,
                                    data = _textQuizList[page -1])
                            }
                            is QuizTypeData.Phonics ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_PHONICS_SOUND_TEXT,
                                    onAction = onAction,
                                    data = _phonicsQuizLista[page -1])
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        if(state.showTaskBox)
        {
            BuildTaskBoxLayout(
                playTime = state.playTime,
                correctCountText = state.answerCorrectText
            )
        }

        if(_isVisibleAnswerView)
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 420)
                    )
                    .align(
                        Alignment.Center
                    ),
                contentAlignment = Alignment.Center
            )
            {
                BuildAnswerAnimationView(
                    _isVisibleAnswerView,
                    _isCorrectQuestion
                )
            }
        }
    }
}

@Composable
private fun BuildTaskBoxLayout(
    playTime: String,
    correctCountText: String
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 110)
            )
            .offset(
                y = getDp(pixel = 110)
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 48)
                )
                .height(
                    getDp(pixel = 48)
                ),
            painter = painterResource(id = R.drawable.icon_time),
            contentScale = ContentScale.Fit,
            contentDescription = "Time Icon"
        )
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 110)
                )
                .height(
                    getDp(pixel = 110)
                )
                .padding(
                    start = getDp(pixel = 10)
                ),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = stringResource(id = R.string.text_time),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                )
            )
        }
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 166)
                )
                .height(
                    getDp(pixel = 110)
                ),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = playTime,
                style = TextStyle(
                    color = colorResource(id = R.color.color_2a4899),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                )
            )
        }


        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 48)
                )
                .height(
                    getDp(pixel = 48)
                ),
            painter = painterResource(id = R.drawable.icon_question),
            contentScale = ContentScale.Fit,
            contentDescription = "Question Icon"
        )

        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 120)
                )
                .height(
                    getDp(pixel = 110)
                )
                .padding(
                    start = getDp(pixel = 10)
                ),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = stringResource(id = R.string.text_score),
                style = TextStyle(
                    color = colorResource(id = R.color.color_444444),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                )
            )
        }
        Box(
            modifier = Modifier
                .width(
                    getDp(pixel = 150)
                )
                .height(
                    getDp(pixel = 110)
                ),
            contentAlignment = Alignment.CenterStart
        )
        {
            Text(
                text = correctCountText,
                style = TextStyle(
                    color = colorResource(id = R.color.color_2a4899),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(
                        Font(
                            resId = R.font.roboto_medium
                        )
                    ),
                )
            )
        }
    }
}

@Composable
private fun BuildAnswerAnimationView(
    isVisible: Boolean,
    isCorrect: Boolean,
)
{
    var rotationValue by remember { 
        mutableStateOf(0f)
    }
    
    var startRotation by remember {
        mutableStateOf(false)
    }
    val rotationAnimation by animateFloatAsState(
        targetValue = rotationValue,
        animationSpec = tween(
            durationMillis = Common.DURATION_LONG.toInt(),
            delayMillis = Common.DURATION_SHORT.toInt(),
            easing = FastOutSlowInEasing
        )
    )

    LaunchedEffect(isVisible) {
        if(isVisible)
        {
            startRotation = true
            rotationValue = 360f
        }
    }
    
    LaunchedEffect(rotationAnimation) {
        if(rotationAnimation == 360f)
        {
            startRotation = false
            rotationValue = 0f
        }
    }

    Box(
        modifier = Modifier
            .width(
                getDp(pixel = 436)
            )
            .height(
                getDp(pixel = 419)
            )
            .graphicsLayer {
                if(startRotation)
                {
                    rotationY = rotationAnimation
                }
            }
    )
    {
        Image(
            painter = if(isCorrect) painterResource(id = R.drawable.img_correct) else painterResource(id = R.drawable.img_incorrect),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Answer Icon"
            
        )
    }
}