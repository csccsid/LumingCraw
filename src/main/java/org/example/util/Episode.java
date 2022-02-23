package org.example.util;

//章节的实体类
public class Episode
{
    private String link = null;
    private String title = null;
    private String substance = null;

    public Episode()
    {
    }

    public synchronized void setLink(String link)
    {
        this.link=link;
    }

    public synchronized void setepisodetitle(String title)
    {
        this.title=title;
    }

    public void setSubstance(String substance){ this.substance = substance; }

    public String getLink()
    {
        return link;
    }

    public String getepisodetitle()
    {
        return title;
    }

    public String getSubstance()
    {
        return  substance;
    }
}