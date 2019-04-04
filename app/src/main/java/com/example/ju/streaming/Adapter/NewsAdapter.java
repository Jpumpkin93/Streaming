package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.ItemClass.NewsItem;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<NewsItem> newsItemArrayList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout panel;
        ImageView newsimage;
        TextView newstext;

        MyViewHolder(View view){
            super(view);
            panel = view.findViewById(R.id.panel);
            newsimage = view.findViewById(R.id.newsimage);
            newstext = view.findViewById(R.id.newstext);
        }
    }

    public NewsAdapter(ArrayList<NewsItem> newsItemArrayList, Context context) {
        this.newsItemArrayList = newsItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.newstext.setText(newsItemArrayList.get(position).getNewstext());
        String image = "http://" + newsItemArrayList.get(position).getNewsimage().substring(2);
        Glide.with(context).load(image).into(myViewHolder.newsimage);
        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.yna.co.kr" + newsItemArrayList.get(position).getNewsurl());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsItemArrayList.size();
    }

    public void add(String newsimage, String newstext, String newsurl){
        NewsItem item = new NewsItem(newsimage, newstext, newsurl);
        newsItemArrayList.add(item);
    }
}
