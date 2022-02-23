package org.example;

import org.example.DAO.operation;
import org.example.pool.AgeIppool;
import org.example.pool.SqlConpool;
import org.example.util.AgIp;
import org.example.util.Book;
import org.example.util.Episode;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class controller
{
    public static void main( String[] args )
    {
        //创建线程池，20个线程
        //任务队列
        LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
        ThreadPoolExecutor thPool = new ThreadPoolExecutor(20, 25, 30,
                TimeUnit.SECONDS, tasks);
        System.out.println("初始化线程池");

        //创建代理IP池并启动
        try
        {
            AgeIppool ipPool = new AgeIppool(10, thPool);
            System.out.println("初始化代理IP池");
            ArrayBlockingQueue<AgIp> returnIpPool = ipPool.init();
            ipPool.getIp();

            //创建数据库连接池
            SqlConpool scp = new SqlConpool(20);

            //新建数据库操作类
            operation op = new operation(scp);

            while(!op.isEmptyNI()) {
                Book book = new Book();

                //获取书籍名
                String bookName = op.getNotInclude();
                book.setBookName(bookName);

                //爬取书本网站，确认能爬到该书，则继续之后操作
                crawContent cC = new crawContent();
                String bookUrl = cC.getSource(bookName);

                if (bookUrl != null) {
                    //获取id
                    String lastId = op.getLastBookId();
                    String bookId = "b" + (Integer.parseInt(lastId.substring(1)) + 1);
                    book.setId(bookId);
                    book.setBookName(bookName);

                    //在书籍列表中插入
                    op.insertBookList(book);

                    //创建新书表
                    op.creatTable(book);

                    //爬取目录
                    ArrayBlockingQueue<Episode> bookContent = null;
                    bookContent = cC.getContent(bookUrl, scp);

                    //爬取章节内容
                    crawEpisode cEpisode = new crawEpisode(bookContent, thPool, returnIpPool);
                    cEpisode.crawLauch();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }



        /* Test
        System.out.println("准备取ip");
        while(true)
        {
            AgIp ipTem = null;
            try
            {
                ipPool.getip();
                ipTem =
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println("取出的ip地址为: "+iptem.getaddress()+" ||| 取出的ip端口为: "+iptem.getport());
        }
        */
    }

}
