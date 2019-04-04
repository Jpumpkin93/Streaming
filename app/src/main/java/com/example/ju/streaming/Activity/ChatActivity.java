package com.example.ju.streaming.Activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.ChatAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.Profile;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;
import com.example.ju.streaming.WebRTC.CallActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import javax.security.auth.login.LoginException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;

import static com.example.ju.streaming.Service.MySocketService.ip;


public class ChatActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ChatAdapter chatAdapter;
    EditText message;
    Button submit;
    ImageButton call;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    PrintWriter writer;

//    String ip = "192.168.0.15";
    int port = 30000;
    String getmessage;
    String usernickname;
    String receiver;
    String receiverimage;
    String userimage;


    String chattitle;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Retrofit retrofit;

    boolean check = true;

    ArrayList<ChatItem> chatItemArrayList;
    ArrayList<ChatItem> anotherchatItemArrayList;


    String channelId;
    String channelName;
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationManager notifManager;
    NotificationCompat.Builder builder;

    Intent notificationIntent;
    int requestID;

    String roomid;
    boolean end = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


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

        builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);

        notificationIntent = new Intent(getApplicationContext(), ChatActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

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

        // 알림끝

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 받는 사람 받아오기.
        Intent intent = getIntent();
        receiver = intent.getStringExtra("younickname");


        Toast.makeText(this, receiver, Toast.LENGTH_SHORT).show();
        //UI 구성
        mRecyclerView = findViewById(R.id.recycler_view);
        message = (EditText) findViewById(R.id.message);
        submit = (Button) findViewById(R.id.submit);
        call = (ImageButton)findViewById(R.id.call);
//        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        sharedPreferences = getSharedPreferences("유저정보", MODE_PRIVATE);

        usernickname = sharedPreferences.getString("nickname", "nickname");
        userimage = sharedPreferences.getString("image", "image");


        load(receiver);  //채팅 로그 불러오기 와서 chatItemArrayList에 넣어주기.
        chatAdapter = new ChatAdapter(chatItemArrayList, usernickname, this);

        //이미지 새로 바꿔주기.
        ReceiverProfile();

        mRecyclerView.setAdapter(chatAdapter);
        mRecyclerView.smoothScrollToPosition (chatAdapter.getItemCount());



        //서비스 스탑하기.
        Intent ServiceStop = new Intent(ChatActivity.this,MySocketService.class);
        stopService(ServiceStop);

        hitfresh();  // hit수 초기화하기


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(ip, port);
                        Log.e("소켓생성", socket.toString());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                        writer = new PrintWriter(out, true);
                        writer.println("접속&"+usernickname);

                        while (check) {
                            getmessage = in.readLine();
                            Log.e("받은 메세지", getmessage);
                            if (getmessage != null) {
                                String data[] = getmessage.split("&");
                                if(data[0].equals("메세지")){
                                    if(data[2].equals(receiver)){
                                        chatAdapter.add(data[2],data[1],data[3]);
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                chatAdapter.notifyDataSetChanged();
                                                mRecyclerView.smoothScrollToPosition (chatAdapter.getItemCount());
                                            }
                                        });
                                        chatlist(usernickname,data[2],data[1],0);
                                    }
                                    if(!data[2].equals(receiver)){  //메세지 받고, 현재 채팅방의 사람과 메세지의 발신인이 같지 않으면, 알림띄워주고, 발신인과의 채팅로그에 메세지 저장하기.
                                        notificationIntent.putExtra("younickname",data[2]);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        notifManager.notify(0, builder.setContentTitle(data[2]).setContentIntent(pendingIntent).build());
                                        Anotherload(data[2]);
                                        anotherchatItemArrayList.add(new ChatItem(data[2],data[1],data[3]));
                                        Anothersave(data[2]);
                                        chatlist(usernickname,data[2],data[1],1);
                                    }
                                }
                                else if(data[0].equals("메세지ok")){  //메세지 발신 후, 서버에서 메세지를 잘받았다는 메세지 날라옴.
                                    chatAdapter.add(data[2],data[1],data[3]);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            chatAdapter.notifyDataSetChanged();
                                            mRecyclerView.smoothScrollToPosition (chatAdapter.getItemCount());

                                        }
                                    });
                                    chatlist(usernickname,receiver,data[1],0);
                                }
                                else if(data[0].equals("영상통화")){
                                    Intent intent = new Intent(ChatActivity.this, CallReceiveActivity.class);
                                    intent.putExtra("roomid",data[1]);
                                    intent.putExtra("caller",data[2]);
                                    intent.putExtra("callerimage",data[3]);
                                    startActivity(intent);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            //서버에 메세지 보내기.  전송 버튼 리스너
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(message.getText().toString().replace(" ", "").equals("")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChatActivity.this, "메세지를 입력 해주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                String sendmessage ="메세지&"+receiver + "&" + message.getText() + "&" + usernickname + "&" + userimage;
                                writer.println(sendmessage);
                            }
                        }
                    }).start();
                    message.setText("");
                }
            });

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Random rnd = new Random();
                            int num = rnd.nextInt(40000);
                            roomid = Integer.toString(num);
                            String sendmessage ="영상통화&"+roomid+"&"+receiver + "&"+ usernickname + "&" + userimage;
                            writer.println(sendmessage);
                            Intent intent = new Intent(ChatActivity.this, CallSendActivity.class);
                            intent.putExtra("receiver",receiver);
                            intent.putExtra("receiverimage",receiverimage);
                            intent.putExtra("roomid",roomid);
                            Log.e("roomid", roomid);
                            startActivity(intent);
                        }
                    }).start();
                }
            });
    }

    //stop시 소켓닫아줌.
    @Override
    protected void onStop() {
        super.onStop();
        save(receiver);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sendmessage ="접속종료&" + usernickname;
                    writer.println(sendmessage);
                    try {
                        socket.close();
                        check = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            finish();
    }

    //메세지 받을 시 채팅방목록 lastmessage 저장.
    public void chatlist(String nickname, String younick, String lastmessage, int hit){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Check> ChatList = apiservice.ChatList(nickname, younick, lastmessage,hit);

        ChatList.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {

            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(ChatActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void save(String receiver){
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(chatItemArrayList);
        editor.putString(usernickname+receiver,json);
        editor.apply();
    }

    public void load(String receiver){
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(usernickname+receiver,null);
        Type type = new TypeToken<ArrayList<ChatItem>>() {}.getType();
        chatItemArrayList = gson.fromJson(json, type);

        if(chatItemArrayList == null){
            chatItemArrayList = new ArrayList<>();
        }
    }

    public void Anothersave(String receiver){
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(anotherchatItemArrayList);
        editor.putString(usernickname+receiver,json);
        editor.apply();
    }

    public void Anotherload(String receiver){
        sharedPreferences = getSharedPreferences("채팅로그",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(usernickname+receiver,null);
        Type type = new TypeToken<ArrayList<ChatItem>>() {}.getType();
        anotherchatItemArrayList = gson.fromJson(json, type);

        if(anotherchatItemArrayList == null){
            anotherchatItemArrayList = new ArrayList<>();
        }
    }

    private void ReceiverProfile(){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Profile> call = apiservice.ReceiverProfile(receiver);

        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Profile profile = response.body();

                if(profile.getImage().equals("")){
                    receiverimage = "http://13.209.72.139/UserProfile/profile.jpg";
                }
                else{
                    String str = profile.getImage().substring(2);
                    receiverimage = BaseURL + str;
                }
                chatAdapter.changeimage(receiverimage);
                chatAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "프로필 받아오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void hitfresh(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Check> call = apiservice.HitFresh(usernickname, receiver);

        call.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                Check check = response.body();
                if(check.getCheck().equals("ok")){
//                    Toast.makeText(ChatActivity.this, "초기화 성공", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ChatActivity.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "프로필 받아오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

