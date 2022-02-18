package org.example;

import org.example.pool.AgeIppool;
import org.example.util.AgIp;;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class controller
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        //创建线程池，20个线程
        //任务队列
        LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
        ThreadPoolExecutor tpool = new ThreadPoolExecutor(20, 25, 30,
                TimeUnit.SECONDS, tasks);
        System.out.println("初始化线程池");

        //创建代理IP池
        AgeIppool ippool = new AgeIppool(10, tpool);
        System.out.println("初始化代理IP池");

        System.out.println("准备取ip");
        while(true)
        {
            AgIp iptem = null;
            try
            {
                iptem = ippool.getip();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println("取出的ip地址为: "+iptem.getaddress()+" ||| 取出的ip端口为: "+iptem.getport());
        }
    }

}
