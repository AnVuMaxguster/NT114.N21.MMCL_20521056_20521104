package com.example.influxdb_dataviewer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses.BoardInfo;
import com.example.influxdb_dataviewer.R;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardHolder> {

    ArrayList<BoardInfo> boardlist;
    Context mcontext;
    public BoardAdapter(ArrayList<BoardInfo> usageList, Context context)
    {
        this.boardlist=usageList;
        this.mcontext=context;
    }
    public void ResetData(ArrayList<BoardInfo>usageList)
    {
        this.boardlist=usageList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public BoardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.board_layout,parent,false);
        return new BoardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardHolder holder, int position) {
        BoardInfo currentBoard=boardlist.get(holder.getAdapterPosition());
        holder.BoardName.setText(currentBoard.Name);
        holder.BoardID.setText("ID: "+currentBoard.ID);
        holder.BoardStatus.setText(currentBoard.LastTimeStamp);
        if(currentBoard.Status==true) holder.BoardStatus.setTextColor(mcontext.getResources().getColor(R.color.confirmGreen));
        else holder.BoardStatus.setTextColor(mcontext.getResources().getColor(R.color.alertRed));
        if(currentBoard.Name.contains("Raspberry"))holder.BoardImage.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.raspberry));
        else holder.BoardImage.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.wemos));
    }

    @Override
    public int getItemCount() {
        return boardlist!=null? boardlist.size():0;
    }

    public class BoardHolder extends RecyclerView.ViewHolder {
        ImageView BoardImage;
        TextView BoardName,BoardStatus, BoardID;
        public BoardHolder(@NonNull View itemView) {
            super(itemView);
            BoardImage=itemView.findViewById(R.id.Board_Image);
            BoardName=itemView.findViewById(R.id.Board_Name);
            BoardStatus=itemView.findViewById(R.id.Board_Status);
            BoardID=itemView.findViewById(R.id.Board_ID);
        }
    }
}
