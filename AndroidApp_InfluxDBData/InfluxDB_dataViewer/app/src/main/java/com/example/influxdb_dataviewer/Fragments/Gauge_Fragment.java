package com.example.influxdb_dataviewer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.example.influxdb_dataviewer.CustomResponse;
import com.example.influxdb_dataviewer.MainActivity;
import com.example.influxdb_dataviewer.R;
import com.example.influxdb_dataviewer.RecyclerView.GaugeAdapter;
import com.influxdb.query.FluxTable;

import java.util.ArrayList;
import java.util.List;

public class Gauge_Fragment extends Fragment {

    List<FluxTable>UsageTables;
    CustomResponse CR;

    ArcGauge temp_gauge, hum_gauge;
    TextView shock_sign, temp_status, hum_status;
    double temp_val, hum_val;
    RecyclerView recyclerView;
    GaugeAdapter gaugeAdapter;

    public Handler UpdateHandler = new Handler();
    public Runnable UpdateRunnable;
    public Gauge_Fragment(List<FluxTable> tables, CustomResponse cr) {
        
        this.UsageTables=tables;
        this.CR= cr;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_gauge_, container, false);
        recyclerView=v.findViewById(R.id.Gauge_RecyclerView);
        SetupGlobalData(UsageTables,false);
        UpdateRunnable = new Runnable() {
            @Override
            public void run() {
                getData();
            }
        };

        UpdateHandler.postDelayed(UpdateRunnable, 500);
        return v;
    }
    public void SetupGlobalData(List<FluxTable> tables,boolean newData)
    {
        if(newData==true)this.UsageTables=tables;
        if(UsageTables!=null) {
            ArrayList<GaugeAdapter.GaugeValue> listGaugeValue=((MainActivity)getActivity()).ComposeGaugeValueList(UsageTables);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newData==false) setFirstData(listGaugeValue);
                    else gaugeAdapter.SetNewData(listGaugeValue);
                    UpdateHandler.postDelayed(UpdateRunnable,5000);
                }
            });
        }
    }
    private void setFirstData(ArrayList<GaugeAdapter.GaugeValue> Datalist)
    {
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        gaugeAdapter=new GaugeAdapter(Datalist,getActivity());
        recyclerView.setAdapter(gaugeAdapter);
    }
    private void getData()
    {
        CR.OnResponse(null);
    }
}