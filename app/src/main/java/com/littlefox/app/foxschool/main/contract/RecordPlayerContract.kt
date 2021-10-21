package com.littlefox.app.foxschool.main.contract

import com.littlefox.app.foxschool.`object`.data.record.RecordIntentParamsObject
import com.littlefox.app.foxschool.enumerate.RecorderStatus
import com.littlefox.app.foxschool.main.contract.base.BaseContract

class RecordPlayerContract
{
    interface View : BaseContract.View
    {
        fun setRecordTitle(contents : RecordIntentParamsObject)     // 화면의 녹음 컨텐츠 타이틀 변경
        fun setTimerText(time : String)                             // 화면의 타이머 텍스트 변경
        fun setCoachMarkView()                                      // 코치마크 표시 설정
        fun setRecorderStatus(status : RecorderStatus)              // 녹음기 상태에 따른 화면 세팅
        fun startRecordingAnimation(duration : Long, percent : Int) // 녹음 progress animation 시작
        fun stopRecordingAnimation(percent : Int)                   // 녹음 progress animation 정지 (현재 진행상황 위치로 고정)

        fun setAudioPlayTime(currentTime : Int, maxTime : Int)      // 오디오 플레이어 타임 세팅

        fun setUploadButtonEnable(isEnable : Boolean)               // 업로드 완료 화면 세팅 (업로드 버튼 비활성화)
    }

    interface Presenter : BaseContract.Presenter
    {
        fun onCoachMarkNeverSeeAgain()      // 코치마크 다시 보지 않기
        fun onClickClose()                  // 닫기
        fun enableTimer(isStart : Boolean)  // 타이머

        fun onClickRecordStart()    // 녹음 시작
        fun onClickRecordPause()    // 녹음 일시정지
        fun onClickRecordPlay()     // 녹음 듣기

        fun onClickRecordReset()    // 녹음 다시하기
        fun onClickRecordStop()     // 녹음 정지(완료)
        fun onClickRecordUpload()   // 녹음 업로드

        fun onSeekTo(time : Int)    // 오디오 재생 위치 변경
    }
}