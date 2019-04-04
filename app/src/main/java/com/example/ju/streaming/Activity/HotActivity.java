package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.HotListAdapter;
import com.example.ju.streaming.Adapter.VideoListAdapter;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.example.ju.streaming.R;
import com.example.ju.streaming.Service.MySocketService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class HotActivity extends AppCompatActivity {

    ImageButton live;
    ImageButton subscription;
    ImageButton message;
    ImageButton profile;

    RecyclerView hotvideolist;
    RecyclerView.LayoutManager mLayoutManager;
    HotListAdapter hotListAdapter;
    ArrayList<VideoListItem> videoListAdapterArrayList;

    Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot);


        hotvideolist = (RecyclerView)findViewById(R.id.hotvideolist);
//        hotvideolist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        hotvideolist.setLayoutManager(mLayoutManager);
        videoListAdapterArrayList = new ArrayList<>();

        hotListAdapter = new HotListAdapter(videoListAdapterArrayList, this);
        hotvideolist.setAdapter(hotListAdapter);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        //메뉴바
        live = (ImageButton)findViewById(R.id.live);
        subscription = (ImageButton)findViewById(R.id.subscription);
        message = (ImageButton)findViewById(R.id.message);
        profile = (ImageButton)findViewById(R.id.profile);

        live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HotActivity.this, LiveActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HotActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HotActivity.this, ChatListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HotActivity.this, MainActivity.class);
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
        Intent ServiceStart = new Intent(HotActivity.this,MySocketService.class);
        startService(ServiceStart);
        videolistview();

    }

    private void videolistview(){
        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<VideoListItem>> call = apiservice.VideoListView("hot");

        call.enqueue(new Callback<List<VideoListItem>>() {
            @Override
            public void onResponse(Call<List<VideoListItem>> call, Response<List<VideoListItem>> response) {
                if(response.body()==null){
                    Toast.makeText(HotActivity.this, "올린 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    List<VideoListItem> data = response.body();

                    hotListAdapter.clear();

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
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hotListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<VideoListItem>> call, Throwable t) {
                Toast.makeText(HotActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
