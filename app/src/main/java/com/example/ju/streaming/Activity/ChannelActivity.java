package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.RecordLiveAdapter;
import com.example.ju.streaming.Adapter.VideoListAdapter;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.Profile;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;

import static com.example.ju.streaming.Service.MySocketService.BaseURL;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChannelActivity extends AppCompatActivity {

    CircleImageView bjimage;
    TextView bjnickname;
    TextView bjintroduce;
    LinearLayout subscriptionyes;
    LinearLayout subscriptionno;
    LinearLayout message;


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

    String nickname;
    String image;

    String usernickname;


    Retrofit retrofit;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Intent i = getIntent();
        nickname = i.getStringExtra("nickname");

        bjimage = (CircleImageView)findViewById(R.id.bjimage);
        bjnickname = (TextView)findViewById(R.id.bjnickname);
        bjintroduce = (TextView)findViewById(R.id.bjintroduce);
        subscriptionyes = (LinearLayout)findViewById(R.id.subscriptionyes);
        subscriptionno = (LinearLayout)findViewById(R.id.subscriptionno);
        message = (LinearLayout)findViewById(R.id.message);
        videolist = (RecyclerView)findViewById(R.id.videolist);


        //비디오 리스트
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

        bjnickname.setText(nickname);

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

//        subscriptionno.setVisibility(View.VISIBLE);

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        usernickname = sharedPreferences.getString("nickname", null);

        subscriptionlist();

        subscriptionyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionyes.setVisibility(View.GONE);
                message.setVisibility(View.GONE);
                subscriptionno.setVisibility(View.VISIBLE);
                Toast.makeText(ChannelActivity.this, "구독 취소", Toast.LENGTH_SHORT).show();
                subcription("구독취소");
                for(int i = 0; i < subscribItemArrayList.size(); i++){
                    if(subscribItemArrayList.get(i).getBjimage().equals(bjnickname)){
                        subscribItemArrayList.remove(i);
                    }
                }
            }
        });

        subscriptionno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionno.setVisibility(View.GONE);
                subscriptionyes.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                Toast.makeText(ChannelActivity.this, "구독 완료", Toast.LENGTH_SHORT).show();
                subcription("구독하기");
                subscribItemArrayList.add(new SubscribItem(nickname,image));
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChannelActivity.this, ChatActivity.class);
                intent.putExtra("younickname",nickname);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Profile();

        Intent ServiceStart = new Intent(ChannelActivity.this,MySocketService.class);
        startService(ServiceStart);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void Profile(){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<Profile> call = apiservice.ChannelProfile(nickname);

        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {

                String introduce;

                Profile profile = response.body();

                //프로필 사진 없을경우 기본이미지로 set(설현)
                if(profile.getImage().equals("")){
                    image = "http://13.209.72.139/UserProfile/profile.jpg";
                }
                //프로필 사진 있을경우 BaseURL 붙여줌
                else{
                    String str = profile.getImage().substring(2);
                    image = BaseURL + str;
                }

                introduce = profile.getIntroduce();

                Glide.with(ChannelActivity.this).load(image).into(bjimage);
                bjintroduce.setText(introduce);
                if(introduce.equals("")){
                    bjintroduce.setVisibility(View.GONE);
                }

                videolistview();
                livelist("자신 조회");

            }
            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Toast.makeText(ChannelActivity.this, "프로필 받아오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void videolistview(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<VideoListItem>> call = apiservice.VideoListView(nickname);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(ChannelActivity.this, "올린 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ChannelActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void livelist(String action){

//        ArrayList<String> nicknamelist = new ArrayList<>();
//        nicknamelist.add(usernickname);

        Gson gson = new Gson();
        String json = gson.toJson(nickname);

        APIservice apiservice = retrofit.create(APIservice.class);
        Call<List<VideoListItem>> call = apiservice.RecordList(action, json);

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(ChannelActivity.this, "Live 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
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

    private void subcription(String action){
        APIservice apIservice = retrofit.create(APIservice.class);

        Call<Check> call = apIservice.Subscription(action, usernickname, nickname, image);

        call.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                if(response.body().getCheck().equals("ok")){
                    Toast.makeText(ChannelActivity.this, "check ok", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ChannelActivity.this, "check false", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(ChannelActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscriptionlist(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<SubscribItem>> call = apiservice.SubscriptionList(usernickname);

        call.enqueue(new Callback<List<SubscribItem>>() {
            @Override
            public void onResponse(Call<List<SubscribItem>> call, Response<List<SubscribItem>> response) {
                List<SubscribItem> data = response.body();

                boolean check = false;
                for (int i = 0; i < data.size(); i++) {
                    if(data.get(i).getBjnickname().equals(nickname)){
                        check = true;
                    }
                    SubscribItem item = data.get(i);
                    subscribItemArrayList.add(item);
                }
                if(check){
                    subscriptionyes.setVisibility(View.VISIBLE);
                    message.setVisibility(View.VISIBLE);
                }
                else{
                    subscriptionno.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<SubscribItem>> call, Throwable t) {
                Toast.makeText(ChannelActivity.this, "구독 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void subscripstate(){

        if(subscribItemArrayList.isEmpty()){
            subscriptionno.setVisibility(View.VISIBLE);
        }
        else{
            for(int i = 0; i<subscribItemArrayList.size(); i++){
                if(subscribItemArrayList.get(i).getBjnickname().equals(nickname)){
                    subscriptionyes.setVisibility(View.VISIBLE);
                }
                else{
                    subscriptionno.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
