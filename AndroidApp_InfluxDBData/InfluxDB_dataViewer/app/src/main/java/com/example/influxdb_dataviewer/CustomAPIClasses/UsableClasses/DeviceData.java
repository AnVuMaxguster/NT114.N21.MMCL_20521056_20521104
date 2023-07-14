package com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses;

import java.util.ArrayList;

public class DeviceData {
    public String Name;
    public String IP;
    public String ID;
    public boolean Status;
    public String TimeStamp;

    public DeviceData(String name, String IP, String ID, boolean status,String timeStamp) {
        Name = name;
        this.IP = IP;
        this.ID = ID;
        Status = status;
        TimeStamp=timeStamp;
    }
}
