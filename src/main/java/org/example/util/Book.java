package org.example.util;

import java.io.Serializable;
import java.util.ArrayList;

public class Book implements Serializable//为了能序列化，一定要实现Serializable接口
{
    //书籍唯一标识id
    private String bookId;
    //书名
    private String bookName;
    //爬该书籍的地址
    private String link;
    //书籍作者
    private String author;
    //书籍简介
    private String BookAbstract;
    //总章节数
    private int episodeNumber;
    //书籍的内容，按章节分
    private ArrayList<Episode> bookEpisode = null;



    public void setId(String bookId)
    {
        this.bookId = bookId;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public void setBookAbstract(String BookAbstract)
    {
        this.BookAbstract = BookAbstract;
    }

    public void setBookEpisode(ArrayList<Episode> bookEpisode)
    {
        this.bookEpisode = bookEpisode;
        episodeNumber = bookEpisode.size();
    }

    public void setBookEpisodeNumber(int EpisodeNumber)
    {
        this.episodeNumber = EpisodeNumber;
    }




    public String getBookId()
    {
        return bookId;
    }

    public String getBookName()
    {
        return bookName;
    }

    public String getLink()
    {
        return link;
    }

    public String getBookAbstract()
    {
        return BookAbstract;
    }

    public String getAuthor()
    {
        return author;
    }

    public int getEpisodeNumber()
    {
        return episodeNumber;
    }

    public ArrayList<Episode> getBookEpisode()
    {
        return bookEpisode;
    }

}