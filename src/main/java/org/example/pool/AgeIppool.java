package org.example.pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.example.util.AgIp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//代理ip池
public class AgeIppool
{
    //四个网址轮流爬取
    String[] urlarray = new String[]{"http://www.66ip.cn/",
            "http://www.66ip.cn/areaindex_1/1.html",
            "http://www.66ip.cn/areaindex_2/1.html",
            "http://www.66ip.cn/areaindex_3/1.html"};
    //装初始ip的队列容器
    private ArrayBlockingQueue<AgIp> ipPool = new ArrayBlockingQueue<AgIp>(20);
    //用来返还ip的队列容器
    public ArrayBlockingQueue<AgIp> returnIpPool = new ArrayBlockingQueue<AgIp>(20);
    //用传递进来的线程池跑多线程
    ThreadPoolExecutor thPool=null;
    //控制ip池大小
    int size=0;
    //计数控制爬取ip的网站
    int count=0;
    String url="";

    //table中数rownumber个td标签才有一个是ip地址
    int tdcount=0;
    //爬到的临时ip地址
    String addresstemp=null;

    public AgeIppool(int size, ThreadPoolExecutor thpool)
    {
        this.thPool=thpool;
        this.size=size;
    }

    public ArrayBlockingQueue<AgIp> init()
    {

        thPool.execute(new Runnable()
        {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                while(true)
                {
                    //储存整个网页
                    StringBuffer data = new StringBuffer("");
                    //读取网页的流
                    InputStream input=null;
                    InputStreamReader inread=null;
                    BufferedReader inbread=null;

                    //三个网址轮流爬取
                    url=urlarray[count];
                    if(count<3)
                        count++;
                    else
                        count=0;
                    URL address;
                    HttpURLConnection uconnect = null;
                    try
                    {
                        System.out.println("爬第"+(count-1)+"个页面");
                        address = new URL(new URI(url).toASCIIString());
                        uconnect = (HttpURLConnection)address.openConnection();
                        uconnect.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.0; "
                                + "Windows NT 6.1; Maxthon;)");
                        //超时5秒
                        uconnect.setConnectTimeout(5000);

                        //用缓冲流能更快读取并且按行读取
                        input = uconnect.getInputStream();//getInputStream方法隐含了.connect方法
                        inread = new InputStreamReader(input,"utf-8");
                        inbread = new BufferedReader(inread);

                        String temp=inbread.readLine();
                        //按行读取整个网页html
                        while(temp!=null)
                        {
                            data.append(temp).append("\n");
                            temp=inbread.readLine();
                        }

                        //找出html中ip的位置
                        Document doc = Jsoup.parse(data.toString(),"utf-8");
                        //找到含有tr或td为ip和port或端口号的table元素
                        Elements etables = doc.select("table:has(tr:contains(2021))");
                        //找到table一共有多少列
                        Elements enumber = etables.select("tr").first().select("td  ");
                        int rownumber = enumber.size();
                        System.out.println("rownumber = "+rownumber);
                        //找到表格中的td
                        Elements es = etables.select("td");

                        //去掉第一行标题的td
                        for(int i=0;i<=rownumber-1;i++)
                            es.remove(0);

                        for(Element et:es)
                        {
                            //每行rownumber个td，只有前两个有用
                            if(tdcount>1)
                            {
                                if(tdcount==rownumber-1)
                                    tdcount=0;
                                else
                                    tdcount++;
                            } else
                            {
                                if(tdcount==0)
                                {
                                    addresstemp=et.text();
                                    tdcount++;
                                } else
                                {
                                    tdcount++;
                                    try
                                    {
                                        System.out.println("爬取一个ip放入池中");
                                        System.out.println("地址为  "+addresstemp+" ||| "+et.text());
                                        ipPool.put(new AgIp(addresstemp, Integer.parseInt(et.text())));
                                    } catch (NumberFormatException | InterruptedException e1)
                                    {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();

                                    }
                                }
                            }
                        }
                    } catch (MalformedURLException | URISyntaxException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally
                    {
                        //释放资源
                        if(uconnect!=null)
                            try
                            {
                                uconnect.disconnect();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        if(input!=null)
                            try
                            {
                                input.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        if(inread!=null)
                            try
                            {
                                inread.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        if(inbread!=null)
                            try
                            {
                                inbread.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                    }

                }

            }
        });

        return returnIpPool;
    }

    //获取ip
    public  void getIp() throws IOException
    {

        thPool.execute(new Runnable()
        {

            @Override
            public void run()
            {
                while(true)
                {
                    //没必要加这段，因为本身ippool使block的，而且加了这段会死锁
            /*
			while(ippool.isEmpty())
			{
				try
				{
					Thread.sleep(2000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
             */

                    AgIp ipTemp = null;
                    try
                    {
                        ipTemp = ipPool.take();
                    } catch (InterruptedException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    System.out.println("验证ip");
                    System.out.println(ipTemp.getaddress() + " --- " + ipTemp.getport());

                    //如果访问百度没问题则判定该代理ip存活
                    //每个返回的ip都保证是存活的
                    String baiduurl = "https://www.baidu.com";
                    URL baiduaddress;
                    //百度html第一行
                    String verify = "<!DOCTYPE html><!--STATUS OK-->";

                    InputStream input = null;
                    InputStreamReader inread = null;
                    BufferedReader inbread = null;
                    HttpURLConnection uconnect = null;
                    try
                    {
                        Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipTemp.getaddress(), ipTemp.getport()));
                        baiduaddress = new URL(new URI(baiduurl).toASCIIString());
                        uconnect = (HttpURLConnection) baiduaddress.openConnection(p);
                        uconnect.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.0; "
                                + "Windows NT 6.1; Maxthon;)");
                        //6秒超时
                        uconnect.setConnectTimeout(6000);

                        input = uconnect.getInputStream();
                        inread = new InputStreamReader(input, "utf-8");
                        inbread = new BufferedReader(inread);

                        //按行读取html
                        String temp = inbread.readLine();
                        //判断是否和百度页面相同
                        if (temp.equals(verify))
                        {
                            //放入返回容器列表
                            returnIpPool.add(ipTemp);
                            input.close();
                            inread.close();
                            inbread.close();
                        }else
                        {
                            System.out.println("存活失败,开始检验下一个代理ip");
                        }

                    } catch (MalformedURLException | URISyntaxException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally
                    {
                        //释放资源
                        if(uconnect!=null)
                            try
                            {
                                uconnect.disconnect();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        if(input!=null)
                            try
                            {
                                input.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        if(inread!=null)
                            try
                            {
                                inread.close();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        if(inbread!=null)
                            try
                            {
                                inbread.close();
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

