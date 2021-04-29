package com.littlefox.app.foxschool.`object`.record

class RecordInfoData(filePath : String, fileName : String, classID : Int, contentsID : String, itemConnt : Int, indexOfDay : Int)
{
    private val mFilePath : String
    private val mFileName : String
    private val mClassID : Int;
    private val mContentsID : String
    private val mItemCount : Int
    private val mIndexOfDay : Int

    init
    {
        mFilePath = filePath
        mFileName = fileName
        mClassID = classID
        mContentsID = contentsID
        mItemCount = itemConnt
        mIndexOfDay = indexOfDay
    }

    fun getFilePath() : String
    {
        return mFilePath;
    }

    fun getFileName() : String
    {
        return mFileName;
    }

    fun getClassID() : Int
    {
        return mClassID;
    }

    fun getContentsID() : String
    {
        return mContentsID;
    }

    fun getItemCount() : Int
    {
        return mItemCount;
    }

    fun getIndexOfDay() : Int
    {
        return mIndexOfDay;
    }
}