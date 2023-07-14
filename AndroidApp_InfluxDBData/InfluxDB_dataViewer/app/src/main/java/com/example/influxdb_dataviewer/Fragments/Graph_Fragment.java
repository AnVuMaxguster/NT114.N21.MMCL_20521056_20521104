package com.example.influxdb_dataviewer.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.influxdb_dataviewer.MainActivity;
import com.example.influxdb_dataviewer.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Graph_Fragment extends Fragment {

    List<FluxTable> UsageTables;
    LineChart lineChart;
    public Graph_Fragment(List<FluxTable> tables) {
        try{
            this.UsageTables =tables;
        }
        catch (Exception ex)
        {
            Log.e("LinkingTableToMain", ex.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_graph_, container, false);
        lineChart=v.findViewById(R.id.Graph_LineChart);
        if(UsageTables!=null)
        {
            UpdateGraph();
        }

        return v;
    }

    private void UpdateGraph()
    {
        LineData lineData=new LineData(ComposeDataSets());
        lineChart.setData(lineData);
        lineChart.invalidate();
        XAxis xAxis=lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        ArrayList<String> Labels=CustomXAxisLabel(lineData);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
//                for(int i=0;i<lineData.getDataSetByIndex(0).getEntryCount();i++)
//                {
//                    if((int)value==(int)lineData.getDataSetByIndex(0).getEntryForIndex(i).getX())return CustomXAxisLabel(lineData).get((int)value);
//                }
//                return "";
                try {
                    return Labels.get((int)value);
                }
                catch (Exception ex)
                {
                    return "";
                }
            }
        });
        xAxis.setLabelRotationAngle(300);
    }
    private ArrayList<ILineDataSet> ComposeDataSets()
    {
        ArrayList<ILineDataSet> DataSets=new ArrayList<>();
        for (FluxTable table :
                UsageTables) {
            ArrayList<Entry>RecordEntry=new ArrayList<>();
            int DupCount=0;
            Instant LatestInstance=Instant.now();
            for(int i=0;i<table.getRecords().size();i++)
            {
                float tempValue = Float.valueOf(table.getRecords().get(i).getValue().toString());
//                for(int t=RecordEntry.size()-1;t<RecordEntry.size() && t>=0;t++)
//                {
//                    if(tempValue==RecordEntry.get(t).getY()) DupCount++;
//                }
//                if(DupCount>=2)
//                {
//                    if(Duration.between(LatestInstance,table.getRecords().get(i).getTime()).toMinutes()>=10)DupCount=0;
//                }
                if(DupCount<2) {
                    RecordEntry.add(new Entry(i, tempValue));
                    LatestInstance=table.getRecords().get(i).getTime();
                }
            }
            LineDataSet lineDataSet=new LineDataSet(RecordEntry,table.getRecords().get(0).getMeasurement());
            int ColorCode=CreateRandomColor().toArgb();
            lineDataSet.setColor(ColorCode);
            lineDataSet.setCircleColor(ColorCode);
            lineDataSet.setCircleRadius(6);
            lineDataSet.setLineWidth(3);
            DataSets.add(lineDataSet);
        }
        return  DataSets;
    }

    private ArrayList<String> CustomXAxisLabel(LineData lineData)
    {
        ArrayList<String> XAxisLabel=new ArrayList<>();
        for (FluxRecord record :
                UsageTables.get(0).getRecords()) {
            Date Time=((MainActivity)getActivity()).ConvertTimeToGMT_Plus7(record.getTime());
            String pattern = "HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            XAxisLabel.add(simpleDateFormat.format(Time.getTime()));
        }
//        ArrayList<Integer> HaveDataIndex=new ArrayList<>();
//        for(int i=0;i<lineData.getDataSetByIndex(0).getEntryCount();i++)
//        {
//            HaveDataIndex.add((int)lineData.getDataSetByIndex(0).getEntryForIndex(i).getX());
//        }
//        for(int i=0;i<XAxisLabel.size();i++)
//        {
//            if(!HaveDataIndex.contains(i))XAxisLabel.set(i,"");
//        }
        return XAxisLabel;
    }

    public Color CreateRandomColor()
    {
        Random rand=new Random();
        float r=rand.nextFloat();
        float g=rand.nextFloat();
        float b=rand.nextFloat();
        return Color.valueOf(r,g,b);
    }
    public void PullDataFromMain_AndRefresh(List<FluxTable>tables)
    {
        if(tables!=null) {
            this.UsageTables = tables;
            UpdateGraph();
        }
    }
}