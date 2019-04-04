package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class VideoPlayerActivity extends AppCompatActivity {


    SimpleExoPlayer exoplayer;
    MediaSource videoSource;
    PlayerView player;
    CircleImageView bjimage;
    TextView bjnickname;
    TextView bjtitle;
    TextView bjcontent;
    LinearLayout subscriptionyes;
    LinearLayout subscriptionno;
    ArrayList<SubscribItem> subscribItemArrayList;


    Retrofit retrofit;

    Uri videouri;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String usernickname;
    String playernickname;
    String playerimage;
    String playertitle;
    String playercontent;
    String playervideo;
    String playerhit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Intent intent = getIntent();
        playernickname = intent.getStringExtra("nickname");
        playerimage = intent.getStringExtra("image");
        playertitle = intent.getStringExtra("title");
        playercontent = intent.getStringExtra("content");
        playervideo = intent.getStringExtra("video");
        playerhit = intent.getStringExtra("hit");

        videouri = Uri.parse(BaseURL+playervideo.substring(2));

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        subscribItemArrayList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        usernickname = sharedPreferences.getString("nickname", null);


        player = (PlayerView)findViewById(R.id.player);
        bjimage = (CircleImageView)findViewById(R.id.playeruserimage);
        bjnickname = (TextView)findViewById(R.id.playernickname);
        bjtitle = (TextView)findViewById(R.id.title);
        bjcontent = (TextView)findViewById(R.id.content);
        subscriptionyes = (LinearLayout)findViewById(R.id.subscriptionyes);
        subscriptionno = (LinearLayout)findViewById(R.id.subscriptionno);

        Glide.with(this).load(playerimage).into(bjimage);
        bjnickname.setText(playernickname);
        bjtitle.setText(playertitle);
        bjcontent.setText(playercontent);

        bjnickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoPlayerActivity.this, ChannelActivity.class);
                intent.putExtra("nickname", playernickname);
                if(usernickname.equals(playernickname)){

                }
                else{
                    startActivity(intent);
                }
            }
        });

        subscriptionyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionyes.setVisibility(View.GONE);
                subscriptionno.setVisibility(View.VISIBLE);
                Toast.makeText(VideoPlayerActivity.this, "구독 취소", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(VideoPlayerActivity.this, "구독 완료", Toast.LENGTH_SHORT).show();
                subcription("구독하기");
                subscribItemArrayList.add(new SubscribItem(playernickname,playerimage));
            }
        });

        subscriptionlist();

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);


        exoplayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        player.setPlayer(exoplayer);



        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Exo2"), defaultBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.

        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videouri);

        exoplayer.prepare(videoSource);

        exoplayer.setPlayWhenReady(true);


        if(!usernickname.equals(playernickname)){
            videohit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoplayer.setPlayWhenReady(false);
        exoplayer.stop();
        finish();
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
                    if(data.get(i).getBjnickname().equals(playernickname)){
                        check = true;
                    }
                    SubscribItem item = data.get(i);
                    subscribItemArrayList.add(item);
                }
                if(check){
                    subscriptionyes.setVisibility(View.VISIBLE);
                }
                else{
                    if(usernickname.equals(playernickname)){

                    }
                    else{
                        subscriptionno.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SubscribItem>> call, Throwable t) {
                Toast.makeText(VideoPlayerActivity.this, "구독 가져오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void subscripstate(){

        if(!usernickname.equals(playernickname)){
            if(subscribItemArrayList.isEmpty()){
                subscriptionno.setVisibility(View.VISIBLE);
            }
            else{
                for(int i = 0; i<subscribItemArrayList.size(); i++){
                    if(subscribItemArrayList.get(i).getBjnickname().equals(playernickname)){
                        subscriptionyes.setVisibility(View.VISIBLE);
                    }
                    else{
                        subscriptionno.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void subcription(String action){
        APIservice apIservice = retrofit.create(APIservice.class);

        Call<Check> call = apIservice.Subscription(action, usernickname, playernickname, playerimage);

        call.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                if(response.body().getCheck().equals("ok")){
                    Toast.makeText(VideoPlayerActivity.this, "check ok", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(VideoPlayerActivity.this, "check false", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(VideoPlayerActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void videohit(){
        APIservice apIservice = retrofit.create(APIservice.class);

        Call<Check> call = apIservice.VideoHit(playernickname, playertitle, playervideo);

        call.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                if(response.body().getCheck().equals("ok")){
                    Toast.makeText(VideoPlayerActivity.this, "check ok", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(VideoPlayerActivity.this, "check false", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(VideoPlayerActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
