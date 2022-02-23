package org.example;

import org.example.DAO.operation;
import org.example.pool.SqlConpool;
import org.example.util.AgIp;
import org.example.util.Book;
import org.example.util.Episode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class crawEpisode
{
    //队列存放Episode类
    ArrayBlockingQueue<Episode> urlQ = null;
    //记录目录章节队列初始大小
    AtomicInteger contentSize = new AtomicInteger(urlQ.size());
    //计数器，记录成功爬取章节数目
    AtomicInteger crawIndex = new AtomicInteger(0);
    //判断是否爬取完成
    AtomicBoolean judge = new AtomicBoolean(true);

    ThreadPoolExecutor thPool = null;
    ArrayBlockingQueue<AgIp> agIpPool = null;
    //临时储存要用的代理IP
    AgIp ipTemp = null;
    //计数器，为代理ip使用次数计数，超过20次就换
    AtomicInteger ipCount = new AtomicInteger(0);

    //数据库连接类
    SqlConpool scp = null;
    //用于定位书本,因为之后不对其改变，所以没必要线程安全
    Book book = null;

    public  crawEpisode(ArrayBlockingQueue<Episode> urlQ, ThreadPoolExecutor thPool,
                        ArrayBlockingQueue<AgIp> agIpPool, SqlConpool scp, Book book)
    {
        // TODO Auto-generated constructor stub
        this.urlQ = urlQ;
        this.thPool = thPool;
        this.agIpPool = agIpPool;
        this.scp = scp;
        this.book = book;
    }


    public void crawLauch()
    {
        // TODO Auto-generated method stub

        aaa:
        while(judge.get())
        {

            //多线程开始爬取
            thPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    //记录重复，到三次时放弃
                    int repeatCount = 0;

                    Episode epTemp = null;
                    try
                    {
                        //若为空且没爬完则等待
                        if(!urlQ.isEmpty())
                            epTemp = urlQ.take();
                        else
                        {
                            if(crawIndex.get() < contentSize.get())
                                this.wait();
                            else
                                //成功爬完所有章节
                                judge.set(false);
                        }
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }


                    bbb:
                    while(repeatCount <= 3 && epTemp != null)
                    {
                        repeatCount++;

                        //当重复超过3次时，放弃尝试将章节放回队列，该线程结束
                        if (repeatCount > 3)
                        {
                            try
                            {
                                //放入例表中，提醒所有等待线程
                                urlQ.put(epTemp);
                                this.notifyAll();
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            System.out.println("重复次数太多，换章节爬取");
                            break bbb;
                        }

                        //开始爬取
                        //章节链接
                        String urlepisode=epTemp.getLink();
                        //章节标题
                        URL episodeLink = null;
                        //整个网页的html
                        StringBuffer cont = new StringBuffer("");
                        //按行读取网页的临时
                        String contemp=null;

                        //章节内容
                        StringBuffer episodeSubstance = new StringBuffer();

                        InputStream inEpisode = null;
                        InputStreamReader inReadEpisode = null;
                        BufferedReader inBufferEpisode = null;

                        try
                        {
                            //若已使用20次，则换新的
                            if(ipCount.equals(20))
                            {
                                synchronized (ipTemp)
                                {
                                    ipTemp = agIpPool.take();
                                }
                                ipCount.set(0);
                            }else
                                ipCount.incrementAndGet();

                            Proxy p = null;

                            synchronized (ipTemp)
                            {
                                p = new Proxy(Proxy.Type.HTTP,
                                        new InetSocketAddress(ipTemp.getaddress(), ipTemp.getport()));
                            }

                            episodeLink = new URL(new URI(urlepisode).toASCIIString());
                            HttpURLConnection uconnectepisode = (HttpURLConnection) episodeLink.openConnection(p);
                            uconnectepisode.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; "
                                    + "rv:85.0) Gecko/20100101 Firefox/85.0");
                            //超时5秒
                            uconnectepisode.setConnectTimeout(5000);

                            //用缓冲流能更快读取并且按行读取
                            inEpisode = uconnectepisode.getInputStream();
                            inReadEpisode = new InputStreamReader(inEpisode,"utf-8");
                            inBufferEpisode = new BufferedReader(inReadEpisode);


                            //按行读取网页整个html
                            contemp=inBufferEpisode.readLine();
                            while(contemp!=null)
                            {
                                cont.append(contemp).append("\n");
                                contemp=inBufferEpisode.readLine();
                            }

                            //Jsoup解析
                            Document docepsode = Jsoup.parse(cont.toString(),"utf-8");
                            //找到id属性为content的元素即正文
                            org.jsoup.nodes.Element eepsode = docepsode.getElementById("content");
                            //把正文中的text提取出来
                            Document docepsode2 = Jsoup.parse(eepsode.html());
                            Elements ees = docepsode2.getElementsByClass("content_detail");
                            for(org.jsoup.nodes.Element eet:ees )
                            {
                                episodeSubstance.append(eet.text());
                                episodeSubstance.append("\r\n\r\n");
                            }


                            //设置章节属性，并存入数据库
                            epTemp.setSubstance(String.valueOf(episodeSubstance));
                            operation operation = new operation(scp);
                            operation.insertContent(book, epTemp);


                        } catch (MalformedURLException | URISyntaxException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        } finally
                        {
                            try
                            {
                                if(inEpisode != null)
                                    inEpisode.close();
                                if(inReadEpisode != null)
                                    inReadEpisode.close();
                                if(inBufferEpisode != null)
                                    inBufferEpisode.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            });
        }
    }
}
