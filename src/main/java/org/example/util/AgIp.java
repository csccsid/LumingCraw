package org.example.util;

//代理ip的实体类
public class AgIp
{
    private String address=null;
    private int port=0;
    private boolean state=false;

    public AgIp(String address, int port)
    {
        this.address=address;
        this.port=port;
    }

    public void setstate()
    {
        state=true;
    }

    public boolean getstate()
    {
        return state;
    }

    public String getaddress()
    {
        return address;
    }

    public int getport()
    {
        return port;
    }
}
