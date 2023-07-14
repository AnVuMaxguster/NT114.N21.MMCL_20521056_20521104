package com.example.influxdb_dataviewer.CustomAPIClasses.GetJson;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class Json_DeviceData {
    @SerializedName("board_name")
    public String Board_name;
    @SerializedName("board_id")
    public String Board_id;
    @SerializedName("board_ip")
    public String Board_ip;
    @SerializedName("status")
    public boolean Board_status;
    @SerializedName("timestamp")
    public long TimeStamp;
}
