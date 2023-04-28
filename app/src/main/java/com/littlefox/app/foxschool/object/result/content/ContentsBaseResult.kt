package com.littlefox.app.foxschool.`object`.result.content

import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult

class ContentsBaseResult  : Parcelable
{
    private var id : String = ""
    private var seq = 0
    private var type : String = Common.CONTENT_TYPE_STORY
    private var name : String = ""
    private var sub_name : String? = ""
    private var thumbnail_url : String = ""
    private var service_info : ServiceSupportedTypeResult? = null
    private var story_chk : String? = ""
    private var isSelected = false
    private var isOptionDisable = false

    constructor() {}

    constructor(data : ContentBasePagingResult)
    {
        id = data.id
        seq = data.seq
        type = data.type
        name = data.name
        sub_name = data.sub_name
        thumbnail_url = data.thumbnail_url
        service_info = data.service_info
        story_chk = data.story_chk
    }

    protected constructor(`in` : Parcel)
    {
        id = `in`.readString()!!
        seq = `in`.readInt()
        type = `in`.readString()!!
        name = `in`.readString()!!
        sub_name = `in`.readString()
        thumbnail_url = `in`.readString()!!
        isSelected = `in`.readByte().toInt() != 0
        isOptionDisable = `in`.readByte().toInt() != 0
        service_info = `in`.readSerializable() as ServiceSupportedTypeResult?
        story_chk = `in`.readString()
    }

    override fun writeToParcel(dest : Parcel, flags : Int)
    {
        dest.writeString(id)
        dest.writeInt(seq)
        dest.writeString(type)
        dest.writeString(name)
        dest.writeString(sub_name)
        dest.writeString(thumbnail_url)
        dest.writeByte((if(isSelected) 1 else 0).toByte())
        dest.writeByte((if(isOptionDisable) 1 else 0).toByte())
        dest.writeSerializable(service_info)
        dest.writeString(story_chk)
    }

    override fun describeContents() : Int = 0

    fun getID() : String = id

    fun getIndex() : Int = seq

    fun getType() : String = type

    fun getName() : String = name

    fun getSubName() : String
    {
        if(sub_name == null)
        {
            return ""
        }
        return sub_name!!
    }

    /**
     * 화면에 보일 컨텐츠 이름을 리턴한다. 서브네임이 있을 경우엔 시리즈 명과 같이 노출
     * @return 컨텐츠 네임
     */
    fun getContentsName() : String
    {
        var result : String = ""
        if(sub_name == "")
        {
            result = name
        }
        else
        {
            result = "$name: ${getSubName()}"
        }
        return result
    }

    /**
     * 단어장은 서브네임이 있을 경우엔 서브네임을 타이틀로, 없을 경우 컨텐츠 네임으로 보여준다.
     * @param data 컨텐츠 데이터
     * @return 컨텐츠 네임
     */
    fun getVocabularyName() : String
    {
        var result : String = ""
        if (getSubName().equals(""))
        {
            result = name
        }
        else
        {
            result = getSubName()
        }
        return result
    }

    fun getThumbnailUrl() : String = thumbnail_url

    fun getServiceInformation() : ServiceSupportedTypeResult? = service_info

    val isStoryViewComplete : Boolean
    get()
    {
        if(story_chk.equals("") == false)
        {
            return true
        }

        return false
    }

    fun setID(id : String)
    {
        this.id = id
    }

    /**
     * 숙제에서만 사용 (컨텐츠 이름 설정)
     */
    fun setTitle(title : String, subTitle : String)
    {
        this.name = title
        this.sub_name = subTitle
    }

    fun setThumbnailUrl(url : String)
    {
        this.thumbnail_url = url
    }

    fun setIndex(index : Int)
    {
        this.seq = index
    }

    fun isSelected() : Boolean = isSelected

    fun setSelected(isSelect : Boolean)
    {
        isSelected = isSelect
    }

    fun isOptionDisable() : Boolean = isOptionDisable

    fun setOptionDisable(isDisable : Boolean)
    {
        isOptionDisable = isDisable
    }

    companion object
    {
        @JvmField
        val CREATOR : Parcelable.Creator<ContentsBaseResult?> = object : Parcelable.Creator<ContentsBaseResult?>
        {
            override fun createFromParcel(`in` : Parcel) : ContentsBaseResult?
            {
                return ContentsBaseResult(`in`)
            }

            override fun newArray(size : Int) : Array<ContentsBaseResult?>
            {
                return arrayOfNulls(size)
            }
        }
    }
}