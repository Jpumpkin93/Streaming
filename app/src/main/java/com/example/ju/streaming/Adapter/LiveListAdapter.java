package com.example.ju.streaming.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.Activity.LivePlayerActivity;
import com.example.ju.streaming.ItemClass.ChatListItem;
import com.example.ju.streaming.ItemClass.LiveListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<LiveListItem> LiveListItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout panel;
        CircleImageView broadcasterimage;
        TextView title;
        TextView broadcasternickname;
        ImageView thumbnail;

        MyViewHolder(View view){
            super(view);
            panel = view.findViewById(R.id.panel);
            broadcasterimage = view.findViewById(R.id.broadcasterimage);
            title = view.findViewById(R.id.title);
            broadcasternickname = view.findViewById(R.id.broadcasternickname);
            thumbnail = view.findViewById(R.id.thumbnail);
        }
    }

    public LiveListAdapter(ArrayList<LiveListItem> LiveListItemArrayList, Context context){
        this.LiveListItemArrayList = LiveListItemArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_list_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Glide.with(context).load(LiveListItemArrayList.get(position).getImage()).into(myViewHolder.broadcasterimage);
        Glide.with(context).load(BaseURL+LiveListItemArrayList.get(position).getThumbnail().substring(2)).into(myViewHolder.thumbnail);
        myViewHolder.title.setText(LiveListItemArrayList.get(position).getTitle());
        myViewHolder.broadcasternickname.setText(LiveListItemArrayList.get(position).getNickname());
        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context,R.style.MyAlertDialogStyle);
                alert.setMessage("시청 하시겠습니까?");
                alert.setCancelable(false);
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(context,LivePlayerActivity.class);
                        i.putExtra("id",LiveListItemArrayList.get(position).getId());
                        i.putExtra("nickname",LiveListItemArrayList.get(position).getNickname());
                        i.putExtra("image",LiveListItemArrayList.get(position).getImage());
                        i.putExtra("title",LiveListItemArrayList.get(position).getTitle());
                        i.putExtra("liveaddress",LiveListItemArrayList.get(position).getLiveaddress());
                        context.startActivity(i);
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return LiveListItemArrayList.size();
    }

    public void add(String id, String image, String title, String nickname, String thumbnail, String liveaddress){
        LiveListItem listitem = new LiveListItem(id, image, title, nickname, thumbnail, liveaddress);
        LiveListItemArrayList.add(listitem);
    }

    public void clear(){
        LiveListItemArrayList.clear();
    }

}
