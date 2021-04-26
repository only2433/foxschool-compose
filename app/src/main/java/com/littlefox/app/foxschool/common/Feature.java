package com.littlefox.app.foxschool.common;

public class Feature
{
	/**
	 * 네비게이션바 ( 소프트키) 가 있는 지 의 유무
	 */
	public static boolean HAVE_NAVIGATION_BAR 		= false;
	
	/**
	 * 태블릿인지의 유무
	 */
	public static boolean IS_TABLET					= false;
	
	/**
	 * 최저 해상도 이하인지의 여부
	 */
	public static boolean IS_MINIMUM_DISPLAY_SIZE 	= false;
	
	/**
	 * 특정 태블릿 비율이 16:9 가 아닌 정상적이지 않은 4:3 비율의 태블릿을 지원하기 위해 사용. 
	 */
	public static boolean IS_4_3_SUPPORT_TABLET_RADIO_DISPLAY = false;
	

	/**
	 * 유료사용자 인지 무료 사용자인지 구분하기 위해 사용
	 */
	public static boolean IS_FREE_USER			= true;


	/**
	 * 기간이 종료된 회원 인지 여부
	 */
	public static boolean IS_REMAIN_DAY_END_USER = false;


	/**
	 * Webview 디버깅 관련 여부
	 */
	public static final  boolean IS_WEBVIEW_DEBUGING = false;

	/**
	 * 구글 스토어 체크 할지 안할지의 여부 ( 안하면 앱을 사용하고, 하면 앱을 사용 못함 )
	 */
	public static final boolean IS_APP_PAYMENT_FAIL_NO_CHECK = true;

	/**
	 * 중국향 모델 인지 여부. TRUE는 오직 사이트에 올리는 거만 허용. 앱스토어에 올릴땐 무조건 FALSE
	 * 깜빡하면 큰일 난다.
	 */
	public static final boolean IS_CHINESE_MODEL = false;


	/**
	 * 크로스워드, 스타워즈 테스트 여부
	 */
	public static final boolean IS_GAME_TEST = false;


	public static final boolean IS_ENABLE_FIREBASE_CRASHLYTICS = true;

	/**
	 * 리틀팍스 클래스 사용 여부
	 */
	public static boolean IS_SUPPORT_LITTLEFOX_CLASS = true;
}
