package com.example.influxdb_dataviewer.ApiRelated.Connection;

import com.example.influxdb_dataviewer.CustomAPIClasses.GetJson.Json_DeviceData;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface API_interface {

    @GET ("/wemos/getConnection")
    Call<ArrayList<Json_DeviceData>> GetData ();

    @GET("/check/connection")
    Call<ResponseBody> CheckConnection();
}
