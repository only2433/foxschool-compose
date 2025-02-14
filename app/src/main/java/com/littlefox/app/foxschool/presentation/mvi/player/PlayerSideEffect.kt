package com.littlefox.app.foxschool.presentation.mvi.player

import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.presentation.mvi.base.SideEffect
import java.util.ArrayList

sealed class PlayerSideEffect: SideEffect()
{
    data class ShowWarningAPIExceptionDialog(val message: String): PlayerSideEffect()
    data class ShowBottomOptionDialog(val data: ContentsBaseResult): PlayerSideEffect()
    data class ShowBookshelfContentsAddDialog(val list: ArrayList<MyBookshelfResult>): PlayerSideEffect()
    object ShowRecordPermissionDialog: PlayerSideEffect()
    object ShowWarningWatchingDialog: PlayerSideEffect()
}