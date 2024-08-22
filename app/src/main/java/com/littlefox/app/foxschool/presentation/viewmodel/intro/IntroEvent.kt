package com.littlefox.app.foxschool.presentation.viewmodel.intro

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.littlefox.app.foxschool.enumerate.ResultLauncherCode
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class IntroEvent: BaseEvent()
{
    object onActivateEasterEgg: IntroEvent()
    object onDeactivateEasterEgg: IntroEvent()
    object onClickIntroduce: IntroEvent()
    object onClickLogin: IntroEvent()
    object onClickHomeButton: IntroEvent()
    object onClickLasterButton: IntroEvent()
    object onClickKeepButton: IntroEvent()

    data class onActivityResult(val code: ResultLauncherCode, val intent: Intent?): IntroEvent()
    data class onAddResultLaunchers(val launchers: ActivityResultLauncher<Intent?>?): IntroEvent()
    data class onRequestPermissionResult(val requestCode: Int, val permissions: Array<out String>, val gransResults: IntArray): IntroEvent()

    data class onClickChangeButton(val oldPassword: String, val newPassword: String, val confirmPassword: String): IntroEvent()
}