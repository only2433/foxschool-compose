package com.littlefox.app.foxschool.presentation.mvi.player

import androidx.media3.exoplayer.ExoPlayer
import com.littlefox.app.foxschool.enumerate.MovieNavigationStatus
import com.littlefox.app.foxschool.`object`.data.player.PageLineData
import com.littlefox.app.foxschool.`object`.data.player.PlayerEndViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Event


sealed class PlayerEvent : Event
{
    data class EnableMovieLoading(val isLoading: Boolean): PlayerEvent()
    data class SetPlayer(val player: ExoPlayer?): PlayerEvent()
    data class SetTitle(val title: String): PlayerEvent()
    data class NotifyContentsList(val list: ArrayList<ContentsBaseResult>): PlayerEvent()
    data class SetPlayerEndViewData(val data: PlayerEndViewData): PlayerEvent()
    data class UpdateCaptionText(val text: String): PlayerEvent()
    data class UpdateCurrentMovieTime(val time: String): PlayerEvent()
    data class SetMaxMovieTime(val time: String): PlayerEvent()
    data class UpdateCurrentProgress(val progress: Int): PlayerEvent()
    data class SetMaxProgress(val maxProgress: Int): PlayerEvent()
    data class ReadyToPlayMovie(val isReady: Boolean): PlayerEvent()
    data class PlayMovie(val isPlaying: Boolean): PlayerEvent()
    data class ShowPlayerEndView(val isShow: Boolean): PlayerEvent()
    data class UpdateNavigationStatus(val status: MovieNavigationStatus): PlayerEvent()
    data class UpdateCurrentPageIndex(val index: Int): PlayerEvent()
    data class UpdateCurrentPageLineData(val data: PageLineData): PlayerEvent()
    data class UpdateCurrentPlayIndex(val index: Int): PlayerEvent()
    data class UpdateCurrentSpeedIndex(val index: Int): PlayerEvent()
    data class SupportSpeedViewButton(val isEnable: Boolean): PlayerEvent()
    data class SupportMovieOption(val isEnable : Boolean): PlayerEvent()
    data class SupportCaptionAndPage(val isSupport: Boolean): PlayerEvent()
    data class ActivatePageView(val isActivate : Boolean): PlayerEvent()
    data class ActivateMovieOption(val isActivate: Boolean): PlayerEvent()
}