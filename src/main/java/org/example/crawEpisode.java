package org.example;

import org.example.pool.SqlConpool;
import org.example.util.AgIp;
import org.example.util.Episode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class crawEpisode
{
    ArrayBlockingQueue<Episode> urlQ = null;//队列存放Episode类
    SqlConpool sqlConPool = null;
    ThreadPoolExecutor thPool = null;
    ArrayBlockingQueue<AgIp> agIpPool = null;
    AgIp ageip = null;
    String bookname = "";
    //判断是否成功爬取
    boolean judge = false;
    //记录重复，次数太多时放弃
    int repeatcount = 0;

    public  crawEpisode(ArrayBlockingQueue<Episode> urlQ, ThreadPoolExecutor thPool,
                        ArrayBlockingQueue<AgIp> agIpPool)
    {
        // TODO Auto-generated constructor stub
        this.urlQ = urlQ;
        this.thPool = thPool;
        this.agIpPool = agIpPool;
    }


    public void crawLauch()
    {
        // TODO Auto-generated method stub
        Episode eptemp=null;
        try
        {
            eptemp = urlQ.take();

            bbb:
            while(!judge)
            {
                repeatcount++;
                //当重复超过3次时，放弃尝试将章节放回队列，该线程结束
                if(repeatcount>3)
                {
                    urlQ.put(eptemp);

                    System.out.println("重复次数太多，下次再读取该章节");
                    break bbb;
                }
                //章节链接
                String urlepisode=eptemp.getlink();
                //章节标题
                String episodetitle=eptemp.getepisodetitle();
                URL episodelink;
                //整个网页的html
                StringBuffer cont = new StringBuffer("");
                //按行读取网页的临时
                String contemp=null;
                //存储章节的sql
                String sqlEp=null;
                //章节内容
                StringBuffer episodecontent = new StringBuffer();

                Connection sqlcEp = sqlconpool.getconnection();
                Statement statEp=null;

                InputStream inepisode=null;
                InputStreamReader inreadepisode=null;
                BufferedReader inbepisode=null;

                try
                {
                    Proxy p = new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(ageip.getaddress(), ageip.getport()));
                    episodelink = new URL(new URI(urlepisode).toASCIIString());
                    HttpURLConnection uconnectepisode = (HttpURLConnection) episodelink.openConnection(p);
                    uconnectepisode.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; "
                            + "rv:85.0) Gecko/20100101 Firefox/85.0");
                    //超时5秒
                    uconnectepisode.setConnectTimeout(5000);

                    //用缓冲流能更快读取并且按行读取
                    inepisode = uconnectepisode.getInputStream();
                    inreadepisode = new InputStreamReader(inepisode,"utf-8");
                    inbepisode = new BufferedReader(inreadepisode);


                    //按行读取网页整个html
                    contemp=inbepisode.readLine();
                    while(contemp!=null)
                    {
                        cont.append(contemp).append("\n");
                        contemp=inbepisode.readLine();
                    }
                    Document docepsode = Jsoup.parse(cont.toString(),"utf-8");
                    //找到id属性为content的元素即正文
                    org.jsoup.nodes.Element eepsode = docepsode.getElementById("content");
                    //把正文中的text提取出来
                    Document docepsode2 = Jsoup.parse(eepsode.html());
                    Elements ees = docepsode2.getElementsByClass("content_detail");
                    for(org.jsoup.nodes.Element eet:ees )
                    {
                        episodecontent.append(eet.text());
                        episodecontent.append("\r\n\r\n");
                    }

                    //存入数据库
                    statEp=sqlcEp.createStatement();
                    sqlEp="update "+bookname+" set content = '"+episodecontent.toString()+
                            "' where link = '"+urlepisode+"';";
                    statEp.executeUpdate(sqlEp);
                    //章节存入成功
                    judge=true;
                    System.out.println("章节存入成功");

                } catch (MalformedURLException | URISyntaxException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SQLException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally
                {
                    if(statEp!=null)
                    {
                        try
                        {
                            statEp.close();
                        } catch (SQLException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    sqlconpool.returnconnection(sqlcEp);
                    try
                    {
                        inepisode.close();
                        inreadepisode.close();
                        inbepisode.close();
                        //fwepisode.close();
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
