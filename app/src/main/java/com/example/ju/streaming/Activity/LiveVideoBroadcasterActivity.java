package com.example.ju.streaming.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.RingtoneManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.ChatListAdapter;
import com.example.ju.streaming.Adapter.LiveChatListAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.ItemCount;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.example.ju.streaming.Service.MySocketService;
import com.example.ju.streaming.liveVideoBroadcasterSDK.ILiveVideoBroadcaster;
import com.example.ju.streaming.liveVideoBroadcasterSDK.LiveVideoBroadcaster;
import com.example.ju.streaming.liveVideoBroadcasterSDK.utils.Resolution;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.ju.streaming.Service.MySocketService.ip;


public class LiveVideoBroadcasterActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public  static  final  String  RTMP_BASE_URL  =  "" ;

    private static final String TAG = LiveVideoBroadcasterActivity.class.getSimpleName();
    private ViewGroup mRootView;
    boolean mIsRecording = false;
    private EditText mStreamNameEditText;
    private Timer mTimer;
    private long mElapsedTime;
    public TimerHandler mTimerHandler;
    private ImageButton mSettingsButton;
    private CameraResolutionsFragment mCameraResolutionsDialog;
    private Intent mLiveVideoBroadcasterServiceIntent;
    private TextView mStreamLiveStatus;
    private GLSurfaceView mGLView;
    private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    private Button mBroadcastControlButton;

    String timeStamp;
    String liveaddress;

    TextToSpeech tts;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
//                Toast.makeText(LiveVideoBroadcasterActivity.this, "설정중........", Toast.LENGTH_SHORT).show();
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(LiveVideoBroadcasterActivity.this, mGLView);  // mLiveVideoBroadcaster 초기화. 설정 액티비티, surfaceview
                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
                mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);  // surfaceview에 카메라 프리뷰 띄워준다. 기본설정 전면으로 되어있음.
//                Toast.makeText(LiveVideoBroadcasterActivity.this, "설정 OK", Toast.LENGTH_SHORT).show();
            }
            else{
//                Toast.makeText(LiveVideoBroadcasterActivity.this, "not null", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };

    String userid;
    String usernickname;
    String userimage;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String BaseURL = "http://13.209.72.139/";
    Retrofit retrofit;

    private ImageButton livestop;

    RecyclerView livechatlist;
    EditText livechatmessage;
    Button send;
    TextView number;
    RecyclerView.LayoutManager mLayoutManager;
    LiveChatListAdapter liveChatListAdapter;
    ArrayList<ChatItem> livechatarraylist;

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

    String personnumber;

    ImageView gifimage;
    TextView giftext;

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, this);
        context = this;

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        userid = sharedPreferences.getString("id",null);
        usernickname = sharedPreferences.getString("nickname",null);
        userimage = sharedPreferences.getString("image",null);

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        liveaddress = RTMP_BASE_URL + userid +'-'+ timeStamp;

        // Hide title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);  //풀스크린, 액션바 없어짐.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //binding on resume not to having leaked service connection
        mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);      // 인텐트 만들고
        //this makes service do its job until done
        startService(mLiveVideoBroadcasterServiceIntent);       //서비스 스타트.

        setContentView(R.layout.activity_live_video_broadcaster);       //XML 붙이고.

        mTimerHandler = new TimerHandler();      //타이머 핸들러 생성 해주고.
        mStreamNameEditText = (EditText) findViewById(R.id.stream_name_edit_text);      // Edittext 바인드 해주고,

        mRootView = (ViewGroup)findViewById(R.id.root_layout);      // viewgroup 바인드 해주고
        mSettingsButton = (ImageButton)findViewById(R.id.settings_button);  //셋팅 버튼 바인드
        mStreamLiveStatus = (TextView) findViewById(R.id.stream_live_status);   // 라이브 방송 시작시 녹화 올라가는 텍스트뷰.

        mBroadcastControlButton = (Button) findViewById(R.id.toggle_broadcasting);  // 방송 시작, 끊내는 버튼.

        gifimage = (ImageView)findViewById(R.id.gifimage);
        giftext = (TextView)findViewById(R.id.giftext);

        Glide.with(context).load(R.drawable.kakaorion).into(gifimage);
        giftext.setText("별풍선 메세지");

        livestop = (ImageButton)findViewById(R.id.livestop);

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL activity.
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView); // surfaceview 바인드. 카메라 프리뷰 띄워줌.
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }

        livestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerStopRecording();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        number = (TextView)findViewById(R.id.number);

        livechatlist = (RecyclerView)findViewById(R.id.livechatlist);
        livechatmessage = (EditText)findViewById(R.id.livechatmessage);
        livechatlist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        livechatlist.setLayoutManager(mLayoutManager);

        livechatarraylist = new ArrayList<>();

        liveChatListAdapter = new LiveChatListAdapter(livechatarraylist,LiveVideoBroadcasterActivity.this);
        livechatlist.setAdapter(liveChatListAdapter);

        send = (Button)findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(livechatmessage.getText().toString().replace(" ", "").equals("")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LiveVideoBroadcasterActivity.this, "메세지를 입력 해주세요.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            String sendmessage ="방송채팅&"+ livechatmessage.getText() + "&" + usernickname + "&" + userimage + "&"+liveaddress;
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
    }   // OnCreate  끝.

    public void livestart(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Intent ServiceStop = new Intent(LiveVideoBroadcasterActivity.this,MySocketService.class);
                    stopService(ServiceStop);

                    socket = new Socket(ip, port);
                    Log.e("소켓생성", socket.toString());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                    writer = new PrintWriter(out, true);
                    writer.println("방송접속&"+usernickname+"&"+liveaddress);

                    while (check) {
                        getmessage = in.readLine();
                        Log.e("받은 메세지", getmessage);
                        if (getmessage != null) {
                            String data[] = getmessage.split("&");
                            if(data[0].equals("메세지")){
                                notificationIntent.putExtra("younickname",data[2]);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                notifManager.notify(0, builder.setContentTitle(data[2]).setContentIntent(pendingIntent).build());
                                Anotherload(data[2]);
                                anotherchatItemArrayList.add(new ChatItem(data[2],data[1],data[3]));
                                Anothersave(data[2]);
                                chatlist(usernickname,data[2],data[1],1);
                            }
                            else if(data[0].equals("방송채팅")){
                                liveChatListAdapter.add(data[2],data[1],data[3]);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveChatListAdapter.notifyDataSetChanged();
                                        livechatlist.smoothScrollToPosition (liveChatListAdapter.getItemCount());
                                    }
                                });
                            }
                            else if(data[0].equals("방송인원")){
                                personnumber = data[1];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        number.setText(personnumber);
                                    }
                                });
                            }
                            else if(data[0].equals("아이템선물")){
                                final String nickname = data[1];
                                String livenickname = data[2];
                                final String count = data[3];
                                String speech = nickname+"님이 별풍선"+count+"개를 선물하셨습니다.";
                                tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                                itemcount("추가",usernickname,count);

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
    }

    //카메라 바꿔주는 메소드, xml 들어가면 카메라 버튼에 onclick에 선언되어있는 메소드. LiveVideoBroadcaster 가 null 이 아니고 선언되어있으면 카메라 바꾼다.
    public void changeCamera(View v) {
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.changeCamera();
        }
    }

    // 액티비티가 start되면 서비스 바인드.
    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(LiveVideoBroadcasterActivity.this, "스타트", Toast.LENGTH_SHORT).show();
        //this lets activity bind
        bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);

    }

    // 카메라 권한, 오디오 권한 요청하는 메소드.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LiveVideoBroadcaster.PERMISSIONS_REQUEST: {
                if (mLiveVideoBroadcaster.isPermissionGranted()) {
                    mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.RECORD_AUDIO) ) {
                        mLiveVideoBroadcaster.requestPermission();
                    }
                    else {
                        new AlertDialog.Builder(LiveVideoBroadcasterActivity.this)
                                .setTitle("Permission")
                                .setMessage("App does not work without permissions. Please give Camera and Record Audio permission on app settings")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch ( ActivityNotFoundException e ) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
                return;
            }
        }
    }

    //액티비티 Pause시, 행동할........
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        //hide dialog if visible not to create leaked window exception
        if (mCameraResolutionsDialog != null && mCameraResolutionsDialog.isVisible()) {
            mCameraResolutionsDialog.dismiss();
        }
        mLiveVideoBroadcaster.pause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        //Toast.makeText(this, "onstop", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    //셋팅 버튼 누르면 나오는 dialog, 해상도 설정.
    public void showSetResolutionDialog(View v) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragmentDialog = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragmentDialog != null) {

            ft.remove(fragmentDialog);
        }

        ArrayList<Resolution> sizeList = mLiveVideoBroadcaster.getPreviewSizeList();


        if (sizeList != null && sizeList.size() > 0) {
            mCameraResolutionsDialog = new CameraResolutionsFragment();

            mCameraResolutionsDialog.setCameraResolutions(sizeList, mLiveVideoBroadcaster.getPreviewSize());
            mCameraResolutionsDialog.show(ft, "resolutiton_dialog");
        }
        else {
            Snackbar.make(mRootView, "No resolution available",Snackbar.LENGTH_LONG).show();
        }

    }

    //이건, 방송 시작 버튼 누르면 실행하는 asynctask....
    public void toggleBroadcasting(View v) {
        if (!mIsRecording)
        {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {
                    String streamName = mStreamNameEditText.getText().toString();
                    if(streamName.replace(" ", "").equals("")){
                        Toast.makeText(this, "방송 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        new AsyncTask<String, String, Boolean>() {
                            ContentLoadingProgressBar
                                    progressBar;

                            @Override
                            protected void onPreExecute() {
                                progressBar = new ContentLoadingProgressBar(LiveVideoBroadcasterActivity.this);
                                progressBar.show();
                            }

                            @Override
                            protected Boolean doInBackground(String... url) {
                                Log.e("url", url[0].toString());
                                return mLiveVideoBroadcaster.startBroadcasting(url[0]);

                            }

                            @Override
                            protected void onProgressUpdate(String... values) {
                                super.onProgressUpdate(values);
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                progressBar.hide();
                                mIsRecording = result;
                                if (result) {
                                    mStreamLiveStatus.setVisibility(View.VISIBLE);   //타이머 보여주기.
                                    livestop.setVisibility(View.VISIBLE);
                                    mBroadcastControlButton.setText("방송 종료");
//                                    mSettingsButton.setVisibility(View.GONE);   //셋팅 버튼 없애기
                                    mStreamNameEditText.setVisibility(View.GONE);  //방송 제목 적는란 없애기
                                    mBroadcastControlButton.setVisibility(View.GONE);
                                    startTimer();//start the recording duration     // 밑에있는 메소드 시작.
                                    livechatlist.setVisibility(View.VISIBLE);
                                    livechatmessage.setVisibility(View.VISIBLE);
                                    send.setVisibility(View.VISIBLE);
                                    number.setVisibility(View.VISIBLE);
                                    LiveList("추가");
                                    livestart();
                                } else {
                                    Snackbar.make(mRootView, "Your previous broadcast still sends packets due to slow internet speed", Snackbar.LENGTH_LONG).show();

                                    triggerStopRecording();
                                }
                            }
                        }.execute(RTMP_BASE_URL + userid +'-'+ timeStamp);
                        Log.e("방송주소",RTMP_BASE_URL + userid +'-'+ timeStamp);
                    }
                }
                else {
                    Snackbar.make(mRootView, "Your previous broadcast still sends packets due to slow internet speed", Snackbar.LENGTH_LONG).show();
                }
            }
            else {
                Snackbar.make(mRootView, "Oopps this shouldn\\'t be happened. Please let the library developer this status", Snackbar.LENGTH_LONG).show();
            }
        }
        else
        {
            triggerStopRecording();
        }

    }

    // 방송 , 녹화 종료시
    public void triggerStopRecording() {
        if (mIsRecording) {
            mBroadcastControlButton.setText("방송시작하기");

            Record("추가");

            livestop.setVisibility(View.GONE);
            mStreamLiveStatus.setVisibility(View.GONE);
            mStreamLiveStatus.setText("Live");
//            mSettingsButton.setVisibility(View.VISIBLE);
            mStreamNameEditText.setVisibility(View.VISIBLE);
            mStreamNameEditText.setText("");
            mBroadcastControlButton.setVisibility(View.VISIBLE);
            livechatlist.setVisibility(View.GONE);
            livechatmessage.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
            number.setVisibility(View.GONE);

            stopTimer();
            mLiveVideoBroadcaster.stopBroadcasting();
            LiveList("삭제");
            Livestop();
        }

        mIsRecording = false;
    }

    //위의 타이머 보여주기.
    //This method starts a mTimer and updates the textview to show elapsed time for recording
    public void startTimer() {

        if(mTimer == null) {
            mTimer = new Timer();
        }

        mElapsedTime = 0;
        mTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                mElapsedTime += 1; //increase every sec
                mTimerHandler.obtainMessage(TimerHandler.INCREASE_TIMER).sendToTarget();

                if (mLiveVideoBroadcaster == null || !mLiveVideoBroadcaster.isConnected()) {
                    mTimerHandler.obtainMessage(TimerHandler.CONNECTION_LOST).sendToTarget();
                }
            }
        }, 0, 1000);
    }


    public void stopTimer()
    {
        if (mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.mElapsedTime = 0;
    }

    public void setResolution(Resolution size) {
        mLiveVideoBroadcaster.setResolution(size);
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

    private class TimerHandler extends Handler {
        static final int CONNECTION_LOST = 2;
        static final int INCREASE_TIMER = 1;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INCREASE_TIMER:
                    mStreamLiveStatus.setText("Live" + " - " + getDurationString((int) mElapsedTime));
                    break;
                case CONNECTION_LOST:
                    triggerStopRecording();
                    new AlertDialog.Builder(LiveVideoBroadcasterActivity.this)
                            .setMessage("Connection to media server is lost")
                            .setPositiveButton(android.R.string.yes, null)
                            .show();

                    break;
            }
        }
    }

    public static String getDurationString(int seconds) {

        if(seconds < 0 || seconds > 2000000)//there is an codec problem and duration is not set correctly,so display meaningfull string
            seconds = 0;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if(hours == 0)
            return twoDigitString(minutes) + " : " + twoDigitString(seconds);
        else
            return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    public static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    private void LiveList(String aciton){

        String action = aciton;
        String thumbnail = "./Record/"+userid+"-"+timeStamp+".png";

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Check> LiveList = apiservice.LiveList(action, userid, usernickname, userimage, mStreamNameEditText.getText().toString(), thumbnail, liveaddress);

        LiveList.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                Check check = response.body();
                if(check.getCheck().equals("ok")){
                    //Toast.makeText(LiveVideoBroadcasterActivity.this, "추가 성공", Toast.LENGTH_SHORT).show();
                }
                else{
                    //Toast.makeText(LiveVideoBroadcasterActivity.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(LiveVideoBroadcasterActivity.this, "통신 에러....", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Livestop(){
        AlertDialog.Builder alert = new AlertDialog.Builder(LiveVideoBroadcasterActivity.this,R.style.MyAlertDialogStyle);
        alert.setMessage("방송을 종료했습니다.");
        alert.setCancelable(false);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendmessage ="방송종료&"+liveaddress;
                writer.println(sendmessage);
            }
        }).start();

    }

    public void Record(String aciton){
        String action = aciton;
        String thumbnail = "./Record/"+userid+"-"+timeStamp+".png";
        String video = "./Record/"+userid.replace("@","%2540")+"-"+timeStamp+".flv";
        String playtime = getDurationString((int) mElapsedTime);

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Check> Record = apiservice.Record(action, userid, usernickname, userimage, thumbnail, mStreamNameEditText.getText().toString(), video, playtime);

        Record.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                if(response.body().getCheck().equals("ok")){
                    Toast.makeText(LiveVideoBroadcasterActivity.this, "추가 성공", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LiveVideoBroadcasterActivity.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(LiveVideoBroadcasterActivity.this, "통신 에러....", Toast.LENGTH_SHORT).show();
            }
        });

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
                Toast.makeText(LiveVideoBroadcasterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }

    public void itemcount(String action, String usernickname, String count){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<ItemCount> call = apiservice.ItemCount(action,usernickname, count);

        call.enqueue(new Callback<ItemCount>() {
            @Override
            public void onResponse(Call<ItemCount> call, Response<ItemCount> response) {
//                Toast.makeText(LiveVideoBroadcasterActivity.this, response.body().getCount()+"개를 선물하였습니다.", Toast.LENGTH_SHORT).show();
//                Toast.makeText(LiveVideoBroadcasterActivity.this, response.body().getNickname(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ItemCount> call, Throwable t) {
                Toast.makeText(LiveVideoBroadcasterActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
