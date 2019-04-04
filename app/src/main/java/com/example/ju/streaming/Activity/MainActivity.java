package com.example.ju.streaming.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.HotListAdapter;
import com.example.ju.streaming.Adapter.RecordLiveAdapter;
import com.example.ju.streaming.Adapter.UserListAdapter;
import com.example.ju.streaming.Adapter.VideoListAdapter;
import com.example.ju.streaming.ItemClass.ChatItem;
import com.example.ju.streaming.ItemClass.NewsItem;
import com.example.ju.streaming.ItemClass.Profile;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;
import com.example.ju.streaming.WebRTC.ConnectActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class MainActivity extends AppCompatActivity {

    CircleImageView profileimage;
    TextView content;
    ImageView profile_edit;
    TextView nickname;
    TextView introduce;

    RecyclerView videolist;
    RecyclerView.LayoutManager mLayoutManager;
    VideoListAdapter videoListAdapter;
    ArrayList<VideoListItem> videoListAdapterArrayList;
    ArrayList<SubscribItem> subscribItemArrayList;

    RecyclerView livelist;
    RecyclerView.LayoutManager lLayoutManager;
    RecordLiveAdapter liveListAdapter;
    ArrayList<VideoListItem> liveListItemArrayList;

    LinearLayout videolistlayout;
    LinearLayout livelistlayout;

    ImageButton live;
    ImageButton hot;
    ImageButton subscription;
    ImageButton message;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Retrofit retrofit;
    String userid;

    String usernickname;
    String userimage;
    String userintroduce;
    String useremail;

    final int PROFILE_EDIT = 100;
    final int VIDEO_PLUS = 200;

    //actionbar에 메뉴 붙이기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.moremenu){
            //Intent i = new Intent(MainActivity.this, NewsActivity.class);
            //startActivity(i);
            Actionbardialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI 구성
        profileimage = (CircleImageView) findViewById(R.id.profile_image);
        profile_edit = (ImageView)findViewById(R.id.profile_edit);
        nickname = (TextView)findViewById(R.id.nickname);
        introduce = (TextView)findViewById(R.id.introduce);

        //업로드 동영상 리스트
        videolist = (RecyclerView)findViewById(R.id.videolist);
        videolist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        videolist.setLayoutManager(mLayoutManager);
        videoListAdapterArrayList = new ArrayList<>();
        subscribItemArrayList = new ArrayList<>();

        videoListAdapter = new VideoListAdapter(videoListAdapterArrayList, this);
        videolist.setAdapter(videoListAdapter);

        //방송 다시보기 리스트
        livelist = (RecyclerView)findViewById(R.id.livelist);
        livelist.setHasFixedSize(true);
        lLayoutManager = new LinearLayoutManager(this);
        livelist.setLayoutManager(lLayoutManager);
        liveListItemArrayList = new ArrayList<>();

        liveListAdapter = new RecordLiveAdapter(liveListItemArrayList, this);
        livelist.setAdapter(liveListAdapter);

        //탭호스트
        videolistlayout = (LinearLayout)findViewById(R.id.videolistlayout);
        livelistlayout = (LinearLayout)findViewById(R.id.livelistlayout);

        TabHost tabhost = (TabHost)findViewById(android.R.id.tabhost);
        tabhost.setup();

        TabHost.TabSpec ts1 = tabhost.newTabSpec("Tab1");
        ts1.setIndicator("동영상");
        ts1.setContent(R.id.videolistlayout);
        tabhost.addTab(ts1);

        TabHost.TabSpec ts2 = tabhost.newTabSpec("Tab2");
        ts2.setIndicator("Live");
        ts2.setContent(R.id.livelistlayout);
        tabhost.addTab(ts2);

        //메뉴 탭 ui
        live = (ImageButton)findViewById(R.id.live);
        hot = (ImageButton)findViewById(R.id.hot);
        subscription = (ImageButton)findViewById(R.id.subscription);
        message = (ImageButton)findViewById(R.id.message);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();


        //프로필 편집 리스너
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
//                Intent intent = new Intent(MainActivity.this,Profile_EditActivity.class);
//                startActivityForResult(intent, PROFILE_EDIT);
            }
        });
        //비디오 추가
//        contentplus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,VideoPlusActivity.class);
//                startActivityForResult(intent, VIDEO_PLUS);
//            }
//        });

        //메뉴탭 리스너
        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HotActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });
        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });
    }  //onCreate 끝

    //로그인한 유저 프로필 불러오기  onResume에서..
    private void Profile(){

        APIservice apiservice = retrofit.create(APIservice.class);

        userid = sharedPreferences.getString("id","null");

        Call<Profile> call = apiservice.Profile(userid);

        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Profile profile = response.body();
                usernickname = profile.getNickname();

                //프로필 사진 없을경우 기본이미지로 set(설현)
                if(profile.getImage().equals("")){
                    userimage = "http://13.209.72.139/UserProfile/profile.jpg";
                }
                //프로필 사진 있을경우 BaseURL 붙여줌
                else{
                    String str = profile.getImage().substring(2);
                    userimage = BaseURL + str;
                }

                userintroduce = profile.getIntroduce();
                useremail = profile.getEmail();

                editor.putString("nickname",usernickname);
                editor.putString("image",userimage);
                editor.putString("introduce",userintroduce);
                editor.putString("email",useremail);
                editor.commit();

                Log.e("유저닉네임",sharedPreferences.getString("nickname","nickname"));
                Log.e("유저이미지",sharedPreferences.getString("image","image"));
                Log.e("유저소개",sharedPreferences.getString("introduce","introduce"));
                Log.e("유저이메일",sharedPreferences.getString("email","email"));

                nickname.setText(usernickname);
                Glide.with(MainActivity.this).load(userimage).into(profileimage);
                introduce.setText(userintroduce);
                if(userintroduce.equals("")){
                    introduce.setVisibility(View.GONE);
                }

                //서비스 시작.(소켓연결, 메세지 받는)
                Intent ServiceStart = new Intent(MainActivity.this,MySocketService.class);
                startService(ServiceStart);

                videolistview();
//                subscriptionlist();
                livelist("자신 조회");

            }
            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Toast.makeText(MainActivity.this, "프로필 받아오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void videolistview(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<VideoListItem>> call = apiservice.VideoListView(usernickname);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(MainActivity.this, "올린 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    List<VideoListItem> data = response.body();
                    videoListAdapter.clear();
                    for(int i = 0; i<data.size(); i++){
                        String nickname = data.get(i).getNickname();
                        String image = data.get(i).getImage();
                        String thumbnail = data.get(i).getThumbnail();
                        String title = data.get(i).getTitle();
                        String content = data.get(i).getContent();
                        String video = data.get(i).getVideo();
                        String date = data.get(i).getDate();
                        String playtime = data.get(i).getPlaytime();
                        String hit = data.get(i).getHit();
                        videoListAdapter.add(nickname, image, thumbnail, title, content, video, date, playtime, hit);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<VideoListItem>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void livelist(String action){

//        ArrayList<String> nicknamelist = new ArrayList<>();
//        nicknamelist.add(usernickname);

        Gson gson = new Gson();
        String json = gson.toJson(usernickname);

        APIservice apiservice = retrofit.create(APIservice.class);
        Call<List<VideoListItem>> call = apiservice.RecordList(action, json);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(MainActivity.this, "Live 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    final List<VideoListItem> data = response.body();
                    Log.e("받은 데이터", data.toString());

                    for(int i = 0; i<data.size(); i++){
                        String nickname = data.get(i).getNickname();
                        String image = data.get(i).getImage();
                        String thumbnail = data.get(i).getThumbnail();
                        String title = data.get(i).getTitle();
                        String content = data.get(i).getContent();
                        String video = data.get(i).getVideo();
                        String date = data.get(i).getDate();
                        String playtime = data.get(i).getPlaytime();
                        String hit = "없음";
                        liveListAdapter.add(nickname, image, thumbnail, title, content, video, date, playtime);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            liveListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<VideoListItem>> call, Throwable t) {

            }
        });

    }

    public void dialog() {
        final CharSequence[] items = {"프로필 수정", "동영상 업로드", "내 아이템"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(MainActivity.this,Profile_EditActivity.class);
                    startActivityForResult(intent, PROFILE_EDIT);
                }
                else if (which == 1) {
                    Intent intent = new Intent(MainActivity.this,VideoPlusActivity.class);
                    startActivityForResult(intent, VIDEO_PLUS);
                }
                else if (which == 2) {
                    Intent intent = new Intent(MainActivity.this,MyItemActivity.class);
                    intent.putExtra("유저닉네임",usernickname);
                    startActivity(intent);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void Actionbardialog(){
        final CharSequence[] items = {"뉴스 보기", "AR이미지검색"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent i = new Intent(MainActivity.this, NewsActivity.class);
                    startActivity(i);
                }
                else if (which == 1) {
                    Intent i = new Intent(MainActivity.this, UnityPlayerActivity.class);
                    startActivity(i);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void subscriptionlist(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<SubscribItem>> call = apiservice.SubscriptionList(usernickname);

        call.enqueue(new Callback<List<SubscribItem>>() {
            @Override
            public void onResponse(Call<List<SubscribItem>> call, Response<List<SubscribItem>> response) {
                List<SubscribItem> data = response.body();
                if(data == null){
                    Toast.makeText(MainActivity.this, "구독 목록 없음", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (int i = 0; i < data.size(); i++) {
                        SubscribItem item = data.get(i);
                        subscribItemArrayList.add(item);
                        Log.e("구독목록",item.getBjnickname());
                        Log.e("구독목록",item.getBjimage());
                    }
                    save();
                }
            }

            @Override
            public void onFailure(Call<List<SubscribItem>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "구독 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void save(){
        Gson gson = new Gson();
        String json = gson.toJson(subscribItemArrayList);
        editor.putString("구독목록",json);
        editor.apply();
        Toast.makeText(this, "저장 성공", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PROFILE_EDIT:
                //프로필 편집 후 다시 받아오기.
                Profile();
                break;

            case VIDEO_PLUS:
                //동영상 업로드
                videolistview();
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

//        Toast.makeText(this, "Main onresume", Toast.LENGTH_SHORT).show();

        //onResume 시 프로필 받아오기, 서비스 시작.
        Profile();

    }
}
