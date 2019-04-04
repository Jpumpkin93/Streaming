package com.example.ju.streaming.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Activity.CallReceiveActivity;
import com.example.ju.streaming.Activity.ChatActivity;
import com.example.ju.streaming.Activity.ChatListActivity;
import com.example.ju.streaming.Activity.MainActivity;
import com.example.ju.streaming.Adapter.ChatListAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.ChatListItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.R;
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

import static android.os.Build.*;

public class MySocketService extends Service {

//    IBinder mybinder = new MyBinder();

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    PrintWriter writer;

    String getmessage;
    String usernickname;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public static final String ip = "";
//    public static final String ip = "192.168.0.15";

    int port = 30000;

    network conn;
    boolean check = true;

    ChatListAdapter chatListAdapter;

    Retrofit retrofit;
    public static final String BaseURL = "";

    String youimage;
    String younick;
    String lastmessage;

    ArrayList<ChatItem> chatItemArrayList;

    String channelId;
    String channelName;
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationManager notifManager;
    NotificationCompat.Builder builder;

    Intent notificationIntent;
    int requestID;

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(this, "서비스 시작", Toast.LENGTH_SHORT).show();

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

        notificationIntent = new Intent(getApplicationContext()

                , ChatActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        requestID = (int) System.currentTimeMillis();

//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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


        sharedPreferences = getSharedPreferences("유저정보", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        usernickname = sharedPreferences.getString("nickname", "nickname");

        chatListAdapter = new ChatListAdapter();

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        conn = new network();
        conn.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
//
//    class MyBinder extends Binder {
//        MySocketService getService() { // 서비스 객체를 리턴
//            return MySocketService.this;
//        }
//    }
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mybinder;
//    }

    public void send(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                writer.println(message);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        check = false;
        conn = null;
//        Toast.makeText(this, "서비스 종료", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendmessage ="접속종료&" + usernickname+"서비스";
                writer.println(sendmessage);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class network extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket(ip, port);
                Log.e("서비스 소켓생성", socket.toString());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                writer = new PrintWriter(out, true);
                writer.println("접속&"+usernickname);
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

                        chatlist chatlistfresh = new chatlist();
                        chatlistfresh.start();
                        load();
                        save();
                    }
                    else if(data[0].equals("영상통화")){
                        Intent intent = new Intent(MySocketService.this, CallReceiveActivity.class);
                        intent.putExtra("roomid",data[1]);
                        intent.putExtra("caller",data[2]);
                        intent.putExtra("callerimage",data[3]);
                        startActivity(intent);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            catch(NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    // 메세지 받으면 채팅 리스트 넣어주기
    public class chatlist extends Thread{

        APIservice apiservice = retrofit.create(APIservice.class);

        @Override
        public void run() {
            super.run();

            Log.e("스레드 시작", "시작");
            Call<Check> ChatList = apiservice.ChatList(usernickname, younick, lastmessage,1);

            ChatList.enqueue(new Callback<Check>() {
                @Override
                public void onResponse(Call<Check> call, Response<Check> response) {
                    Check check = response.body();
//                    if(check.getCheck().equals("if")){
//                        Toast.makeText(MySocketService.this, "if", Toast.LENGTH_SHORT).show();
//                    }
//                    else if(check.getCheck().equals("else")){
//                        Toast.makeText(MySocketService.this, "else", Toast.LENGTH_SHORT).show();
//                    }
//                    Toast.makeText(MySocketService.this, "성공", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Check> call, Throwable t) {
                    Toast.makeText(MySocketService.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

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
