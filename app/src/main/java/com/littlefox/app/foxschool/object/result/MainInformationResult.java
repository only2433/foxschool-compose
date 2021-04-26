package com.littlefox.app.foxschool.object.result;

import net.littlefox.lf_app_fragment.object.result.common.ContentsBaseResult;
import net.littlefox.lf_app_fragment.object.result.common.SeriesBaseResult;
import net.littlefox.lf_app_fragment.object.result.littlefoxClass.ClassMainResult;

import java.util.ArrayList;

public class MainInformationResult
{
    private MainStoryInformationResult story = null;
    private MainSongInformationResult song = null;
    private ArrayList<MyBookshelfResult> bookshelves = new ArrayList<MyBookshelfResult>();
    private ArrayList<MyVocabularyResult> vocabularies = new ArrayList<MyVocabularyResult>();
    private CompanyInformationResult company_information = null;
    private InAppCompaignResult in_app_campaign = null;

    public MainStoryInformationResult getMainStoryInformation()
    {
        return story;
    }

    public MainSongInformationResult getMainSongInformation()
    {
        return song;
    }

    public ArrayList<MyBookshelfResult> getBookShelvesList()
    {
        return bookshelves;
    }

    public ArrayList<MyVocabularyResult> getVocabulariesList()
    {
        return vocabularies;
    }

    public CompanyInformationResult getCompanyInformation()
    {
        return company_information;
    }

    public InAppCompaignResult getInAppCompaignInformation()
    {
        return in_app_campaign;
    }

}
