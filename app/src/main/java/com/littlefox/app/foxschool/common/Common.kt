package com.littlefox.app.foxschool.common

class Common
{
    companion object
    {
        const val PACKAGE_NAME : String = "com.littlefox.app.foxschool"
        const val PATH_APP_ROOT : String = "/data/data/" + PACKAGE_NAME + "/files/"

        const val BASE_URL : String                 = "https://apis.littlefox.com/"
        const val BASE_PUBLIC_API : String          = BASE_URL + "api/v1/"
        const val BASE_PUBLIC_API_V2 : String       = BASE_URL + "api/v2/"
        const val API_CONTENTS : String             = BASE_PUBLIC_API + "contents/"
        const val API_QUIZ : String                 = API_CONTENTS
        const val API_AUTH_CONTENT_PLAY : String    = API_CONTENTS
        const val API_STUDY_LOG_SAVE : String       = API_CONTENTS
        const val API_QUIZ_SAVE_RECORD : String     = API_CONTENTS
        const val API_VOCABULARY_CONTENTS : String  = API_CONTENTS
        const val API_SEARCH_LIST : String          = BASE_PUBLIC_API + "contents"
        const val API_BOOKSHELF : String            = BASE_PUBLIC_API + "bookshelves"
        const val API_VOCABULARY_SHELF : String     = BASE_PUBLIC_API + "vocabularies/"

        const val API_CLASS_RECORD_UPLOAD : String = "https://www.littlefox.co.kr/ko/class_service/appUpload"

        const val SECOND : Int = 1000;
        const val MINIMUM_TABLET_DISPLAY_RADIO : Float = 1.4f

        const val PARAMS_DISPLAY_METRICS : String           = "display_metrics"
        const val PARAMS_REGISTER_APP_VERSION : String      = "app_version"
        const val PARAMS_USER_LOGIN : String                = "user_login"
        const val PARAMS_FILE_MAIN_INFO : String            = "file_main_info"
        const val PARAMS_ACCESS_TOKEN : String              = "access_token"
        const val PARAMS_USER_API_INFORMATION : String      = "user_api_information"
        const val PARAMS_IS_AUTO_LOGIN_DATA : String        = "is_auto_login_data"
        const val PARAMS_IS_DISPOSABLE_LOGIN : String       = "is_disposable_login"
        const val PARAMS_IS_VIDEO_HIGH_RESOLUTION : String  = "is_video_high_resolution"
        const val PARAMS_PLAYER_SPEED_INDEX : String        = "player_speed_index"
        const val PARAMS_IS_ENABLE_CAPTION : String         = "is_enable_caption"
        const val PARAMS_IS_ENABLE_PAGE_BY_PAGE : String    = "is_enable_page_by_page"
        const val PARAMS_VOCABULARY_INTERVAL : String       = "vocabulary_interval"

        const val INTENT_PLAYER_DATA_PARAMS : String                = "player_data_params"
        const val INTENT_PLAYER_INTRODUCE_VIDEO_PARAMS : String     = "introduce_video"
        const val INTENT_VOCABULARY_DATA : String                   = "vocabulary_data"
        const val INTENT_MODIFY_VOCABULARY_NAME : String            = "vocabulary_name"


        const val COROUTINE_CODE_FILE_DOWNLOAD : String                 = "file_download";
        const val COROUTINE_CODE_CLASS_RECORD_FILE : String             = "record_file";
        const val COROUTINE_CODE_AUTH_CONTENT_PLAY : String             = "auth_content_play"
        const val COROUTINE_CODE_BOOKSHELF_CONTENTS_ADD : String        = "bookshelf_contents_add"
        const val COROUTINE_CODE_STUDY_LOG_SAVE : String                = "study_log_save"
        const val COROUTINE_CODE_VOCABULARY_SHELF : String              = "vocabulary_shelf_list"
        const val COROUTINE_CODE_VOCABULARY_CREATE : String             = "vocabulary_create"
        const val COROUTINE_CODE_VOCABULARY_DELETE : String             = "vocabulary_delete"
        const val COROUTINE_CODE_VOCABULARY_UPDATE : String             = "vocabulary_update"
        const val COROUTINE_CODE_VOCABULARY_CONTENTS_ADD : String       = "vocabulary_contents_add"
        const val COROUTINE_CODE_VOCABULARY_CONTENTS_DELETE : String    = "vocabulary_contents_delete"
        const val COROUTINE_CODE_VOCABULARY_CONTENTS_LIST : String      = "vocabulary_contents_list"


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

        const val TARGET_PHONE_DISPLAY_WIDTH : Float = 1080.0f
        const val TARGET_TABLET_DISPLAY_WIDTH : Float = 1920.0f

        const val LOADING_DIALOG_SIZE : Int              = 150

        const val DURATION_SHORTEST : Long               = 100
        const val DURATION_SHORTER : Long                = 150
        const val DURATION_SHORT : Long                  = 300
        const val DURATION_MENU_ANIMATION_PHONE : Long   = 350
        const val DURATION_NORMAL : Long                 = 500
        const val DURATION_CHANGE_USER : Long            = 600
        const val DURATION_SHORT_LONG : Long             = 700
        const val DURATION_LONG : Long                   = 1000
        const val DURATION_LONGER : Long                 = 1500
        const val DURATION_LONGEST : Long                = 2000

        /**
         * 한 화면에 페이지 표시 개수
         */
        const val MAX_PAGE_BY_PAGE_COUNT_IN_LINE : Int = 5

        const val FREE_USER_NAME : String = "free_user"

        const val MAX_RECENTLY_LEARN_CONTENTS : Int     = 10
        const val MAX_BOOKSHELF_SIZE : Int              = 10
        const val MAX_BOOKSHELF_CONTENTS_SIZE : Int     = 300
        const val MAX_VOCABULARY_SIZE : Int             = 10
        const val MAX_VOCABULARY_CONTENTS_SIZE : Int    = 300

        const val PAGE_STORY : Int      = 0
        const val PAGE_SONG : Int       = 1
        const val PAGE_MY_BOOKS : Int   = 2

    }

}