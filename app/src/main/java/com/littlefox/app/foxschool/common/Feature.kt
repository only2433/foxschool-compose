package com.littlefox.app.foxschool.common

import com.littlefox.app.foxschool.enumerate.UserType

object Feature {
    /**
     * 네비게이션바 ( 소프트키) 가 있는 지 의 유무
     */
    var HAVE_NAVIGATION_BAR : Boolean = false

    /**
     * 최저 해상도 이하인지의 여부
     */
    var IS_MINIMUM_DISPLAY_SIZE : Boolean  = false

   /*
    var IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY : Boolean  = false


    var IS_ABOVE_20_9_SUPPORT_RADIO_DISPLAY = false */

    /**
     * Webview 디버깅 관련 여부
     */
    const val IS_WEBVIEW_DEBUGING : Boolean  = false

    /**
     * 구글 스토어 체크 할지 안할지의 여부 ( 안하면 앱을 사용하고, 하면 앱을 사용 못함 )
     */
    const val IS_APP_PAYMENT_FAIL_NO_CHECK : Boolean  = true



    const val IS_ENABLE_FIREBASE_CRASHLYTICS : Boolean  = true

    /**
     * 리틀팍스 클래스 사용 여부
     */
    var IS_SUPPORT_LITTLEFOX_CLASS : Boolean  = true

    /**
     * 이북 폰 지원 여부
     */
    var IS_SUPPORT_EBOOK_PHONE : Boolean  = false

}