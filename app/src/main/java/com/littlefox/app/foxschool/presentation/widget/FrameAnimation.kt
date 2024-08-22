import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.presentation.common.getDp
import kotlinx.coroutines.delay


@Composable
fun LogoFrameAnimationView(
    modifier : Modifier = Modifier,
) {
    // 애니메이션 이미지 목록
    val animationPath = listOf(
        R.drawable.intro_img01,
        R.drawable.intro_img02,
        R.drawable.intro_img03,
        R.drawable.intro_img04,
        R.drawable.intro_img05
    )

    var currentFrame by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(Unit) {
        while(true)
        {
            delay(70)
            if(currentFrame >= animationPath.size - 1)
            {
                currentFrame = 0
            }
            currentFrame++
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(
                getDp(pixel = 200)
            ),
        contentAlignment = Alignment.Center
        )
    {
        Image(
            painter = painterResource(id = animationPath[currentFrame]),
            contentDescription = "Logo Image",
            modifier = Modifier
                .width(
                    getDp(pixel = 160)
                )
                .height(
                    getDp(pixel = 160)
                )
        )
    }


}

