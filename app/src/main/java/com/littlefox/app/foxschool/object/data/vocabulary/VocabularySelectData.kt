data class VocabularySelectData(
    var isSelectAll: Boolean = true,
    var isSelectedWord: Boolean = true,
    var isSelectedMeaning: Boolean = true,
    var isSelectedExample: Boolean = true
) {

    constructor(data: VocabularySelectData) : this(
        data.isSelectAll,
        data.isSelectedWord,
        data.isSelectedMeaning,
        data.isSelectedExample
    )

    fun setData(data: VocabularySelectData)
    {
        this.isSelectAll = data.isSelectAll
        this.isSelectedWord = data.isSelectedWord
        this.isSelectedMeaning = data.isSelectedMeaning
        this.isSelectedExample = data.isSelectedExample
    }

    fun setSelectWord() {
        isSelectedWord = !isSelectedWord
        isSelectAll = isCheckAll()
    }fun setSelectMeaning() {
        isSelectedMeaning = !isSelectedMeaning
        isSelectAll = isCheckAll()
    }

    fun setSelectExample() {
        isSelectedExample = !isSelectedExample
        isSelectAll = isCheckAll()
    }

    fun setSelectAll() {
        isSelectAll = !isSelectAll
        isSelectedWord = isSelectAll
        isSelectedMeaning = isSelectAll
        isSelectedExample = isSelectAll
    }

    fun isCheckAll(): Boolean {
        val result = isSelectedWord && isSelectedMeaning && isSelectedExample
        return result
    }
}