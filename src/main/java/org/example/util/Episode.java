package org.example.util;

//章节的实体类
public class Episode
{
    private String link=null;
    private String title=null;

    public Episode()
    {
    }

    public synchronized void setlink(String link)
    {
        this.link=link;
    }

    public synchronized void setepisodetitle(String title)
    {
        this.title=title;
    }

    public String getlink()
    {
        return link;
    }

    public String getepisodetitle()
    {
        return title;
    }
}