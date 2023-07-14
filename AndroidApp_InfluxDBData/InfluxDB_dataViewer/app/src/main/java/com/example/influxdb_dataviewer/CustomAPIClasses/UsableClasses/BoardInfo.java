package com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses;

public class BoardInfo
{
    public String Name;
    public String LastTimeStamp;
    public boolean Status;
    public String ID;
    public BoardInfo(String name, String lst, String id, boolean status)
    {
        this.Name=name;
        this.LastTimeStamp=lst;
        this.Status =status;
        this.ID=id;
    }
}
