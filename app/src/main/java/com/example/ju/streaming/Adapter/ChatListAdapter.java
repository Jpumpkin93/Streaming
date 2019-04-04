package com.example.ju.streaming.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.Activity.ChatActivity;
import com.example.ju.streaming.ItemClass.ChatListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static ArrayList<ChatListItem> chatListItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";



    public class MyViewHolder extends RecyclerView.ViewHolder {


        CircleImageView youimage;
        LinearLayout panel;
        TextView younickname;
        TextView lastmessage;
        TextView hit;

        MyViewHolder(View view){
            super(view);
            youimage = view.findViewById(R.id.youimage);
            panel = view.findViewById(R.id.panel);
            younickname = view.findViewById(R.id.younickname);
            lastmessage = view.findViewById(R.id.lastmessage);
            hit = view.findViewById(R.id.hit);
        }
    }


    public ChatListAdapter(ArrayList<ChatListItem> chatListItemArrayList, Context context){
        this.chatListItemArrayList = chatListItemArrayList;
        this.context = context;
    }

    public ChatListAdapter(){

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item_layout, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;
//        if(chatListItemArrayList.get(position).getHit() == 0){
//            myViewHolder.hit.setVisibility(View.INVISIBLE);
//        }

        String hit = Integer.toString(chatListItemArrayList.get(position).getHit());
        Glide.with(context).load(BaseURL + chatListItemArrayList.get(position).getYouimage()).into(myViewHolder.youimage);
        myViewHolder.younickname.setText(chatListItemArrayList.get(position).getYounickname());
        myViewHolder.lastmessage.setText(chatListItemArrayList.get(position).getLastmessage());
        myViewHolder.hit.setText(hit);

        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, chatListItemArrayList.get(position).getYounickname(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("younickname",chatListItemArrayList.get(position).getYounickname());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatListItemArrayList.size();
    }

    public void add(String youimage, String younickname, String lastmessage, int hit){
        ChatListItem listitem = new ChatListItem(youimage, younickname, lastmessage, hit);
        chatListItemArrayList.add(listitem);
    }

    public void change(String youimage, String younickname, String lastmessage, int hit){
        boolean check = true;
        ChatListItem listitem = new ChatListItem(youimage, younickname, lastmessage, hit);
        for(int i = 0; i < chatListItemArrayList.size(); i++){
            if(listitem.getYounickname().equals(chatListItemArrayList.get(i).getYounickname())){
//                int pasthit = chatListItemArrayList.get(i).getHit();
//                chatListItemArrayList.remove(i);
//                MyViewHolder holder = new MyViewHolder();
//                ChatListItem item = new ChatListItem(youimage.substring(21), younickname, lastmessage, pasthit+hit);
//                Log.e("change이미지",item.getYouimage());
//                chatListItemArrayList.add(0,item);
//                chatListItemArrayList.get(i).setLastmessage(lastmessage);
                chatListItemArrayList.get(i).setHit(chatListItemArrayList.get(i).getHit()+hit);
                Log.e("changehit", Integer.toString(chatListItemArrayList.get(i).getHit()));
                chatListItemArrayList.get(i).setLastmessage(lastmessage);
                check = false;
            }
        }
        if(check){
            String image = youimage.replaceFirst(BaseURL,"");
            listitem.setYouimage(image);
            chatListItemArrayList.add(listitem);
        }
    }

    public void clear(){
        chatListItemArrayList.clear();
    }
//    public void add(String image, String title){
//        ChatListItem listitem = new ChatListItem(image, title, "");
//        chatListItemArrayList.add(listitem);
//    }
}
