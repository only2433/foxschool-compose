package com.littlefox.app.foxschool.presentation.viewmodel.player

import com.littlefox.app.foxschool.enumerate.ActionContentsType
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.presentation.viewmodel.base.BaseEvent

sealed class PlayerEvent : BaseEvent()
{
    object onClickControllerPlay: PlayerEvent()
    object onClickControllerPrev: PlayerEvent()
    object onClickControllerNext: PlayerEvent()

    object onChangeOrientationPortrait: PlayerEvent()
    object onChangeOrientationLandscape: PlayerEvent()
    object onClickReplay: PlayerEvent()
    object onClickLoadNextMovie: PlayerEvent()
    object onCancelBottomOptionDialog: PlayerEvent()
    object onCloseCoackmarkNeverSeeAgain: PlayerEvent()

    object onStartTrackingTouch: PlayerEvent()
    data class onStopTrackingTouch(val progress: Int): PlayerEvent()

    data class onSelectItem(val index: Int): PlayerEvent()
    data class onClickOption(val item: ContentsBaseResult): PlayerEvent()
    data class onSelectSpeed(val index: Int): PlayerEvent()
    data class onClickActionContentsType(val type: ActionContentsType): PlayerEvent()

    data class onClickPageByPageIndex(val index : Int): PlayerEvent()
    data class onClickPageByPageNext(val lastIndexInPag: Int): PlayerEvent()
    data class onClickPageByPagePrev(val startIndexInPage : Int): PlayerEvent()

}