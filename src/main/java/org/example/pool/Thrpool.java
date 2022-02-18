package org.example.pool;

import java.util.ArrayList;

//线程池
public class Thrpool
{
    ArrayList<Runnable> tasks = new ArrayList<>();
    int size=0;

    public Thrpool(int size)
    {
        this.size=size;
        System.out.println("进程池启动"+size);
        init();
    }

    //创建线程
    public void init()
    {
        synchronized (tasks)
        {
            for(int i=0;i<=size-1;i++)
                new Poolthread(tasks).start();
        }
    }

    //在任务列表中添加任务
    public void add(Runnable task)
    {
        synchronized (tasks)
        {
            tasks.add(task);
            tasks.notifyAll();
        }
    }
}