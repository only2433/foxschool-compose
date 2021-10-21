package com.littlefox.app.foxschool.`object`.data.record

class RecordInfoData(filePath : String, fileName : String, contentsID : String, recordTime : Int, homeworkNo : Int)
{
    private val mFilePath : String
    private val mFileName : String
    private val mContentsID : String
    private val mRecordTime : Int
    private val mHomeworkNumber : Int

    init
    {
        mFilePath = filePath
        mFileName = fileName
        mContentsID = contentsID
        mRecordTime = recordTime
        mHomeworkNumber = homeworkNo
    }

    fun getFilePath() : String
    {
        return mFilePath;
    }

    fun getFileName() : String
    {
        return mFileName;
    }

    fun getContentsID() : String
    {
        return mContentsID;
    }

    fun getRecordTime() : Int
    {
        return mRecordTime
    }

    fun getHomeworkNumber() : Int
    {
        return mHomeworkNumber
    }
}