package com.littlefox.app.foxschool.presentation.mvi.player

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.mvi.base.Action


sealed class PlayerAction: Action
{
    object ClickControllerPlay: PlayerAction()
    object ClickControllerPrev: PlayerAction()
    object ClickControllerNext: PlayerAction()
    object ChangeOrientationPortrait: PlayerAction()
    object ChangeOrientationLandscape: PlayerAction()
    object ClickReplay: PlayerAction()
    object ClickLoadNextMovie: PlayerAction()
    object CancelBottomOptionDialog: PlayerAction()
    object CloseCoachmarkNeverSeeAgain: PlayerAction()
    object StartTrackingTouch: PlayerAction()

    data class StopTrackingTouch(val progress: Int): PlayerAction()
    data class SelectItem(val index: Int): PlayerAction()
    data class ClickOption(val item: ContentsBaseResult): PlayerAction()
    data class SelectSpeed(val index: Int): PlayerAction()
    data class ClickActionContentsType(val type: ActionContentsType): PlayerAction()
    data class ClickPageByPageIndex(val index : Int): PlayerAction()
    data class ClickPageByPageNext(val lastIndexInPag: Int): PlayerAction()
    data class ClickPageByPagePrev(val startIndexInPage : Int): PlayerAction()
}