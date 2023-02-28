package com.littlefox.app.foxschool.`object`.result.forum.paging

import com.google.gson.annotations.SerializedName

data class ForumMetaDataPagingResult(

    @SerializedName("current_page")
    var current_page : Int = 0,

    @SerializedName("last_page")
    var last_page : Int  = 0,

    @SerializedName("per_page")
    var per_page : Int  = 0,

    @SerializedName("total")
    var total : Int  = 0
)
{}