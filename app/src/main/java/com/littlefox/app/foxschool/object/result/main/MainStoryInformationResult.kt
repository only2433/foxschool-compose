package com.littlefox.app.foxschool.object.result.main;

import net.littlefox.lf_app_fragment.object.result.common.SeriesInformationResult;

import java.util.ArrayList;

public class MainStoryInformationResult
{
    private ArrayList<SeriesInformationResult> levels = null;
    private ArrayList<SeriesInformationResult> categories = null;

    public ArrayList<SeriesInformationResult> getContentByLevelToList()
    {
        return levels;
    }

    public void setContentByLevelToList(ArrayList<SeriesInformationResult> data)
    {
        levels = data;
    }

    public ArrayList<SeriesInformationResult> getContentByCategoriesToList()
    {
        return categories;
    }
}
