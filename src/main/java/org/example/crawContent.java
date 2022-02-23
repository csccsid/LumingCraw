package org.example;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.example.pool.SqlConpool;
import org.example.util.Episode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * 找到爬取书本的页面，并爬取每个章节的网址
 */
public class crawContent
{
    //队列存放Episode类
    ArrayBlockingQueue<Episode> urlQueue = new ArrayBlockingQueue<Episode>(300);
    
    private String webUrl = "https://www.bswtan.com/modules/article/search.php";
    private String bookname = null;

    //爬取指定书本网站
    public String getSource(String bookname)
    {
        this.bookname = bookname;
        //读取网页html
        String data = null;
        //返回网址
        String bookUrl = null;
        //response
        CloseableHttpResponse response = null;


        /*
        //所需的数据只需读取html代码的前n行，节约资源
        int c=0;

        //设置POST请求的流
        DataOutputStream out =null;

        //读取目录的流
        InputStream input=null;
        InputStreamReader inread=null;
        BufferedReader inbread=null;
         */

        /**
         jvm会自动回收HttpClient，但是response不关闭的话，会一直占用httpclient导致其无法被jvm回收。
         所以要关闭response
         */
        try
        {
            URL address = new URL(new URI(webUrl).toASCIIString());
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(webUrl);
            //伪装请求头部
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.0; "
                    + "Windows NT 6.1; Maxthon;)");
            //存放参数键值对的容器
            ArrayList<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("searchkey", bookname));

            //在请求体中设置post参数
            httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            // 设置httpClient连接服务器超时时间：6秒
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(2000).setConnectionRequestTimeout(2000)
                    .setSocketTimeout(2000).build();
            httpPost.setConfig(requestConfig);
            //执行，获取响应
            response = client.execute(httpPost);
            //获取响应码,若不为200则说明连接失败
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                return null;
            } else
            {
                HttpEntity entity = response.getEntity();
                data = entity.toString();
                //如果用这个方法，内部会自动关闭response
                //data =  EntityUtils.toString(response.getEntity());\

                //解析html代码
                Document doc1 = Jsoup.parse(data.toString(), "utf-8");
                //找到id属性为的元素即目录
                Elements e = doc1.getElementsByTag("tr");
                //把目录中的html提取出来
                Document doc2 = Jsoup.parse(e.html());
                //在目录中找到每个属性为href的即每个具体章节链接
                Elements es = doc2.getElementsByAttribute("href");
                //存入队列，章节不用去重

                System.out.println("书本网址为：" + bookUrl);
                return bookUrl;

            }

            /**
             JDK自带的HttpURLConnection的写法，两种做一个对比，HttpClient主要简单在封装了输入输出流。
             还一个是JDK自带的有点乱，HttpClient的感觉规范一些
             */
            /*
            HttpURLConnection uconnect = (HttpURLConnection)address.openConnection();
            //伪装请求头部
            uconnect.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.0; "
                    + "Windows NT 6.1; Maxthon;)");
            //设置请求参数
            uconnect.setRequestMethod("POST");
            // 设置是否向connection输出，因为这个是post请求，参数要放在
            // http正文内，因此需要设为true
            uconnect.setDoOutput(true);
            connection.setDoInput(true);
            // Post 请求不能使用缓存
            connection.setUseCaches(false);
            // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            //超时5秒
            uconnect.setConnectTimeout(5000);

            //uconnect.getOutputStream会隐含的进行connect。
            DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());
            //请求正文改写添加
            String content；
            out.writeBytes(content);
            out.flush();
            out.close();

            //用缓冲流能更快读取并且按行读取
            input = uconnect.getInputStream();
            inread = new InputStreamReader(input,"utf-8");
            inbread = new BufferedReader(inread);


            //按行读取整个网页html
            String temp=inbread.readLine();
            while(temp!=null && c<=1000)
            {
                data.append(temp).append("\n");
                temp=inbread.readLine();
                c++;
            }
            */


        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //SocketAddress
        catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally
        {
            try
            {
                if (response != null)
                    response.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return data;
    }



    //爬取指定书本目录，即每个章节的网址
    public ArrayBlockingQueue<Episode> getContent(String bookUrl, SqlConpool scp)
    {
        //存储页面html
        StringBuffer htmlData = new StringBuffer("");

        //读取目录的流
        InputStream input=null;
        InputStreamReader inread=null;
        BufferedReader inbread=null;

        //爬取目录
        try
        {
            URL address = new URL(new URI(bookUrl).toASCIIString());
            HttpURLConnection uconnect = (HttpURLConnection)address.openConnection();
            uconnect.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.0; "
                    + "Windows NT 6.1; Maxthon;)");
            //超时5秒
            uconnect.setConnectTimeout(5000);

            //用缓冲流能更快读取并且按行读取
            input = uconnect.getInputStream();
            inread = new InputStreamReader(input,"utf-8");
            inbread = new BufferedReader(inread);

            //按行读取整个网页html
            String temp=inbread.readLine();
            while(temp!=null)
            {
                htmlData.append(temp).append("\n");
                temp=inbread.readLine();
            }

            Document doc1 = Jsoup.parse(htmlData.toString(),"utf-8");
            //找到id属性为list的元素即目录
            Element e = doc1.getElementById("list");
            //把目录中的html提取出来
            Document doc2 = Jsoup.parse(e.html());
            //在目录中找到每个属性为href的即每个具体章节链接
            Elements es = doc2.getElementsByAttribute("href");
            //存入队列，章节不用去重
            for(Element et:es)
            {
                Episode etemp = new Episode();
                etemp.setlink(bookUrl+et.attr("href"));
                etemp.setepisodetitle(et.text());
                urlQueue.add(etemp);

                System.out.println(et.attr("href"));

            }
            //System.out.println(e.attributes());
            //System.out.println(e.text());
            //System.out.println(e);

        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //SocketAddress
        catch (URISyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally
        {
            try
            {
                input.close();
                inread.close();
                inbread.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return urlQueue;
    }
}
