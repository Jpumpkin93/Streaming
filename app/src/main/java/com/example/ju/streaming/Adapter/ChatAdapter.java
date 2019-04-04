package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ChatItem> chatItemArrayList;
    String usernickname;
    Context context;

    public class YouViewHolder extends RecyclerView.ViewHolder {


        LinearLayout youLayout;
        CircleImageView youimage;
        TextView youid;
        TextView youmessage;


        YouViewHolder(View view){
            super(view);
            youLayout = view.findViewById(R.id.youLayout);
            youimage = view.findViewById(R.id.youimage);
            youid = view.findViewById(R.id.youid);
            youmessage = view.findViewById(R.id.youmessage);
        }


    }

    public static class MeViewHolder extends RecyclerView.ViewHolder {


        LinearLayout meLayout;
        TextView memessage;


        MeViewHolder(View view){
            super(view);
            meLayout = view.findViewById(R.id.meLayout);
            memessage = view.findViewById(R.id.memessage);
        }


    }

    public ChatAdapter(ArrayList<ChatItem> chatItemArrayList, String usernickname, Context context){
        this.chatItemArrayList = chatItemArrayList;
        this.usernickname = usernickname;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        switch (viewType){
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_me_layout, parent, false);

                return new MeViewHolder(v);
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_you_layout, parent, false);

                return new YouViewHolder(v);
            case -1:
                return null;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof MeViewHolder){
            ((MeViewHolder) holder).memessage.setText(chatItemArrayList.get(position).getMessage());
        }
        else if(holder instanceof YouViewHolder){
            Glide.with(context).load(chatItemArrayList.get(position).getImage()).into(((YouViewHolder) holder).youimage);
            ((YouViewHolder) holder).youid.setText(chatItemArrayList.get(position).getId());
            ((YouViewHolder) holder).youmessage.setText(chatItemArrayList.get(position).getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatItemArrayList.size();
    }

    public void add(String id, String message, String image){
        ChatItem chatItem = new ChatItem(id,message,image);
        Log.e("들어갓니?",chatItem.toString());
        chatItemArrayList.add(chatItem);
        Log.e("들어왔어",chatItemArrayList.toString());
    }

    public void changeimage(String image){
        for(int i = 0; i<chatItemArrayList.size(); i++){
            if(!chatItemArrayList.get(i).getId().equals(usernickname)){
                chatItemArrayList.get(i).setImage(image);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(chatItemArrayList.get(position).getId().equals(usernickname)){
            return 1;
        }
        else if(!chatItemArrayList.get(position).getId().equals(usernickname)){
            return 2;
        }
        else{
            return -1;
        }
    }
}

