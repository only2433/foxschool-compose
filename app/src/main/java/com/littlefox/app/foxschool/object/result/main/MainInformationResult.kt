package com.littlefox.app.foxschool.`object`.result.main

import com.littlefox.app.foxschool.`object`.result.main.*
import com.littlefox.app.foxschool.`object`.result.story.SeriesInformationResult
import java.util.*

class MainInformationResult
{
    private val story : MainStoryInformationResult? = null
    private val song : ArrayList<SeriesInformationResult> = ArrayList<SeriesInformationResult>()
    private val bookshelves : ArrayList<MyBookshelfResult> = ArrayList<MyBookshelfResult>()
    private val vocabularies : ArrayList<MyVocabularyResult> = ArrayList<MyVocabularyResult>()
    private val company_information : CompanyInformationResult? = null
    private val in_app_campaign : InAppCompaignResult? = null
    private val homework : String  = ""
    private val news : String      = ""

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
}