package com.littlefox.app.foxschool.enumerate

enum class RecorderStatus
{
    RECORD_STOP,    // 녹음 정지 (녹음 초기상태)
    RECORD_START,   // 녹음 시작
    RECORD_PAUSE,   // 녹음 일시정지
    AUDIO_STOP,     // 오디오 정지 (오디오 초기상태)
    AUDIO_PLAY,     // 오디오 재생
    AUDIO_PAUSE     // 오디오 일시정지
}