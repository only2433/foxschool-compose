package com.littlefox.app.foxschool.record.listener

interface VoiceRecordEventListener
{
    fun onStartRecord()
    fun onRecordProgress(percent : Int)
    fun onCompleteRecord()
    fun onCompleteFileMerged()
    fun inFailure(status : Int, message : String)
}