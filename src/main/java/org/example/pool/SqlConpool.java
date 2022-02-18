package org.example.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

//数据库连接池
public class SqlConpool
{
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_url = "jdbc:mysql://localhost:3306/luming?useSSL=false&" +
            "serverTimezone=UTC&characterEncoding=UTF-8";
    static final String user = "root";
    static final String pass = "12345";

    //因为get和return方法都是线程安全型的，所以连接list不需要是线程安全型的了。
    private ArrayList<Connection> conal = new ArrayList<>();
    private int size;

    public SqlConpool(int size)
    {
        this.size=size;
        init();
    }

    //开始创建连接
    public void init()
    {
        //这里不能使用try-with-resource的处理异常方式，因为这些连接都需要是"活"的，不要被自动关闭了
        try
        {
            //这个是利用"com.mysql.cj.jdbc.Driver"有个静态代码块，初始化的时候会自动加载
            Class.forName(JDBC_DRIVER);
            System.out.println("数据库驱动加载成功 ！");

            for(int i=0;i<=size-1;i++)
            {
                Connection con = DriverManager.getConnection(DB_url,user,pass);
                conal.add(con);
            }
        } catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //获取连接，是线程安全型的
    public synchronized Connection getconnection()
    {
        while(conal.isEmpty())
        {
            try
            {
                this.wait();
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return conal.remove(0);
    }

    //返回连接，也是线程安全型的
    public synchronized void returnconnection(Connection c)
    {
        conal.add(c);
        this.notifyAll();
    }

    //关闭所有连接
    public synchronized void close()
    {
        while(!conal.isEmpty())
            try
            {
                conal.remove(0).close();
            } catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}
