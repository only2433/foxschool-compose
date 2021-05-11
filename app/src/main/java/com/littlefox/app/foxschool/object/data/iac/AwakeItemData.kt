package com.littlefox.app.foxschool.`object`.data.iac

/**
 * 서버정보와 비교하여 깨워야할 정보
 * @author 정재현
 */
class AwakeItemData
{
    /** IAC 각각의 고유 식별자  */
    var iacCode = 0

    /** IAC 닫기를 눌렀을 때의 시간  */
    var iacCloseTime : Long = -1

    /**
     * IAC 두번째 버튼 타입
     */
    var iacType = ""

    /**
     * 특정 날짜가 되엇을 때 노출 시키기 위한 day
     */
    var latingDate : Int = 0

    constructor(iacCode : Int, iacCloseTime : Long, iacType : String, latingDate : Int)
    {
        this.iacCode = iacCode
        this.iacCloseTime = iacCloseTime
        this.iacType = iacType
        this.latingDate = latingDate
    }
}