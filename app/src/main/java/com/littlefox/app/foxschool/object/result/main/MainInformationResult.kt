package com.littlefox.app.foxschool.`object`.result.main

import com.google.gson.annotations.SerializedName
import com.littlefox.app.foxschool.`object`.result.main.*
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*

class MainInformationResult
{
    @SerializedName("story")
    private val story : MainStoryInformationResult? = null

    @SerializedName("song")
    private val song : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()

    @SerializedName("bookshelves")
    private val bookshelves : ArrayList<MyBookshelfResult> = ArrayList<MyBookshelfResult>()

    @SerializedName("vocabularies")
    private val vocabularies : ArrayList<MyVocabularyResult> = ArrayList<MyVocabularyResult>()

    @SerializedName("company_information")
    private val company_information : CompanyInformationResult? = null

    @SerializedName("in_app_campaign")
    private val in_app_campaign : InAppCompaignResult? = null

    @SerializedName("homework")
    private val homework : String  = ""

    @SerializedName("news")
    private val news : String      = ""

    @SerializedName("files")
    private val files : FileInformationResult? = null

    fun getMainStoryInformation() : MainStoryInformationResult = story!!

    fun getMainSongInformationList() : ArrayList<SeriesInformationResult> = song

    fun getBookShelvesList() : ArrayList<MyBookshelfResult> = bookshelves

    fun getVocabulariesList() : ArrayList<MyVocabularyResult> = vocabularies

    fun getCompanyInformation() : CompanyInformationResult? = company_information

    fun getInAppCompaignInformation() : InAppCompaignResult? = in_app_campaign

    val isUpdateHomework : Boolean
        get()
        {
            if(homework.equals("Y"))
            {
                return true
            }
            return false
        }

    val isUpdateNews : Boolean
        get()
        {
            if(news.equals("Y"))
            {
                return true
            }
            return false
        }

    fun getFileInformation() : FileInformationResult? = files
}