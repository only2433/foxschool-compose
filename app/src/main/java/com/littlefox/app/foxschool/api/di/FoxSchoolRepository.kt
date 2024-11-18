package com.littlefox.app.foxschool.api.di

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.JsonObject
import com.littlefox.app.foxschool.`object`.result.forum.paging.ForumBasePagingResult
import com.littlefox.app.foxschool.api.ApiService
import com.littlefox.app.foxschool.api.base.safeApiCall
import com.littlefox.app.foxschool.api.paging.ForumPagingSource
import com.littlefox.app.foxschool.api.paging.SearchPagingSource
import com.littlefox.app.foxschool.common.Common

import com.littlefox.app.foxschool.`object`.data.quiz.QuizStudyRecordData
import com.littlefox.app.foxschool.`object`.result.content.ContentsBaseResult
import com.littlefox.app.foxschool.`object`.result.search.paging.ContentBasePagingResult
import com.littlefox.app.foxschool.`object`.result.search.paging.SearchListPagingResult
import com.littlefox.app.foxschool.`object`.result.vocabulary.VocabularyDataResult
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import javax.inject.Inject

class FoxSchoolRepository @Inject constructor(private val remote: ApiService)
{
    /**
     * 버전 정보
     */
    suspend fun getVersion(deviceID: String, pushAddress: String, pushOn: String) = safeApiCall{
        remote.initAsync(deviceID, pushAddress, pushOn)
    }

    /**
     * 자동 로그인
     */
    suspend fun getAuthMe() = safeApiCall {
        remote.authMeAsync()
    }

    /**
     * 메인 정보
     */
    suspend fun getMain() = safeApiCall {
        remote.mainAsync()
    }

    /**
     * 비밀번호 변경
     */
    suspend fun setChangePassword(currentPassword: String, changePassword: String, changePasswordConfirm: String) = safeApiCall {
        remote.passwordChangeAsync(currentPassword, changePassword, changePasswordConfirm)
    }

    /**
     * 비밀번호 변경 - 다음에 하기
     */
    suspend fun setChangePasswordToDoNext() = safeApiCall {
        remote.passwordChangeNextAsync()
    }

    /**
     * 비밀번호 변경 - 유지 하기
     */
    suspend fun setChangePasswordToKeep() = safeApiCall {
        remote.passwordChangeKeepAsync()
    }

    /**
     * 학교 리스트 가져오기
     */
    suspend fun getSchoolList() = safeApiCall {
        remote.schoolListAsync()
    }

    /**
     * 로그인
     */
    suspend fun login(id : String, password : String, schoolCode : String) = safeApiCall {
        remote.loginAsync(id, password, schoolCode)
    }

    /**
     * 스토리 컨텐츠 리스트 가져오기
     */
    suspend fun getStoryContentsList(displayID: String) = safeApiCall {
        remote.storyContentsListAsync(displayID)
    }

    /**
     * 송 컨텐츠 리스트 가져오기
     */
    suspend fun getSongContentsList(displayID : String) = safeApiCall {
        remote.songContentsListAsync(displayID)
    }

    /**
     * 카테고리 리스트 가져오기
     */
    suspend fun getCategoryList(displayID : String) = safeApiCall {
        remote.categoryListAsync(displayID)
    }

    /**
     * 학생 - 숙제관리(달력) 정보 가져오기
     */
    suspend fun getStudentHomeworkCalendar(year : String, month : String) = safeApiCall {
        remote.studentHomeworkCalendarAsync(year, month)
    }

    /**
     * 학생 - 숙제현황(리스트) 정보 가져오기
     */
    suspend fun getStudentHomeworkList(homeworkNumber : Int) = safeApiCall {
        remote.studentHomeworkListAsync(homeworkNumber)
    }

    /**
     * 학생 - 학습자 한마디 등록
     */
    suspend fun setStudentCommentRegister(comment : String, homeworkNumber : Int) = safeApiCall {
        remote.studentCommentRegisterAsync(comment, homeworkNumber)
    }

    /**
     * 학생 - 학습자 한마디 수정
     */
    suspend fun setStudentCommentUpdate(comment : String, homeworkNumber : Int) = safeApiCall {
        remote.studentCommentUpdateAsync(comment, homeworkNumber)
    }

    /**
     * 학생 - 학습자 한마디 삭제
     */
    suspend fun setStudentCommentDelete(homeworkNumber : Int) = safeApiCall {
        remote.studentCommentDeleteAsync(homeworkNumber)
    }

    /**
     * 선생님 - 학급 리스트 가져오기
     */
    suspend fun getTeacherHomeworkClassList() = safeApiCall {
        remote.teacherHomeworkClassListAsync()
    }

    /**
     * 선생님 - 숙제관리(달력) 정보 가져오기
     */
    suspend fun getTeacherHomeworkCalendar(classID: String, year : String, month : String) = safeApiCall {
        remote.teacherHomeworkCalendarAsync(classID, year, month)
    }

    /**
     * 선생님 - 숙제현황 정보 가져오기
     */
    suspend fun getTeacherHomeworkStatus(classID : Int, homeworkNumber : Int) = safeApiCall {
        remote.teacherHomeworkStatusAsync(classID, homeworkNumber)
    }

    /**
     * 선생님 - 숙제현황 상세보기 정보 가져오기
     */
    suspend fun getTeacherHomeworkDetail(classID : Int, homeworkNumber : Int, userID : String) = safeApiCall {
        remote.teacherHomeworkDetailAsync(classID, homeworkNumber, userID)
    }

    /**
     * 선생님 - 숙제내용 정보 가져오기
     */
    suspend fun getTeacherHomeworkContents(classID : Int, homeworkNumber : Int) = safeApiCall {
        remote.teacherHomeworkContentsAsync(classID, homeworkNumber)
    }

    /**
     * 선생님 - 숙제검사
     */
    suspend fun setTeacherHomeworkChecking(homeworkNumber : Int, classID : Int, userID : String, evaluationState : String, evaluationComment : String) = safeApiCall {
        if (evaluationComment != "")
        {
            remote.teacherHomeworkCheckingAsync(homeworkNumber, classID, userID, evaluationState, evaluationComment)
        }
        else
        {
            remote.teacherHomeworkCheckingAsync(homeworkNumber, classID, userID, evaluationState)
        }
    }

    suspend fun getAuthContentPlay(contentID: String, isHighRevoluation: Boolean) = safeApiCall {
        if(isHighRevoluation)
        {
            remote.authContentsPlayAsync(contentID +"/Y")
        }
        else
        {
            remote.authContentsPlayAsync(contentID)
        }
    }

    suspend fun savePlayerStudyLog(contentID: String, playType: String, playTime: String, homeworkNumber : Int) = safeApiCall {
        if(homeworkNumber != 0)
        {
            remote.savePlayerStudyAsync(contentID, playType, playTime, homeworkNumber)
        }
        else
        {
            remote.savePlayerStudyAsync(contentID, playType, playTime)
        }

    }

    suspend fun addBookshelfContents(bookshelfID: String, contentsList: ArrayList<ContentsBaseResult>) = safeApiCall {
        var queryMap = mutableMapOf<String, String>()
        for(i in contentsList.indices)
        {
            queryMap["content_ids[$i]"] = contentsList.get(i).id
        }

        remote.addBookshelfContentsAsync(bookshelfID, queryMap)
    }

    suspend fun getQuizInformation(contentID: String) = safeApiCall {
        remote.quizInformationAsync(contentID)
    }

    suspend fun saveQuizRecord(answerData: QuizStudyRecordData, homeworkNumber: Int = 0) = safeApiCall {
        var remoteObject: JsonObject = JsonObject()
        var answerDataArray: JSONArray = JSONArray()
        var itemObject: JSONObject
        for(i in 0 until answerData.getQuizResultInformationList().size)
        {
            itemObject = JSONObject()
            try
            {
                itemObject.put(
                    "chosen_number",
                    java.lang.String.valueOf(answerData.getQuizResultInformationList()[i].getChosenNumber())
                )
                itemObject.put(
                    "correct_number",
                    answerData.getQuizResultInformationList()[i].getCorrectNumber()
                )
                itemObject.put(
                    "question_numbers",
                    java.lang.String.valueOf(answerData.getQuizResultInformationList()[i].getQuestionSequence())
                )
            }
            catch(e : JSONException)
            {
                e.printStackTrace()
            }
            answerDataArray.put(itemObject)
        }
        remoteObject.addProperty("results_json", answerDataArray.toString())
        if(homeworkNumber != 0)
        {
            remoteObject.addProperty("hw_no", homeworkNumber)
        }

        remote.saveQuizRecordAsync(
            answerData.getContentId(),
            remoteObject)

    }

    suspend fun getVocabularyContentsList(id : String) = safeApiCall {
        remote.getVocabularyContentsList(id)
    }


    suspend fun createBookshelf(name: String, color: String) = safeApiCall {
        remote.createBookshelf(name, color)
    }

    suspend fun updateBookshelf(bookshelfID: String, name: String, color: String) = safeApiCall {
        remote.updateBookshelf(bookshelfID, name, color)
    }

    suspend fun deleteBookshelf(bookshelfID: String) = safeApiCall {
        remote.deleteBookshelf(bookshelfID)
    }

    suspend fun createVocabulary(name: String, color: String) = safeApiCall {
        remote.createVocabulary(name, color)
    }

    suspend fun updateVocabulary(vocabularyID: String, name: String, color: String) = safeApiCall {
        remote.updateVocabulary(vocabularyID, name, color)
    }

    suspend fun deleteVocabulary(vocabularyID: String) = safeApiCall {
        remote.deleteVocabulary(vocabularyID)
    }

    suspend fun addVocabularyContents(contentID : String, vocabularyID : String, itemList : ArrayList<VocabularyDataResult>) = safeApiCall {
        val queryMap = mutableMapOf<String, String>()
        queryMap["content_id"] =  contentID
        for(i in itemList.indices)
        {
            queryMap["word_ids[$i]"] = itemList[i].getID()
        }
        remote.addVocabularyContents(
            vocabularyID,
            queryMap
        )
    }

    suspend fun deleteVocabularyContents(vocabularyID : String, itemList : ArrayList<VocabularyDataResult>) = safeApiCall {
        var queryMap = mutableMapOf<String, String>()
        for(i in itemList.indices)
        {
            queryMap["words[$i][word_id]"] = itemList[i].getID()
            queryMap["words[$i][content_id]"] = itemList[i].getContentID()
        }

        remote.deleteVocabularyContents(
            vocabularyID,
            queryMap
        )
    }

    suspend fun flashcardSaveAsync(contentID : String) = safeApiCall{
        remote.saveFlashcardRecordAsync(contentID)
    }

    suspend fun getBookshelfContentsList(bookshelfID : String) = safeApiCall {
        remote.getBookshelfContentsList(bookshelfID)
    }

    suspend fun deleteBookshelfContents(bookshelfID : String, list: ArrayList<ContentsBaseResult>) = safeApiCall {
        var queryMap = mutableMapOf<String, String>()
        for(i in list.indices)
        {
            queryMap["content_ids[$i]"] = list[i].id
        }
        remote.deleteBookshelfContents(
            bookshelfID,
            queryMap)
    }

    suspend fun getForumFAQList(pageCount : Int, currentPage : Int) = safeApiCall {
        remote.forumFAQListAsync(pageCount, currentPage)
    }
    suspend fun getForumNewsList(pageCount : Int, currentPage : Int) = safeApiCall {
        remote.forumNewsListAsync(pageCount, currentPage)
    }

    fun getForumListStream() : Flow<PagingData<ForumBasePagingResult>>
    {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = Common.PAGE_LOAD_COUNT),
            pagingSourceFactory = { ForumPagingSource(remote)}
        ).flow
    }

    fun getSearchListStream(searchType : String = "", keyword: String) : Flow<PagingData<ContentBasePagingResult>>
    {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = Common.PAGE_LOAD_COUNT),
            pagingSourceFactory = {SearchPagingSource(remote, searchType, keyword)}
        ).flow
    }


}