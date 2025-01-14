package com.littlefox.app.foxschool.presentation.mvi.main

import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainSongInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class MainState(
    val isUpdateHomework: Boolean = false,
    val isUpdateNews: Boolean = false,
    val userInformation: LoginInformationResult = LoginInformationResult(),
    val storyData: MainStoryInformationResult = MainStoryInformationResult(),
    val songData: List<SeriesInformationResult> = listOf(),
    val myBooksData: MainInformationResult = MainInformationResult()
) : State