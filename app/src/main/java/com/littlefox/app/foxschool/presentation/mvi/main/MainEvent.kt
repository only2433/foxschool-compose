package com.littlefox.app.foxschool.presentation.mvi.main

import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.main.MainStoryInformationResult
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class MainEvent: Event
{
    data class NotifyHomeworkMenu(val isUpdate: Boolean): MainEvent()
    data class NotifyNewsMenu(val isUpdate: Boolean): MainEvent()
    data class SettingUserInformation(val data: LoginInformationResult): MainEvent()
    data class NotifyStoryTab(val data: MainStoryInformationResult): MainEvent()
    data class NotifySongTab(val data: List<SeriesInformationResult>): MainEvent()
    data class NotifyMyBooksTab(val data: MainInformationResult): MainEvent()
}