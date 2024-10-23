package com.littlefox.app.foxschool.`object`.result.content

import ServiceSupportedTypeResult
import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.common.Common
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult

data class ContentsBaseResult(
    var id: String = "",
    var seq: Int = 0,
    var type: String = Common.CONTENT_TYPE_STORY,
    var name: String = "",
    var sub_name: String = "",
    var thumbnail_url: String = "",
    var service_info: ServiceSupportedTypeResult? = null,
    var story_chk: String = "",
    var isSelected: Boolean = false,
    var isOptionDisable: Boolean = false
) : Parcelable {

    constructor(data: ContentBasePagingResult) : this(
        id = data.id,
        seq = data.seq,
        type = data.type,
        name = data.name,
        sub_name = data.sub_name ?: "",
        thumbnail_url = data.thumbnail_url,
        service_info = data.service_info,
        story_chk = data.story_chk ?: ""
    )

    private constructor(`in`: Parcel) : this(
        id = `in`.readString()!!,
        seq = `in`.readInt(),
        type = `in`.readString()!!,
        name = `in`.readString()!!,
        sub_name = `in`.readString() ?: "",
        thumbnail_url = `in`.readString()!!,
        service_info = `in`.readTypedObject(ServiceSupportedTypeResult.CREATOR),
        story_chk = `in`.readString() ?: "",
        isSelected = `in`.readByte().toInt() != 0,
        isOptionDisable = `in`.readByte().toInt() != 0
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeInt(seq)
        dest.writeString(type)
        dest.writeString(name)
        dest.writeString(sub_name)
        dest.writeString(thumbnail_url)
        dest.writeByte((if (isSelected) 1 else 0).toByte())
        dest.writeByte((if (isOptionDisable) 1 else 0).toByte())
        dest.writeTypedObject(service_info, flags)
        dest.writeString(story_chk)
    }

    override fun describeContents(): Int = 0

    fun getContentsName(): String {
        return if (sub_name.isNullOrEmpty()) {
            name
        } else {
            "$name: ${sub_name}"
        }
    }

    fun getVocabularyName(): String {
        return sub_name?.takeIf { it.isNotEmpty() } ?: name
    }

    fun setIndex(index: Int)
    {
        seq = index
    }

    fun setTitle(title : String, subTitle : String)
    {
        this.name = title
        this.sub_name = subTitle
    }

    val index: Int
        get() = seq


    val isStoryViewComplete: Boolean
        get() = !story_chk.isNullOrEmpty()

    companion object CREATOR : Parcelable.Creator<ContentsBaseResult> {
        override fun createFromParcel(`in`: Parcel): ContentsBaseResult {
            return ContentsBaseResult(`in`)
        }

        override fun newArray(size: Int): Array<ContentsBaseResult?> {
            return arrayOfNulls(size)
        }
    }
}