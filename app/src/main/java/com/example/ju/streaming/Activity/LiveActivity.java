package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.LiveListAdapter;
import com.example.ju.streaming.Adapter.UserListAdapter;
import com.example.ju.streaming.ItemClass.LiveListItem;
import com.example.ju.streaming.ItemClass.UserListItem;
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


public class LiveActivity extends AppCompatActivity {

    ImageButton hot;
    ImageButton subscription;
    ImageButton message;
    ImageButton profile;

    FloatingActionButton livestart;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    LiveListAdapter liveListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);



        //메뉴바
        hot = (ImageButton)findViewById(R.id.hot);
        subscription = (ImageButton)findViewById(R.id.subscription);
        message = (ImageButton)findViewById(R.id.message);
        profile = (ImageButton)findViewById(R.id.profile);

        hot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveActivity.this, HotActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        subscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveActivity.this, ChatListActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        // 메뉴바 끝

        livestart = (FloatingActionButton)findViewById(R.id.livestart);
        livestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LiveActivity.this, LiveVideoBroadcasterActivity.class);
                startActivity(i);
            }
        });

        mRecyclerView = (RecyclerView)findViewById(R.id.livelist);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Livelist();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ArrayList<LiveListItem> liveListItemArrayList = new ArrayList<>();
        liveListAdapter = new LiveListAdapter(liveListItemArrayList, this);

        mRecyclerView.setAdapter(liveListAdapter);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Livelist();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Intent ServiceStart = new Intent(LiveActivity.this,MySocketService.class);
        startService(ServiceStart);
    }

    public void Livelist(){

        Log.e("시작", "시작됨");

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<LiveListItem>> call = apiservice.LiveListView("조회");

        call.enqueue(new Callback<List<LiveListItem>>() {
            @Override
            public void onResponse(Call<List<LiveListItem>> call, Response<List<LiveListItem>> response) {
                if(response.body() !=null){
                    List<LiveListItem> data = response.body();
                    liveListAdapter.clear();
                    for(int i = 0; i<data.size(); i++){
                        String id = data.get(i).getId();
                        String image = data.get(i).getImage();
                        String nickname = data.get(i).getNickname();
                        String title = data.get(i).getTitle();
                        String thumbnail = data.get(i).getThumbnail();
                        String liveaddress = data.get(i).getLiveaddress();
                        liveListAdapter.add(id,image,title,nickname, thumbnail, liveaddress);
                    }
                    liveListAdapter.notifyDataSetChanged();
                }
                else{
                    Log.e("상태","no");
                }
            }

            @Override
            public void onFailure(Call<List<LiveListItem>> call, Throwable t) {
                Toast.makeText(LiveActivity.this, "정보 받아오기 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러",t.getMessage());
            }
        });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
////        액티비티 Destroy시 서비스 중지.
//        Intent ServiceStop = new Intent(LiveActivity.this,MySocketService.class);
//        stopService(ServiceStop);
//    }
}
