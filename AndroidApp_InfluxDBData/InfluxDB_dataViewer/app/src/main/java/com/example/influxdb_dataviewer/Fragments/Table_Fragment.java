package com.example.influxdb_dataviewer.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.influxdb_dataviewer.R;
import com.example.influxdb_dataviewer.RecyclerView.DataTableAdapter;
import com.example.influxdb_dataviewer.Spinner.Category;
import com.example.influxdb_dataviewer.Spinner.CategoryAdapter;
import com.influxdb.query.FluxTable;

import java.util.ArrayList;
import java.util.List;

public class Table_Fragment extends Fragment {
    private List<FluxTable> UsageTables;

    public Table_Fragment(List<FluxTable>tables) {
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
        View v= inflater.inflate(R.layout.fragment_table_, container, false);

        if(UsageTables !=null)
        {
            RecyclerView DataTable=v.findViewById(R.id.Table_DataTable);
            LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
            DataTable.setLayoutManager(linearLayoutManager);
            DataTable.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

            Spinner MeasurementSpinner=v.findViewById(R.id.Table_SpinnerMeasurement);
            CategoryAdapter categoryAdapter=new CategoryAdapter(getActivity(),R.layout.item_select,getMeasurementSpinnerList(UsageTables));
            MeasurementSpinner.setAdapter(categoryAdapter);
            MeasurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    DataTableAdapter dataTableAdapter=new DataTableAdapter(getActivity(), UsageTables.get((int)id).getRecords());
                    DataTable.setAdapter(dataTableAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }
        return v;
    }
    public List<Category> getMeasurementSpinnerList(List<FluxTable> tables)
    {
        if(tables!=null)
        {
            List<Category> DaList=new ArrayList<>();
            for(FluxTable table:tables)
            {
                DaList.add(new Category(table.getRecords().get(0).getMeasurement(),""));
            }
            return DaList;
        }
        return null;
    }

}