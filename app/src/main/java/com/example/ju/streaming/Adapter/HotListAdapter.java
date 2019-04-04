package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.Activity.VideoPlayerActivity;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HotListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<VideoListItem> videoListItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout panel;
//        FrameLayout myframe;
        ImageView thumbnail;
        CircleImageView bjimage;
        TextView title;
//        TextView playtime;
        TextView bjnickname;
        TextView date;
        TextView hit;

        MyViewHolder(View view){
            super(view);
//            myframe = view.findViewById(R.id.myframe);
            thumbnail = view.findViewById(R.id.thumbnail);
            bjimage = view.findViewById(R.id.bjimage);
            panel = view.findViewById(R.id.panel);
            title = view.findViewById(R.id.title);
//            playtime = view.findViewById(R.id.playtime);
            bjnickname = view.findViewById(R.id.bjnickname);
            date = view.findViewById(R.id.date);
            hit = view.findViewById(R.id.hit);
        }
    }

    public HotListAdapter(ArrayList<VideoListItem> videoListItemArrayList, Context context) {
        this.videoListItemArrayList = videoListItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hot_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            MyViewHolder myViewHolder = (MyViewHolder) holder;

            Glide.with(context).load(BaseURL+videoListItemArrayList.get(position).getThumbnail().trim().substring(2)).into(myViewHolder.thumbnail);
            Glide.with(context).load(videoListItemArrayList.get(position).getImage()).into(myViewHolder.bjimage);
            myViewHolder.title.setText(videoListItemArrayList.get(position).getTitle());
//            myViewHolder.playtime.setText(videoListItemArrayList.get(position).getPlaytime());
            myViewHolder.bjnickname.setText(videoListItemArrayList.get(position).getNickname());
            myViewHolder.date.setText(videoListItemArrayList.get(position).getDate());
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
//                    Toast.makeText(context, videoListItemArrayList.get(position).getThumbnail(), Toast.LENGTH_SHORT).show();
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
        Log.e("어댑터에 추가", "추가");
    }

    public void clear(){
        videoListItemArrayList.clear();
    }

}
