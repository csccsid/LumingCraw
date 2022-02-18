package org.example.pool;

import java.util.ArrayList;

//线程池的线程
public class Poolthread extends Thread
{
    ArrayList<Runnable> tasks = null;
    Runnable task=null;

    public Poolthread(ArrayList<Runnable> tasks)
    {
        this.tasks=tasks;
    }

    public void run()
    {
        while(true)
        {
            if(tasks.isEmpty())
                try
                {
                    tasks.wait();
                } catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            task=tasks.remove(0);
            task.run();
        }
    }
}
