package com.littlefox.app.foxschool.presentation.common

import android.app.Activity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockScreenOrientation(orientation : Int)
{
    val activity = LocalContext.current as? Activity
    activity?.requestedOrientation = orientation
}