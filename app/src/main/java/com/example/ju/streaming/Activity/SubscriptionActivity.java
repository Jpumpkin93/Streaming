package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.HotListAdapter;
import com.example.ju.streaming.Adapter.LiveListAdapter;
import com.example.ju.streaming.Adapter.RecordLiveAdapter;
import com.example.ju.streaming.Adapter.SubscribListAdapter;
import com.example.ju.streaming.Adapter.VideoListAdapter;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class SubscriptionActivity extends AppCompatActivity{

    ImageButton live;
    ImageButton hot;
    ImageButton message;
    ImageButton profile;

    RecyclerView subscriblist;
    RecyclerView.LayoutManager mLayoutManager;
    SubscribListAdapter subscribListAdapter;
    ArrayList<SubscribItem> subscribItemArrayList;

    RecyclerView subscribevideo;
    RecyclerView.LayoutManager sLayoutManager;
    HotListAdapter hotListAdapter;
    ArrayList<VideoListItem> videoListItemArrayList;

    RecyclerView subscribelive;
    RecyclerView.LayoutManager lLayoutManager;
    RecordLiveAdapter liveListAdapter;
    ArrayList<VideoListItem> liveListItemArrayList;

    LinearLayout videolist;
    LinearLayout livelist;


    String nickname;
    String image;
    String usernickname;

    Retrofit retrofit;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        videolist = (LinearLayout)findViewById(R.id.videolist);
        livelist = (LinearLayout)findViewById(R.id.livelist);


        TabHost tabhost = (TabHost)findViewById(android.R.id.tabhost);
        tabhost.setup();

        TabHost.TabSpec ts1 = tabhost.newTabSpec("Tab1");
        ts1.setIndicator("동영상");
        ts1.setContent(R.id.videolist);
        tabhost.addTab(ts1);

        TabHost.TabSpec ts2 = tabhost.newTabSpec("Tab2");
        ts2.setIndicator("Live");
        ts2.setContent(R.id.livelist);
        tabhost.addTab(ts2);

//        tabhost.setCurrentTab(0);

        //구독 목록
        subscriblist = (RecyclerView)findViewById(R.id.subscriptionlist);
        subscriblist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        subscriblist.setLayoutManager(mLayoutManager);
        subscribItemArrayList = new ArrayList<>();
        subscribListAdapter = new SubscribListAdapter(subscribItemArrayList, this);
        subscriblist.setAdapter(subscribListAdapter);

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        usernickname = sharedPreferences.getString("nickname", null);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //동영상 리사이클러뷰
        subscribevideo = (RecyclerView)findViewById(R.id.subscribevideo);
//        subscribevideo.setHasFixedSize(true);
        sLayoutManager = new LinearLayoutManager(this);
        subscribevideo.setLayoutManager(sLayoutManager);
        videoListItemArrayList = new ArrayList<>();

        hotListAdapter = new HotListAdapter(videoListItemArrayList, this);
        subscribevideo.setAdapter(hotListAdapter);

        //live다시보기 리사이클러뷰
        subscribelive = (RecyclerView)findViewById(R.id.subscribelive);
        subscribelive.setHasFixedSize(true);
        lLayoutManager = new LinearLayoutManager(this);
        subscribelive.setLayoutManager(lLayoutManager);
        liveListItemArrayList = new ArrayList<>();

        liveListAdapter = new RecordLiveAdapter(liveListItemArrayList, this);
        subscribelive.setAdapter(liveListAdapter);

        //메뉴바
        live = (ImageButton)findViewById(R.id.live);
        hot = (ImageButton)findViewById(R.id.hot);
        message = (ImageButton)findViewById(R.id.message);
        profile = (ImageButton)findViewById(R.id.profile);

        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriptionActivity.this, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriptionActivity.this, HotActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriptionActivity.this, ChatListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriptionActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        // 메뉴바 끝

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Intent ServiceStart = new Intent(SubscriptionActivity.this,MySocketService.class);
        startService(ServiceStart);
        subscriptionlist();
    }

    private void subscriptionlist(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<SubscribItem>> call = apiservice.SubscriptionList(usernickname);

        call.enqueue(new Callback<List<SubscribItem>>() {
            @Override
            public void onResponse(Call<List<SubscribItem>> call, Response<List<SubscribItem>> response) {
                List<SubscribItem> data = response.body();
                subscribListAdapter.clear();
                for (int i = 0; i < data.size(); i++) {
                    SubscribItem item = data.get(i);
                    subscribListAdapter.add(item);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        subscribListAdapter.notifyDataSetChanged();
                    }
                });
                subscriptionvideolist();
                subscriptionlivelist("조회");
            }

            @Override
            public void onFailure(Call<List<SubscribItem>> call, Throwable t) {
                Toast.makeText(SubscriptionActivity.this, "구독 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscriptionvideolist(){
        ArrayList<SubscribItem> list = subscribListAdapter.list();
        ArrayList<String> nicknamelist = new ArrayList<>();

        for(int i = 0; i <list.size(); i++){
            nicknamelist.add(list.get(i).getBjnickname());
        }
        Gson gson = new Gson();
        String json = gson.toJson(nicknamelist);

        APIservice apiservice = retrofit.create(APIservice.class);
        Call<List<VideoListItem>> call = apiservice.SubscriptionVideoList(json);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(SubscriptionActivity.this, "올린 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    final List<VideoListItem> data = response.body();
                    hotListAdapter.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                hotListAdapter.add(nickname, image, thumbnail, title, content, video, date, playtime, hit);
                                Log.e("php에서 받아온 데이터", nickname+"&"+image+"&"+thumbnail+"&"+title+"&"+content+"&"+video+"&"+date+"&"+playtime+hit);

                                hotListAdapter.notifyDataSetChanged();
                           }

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<VideoListItem>> call, Throwable t) {
                Toast.makeText(SubscriptionActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
                Log.e("통신 에러", t.toString());
            }
        });
    }

    private void subscriptionlivelist(String action){

        ArrayList<SubscribItem> list = subscribListAdapter.list();
        ArrayList<String> nicknamelist = new ArrayList<>();

        for(int i = 0; i <list.size(); i++){
            nicknamelist.add(list.get(i).getBjnickname());
        }
        Gson gson = new Gson();
        String json = gson.toJson(nicknamelist);

        APIservice apiservice = retrofit.create(APIservice.class);
        Call<List<VideoListItem>> call = apiservice.RecordList(action, json);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(SubscriptionActivity.this, "Live 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    final List<VideoListItem> data = response.body();
                    Log.e("받은 데이터", data.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                Log.e("받은이미지", thumbnail);
                                liveListAdapter.add(nickname, image, thumbnail, title, content, video, date, playtime);
                                liveListAdapter.notifyDataSetChanged();
                            }


                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<VideoListItem>> call, Throwable t) {

            }
        });

    }
}
