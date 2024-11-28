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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.littlefox.app.foxschool.presentation.viewmodel.QuizViewModel
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common

import com.littlefox.app.foxschool.`object`.data.quiz.QuizPhonicsTextData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizPictureData
import com.littlefox.app.foxschool.`object`.data.quiz.QuizTextData
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizEvent
import com.littlefox.app.foxschool.presentation.viewmodel.quiz.QuizTypeData
import com.littlefox.app.foxschool.presentation.widget.TopBarCloseLayout
import com.littlefox.app.foxschool.viewmodel.base.EventWrapper
import com.littlefox.logmonitor.Log

@Composable
fun QuizScreenV(
    viewModel : QuizViewModel,
    onEvent: (BaseEvent) -> Unit
)
{
    val _maxPageCount by viewModel.viewPageCount.observeAsState(initial = 1)
    val _quizTypeData by viewModel.quizPlayList.observeAsState(
        initial = QuizTypeData.Picture(
            ArrayList()
    ))
    val _checkAnswerView by viewModel.checkAnswerView.observeAsState(initial = EventWrapper(false))
    val _hideAnswerView by viewModel.hideAnswerView.observeAsState(EventWrapper(Unit))
    val _setCurrentPage by viewModel.setPageView.observeAsState(initial = 0)
    val _enableTaskBox by viewModel.enableTaskBoxLayout.observeAsState(initial = false)
    val _quizPlayTime by viewModel.showPlayTime.observeAsState(initial = "")
    val _quizUserCount by viewModel.answerCorrectText.observeAsState(initial = "")

    val _quizResultData by viewModel.resultData.observeAsState(
        initial = EventWrapper("")
    )

    var _quizResultScreenData by remember {
        mutableStateOf("")
    }

    _quizResultData.getContentIfNotHandled()?.let {

        Log.i("------- result data : $it")
        LaunchedEffect(_quizResultData) {
            _quizResultScreenData = it
        }

    }


    val _pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {_maxPageCount}
    )

    LaunchedEffect(_setCurrentPage) {
        Log.i("_setCurrentPage : $_setCurrentPage")
        _pagerState.animateScrollToPage(
            page = _setCurrentPage,
            animationSpec = tween(
                durationMillis = Common.DURATION_NORMAL.toInt(),
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(_pagerState.currentPage) {
        Log.i("currentPage : ${_pagerState.currentPage}")
        onEvent(
            QuizEvent.onPageSelected
        )
    }

    var _isVisibleAnswerView by remember {
        mutableStateOf(false)
    }

    var _isCorrectQuestion by remember {
        mutableStateOf(false)
    }

    _checkAnswerView.getContentIfNotHandled()?.let {
        LaunchedEffect(_checkAnswerView) {
            Log.i("_checkAnswerView View : $_checkAnswerView")
            _isCorrectQuestion = it
            _isVisibleAnswerView = true
        }
    }
    _hideAnswerView.getContentIfNotHandled()?.let {
        LaunchedEffect(_hideAnswerView) {
            Log.i("hide View")
            _isVisibleAnswerView = false
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

    LaunchedEffect(_quizTypeData) {
        when(_quizTypeData)
        {
            is QuizTypeData.Picture ->
            {
                _pictureQuizList = (_quizTypeData as QuizTypeData.Picture).list.toTypedArray()
                Log.i("Quiz Picture size: ${_pictureQuizList.size}")
            }
            is QuizTypeData.Text ->
            {
                _textQuizList = (_quizTypeData as QuizTypeData.Text).list.toTypedArray()
                Log.i("Quiz Text size: ${_textQuizList.size}")
            }
            is QuizTypeData.SoundText ->
            {
                _textQuizList = (_quizTypeData as QuizTypeData.SoundText).list.toTypedArray()
                Log.i("Quiz Sound Text size: ${_textQuizList.size}")
            }
            is QuizTypeData.Phonics ->
            {
                _phonicsQuizLista = (_quizTypeData as QuizTypeData.Phonics).list.toTypedArray()
                Log.i("Quiz Phonics Text size: ${_phonicsQuizLista.size}")
            }
        }
    }

    LaunchedEffect(_maxPageCount) {
        Log.i("max Page Count : $_maxPageCount")
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
                backgroundColor = colorResource(id = R.color.color_23cc8a)) {
                onEvent(BaseEvent.onBackPressed)
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
                            viewModel = viewModel,
                            onEvent = onEvent)

                    }
                    _maxPageCount - 1 ->
                    {
                        QuizResultScreenV(
                            resultData = _quizResultScreenData,
                            onEvent = onEvent)
                    }
                    else ->
                    {
                        when(_quizTypeData)
                        {
                            is QuizTypeData.Picture ->
                            {
                                QuizPlayPictureScreenV(
                                    onEvent = onEvent,
                                    data = _pictureQuizList[page - 1]
                                )
                            }
                            is QuizTypeData.Text ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_TEXT,
                                    onEvent = onEvent,
                                    data = _textQuizList[page -1])
                            }
                            is QuizTypeData.SoundText ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_SOUND_TEXT,
                                    onEvent = onEvent,
                                    data = _textQuizList[page -1])
                            }
                            is QuizTypeData.Phonics ->
                            {
                                QuizPlayTextScreenV(
                                    quizType = Common.QUIZ_CODE_PHONICS_SOUND_TEXT,
                                    onEvent = onEvent,
                                    data = _phonicsQuizLista[page -1])
                            }
                        }
                    }
                }
            }
        }

        if(_enableTaskBox)
        {
            BuildTaskBoxLayout(
                playTime = _quizPlayTime,
                correctCount = _quizUserCount
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
    correctCount: String
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
                text = correctCount,
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