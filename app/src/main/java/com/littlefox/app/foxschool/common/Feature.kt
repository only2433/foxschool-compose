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

    /**
     * 특정 태블릿 비율이 16:9 가 아닌 정상적이지 않은 4:3 비율의 태블릿을 지원하기 위해 사용.
     */
    var IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY : Boolean  = false

    /**
     * 특정 폰 비율이 16:9 가 아닌 정상적이지 않은 20:9 비율의 폰을 지원하기 위해 사용.
     */
    var IS_20_9_SUPPORT_RADIO_DISPLAY = false

    /**
     * 유료사용자 인지 무료 사용자인지 구분하기 위해 사용
     */
    var IS_FREE_USER : Boolean  = true

    /**
     * 기간이 종료된 회원 인지 여부
     */
    var IS_REMAIN_DAY_END_USER : Boolean  = false

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
     * 앱 사용자의 타입
     */
    var CURRENT_USER_TYPE : UserType = UserType.STUDENT
}