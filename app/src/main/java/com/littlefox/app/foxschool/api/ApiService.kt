package com.littlefox.app.foxschool.api

import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBaseListPagingResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkCalendarBaseResult
import com.littlefox.app.foxschool.`object`.result.homework.HomeworkDetailBaseResult
import com.littlefox.app.foxschool.`object`.result.login.LoginInformationResult
import com.littlefox.app.foxschool.`object`.result.login.SchoolItemDataResult
import com.littlefox.app.foxschool.`object`.result.main.MainInformationResult
import com.littlefox.app.foxschool.`object`.result.version.VersionDataResult
import com.littlefox.app.foxschool.api.base.BaseResponse
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.result.main.MyBookshelfResult
import com.littlefox.app.foxschool.`object`.result.player.PlayItemResult
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService
{
    @FormUrlEncoded
    @Headers("Content-Type: application/json")
    @POST("app/version")
    suspend fun initAsync(
        @Field("device_id") device_id: String,
        @Field("push_address") push_address: String,
        @Field("push_on") push_on: String
    ) : Response<BaseResponse<VersionDataResult>>

    @Headers("Content-Type: application/json")
    @GET("auth/me")
    suspend fun authMeAsync() : Response<BaseResponse<LoginInformationResult>>

    @Headers("Content-Type: application/json")
    @GET("app/main")
    suspend fun mainAsync() : Response<BaseResponse<MainInformationResult>>

    @FormUrlEncoded
    @Headers("Content-Type: application/json")
    @POST("users/password/update")
    suspend fun passwordChangeAsync(
        @Field("password_check") currentPassword: String,
        @Field("password") changePassword: String,
        @Field("password_confirm") changePasswordConfirm: String
    ) : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @GET("users/password/next")
    suspend fun passwordChangeNextAsync() : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @GET("users/password/keep")
    suspend fun passwordChangeKeepAsync() : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @GET("users/school")
    suspend fun schoolListAsync() : Response<BaseResponse<ArrayList<SchoolItemDataResult>>>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun loginAsync(
        @Field("login_id") id : String,
        @Field("password") password : String,
        @Field("school_id") schoolCode : String
    ) : Response<BaseResponse<LoginInformationResult>>

    @Headers("Content-Type: application/json")
    @GET("contents/player/{data}")
    suspend fun authContentsPlayAsync(
        @Path("data") requestData: String
    ) : Response<BaseResponse<PlayItemResult>>


    @FormUrlEncoded
    @POST("contents/player/save")
    suspend fun savePlayerStudyAsync(
        @Field("content_id") contentsID: String,
        @Field("play_type") playType: String,
        @Field("play_time") playTime: String
    ) : Response<BaseResponse<Nothing>>

    @FormUrlEncoded
    @POST("contents/bookshelves/{data}/contents")
    suspend fun addBookshelfContentsAsync(
        @Path("data") bookshelfID: String,
        @FieldMap queryMap : Map<String, String>
    ) : Response<BaseResponse<MyBookshelfResult>>

    @Headers("Content-Type: application/json")
    @GET("homeworks/student")
    suspend fun studentHomeworkCalendarAsync(
        @Query("year") id : String,
        @Query("month") password : String
    ) : Response<BaseResponse<HomeworkCalendarBaseResult>>

    @Headers("Content-Type: application/json")
    @GET("homeworks/student/list/{hw_no}")
    suspend fun studentHomeworkListAsync(
        @Path("hw_no") homeworkNumber : Int
    ) : Response<BaseResponse<HomeworkDetailBaseResult>>

    @Headers("Content-Type: application/json")
    @PUT("homeworks/student")
    suspend fun studentCommentRegisterAsync(
        @Query("comment") comment : String,
        @Query("hw_no") homeworkNumber : Int
    ) : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @POST("homeworks/student")
    suspend fun studentCommentUpdateAsync(
        @Query("comment") comment : String,
        @Query("hw_no") homeworkNumber : Int
    ) : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @DELETE("homeworks/student")
    suspend fun studentCommentDeleteAsync(
        @Query("hw_no") homeworkNumber : Int
    ) : Response<BaseResponse<Nothing>>

    @Headers("Content-Type: application/json")
    @GET("forum/board/news")
    suspend fun forumListAsync(
        @Query("per_page") pageCount: Int,
        @Query("page") currentPage: Int
    ) : Response<BaseResponse<ForumBaseListPagingResult>>

    companion object
    {
        const val CONNECTION_TIMEOUT: Long = 20000L

        fun create() : ApiService
        {
            val okHttpClient = OkHttpClient.Builder().apply {
                addInterceptor(
                    HeaderInterceptor()
                )
                addInterceptor(
                    SaveLogInterceptor()
                )
            }

            val client = okHttpClient
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(Common.BASE_API)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}