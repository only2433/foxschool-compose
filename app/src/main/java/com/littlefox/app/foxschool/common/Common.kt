package com.littlefox.app.foxschool.common

class Common
{
    companion object
    {
        const val PACKAGE_NAME : String = "com.littlefox.app.foxschool"
        const val PATH_APP_ROOT = "/data/data/" + PACKAGE_NAME + "/files/"

        const val SECOND : Int = 1000;
        const val MINIMUM_TABLET_DISPLAY_RADIO : Float = 1.4f

        const val PARAMS_DISPLAY_METRICS : String       = "display_metrics"
        const val PARAMS_REGISTER_APP_VERSION : String  = "app_version"
        const val PARAMS_USER_LOGIN : String            = "user_login"
        const val PARAMS_FILE_MAIN_INFO : String        = "file_main_info";
        const val PARAMS_ACCESS_TOKEN : String          = "access_token";

        const val COROUTINE_CODE_FILE_DOWNLOAD          = "file_download";

        const val CONTENT_TYPE_ALL : String     = ""
        const val CONTENT_TYPE_STORY : String   = "S"
        const val CONTENT_TYPE_SONG : String    = "M"

        const val HTTP_HEADER_ANDROID : String  = "Android"
        const val HTTP_HEADER_APP_NAME : String = "LF_APP_AOS"
        const val DEVICE_TYPE_PHONE : String    = "phone"
        const val DEVICE_TYPE_TABLET : String   = "tablet"

        const val SERVICE_NOT_SUPPORTED : String    = "N"
        const val SERVICE_SUPPORTED_PAID : String   = "Y"
        const val SERVICE_SUPPORTED_FREE : String   = "F"

        const val SERVICE_NOT_ENDED : String    = "N"
        const val SERVICE_ENDED : String        = "Y"

        const val TARGET_PHONE_DISPLAY_WIDTH : Int = 1080
        const val TARGET_TABLET_DISPLAY_WIDTH : Int = 1920


    }

}