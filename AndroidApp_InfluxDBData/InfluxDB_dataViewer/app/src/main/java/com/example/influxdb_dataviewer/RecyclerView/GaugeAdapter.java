package com.example.influxdb_dataviewer.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.influxdb_dataviewer.R;

import java.util.ArrayList;

public class GaugeAdapter extends RecyclerView.Adapter<GaugeAdapter.GaugeHolder> {
    public static class GaugeValue
    {
        public String Measurement;
        public int GaugeType;
        public double Value;

        public GaugeValue(String name, int gaugeType, double value) {
            Measurement = name;
            GaugeType = gaugeType;
            Value = value;
        }
    }
    ArrayList<GaugeValue> dataList;
    Context mcontext;

    public GaugeAdapter(ArrayList<GaugeValue> dataList, Context context) {
        this.dataList = dataList;
        this.mcontext=context;
    }
    public void SetNewData(ArrayList<GaugeValue>newData)
    {
        this.dataList=newData;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public GaugeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.gauge_layout,parent,false);
        return new GaugeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GaugeHolder holder, int position) {
        GaugeValue currentValue=dataList.get(holder.getAdapterPosition());
        ToState1(holder);
        if(currentValue.GaugeType==0)
        {
            preset(holder.arcGauge);
        }
        else preset(holder.halfGauge);
        holder.gaugeMeasurement.setText(currentValue.Measurement);
        setData(currentValue,holder);
    }
    private void preset(Object obj)
    {
        // Basic temperature categories for goods
        Range Low = new Range();
        Low.setFrom(0); Low.setTo(30); Low.setColor(mcontext.getResources().getColor(R.color.frozen));
        Range Normal = new Range();
        Normal.setFrom(31); Normal.setTo(70); Normal.setColor(mcontext.getResources().getColor(R.color.warm));
        Range High = new Range();
        High.setFrom(71); High.setTo(100); High.setColor(mcontext.getResources().getColor(R.color.extreme));
        //

        // Init temp gauge
        try {
            ArcGauge temp_gauge=(ArcGauge)obj;
            temp_gauge.addRange(Low);
            temp_gauge.addRange(Normal);
            temp_gauge.addRange(High);
            temp_gauge.setMinValue(0);
            temp_gauge.setMaxValue(100);
            temp_gauge.setVisibility(View.VISIBLE);
        }
        catch (Exception ex)
        {
            HalfGauge temp_gauge=(HalfGauge) obj;
            temp_gauge.addRange(High);
            temp_gauge.addRange(Normal);
            temp_gauge.addRange(Low);
            temp_gauge.setMinValue(0);
            temp_gauge.setMaxValue(100);
            temp_gauge.setVisibility(View.VISIBLE);

        }
    }
    private void ToState1(GaugeHolder holder)
    {
        holder.arcGauge.setVisibility(View.INVISIBLE);
        holder.halfGauge.setVisibility(View.INVISIBLE);
    }
    public void setData(GaugeValue data,GaugeHolder holder)
    {
        if(data.Value>=71)
        {
            holder.gaugeStatus.setText("High");
            holder.gaugeStatus.setTextColor(mcontext.getResources().getColor(R.color.extreme));
        }
        else if(data.Value>=31)
        {
            holder.gaugeStatus.setText("Normal");
            holder.gaugeStatus.setTextColor(mcontext.getResources().getColor(R.color.warm));
        }
        else{
            holder.gaugeStatus.setText("Low");
            holder.gaugeStatus.setTextColor(mcontext.getResources().getColor(R.color.frozen));
        }
        holder.halfGauge.setValue(data.Value);
        holder.arcGauge.setValue(data.Value);
    }
    @Override
    public int getItemCount() {
        if(dataList!=null) return dataList.size();
        return 0;
    }

    public class GaugeHolder extends RecyclerView.ViewHolder {
        TextView gaugeStatus,gaugeMeasurement;
        ArcGauge arcGauge;
        HalfGauge halfGauge;
        public GaugeHolder(@NonNull View itemView) {
            super(itemView);
            gaugeStatus=itemView.findViewById(R.id.gauge_Status);
            gaugeMeasurement=itemView.findViewById(R.id.gauge_Measurement);
            arcGauge=itemView.findViewById(R.id.gauge_ArcGauge);
            halfGauge=itemView.findViewById(R.id.gauge_HalfGauge);
        }
    }
}
