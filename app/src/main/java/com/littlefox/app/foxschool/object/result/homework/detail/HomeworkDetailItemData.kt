package com.littlefox.app.foxschool.`object`.result.homework.detail

import com.littlefox.app.foxschool.enumerate.HomeworkType

class HomeworkDetailItemData
{
    private var is_complete : String        = ""
    private var complete_date : String      = ""
    private var content_id : String         = ""
    private var content_type : String       = ""
    private var thumbnail_url : String      = ""
    private var hw_type : String            = ""
    private var cont_name : String          = ""
    private var cont_sub_name : String      = ""

    // 선생님 데이터
    private var mp3_expired : Int           = 0
    private var mp3_path : String           = ""

    val isComplete : Boolean
    get()
    {
        if(is_complete.equals("Y"))
            return true
        return false
    }

    fun getCompleteDate() : String = complete_date

    fun getContentID() : String = content_id

    fun getContentType() : String = content_type

    fun getThumbnailUrl() : String = thumbnail_url

    fun getHomeworkType() : HomeworkType
    {
        when (hw_type)
        {
            "A" -> return HomeworkType.ANIMATION
            "E" -> return HomeworkType.EBOOK
            "Q" -> return HomeworkType.QUIZ
            "C" -> return HomeworkType.CROSSWORD
            "S" -> return HomeworkType.STARWORDS
            "R" -> return HomeworkType.RECORDER
            else -> return HomeworkType.ANIMATION
        }
    }

    fun getName() : String = cont_name

    fun getSubName() : String = cont_sub_name

    /**
     * 화면에 보일 컨텐츠 이름을 리턴한다. 서브네임이 있을 경우엔 시리즈 명과 같이 노출
     * @return 컨텐츠 네임
     */
    fun getContentsName() : String
    {
        var result : String = ""
        if(cont_sub_name == "")
        {
            result = cont_name
        } else
        {
            result = "$cont_name: $cont_sub_name"
        }
        return result
    }

    // 선생님용 ----------
    fun getExpired() : Int = mp3_expired

    fun getMp3Path() : String = mp3_path
}