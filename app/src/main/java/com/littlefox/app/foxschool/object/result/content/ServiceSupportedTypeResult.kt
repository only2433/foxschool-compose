import android.os.Parcel
import android.os.Parcelable
import com.littlefox.app.foxschool.common.Common

data class ServiceSupportedTypeResult(
    var story: String = Common.SERVICE_NOT_SUPPORTED,  // 동화 서비스
    var service: String = Common.SERVICE_NOT_SUPPORTED,
    var original_text: String = Common.SERVICE_NOT_SUPPORTED,  // 원문 서비스
    var vocabulary: String = Common.SERVICE_NOT_SUPPORTED,  // 단어장 서비스
    var quiz: String = Common.SERVICE_NOT_SUPPORTED,  // 퀴즈 서비스
    var ebook: String = Common.SERVICE_NOT_SUPPORTED,  // ebook 서비스
    var crossword: String = Common.SERVICE_NOT_SUPPORTED,  // 크로스워드 서비스
    var starwords: String = Common.SERVICE_NOT_SUPPORTED,  // 스타워즈 서비스
    var flash_card: String = Common.SERVICE_NOT_SUPPORTED,  // 플래시카드 서비스
    var record: String = Common.SERVICE_NOT_SUPPORTED  // 녹음기 서비스
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED,
        parcel.readString() ?: Common.SERVICE_NOT_SUPPORTED
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(story)
        parcel.writeString(service)
        parcel.writeString(original_text)
        parcel.writeString(vocabulary)
        parcel.writeString(quiz)
        parcel.writeString(ebook)
        parcel.writeString(crossword)
        parcel.writeString(starwords)
        parcel.writeString(flash_card)
        parcel.writeString(record)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ServiceSupportedTypeResult> {
        override fun createFromParcel(parcel: Parcel): ServiceSupportedTypeResult {
            return ServiceSupportedTypeResult(parcel)
        }

        override fun newArray(size: Int): Array<ServiceSupportedTypeResult?> {
            return arrayOfNulls(size)
        }
    }
}