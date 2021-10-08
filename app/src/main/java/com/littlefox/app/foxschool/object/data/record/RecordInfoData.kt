package com.littlefox.app.foxschool.`object`.data.record

class RecordInfoData(filePath : String, fileName : String, contentsID : String, recordTime : Int)
{
    private val mFilePath : String
    private val mFileName : String
    private val mContentsID : String
    private val mRecordTime : Int

    init
    {
        mFilePath = filePath
        mFileName = fileName
        mContentsID = contentsID
        mRecordTime = recordTime
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
}