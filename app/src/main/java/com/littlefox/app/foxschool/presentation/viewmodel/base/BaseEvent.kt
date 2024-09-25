package com.littlefox.app.foxschool.presentation.viewmodel.base
import com.littlefox.app.foxschool.enumerate.DialogButtonType

abstract class BaseEvent
{
    object onBackPressed : BaseEvent()
    data class DialogClick(val eventType: Int) : BaseEvent()
    data class DialogChoiceClick(val buttonType: DialogButtonType, val eventType: Int) : BaseEvent()
}