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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.Activity.ChatActivity;
import com.example.ju.streaming.ItemClass.UserListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {


    ArrayList<UserListItem> userListItemArrayList;
    Context context;

    String BaseURL = "http://13.209.72.139/";


    public class MyViewHolder extends RecyclerView.ViewHolder {


        CircleImageView image;
        LinearLayout panel;
        TextView nickname;

        MyViewHolder(View view){
            super(view);
            image = view.findViewById(R.id.profile_image);
            panel = view.findViewById(R.id.panel);
            nickname = view.findViewById(R.id.nickname);
        }
    }

    public UserListAdapter(ArrayList<UserListItem> userListItemArrayList, Context context){
        this.userListItemArrayList = userListItemArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;
        Glide.with(context).load(BaseURL+userListItemArrayList.get(position).getImage().substring(2)).into(myViewHolder.image);
        myViewHolder.nickname.setText(userListItemArrayList.get(position).getNickname());
        myViewHolder.panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, userListItemArrayList.get(position).getNickname(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("younickname",userListItemArrayList.get(position).getNickname());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userListItemArrayList.size();
    }

    public void add(String image, String nickname){
        UserListItem listitem = new UserListItem(image, nickname);
        userListItemArrayList.add(listitem);
    }
}
