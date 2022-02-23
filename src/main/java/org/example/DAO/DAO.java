package org.example.DAO;

import org.example.pool.SqlConpool;
import org.example.util.Book;
import org.example.util.Episode;

public interface DAO
{
    SqlConpool scp = null;

    //判断未收录列表是否为空
    public boolean isEmptyNI();

    //获取未收录列表中书名
    public String getNotInclude();

    //获取最后的bookid，从而生成新书的id
    public String getLastBookId();

    //创建新书表
    public void creatTable(Book book);

    //在booklist中插入新书
    public void insertBookList(Book book);

    //在新书中插入章节内容
    public void insertContent(Episode episode);
}
