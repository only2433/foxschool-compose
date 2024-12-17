package com.littlefox.app.foxschool.presentation.widget

import VocabularySelectData
import android.content.res.Configuration
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.HtmlText
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.ViewPropertyTransition
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.common.CommonUtils

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.logmonitor.Log
import de.charlex.compose.material.HtmlText




@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SeriesGridViewItem(
    modifier : Modifier = Modifier,
    data: SeriesInformationResult,
    isVisibleLevel: Boolean = true,
    onItemClick: () -> Unit
)
{
    val context = LocalContext.current

    Box(
        modifier = modifier
            .width(
                getDp(pixel = 502)
            )
            .height(
                getDp(pixel = 374)
            )
            .background(
                color = colorResource(id = R.color.color_edeef2)
            )
    )
    {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 282)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onItemClick
                    )
            )
            {

                GlideImage(
                    model = data.getThumbnailUrl(),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Thumbnail Image",
                    requestBuilderTransform = { requestBuilder ->
                        requestBuilder.transition(DrawableTransitionOptions.withCrossFade(Common.DURATION_NORMAL.toInt()))
                    }
                )


                if(isVisibleLevel)
                {
                    Image(
                        painter = painterResource(id = CommonUtils.getInstance(context).getLevelResource(data.getLevel())),
                        contentDescription = "Level Icon Image",
                        modifier = Modifier
                            .width(
                                getDp(pixel = 65)
                            )
                            .height(
                                getDp(pixel = 55)
                            )
                            .offset(
                                y = getDp(pixel = 227)
                            )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 78)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "${data.getContentsCount()}편",
                    style = TextStyle(
                        color = colorResource(id = R.color.color_333333),

                        fontSize = 12.sp, fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_medium
                            )
                        )
                    )
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun BuildVocabularyListItem(
    data: VocabularyDataResult,
    type: VocabularySelectData,
    backgroundColor: Color,
    onPlayItem: () -> Unit,
    onSelectItem: () -> Unit
)
{
    var titleText by remember {
        mutableStateOf("")
    }
    var contentText by remember {
        mutableStateOf("")
    }



    LaunchedEffect(type) {
        titleText = if(type.isSelectedWord)
        {
            data.getWordText()
        } else
        {
            ""
        }

        contentText = when {
            type.isSelectedMeaning && type.isSelectedExample ->{
                data.getMeaningText() + "<br>" + data.getExampleText()
            }
            type.isSelectedMeaning -> {
                data.getMeaningText()
            }
            type.isSelectedExample -> {
                data.getExampleText()
            }
            else -> ""
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(
                getDp(pixel = 134 + data.getContentViewSize())
            )
            .background(color = backgroundColor)
            .border(
                width = getDp(pixel = 1),
                color = colorResource(id = R.color.color_a0a0a0),
                shape = RoundedCornerShape(
                    getDp(pixel = 10)
                )
            )
            .clip(
                shape = RoundedCornerShape(getDp(pixel = 10))
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = onSelectItem
            )
    )
    {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 132)
                    )
            ){
                Box(
                    modifier = Modifier
                        .width(

                            getDp(pixel = 840)
                        )
                        .height(
                            getDp(pixel = 132)
                        )
                        .padding(
                            start = getDp(pixel = 70)
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    HtmlText(
                        text = titleText,
                        style = TextStyle(
                            color = colorResource(id = R.color.color_2e3192),
                            fontSize = 20.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_bold
                                )
                            ),
                            
                        )
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 140)
                        )
                        .height(
                            getDp(pixel = 132)
                        )
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            }, indication = null, onClick = onPlayItem
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 60)
                            )
                            .height(
                                getDp(pixel = 60)
                            ),
                        painter = painterResource(id = R.drawable.icon_sound),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Speak Icon"
                    )
                }
                
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = 2)
                    )
                    .background(
                        color = colorResource(id = R.color.color_a0a0a0)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getDp(pixel = data.getContentViewSize())
                    )
                    .padding(
                        start = getDp(pixel = 40), end = getDp(pixel = 40)
                    ),
                contentAlignment = Alignment.CenterStart
            )
            {
                HtmlText(
                    text = contentText,
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontSize = 14.sp,
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BuildContentsListItem(
    modifier : Modifier = Modifier,
    data: ContentsBaseResult,
    itemIndexColor : String = "",
    onBackgroundClick: () -> Unit,
    onThumbnailClick: () -> Unit,
    onOptionClick: () -> Unit
)
{
    var indexColor: Color = Color.LightGray;
    if(itemIndexColor != "")
    {
        indexColor = Color(android.graphics.Color.parseColor(itemIndexColor))
    }

    var backgroundColor by remember {
        mutableStateOf(R.color.color_ffffff)
    }

    LaunchedEffect(data.isSelected) {
        backgroundColor = if (data.isSelected) {
            R.color.color_fff55a
        } else {
            R.color.color_ffffff
        }
    }


    Box(
        modifier = modifier
            .width(
                getDp(pixel = 1080)
            )
            .height(
                getDp(pixel = 244)
            )
            .clip(
                shape = RoundedCornerShape(getDp(pixel = 10))
            )
            .background(color = colorResource(id = backgroundColor))
            .border(
                width = getDp(pixel = 1),
                color = colorResource(id = R.color.color_a0a0a0),
                shape = RoundedCornerShape(
                    getDp(pixel = 10)
                )
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = onBackgroundClick
            ),
        contentAlignment = Alignment.CenterStart
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    getDp(pixel = 244)
                ),
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 28)
                    )
            )
            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 324)
                    )
                    .height(
                        getDp(pixel = 192)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onThumbnailClick
                    ),
            ) {
                GlideImage(
                    model = data.thumbnail_url,
                    modifier = Modifier
                        .width(
                            getDp(pixel = 324)
                        )
                        .height(
                            getDp(pixel = 192)
                        ),
                    contentDescription = "Thumbnail Image",
                    contentScale = ContentScale.FillBounds,
                    requestBuilderTransform = { requestBuilder ->
                        requestBuilder.transition(DrawableTransitionOptions.withCrossFade(Common.DURATION_NORMAL.toInt()))
                    }
                )

            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 20)
                    )
            )

            if(itemIndexColor != "")
            {
                Box(
                    modifier = Modifier
                        .width(
                            getDp(pixel = 80)
                        )
                        .height(
                            getDp(pixel = 182)
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        text = if(data.index < 10)  "0${data.index}" else "${data.index}",
                        style = TextStyle(
                            color = indexColor,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_medium
                                )
                            )
                        )
                    )
                }
            }


            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 10)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 450)
                    )
                    .height(
                        getDp(pixel = 182)
                    ),
                contentAlignment = Alignment.CenterStart
            )
            {
                Text(
                    text = data.getContentsName(),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_medium
                            )
                        )
                    ),

                    )
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 5)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 92)
                    )
                    .height(
                        getDp(pixel = 125)
                    )
                    .offset(
                        x = if(itemIndexColor != "")
                        {
                            getDp(pixel = 0)
                        } else
                        {
                            getDp(pixel = 80)
                        }
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onOptionClick
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learning),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 92)
                        )
                        .height(
                            getDp(pixel = 125)
                        ),
                    contentScale = ContentScale.Inside,
                    contentDescription = "Option Icon"
                )
            }
        }
    }
}

@Composable
fun BuildPagingContentsListItem(
    data: ContentBasePagingResult,
    modifier: Modifier = Modifier,
    onThumbnailClick: () -> Unit,
    onOptionClick: () -> Unit
)
{

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 244)
            )
            .background(colorResource(id = R.color.color_ffffff))
            .border(
                width = getDp(pixel = 2),
                color = colorResource(id = R.color.color_999999),
                shape = RoundedCornerShape(
                    getDp(pixel = 10)
                )
            )
            .clip(
                shape = RoundedCornerShape(getDp(pixel = 10))
            ),
        contentAlignment = Alignment.CenterStart
    )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 28)
                    )
            )
            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 324)
                    )
                    .height(
                        getDp(pixel = 192)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onThumbnailClick
                    ),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(data.thumbnail_url),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 324)
                        )
                        .height(
                            getDp(pixel = 192)
                        ),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "Thumbnail Image",
                )
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 20)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 80)
                    )
                    .height(
                        getDp(pixel = 182)
                    ),
                contentAlignment = Alignment.Center
            )
            {

            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 10)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 450)
                    )
                    .height(
                        getDp(pixel = 182)
                    ),
                contentAlignment = Alignment.CenterStart
            )
            {
                Text(
                    text = data.getContentsName(),
                    style = TextStyle(
                        color = colorResource(id = R.color.color_444444),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(
                            Font(
                                resId = R.font.roboto_medium
                            )
                        )
                    ),

                    )
            }

            Spacer(
                modifier = Modifier
                    .width(
                        getDp(pixel = 5)
                    )
            )

            Box(
                modifier = Modifier
                    .width(
                        getDp(pixel = 92)
                    )
                    .height(
                        getDp(pixel = 125)
                    )
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = onOptionClick
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learning),
                    modifier = Modifier
                        .width(
                            getDp(pixel = 92)
                        )
                        .height(
                            getDp(pixel = 125)
                        ),
                    contentScale = ContentScale.Inside,
                    contentDescription = "Option Icon"
                )
            }


        }
    }
}

@Composable
fun BuildSpeedListItem(
    index: Int,
    currentSelectIndex: Int,
    speedText: String,
    onSelect: () -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 100)
            )
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = onSelect
            ),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 42)
                )
        )

        Image(
            modifier = Modifier
                .width(
                    getDp(pixel = 54)
                )
                .height(
                    getDp(pixel = 54)
                ),
            painter = painterResource(
                id = when(currentSelectIndex == index)
                {
                    true -> R.drawable.player__speed_select
                    else -> R.drawable.player__speed_select_default
                }),
            contentScale = ContentScale.Fit,
            contentDescription = "Speed Icon"
        )

        Spacer(
            modifier = Modifier
                .width(
                    getDp(pixel = 25)
                )
        )

        Text(
            text = speedText,
            style = TextStyle(
                color = colorResource(id = R.color.color_ffffff),
                fontSize = 14.sp,
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.roboto_medium
                    )
                )
            )
        )

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BuildPlayerListItem(
    data: ContentsBaseResult,
    index: Int,
    currentPlayIndex: Int,
    onSelectItem: () -> Unit
)
{
    val configuration = LocalConfiguration.current
    Box(
        modifier = Modifier
            .width(
                getDp(
                    pixel = when(configuration.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> 1080
                        else -> 654
                    }
                )
            )
            .height(
                getDp(
                    pixel = when(configuration.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> 318
                        else -> 220
                    }
                )
            ),
        contentAlignment = Alignment.TopCenter
    )
    {
        Box(
            modifier = Modifier
                .width(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 1020
                            else -> 614
                        }
                    )
                )
                .height(
                    getDp(
                        pixel = when(configuration.orientation)
                        {
                            Configuration.ORIENTATION_PORTRAIT -> 288
                            else -> 200
                        }
                    )
                )
                .clip(
                    shape = RoundedCornerShape(getDp(pixel = 10))
                )
                .background(
                    color = colorResource(
                        id = when(index == currentPlayIndex)
                        {
                            true -> R.color.color_fff55a
                            false -> R.color.color_ffffff
                        }
                    )
                )
                .border(
                    width = getDp(pixel = 1),
                    color = colorResource(id = R.color.color_a0a0a0),
                    shape = RoundedCornerShape(
                        getDp(pixel = 10)
                    )
                )
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null,
                    onClick = onSelectItem
                )
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    modifier = Modifier
                        .width(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 325
                                    else -> 226
                                }
                            )
                        )
                        .height(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 230
                                    else -> 160
                                }
                            )
                        ),
                    model = data.thumbnail_url,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "Thumb Image",
                    requestBuilderTransform = { requestBuilder ->
                        requestBuilder.transition(DrawableTransitionOptions.withCrossFade(Common.DURATION_NORMAL.toInt()))
                    }
                )

                Box(
                    modifier = Modifier
                        .width(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 500
                                    else -> 322
                                }
                            )
                        )
                        .height(
                            getDp(
                                pixel = when(configuration.orientation)
                                {
                                    Configuration.ORIENTATION_PORTRAIT -> 220
                                    else -> 160
                                }
                            )
                        ),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Text(
                        text = data.getContentsName(),
                        style = TextStyle(
                            color = colorResource(id = R.color.color_444444),
                            fontSize = 13.sp,
                            fontFamily = FontFamily(
                                Font(
                                    resId = R.font.roboto_regular
                                )
                            )
                        )
                    )
                }
                
                if(configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    Image(
                        modifier = Modifier
                            .width(
                                getDp(pixel = 92)
                            )
                            .height(
                                getDp(pixel = 125)
                            ),
                        painter = painterResource(id = R.drawable.icon_learning),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = "Option Icon",
                    )
                }
            }
        }
    }
}



