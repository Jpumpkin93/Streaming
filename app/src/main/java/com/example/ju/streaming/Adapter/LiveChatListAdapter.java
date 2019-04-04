package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.LiveListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    static ArrayList<ChatItem> LiveChatListArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userimage;
        TextView id;
        TextView message;

        MyViewHolder(View view){
            super(view);

            userimage = view.findViewById(R.id.userimage);
            id = view.findViewById(R.id.usernickname);
            message = view.findViewById(R.id.message);
        }
    }

    public LiveChatListAdapter(ArrayList<ChatItem> LiveChatListArrayList, Context context){
        this.LiveChatListArrayList = LiveChatListArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_chat_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;

        Glide.with(context).load(LiveChatListArrayList.get(position).getImage()).into(myViewHolder.userimage);
        myViewHolder.id.setText(LiveChatListArrayList.get(position).getId());
        myViewHolder.message.setText(LiveChatListArrayList.get(position).getMessage());

    }

    @Override
    public int getItemCount() {
        return LiveChatListArrayList.size();
    }

    public void add(String id, String message, String image){
        ChatItem listitem = new ChatItem(id, message, image);
        LiveChatListArrayList.add(listitem);
    }
}
