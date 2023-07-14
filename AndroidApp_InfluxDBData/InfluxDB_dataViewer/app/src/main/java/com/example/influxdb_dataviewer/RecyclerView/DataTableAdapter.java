package com.example.influxdb_dataviewer.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.influxdb_dataviewer.Fragments.Table_Fragment;
import com.example.influxdb_dataviewer.MainActivity;
import com.example.influxdb_dataviewer.R;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.InfluxQLQueryResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataTableAdapter extends RecyclerView.Adapter<DataTableAdapter.DataTable_Holder> {
    Context mContext;
    List<FluxRecord> RecordsList;

    public DataTableAdapter(Context mContext, List<FluxRecord> recordsList) {
        this.mContext = mContext;
        RecordsList = recordsList;
    }

    @NonNull
    @Override
    public DataTable_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.table_data,parent,false);
        return new DataTable_Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataTable_Holder holder, int position) {
        Date Time=((MainActivity)mContext).ConvertTimeToGMT_Plus7(RecordsList.get(holder.getAdapterPosition()).getTime());
        String pattern = "dd-MM-yy hh:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        holder.TimeTV.setText(simpleDateFormat.format(Time.getTime()));
        holder.ValueTV.setText(RecordsList.get(holder.getAdapterPosition()).getValue().toString());
    }

    @Override
    public int getItemCount() {
        if(RecordsList!=null)return RecordsList.size();
        else return 0;
    }

    public class DataTable_Holder extends RecyclerView.ViewHolder {
        TextView TimeTV,ValueTV;
        public DataTable_Holder(@NonNull View itemView) {
            super(itemView);
            TimeTV=itemView.findViewById(R.id.TableData_Time);
            ValueTV=itemView.findViewById(R.id.TableData_Value);
        }
    }
}
