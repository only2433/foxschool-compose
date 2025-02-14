package com.littlefox.app.foxschool.presentation.mvi.player

import androidx.media3.exoplayer.ExoPlayer
import com.littlefox.app.foxschool.enumerate.MovieNavigationStatus
import com.littlefox.app.foxschool.`object`.data.player.PageLineData
import com.littlefox.app.foxschool.`object`.data.player.PlayerEndViewData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.State

data class PlayerState(
    val player: ExoPlayer? = null,
    val contentsList: ArrayList<ContentsBaseResult> = arrayListOf(),
    val playerEndViewData: PlayerEndViewData = PlayerEndViewData(),
    val isMovieLoading: Boolean = false,
    val title: String = "",
    val captionText: String = "",
    val totalMovieTime: String = "",
    val currentMovieTime: String = "",
    val currentProgress: Int = 0,
    val maxProgress: Int = 0,
    val isReadyToPlayMovie: Boolean = false,
    val playMovie: Boolean = false,
    val showPlayerEndView: Boolean = false,
    val navigationStatus: MovieNavigationStatus = MovieNavigationStatus.NORMAL,
    val currentPageIndex: Int = 0,
    val currentPageLineData : PageLineData = PageLineData(),
    val currentPlayIndex: Int = 0,
    val currentSpeedIndex: Int = 0,
    val supportMovieOption: Boolean = false,
    val supportCaptionAndPage: Boolean = false,
    val supportSpeedViewButton: Boolean = false,
    val activatePageView: Boolean = false,
    val activateMovieOption: Boolean = false,
): State