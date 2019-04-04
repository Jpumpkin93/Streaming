package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.content.Intent;
import android.icu.text.Collator;
import android.media.Image;
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
import com.example.ju.streaming.Activity.ChatActivity;
import com.example.ju.streaming.Activity.VideoPlayerActivity;
import com.example.ju.streaming.ItemClass.LiveListItem;
import com.example.ju.streaming.ItemClass.UserListItem;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<VideoListItem> videoListItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout panel;
        ImageView thumbnail;
        TextView title;
        TextView date;
//        TextView playtime;
        TextView hit;

        MyViewHolder(View view){
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            title = view.findViewById(R.id.title);
            panel = view.findViewById(R.id.panel);
            date = view.findViewById(R.id.date);
//            playtime = view.findViewById(R.id.playtime);
            hit = view.findViewById(R.id.hit);

        }
    }

    public VideoListAdapter(ArrayList<VideoListItem> videoListItemArrayList, Context context) {
        this.videoListItemArrayList = videoListItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Glide.with(context).load(BaseURL+videoListItemArrayList.get(position).getThumbnail().substring(2)).into(myViewHolder.thumbnail);
        myViewHolder.title.setText(videoListItemArrayList.get(position).getTitle());
        myViewHolder.date.setText(videoListItemArrayList.get(position).getDate());
//        myViewHolder.playtime.setText(videoListItemArrayList.get(position).getPlaytime());
        myViewHolder.hit.setText(videoListItemArrayList.get(position).getHit());
        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("nickname",videoListItemArrayList.get(position).getNickname());
                intent.putExtra("image",videoListItemArrayList.get(position).getImage());
                intent.putExtra("thumbnail",videoListItemArrayList.get(position).getThumbnail());
                intent.putExtra("title",videoListItemArrayList.get(position).getTitle());
                intent.putExtra("content",videoListItemArrayList.get(position).getContent());
                intent.putExtra("video",videoListItemArrayList.get(position).getVideo());
                intent.putExtra("hit",videoListItemArrayList.get(position).getHit());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoListItemArrayList.size();
    }

    public void add(String nickname, String image, String thumbnail, String title, String content, String video, String date, String playtime, String hit){
        VideoListItem listitem = new VideoListItem(nickname, image, thumbnail, title, content, video, date, playtime, hit);
        videoListItemArrayList.add(0,listitem);
    }

    public void clear(){
        videoListItemArrayList.clear();
    }

}
