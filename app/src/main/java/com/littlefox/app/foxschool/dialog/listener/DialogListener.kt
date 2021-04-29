package com.littlefox.app.foxschool.dialog.listener

import com.littlefox.app.foxschool.enumerate.DialogButtonType

interface DialogListener
{
    /**
     * Custom Dialog에서 사용하는 메소드 ( Flexible Dialog 를 제외한 Dialog)
     * @param eventType Common에 명시되어있는 Dialog Status Type
     */
    fun onConfirmButtonClick(eventType : Int)

    /**
     * 두개의 버튼 중 선택한 부분에 대한 메소드
     * @param buttonType 선택한 버튼
     * @param eventType 보낼 Dialog Status Type
     */
    fun onChoiceButtonClick(buttonType : DialogButtonType?, eventType : Int)
}