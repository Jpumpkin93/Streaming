package com.example.ju.streaming.Activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.LiveChatListAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.ItemCount;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.ju.streaming.Activity.MyItemActivity;

import static com.example.ju.streaming.Service.MySocketService.ip;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;
import static java.lang.Integer.parseInt;

public class LivePlayerActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    SimpleExoPlayer player;
    MediaSource videoSource;

    CircleImageView liveuserimage;
    TextView liveusernickname;
    TextView livetitle;
    ImageButton livestop;
    TextView number;

    RecyclerView livechatlist;
    EditText livechatmessage;
    Button send;
    RecyclerView.LayoutManager mLayoutManager;
    LiveChatListAdapter liveChatListAdapter;
    ArrayList<ChatItem> livechatarraylist;

    @BindView(R.id.gifimage)
    ImageView gifimage;
    @BindView(R.id.giftext)
    TextView giftext;
    @BindView(R.id.gift)
    Button gift;

    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    PrintWriter writer;

    int port = 30000;

    String getmessage;

    String channelId;
    String channelName;
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationManager notifManager;
    NotificationCompat.Builder builder;

    Intent notificationIntent;
    int requestID;
    boolean check = true;
    ArrayList<ChatItem> anotherchatItemArrayList;

    String userid;
    String usernickname;
    String userimage;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Retrofit retrofit;

    String Roomid;
    AlertDialog.Builder alert;
    String personnumber;
    String liveid;

    TextToSpeech tts;
    int coins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player);

        ButterKnife.bind(this);

        tts = new TextToSpeech(this, this);

        sharedPreferences = getSharedPreferences("유저정보", MODE_PRIVATE);
        userid = sharedPreferences.getString("id", null);
        usernickname = sharedPreferences.getString("nickname", null);
        userimage = sharedPreferences.getString("image", null);

        liveuserimage = (CircleImageView) findViewById(R.id.userimage);
        liveusernickname = (TextView) findViewById(R.id.usernickname);
        livetitle = (TextView) findViewById(R.id.livetitle);
        livestop = (ImageButton) findViewById(R.id.livestop);
        number = (TextView)findViewById(R.id.number);

        Glide.with(this).load(R.drawable.kakaorion).into(gifimage);

        Intent intent = getIntent();
        Roomid = intent.getStringExtra("liveaddress");
        liveid = intent.getStringExtra("nickname");

        Glide.with(this).load(intent.getStringExtra("image")).into(liveuserimage);
        liveusernickname.setText(intent.getStringExtra("nickname"));
        livetitle.setText("(" + intent.getStringExtra("title") + ")");
        livestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(LivePlayerActivity.this, R.style.MyAlertDialogStyle);
                alert.setMessage("그만 시청하시겠습니까?");
                alert.setCancelable(false);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert.show();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        itemcount("조회",usernickname,"");


        livechatlist = (RecyclerView) findViewById(R.id.livechatlist);
        livechatmessage = (EditText) findViewById(R.id.livechatmessage);
        livechatlist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        livechatlist.setLayoutManager(mLayoutManager);

        livechatarraylist = new ArrayList<>();

        liveChatListAdapter = new LiveChatListAdapter(livechatarraylist, LivePlayerActivity.this);
        livechatlist.setAdapter(liveChatListAdapter);

        send = (Button) findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (livechatmessage.getText().toString().replace(" ", "").equals("")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LivePlayerActivity.this, "메세지를 입력 해주세요.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            String sendmessage = "방송채팅&" + livechatmessage.getText() + "&" + usernickname + "&" + userimage + "&" + Roomid;
                            writer.println(sendmessage);
                            liveChatListAdapter.add(usernickname, livechatmessage.getText().toString(), userimage);  // 자신의 메세지 추가
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    liveChatListAdapter.notifyDataSetChanged();
                                    livechatlist.smoothScrollToPosition (liveChatListAdapter.getItemCount());

                                }
                            });
                        }
                    }
                }).start();
                livechatmessage.setText("");
            }
        });

        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LivePlayerActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialog_layout = inflater.inflate(R.layout.item_gift_layout,null);
                final TextView coincount = (TextView)dialog_layout.findViewById(R.id.coincount);
                final EditText giftcoincount = (EditText)dialog_layout.findViewById(R.id.giftcoincount);
                final Button sendcoin = (Button)dialog_layout.findViewById(R.id.sendcoin);
                Log.e("텍스트뷰 코인 갯수", Integer.toString(coins));
                coincount.setText(Integer.toString(coins));
                builder.setView(dialog_layout);
                final AlertDialog dialog = builder.create();
                sendcoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String text = giftcoincount.getText().toString().trim();
                        if(text.trim().equals("")){
                            Toast.makeText(LivePlayerActivity.this, "선물하실 갯수를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(coins < parseInt(text)){
                                Toast.makeText(LivePlayerActivity.this, "보유한 별풍선보다 적은 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                            else if(parseInt(text) <=0){
                                Toast.makeText(LivePlayerActivity.this, "0보다 큰 수를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                gifimage.setVisibility(View.VISIBLE);
                                giftext.setVisibility(View.VISIBLE);
                                giftext.setText(usernickname+"님이 별풍선"+text+"개를 선물하셨습니다.");

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        gifimage.setVisibility(View.GONE);
                                        giftext.setVisibility(View.GONE);
                                    }
                                }, 4000);

                                String speech = usernickname+"님이 별풍선"+text+"개를 선물하셨습니다.";
                                tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

                                //선물 메세지 보내기.
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String sendmessage = "아이템선물&" + Roomid + "&"+ usernickname +"&"+ liveid + "&" + text;
                                        writer.println(sendmessage);
                                    }
                                }).start();
                                itemcount("선물",usernickname,text);
                                Toast.makeText(LivePlayerActivity.this, "별풍선 "+text+"개를 선물하셨습니다.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    }
                });
                dialog.show();
            }
        });

        Intent ServiceStop = new Intent(LivePlayerActivity.this, MySocketService.class);
        stopService(ServiceStop);

        //알림 관련
        channelId = "channel";
        channelName = "Channel Name";

        notifManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    Log.e("소켓생성", socket.toString());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                    writer = new PrintWriter(out, true);
                    writer.println("방송접속&" + usernickname + "&" + Roomid);

                    while (check) {
                        getmessage = in.readLine();
//                        Log.e("받은 메세지", getmessage);
                        if (getmessage != null) {
                            String data[] = getmessage.split("&");
                            if (data[0].equals("메세지")) {
                                notificationIntent.putExtra("younickname", data[2]);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                notifManager.notify(0, builder.setContentTitle(data[2]).setContentIntent(pendingIntent).build());
                                Anotherload(data[2]);
                                anotherchatItemArrayList.add(new ChatItem(data[2], data[1], data[3]));
                                Anothersave(data[2]);
                                chatlist(usernickname, data[2], data[1], 1);
                            } else if (data[0].equals("방송채팅")) {
                                liveChatListAdapter.add(data[2], data[1], data[3]);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveChatListAdapter.notifyDataSetChanged();
                                        livechatlist.smoothScrollToPosition(liveChatListAdapter.getItemCount());
                                    }
                                });
                            } else if (data[0].equals("방송인원")) {
                                personnumber = data[1];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        number.setText(personnumber);
                                    }
                                });
                            } else if (data[0].equals("방송종료")) {
                                Livestop();
                            }
                            else if(data[0].equals("아이템선물")){
                                final String nickname = data[1];
                                String livenickname = data[2];
                                final String count = data[3];
                                String speech = nickname+"님이 별풍선"+count+"개를 선물하셨습니다.";
                                tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        gifimage.setVisibility(View.VISIBLE);
//                                        giftext.setVisibility(View.VISIBLE);
//                                        giftext.setText(nickname+"님이 별풍선"+count+"개를 선물하셨습니다.");
//                                    }
//                                });
//                                final Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        gifimage.setVisibility(View.GONE);
//                                        giftext.setVisibility(View.GONE);
//                                    }
//                                }, 4000);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                gifimage.setVisibility(View.VISIBLE);
                                                giftext.setText(nickname+"님이 별풍선"+count+"개를 선물하셨습니다.");
                                                giftext.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        try {
                                            Thread.sleep(4000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                gifimage.setVisibility(View.GONE);
                                                giftext.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        /*
          Create Simple Exoplayer Player
         */
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        bandwidthMeter.getBitrateEstimate();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        final PlayerView playerView = findViewById(R.id.simple_player);

        playerView.setPlayer(player);

        /*
          Create RTMP Data Source
         */

        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();

//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("rtmp://184.72.239.149/vod/mp4:bigbuckbunny_750.mp4"),
//                rtmpDataSourceFactory, extractorsFactory, null, null);

        videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                .createMediaSource(Uri.parse(Roomid));


        player.prepare(videoSource);

        player.setPlayWhenReady(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendmessage = "시청종료&" + Roomid + "&" + usernickname;
                writer.println(sendmessage);
                sendmessage ="접속종료&" + usernickname;
                writer.println(sendmessage);
                try {
                    check = false;
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        player.setPlayWhenReady(false);
        player.stop();
        finish();
    }


    //메세지 받을 시 채팅방목록 lastmessage 저장.
    public void chatlist(String nickname, String younick, String lastmessage, int hit) {

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Check> ChatList = apiservice.ChatList(nickname, younick, lastmessage, hit);

        ChatList.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {

            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(LivePlayerActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Anothersave(String receiver) {
        sharedPreferences = getSharedPreferences("채팅로그", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(anotherchatItemArrayList);
        editor.putString(usernickname + receiver, json);
        editor.apply();
    }

    public void Anotherload(String receiver) {
        sharedPreferences = getSharedPreferences("채팅로그", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(usernickname + receiver, null);
        Type type = new TypeToken<ArrayList<ChatItem>>() {
        }.getType();
        anotherchatItemArrayList = gson.fromJson(json, type);

        if (anotherchatItemArrayList == null) {
            anotherchatItemArrayList = new ArrayList<>();
        }
    }

    public void Livestop() {
        alert = new AlertDialog.Builder(LivePlayerActivity.this, R.style.MyAlertDialogStyle);
        alert.setMessage("방송이 종료되었습니다.");
        alert.setCancelable(false);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alert.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            // 작업 성공
            int language = tts.setLanguage(Locale.KOREAN);  // 언어 설정
            if (language == TextToSpeech.LANG_MISSING_DATA
                    || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 언어 데이터가 없거나, 지원하지 않는경우
                Toast.makeText(this, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 준비 완료
            }

        } else {
            // 작업 실패
            Toast.makeText(this, "TTS 작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void itemcount(String action, String usernickname, String count){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<ItemCount> call = apiservice.ItemCount(action,usernickname, count);

        call.enqueue(new Callback<ItemCount>() {
            @Override
            public void onResponse(Call<ItemCount> call, Response<ItemCount> response) {
                coins = parseInt(response.body().getCount());
            }

            @Override
            public void onFailure(Call<ItemCount> call, Throwable t) {
                Toast.makeText(LivePlayerActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


