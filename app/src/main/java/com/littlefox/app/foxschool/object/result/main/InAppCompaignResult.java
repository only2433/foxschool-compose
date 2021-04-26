package com.littlefox.app.foxschool.object.result.main;

public class InAppCompaignResult
{
    private int id = 0;
    private int article_id = 0;
    private String title = "";
    private String content = "";
    private String btn1_use = "";
    private String btn1_mode = "";
    private String btn1_text = "";
    private String btn1_link = "";
    private String btn2_use = "";
    private String btn2_mode = "";
    private String btn2_text = "";
    private int not_display_days = 0;

    public int getID()
    {
        return id;
    }

    public int getArticleID()
    {
        return article_id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public boolean isButton1Use()
    {
        if(btn1_use.equals("Y"))
        {
            return true;
        }

        return false;
    }

    public String getButton1Mode()
    {
        return btn1_mode;
    }

    public String getButton1Text()
    {
        return btn1_text;
    }

    public String getButton1Link()
    {
        return btn1_link;
    }

    public boolean isButton2Use()
    {
        if(btn2_use.equals("Y"))
        {
            return true;
        }

        return false;
    }

    public String getButton2Mode()
    {
        return btn2_mode;
    }

    public String getButton2Text()
    {
        return btn2_text;
    }

    public int getNotDisplayDays()
    {
        return not_display_days;
    }
}
