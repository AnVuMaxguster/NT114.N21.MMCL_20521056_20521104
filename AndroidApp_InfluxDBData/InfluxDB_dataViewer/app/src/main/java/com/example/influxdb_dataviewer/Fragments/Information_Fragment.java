package com.example.influxdb_dataviewer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.influxdb_dataviewer.Adapter.BoardAdapter;
import com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses.BoardInfo;
import com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses.DeviceData;
import com.example.influxdb_dataviewer.R;

import java.util.ArrayList;

public class Information_Fragment extends Fragment {
    BoardAdapter boardAdapter;
    ArrayList<DeviceData> UsageList;
    public Information_Fragment(ArrayList<DeviceData> currentList) {
        this.UsageList=currentList;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_information_, container, false);
        if(UsageList!=null) {
            RecyclerView BoardRecyclerView = v.findViewById(R.id.Information_BoardsRecyclerview);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
            BoardRecyclerView.setLayoutManager(linearLayoutManager);
            boardAdapter = new BoardAdapter(ComposingBoardData(UsageList), getActivity());
            BoardRecyclerView.setAdapter(boardAdapter);
        }
        return v;
    }
    private ArrayList<BoardInfo> setDefaultData()
    {
        ArrayList<BoardInfo> defaultlist=new ArrayList<>();
        defaultlist.add(new BoardInfo("Raspberry pi","• 1/1/2020","1",true));
        defaultlist.add(new BoardInfo("Wemos D1","• 1/1/2020","2",false));
        defaultlist.add(new BoardInfo("Wemos D1","• 1/1/2020","3",false));
        return defaultlist;
    }
    private ArrayList<BoardInfo> ComposingBoardData(ArrayList<DeviceData> currentList)
    {
        ArrayList<BoardInfo> ComposedList=new ArrayList<>();
        for (DeviceData board :
                currentList) {
            ComposedList.add(new BoardInfo(board.Name,board.TimeStamp,board.ID,board.Status));
        }
        return ComposedList;
    }
    public void RefreshList(ArrayList<DeviceData> newList)
    {
       this.UsageList=newList;
        if(UsageList!=null)boardAdapter.ResetData(ComposingBoardData(UsageList));
    }
}