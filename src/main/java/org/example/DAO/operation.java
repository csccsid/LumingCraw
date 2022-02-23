package org.example.DAO;

import org.example.pool.SqlConpool;
import org.example.util.Book;
import org.example.util.Episode;

public class operation implements DAO
{
    SqlConpool scp = null;

    public operation(SqlConpool scp)
    {
        this.scp = scp;
    }

    @Override
    public boolean isEmptyNI() {
        return false;
    }

    @Override
    public String getNotInclude() {
        return null;
    }

    @Override
    public String getLastBookId() {
        return null;
    }

    @Override
    public void creatTable(Book book)
    {
    }

    @Override
    public void insertBookList(Book book)
    {
    }

    @Override
    public void  insertContent(Book book, Episode episode)
    {
    }
}
