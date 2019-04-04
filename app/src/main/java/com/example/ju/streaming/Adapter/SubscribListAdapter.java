package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.Activity.ChannelActivity;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubscribListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<SubscribItem> subscribItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";


    public class MyViewHolder extends RecyclerView.ViewHolder {


        CircleImageView bjimage;
        LinearLayout panel;
        TextView bjnickname;

        MyViewHolder(View view){
            super(view);
            bjimage = view.findViewById(R.id.bjimage);
            panel = view.findViewById(R.id.panel);
            bjnickname = view.findViewById(R.id.bjnickname);
        }
    }

    public SubscribListAdapter(ArrayList<SubscribItem> subscribItemArrayList, Context context){
        this.subscribItemArrayList = subscribItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscrib_list_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Glide.with(context).load(subscribItemArrayList.get(position).getBjimage()).into(myViewHolder.bjimage);
        myViewHolder.bjnickname.setText(subscribItemArrayList.get(position).getBjnickname());
        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ChannelActivity.class);
                intent.putExtra("nickname",subscribItemArrayList.get(position).getBjnickname());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscribItemArrayList.size();
    }

    public void add(SubscribItem item){
        subscribItemArrayList.add(item);
    }

    public void clear(){
        subscribItemArrayList.clear();
    }

    public ArrayList<SubscribItem> list(){
        return subscribItemArrayList;
    }
}
