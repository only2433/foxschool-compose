package com.littlefox.app.foxschool.`object`.data.player

/**
 * 플레이 정보를 기록하기 위해 만든 정보
 * Created by 정재현 on 2015-07-06.
 */
class PlayedContentData
{
    private var contentID : String = ""

    /**
     * 플레이 될 때 마다 업데이트 시킨다. 그래서 특정기한이 지나거나 용량이 어느정도 되엇을 때 삭제한다.
     */
    private var recentPlayTime : String = ""
    private var filePath : String = ""
    private var totalPlayTime : String  = ""
    private var isDownloadComplete : Boolean = false

    constructor(contentID : String, recentPlayTime : String, filePath : String, totalPlayTime : String, isDownloadComplete : Boolean)
    {
        this.contentID = contentID
        this.recentPlayTime = recentPlayTime
        this.filePath = filePath
        this.totalPlayTime = totalPlayTime
        this.isDownloadComplete = isDownloadComplete
    }

    fun getContentID() : String = contentID

    fun setContentID(contentID : String)
    {
        this.contentID = contentID
    }

    fun getRecentPlayTime() : String = recentPlayTime

    fun setRecentPlayTime(recentPlayTime : String)
    {
        this.recentPlayTime = recentPlayTime
    }

    fun getFilePath() : String = filePath

    fun setFilePath(filePath : String)
    {
        this.filePath = filePath
    }

    fun getTotalPlayTime() : String = totalPlayTime

    fun setTotalPlayTime(totalPlayTime : String)
    {
        this.totalPlayTime = totalPlayTime
    }

    fun isDownloadComplete() : Boolean = isDownloadComplete

    fun setDownloadComplete(isDownloadComplete : Boolean)
    {
        this.isDownloadComplete = isDownloadComplete
    }
}