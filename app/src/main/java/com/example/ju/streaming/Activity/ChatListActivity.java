package com.example.ju.streaming.Activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.ChatListAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.ChatListItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;
import static com.example.ju.streaming.Service.MySocketService.ip;

public class ChatListActivity extends AppCompatActivity {

    ImageButton live;
    ImageButton hot;
    ImageButton subscription;
    ImageButton profile;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    PrintWriter writer;

    String getmessage;


//    String ip = "192.168.0.15";
    int port = 30000;

    network conn;
    boolean check = true;

    String youimage;
    String younick;
    String lastmessage;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ChatListAdapter chatListAdapter;

    String usernickname;
    String userimage;

    Retrofit retrofit;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ArrayList<ChatItem> chatItemArrayList;

    String channelId;
    String channelName;
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationManager notifManager;
    NotificationCompat.Builder builder;

    Intent notificationIntent;

    int requestID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);


        //알림 관련
        channelId = "channel";
        channelName = "Channel Name";

        notifManager
                = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);

            notifManager.createNotificationChannel(mChannel);

        }

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        notificationIntent = new Intent(getApplicationContext(), ChatActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        requestID = (int) System.currentTimeMillis();

        builder.setContentTitle("younick") // required
                .setContentText("메세지를 보냈습니다.")  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제

                .setSound(RingtoneManager

                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                .setSmallIcon(android.R.drawable.btn_star)
                .setLargeIcon(BitmapFactory.decodeResource(getResources()
                        , R.drawable.ic_person_black_24dp))
                .setBadgeIconType(R.drawable.ic_person_black_24dp);
        //알림 관련 끝

        //메뉴바
        live = (ImageButton)findViewById(R.id.live);
        subscription = (ImageButton)findViewById(R.id.subscription);
        hot = (ImageButton)findViewById(R.id.hot);
        profile = (ImageButton)findViewById(R.id.profile);

        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, HotActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        // 메뉴바 끝


        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);


        usernickname = sharedPreferences.getString("nickname", "nickname");
        userimage = sharedPreferences.getString("image", "image");



        ArrayList<ChatListItem> chatListItemArrayList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatListItemArrayList, ChatListActivity.this);
        mRecyclerView.setAdapter(chatListAdapter);


        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Intent ServiceStop = new Intent(ChatListActivity.this,MySocketService.class);
        stopService(ServiceStop);
    }
    //채팅방 목록 보여주기
    public void ChatListView(){

        Log.e("시작", "시작됨");

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<ChatListItem>> call = apiservice.ChatListView(usernickname);

        call.enqueue(new Callback<List<ChatListItem>>() {
            @Override
            public void onResponse(Call<List<ChatListItem>> call, Response<List<ChatListItem>> response) {
                if(response.body() !=null){
                    chatListAdapter.clear();
                    List<ChatListItem> data = response.body();
                    for(int i = 0; i<data.size(); i++){
                        String image = data.get(i).getYouimage().substring(2);
                        userimage = BaseURL + image;
                        if(image.equals("")){
                            image = "./UserProfile/profile.jpg";
                        }
                        String nickname = data.get(i).getYounickname();
                        String lastmessage = data.get(i).getLastmessage();
                        int hit = data.get(i).getHit();
                        chatListAdapter.add(image,nickname,lastmessage,hit);
                        Log.e("hit 수", Integer.toString(hit));
                    }
                    chatListAdapter.notifyDataSetChanged();
                }
                else{
                    Log.e("상태","no");
                }
            }

            @Override
            public void onFailure(Call<List<ChatListItem>> call, Throwable t) {
                Toast.makeText(ChatListActivity.this, "정보 받아오기 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러",t.getMessage());
            }
        });
    }
    //서비스 중지하고, 소켓 연결하는 클래스
    public class network extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(ip, port);
                Log.e("챗리스트 소켓생성", socket.toString());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                writer = new PrintWriter(out, true);
                writer.println("접속&"+usernickname);
                //check이용하여 메소드 중지, 시작
                while(check){
                    getmessage = in.readLine();
                    Log.e("받은 메세지", getmessage);
                    String data[] = getmessage.split("&");
                    if (data[0].equals("메세지")) {
                        youimage = data[3];
                        younick = data[2];
                        lastmessage = data[1];

                        notificationIntent.putExtra("younickname",data[2]);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        notifManager.notify(0, builder.setContentTitle(data[2]).setContentIntent(pendingIntent).build());

                        ChatListFresh chatlistfresh = new ChatListFresh();
                        chatlistfresh.start();

                        chatListAdapter.change(data[3],data[2],data[1],1);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatListAdapter.notifyDataSetChanged();

                            }
                        });
                        load();
                        save();
                    }
                    else if(data[0].equals("메세지ok")){

                    }
                    else if(data[0].equals("영상통화")){
                        Intent intent = new Intent(ChatListActivity.this, CallReceiveActivity.class);
                        intent.putExtra("roomid",data[1]);
                        intent.putExtra("caller",data[2]);
                        intent.putExtra("callerimage",data[3]);
                        startActivity(intent);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    //메세지 받으면 DB에 채팅방 목록 lastmessage 바꿔주거나, 채팅방목록 생성
    public class ChatListFresh extends Thread{

        APIservice apiservice = retrofit.create(APIservice.class);

        @Override
        public void run() {
            super.run();

            Call<Check> ChatList = apiservice.ChatList(usernickname, younick, lastmessage, 1);

            ChatList.enqueue(new Callback<Check>() {
                @Override
                public void onResponse(Call<Check> call, Response<Check> response) {

                }

                @Override
                public void onFailure(Call<Check> call, Throwable t) {
                    Toast.makeText(ChatListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    //onStop 시 접속종료 메세지 보내고, 소켓 닫음. check -> false로 바꿔줌
    @Override
    protected void onStop() {
        super.onStop();
        check = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendmessage ="접속종료&"+ usernickname;
                writer.println(sendmessage);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                conn = null;
            }
        }).start();
    }

    //onResume시 check -> true, 채팅목록 보여주기, 소켓 연결하기
    @Override
    protected void onPostResume() {
        super.onPostResume();
//        Toast.makeText(this, "onresume", Toast.LENGTH_SHORT).show();
        check = true;
        ChatListView();
        conn = new network();
        conn.start();
    }
    //메세지 받을시, 채팅메세지 해당 id의 채팅방에 저장.
    public void save(){
        Log.e("채팅로그", "쉐어드 저장시작");
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(chatItemArrayList);
        editor.putString(usernickname+younick,json);
        editor.apply();
        Log.e("채팅로그", "쉐어드 저장끝");
    }
    //메세지 받을시, 채팅메세지 해당 id의 채팅방 목록 불러오기.
    public void load(){
        Log.e("채팅로그", "얻어옴");
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(usernickname+younick,null);
        Type type = new TypeToken<ArrayList<ChatItem>>() {}.getType();
        chatItemArrayList = gson.fromJson(json, type);

        if(json == null){
            Log.e("리스트가","null이야");
            chatItemArrayList = new ArrayList<>();
            ChatItem item = new ChatItem(younick, lastmessage, youimage);
            chatItemArrayList.add(item);
        }
        else{
            ChatItem item = new ChatItem(younick, lastmessage, youimage);
            chatItemArrayList.add(item);
        }
        Log.e("채팅로그", "얻어와서 저추가함");
    }
}

