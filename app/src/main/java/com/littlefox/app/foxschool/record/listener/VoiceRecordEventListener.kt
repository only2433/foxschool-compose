package com.littlefox.app.foxschool.record.listener

interface VoiceRecordEventListener
{
    fun onStartRecord()
    fun onRecordProgress(percent : Int)
    fun onCompleteRecord()
    fun inFailure(status : Int, message : String)
}