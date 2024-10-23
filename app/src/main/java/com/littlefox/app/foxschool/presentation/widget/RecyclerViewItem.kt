package com.littlefox.app.foxschool.presentation.widget

import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.littlefox.app.foxschool.presentation.common.getDp
import com.littlefox.logmonitor.Log


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


@Composable
fun BuildContentsListItem(
    data: ContentsBaseResult,
    itemColor : String,
    onBackgroundClick: () -> Unit,
    onThumbnailClick: () -> Unit,
    onOptionClick: () -> Unit
)
{
    Log.i("data selected : ${data.isSelected}, text : ${data.getContentsName()}")

    var indexColor: Color;
    try {
        indexColor = Color(android.graphics.Color.parseColor(itemColor))
        // Use the color
    } catch (e: Exception) {
        // Handle invalid color, e.g., use a default color
        indexColor = Color.LightGray
    }

    val backgroundColor = remember {
        mutableStateOf(R.color.color_ffffff)
    }

    LaunchedEffect(data.isSelected) {
        if(data.isSelected)
        {
            backgroundColor.value = R.color.color_fff55a
        }
        else
        {
            backgroundColor.value = R.color.color_ffffff
        }
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 244)
            )
            .background(color = colorResource(id = backgroundColor.value))
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
                },
                indication = null,
                onClick = onBackgroundClick
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


