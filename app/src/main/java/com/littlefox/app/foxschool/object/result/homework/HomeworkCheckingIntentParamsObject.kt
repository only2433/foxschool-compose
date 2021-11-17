package com.littlefox.app.foxschool.`object`.result.homework

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.`object`.result.homework.status.HomeworkStatusItemData

/**
 * 선생님 평가 Intent 용 아이템
 */
class HomeworkCheckingIntentParamsObject : Parcelable
{
    private var homeworkNo : Int            = 0
    private var classNo : Int               = 0
    private var id : String                 = ""
    private var eval : Int                  = -1
    private var comment : String            = ""

    constructor(hw_no : Int, class_no : Int, fu_id : ArrayList<String>)
    {
        this.homeworkNo = hw_no
        this.classNo = class_no
        this.id = getBundleId(fu_id)
    }

    constructor(hw_no : Int, class_no : Int, item : HomeworkStatusItemData)
    {
        this.homeworkNo = hw_no
        this.classNo = class_no
        this.id = item.getUserID()
        this.comment = item.getEvalComment()
        if (item.getEvaluationState() != "N")
        {
            val checked = item.getEvaluationState().substring(1)
            this.eval = checked.toInt()
        }
    }

    constructor(hw_no : Int, class_no : Int, fu_id : ArrayList<String>, checked : Int, comment : String) : this(hw_no, class_no, fu_id)
    {
        this.eval = checked
        this.comment = comment
    }

    protected constructor(`in` : Parcel)
    {
        homeworkNo = `in`.readInt()
        classNo = `in`.readInt()
        id = `in`.readString()!!
        eval = `in`.readInt()
        comment = `in`.readString() ?: ""
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeInt(homeworkNo)
        dest.writeInt(classNo)
        dest.writeString(id)
        dest.writeInt(eval)
        dest.writeString(comment)
    }

    override fun describeContents() : Int = 0

    // 학생 ID 하나로 합치기
    fun getBundleId(idList : ArrayList<String>) : String
    {
        var idBundle = ""
        for (i in idList.indices)
        {
            idBundle += idList[i]
            if (i != idList.size - 1)
            {
                idBundle += ","
            }
        }
        return idBundle
    }

    fun getHomeworkNumber() : Int = homeworkNo

    fun getClassNumber() : Int = classNo

    fun getID() : String = id

    fun getEval() : Int = eval

    fun getComment() : String = comment

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<HomeworkCheckingIntentParamsObject?> = object : Parcelable.Creator<HomeworkCheckingIntentParamsObject?>
        {
            override fun createFromParcel(`in` : Parcel) : HomeworkCheckingIntentParamsObject?
            {
                return HomeworkCheckingIntentParamsObject(`in`)
            }

            override fun newArray(size : Int) : Array<HomeworkCheckingIntentParamsObject?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}