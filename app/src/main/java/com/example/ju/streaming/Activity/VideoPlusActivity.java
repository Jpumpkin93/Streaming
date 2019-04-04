package com.example.ju.streaming.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class VideoPlusActivity extends AppCompatActivity {

    ImageView thumbnail;
    Button thumbnailselect;
    TextView videopath;
    Button videoselect;
    EditText videotitle;
    EditText videocontent;
    Button upload;

    String userid;
    String usernickname;
    String userimage;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Retrofit retrofit;

    final int VIDEO_PLUS = 200;
    final int TAKE_ALBUM = 400;
    final int TAKE_VIDEO = 500;
    String currentimagepath;
    String currentvideopath;
    Uri photoURI;
    Uri VideoURI;

    Boolean aBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_plus);

        thumbnail = (ImageView)findViewById(R.id.thumbnail);
        thumbnailselect = (Button)findViewById(R.id.thumbnailselect);
        videopath = (TextView)findViewById(R.id.videopath);
        videoselect = (Button)findViewById(R.id.videoselect);
        videotitle = (EditText)findViewById(R.id.videotitle);
        videocontent = (EditText)findViewById(R.id.videocontent);
        upload = (Button)findViewById(R.id.upload);

        sharedPreferences = getSharedPreferences("유저정보", MODE_PRIVATE);
        userid = sharedPreferences.getString("id",null);
        usernickname = sharedPreferences.getString("nickname",null);
        userimage = sharedPreferences.getString("image",null);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        thumbnailselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbum();
            }
        });

        videoselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideo();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentimagepath == null || currentvideopath == null || videotitle.getText().toString().replace(" ","").equals("") || videocontent.getText().toString().replace(" ","").equals("")){
                    Toast.makeText(VideoPlusActivity.this, "빈 칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    UploadAsync async = new UploadAsync();
                    async.execute();
                }
            }
        });
    }

    private void getAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, TAKE_ALBUM);
    }

    private void getVideo(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, TAKE_VIDEO);
    }

    private void videoupload(){
        APIservice apiservice = retrofit.create(APIservice.class);

        MultipartBody.Part nicknamebody = MultipartBody.Part.createFormData("nickname",usernickname);
        MultipartBody.Part imagebody = MultipartBody.Part.createFormData("image",userimage);
        MultipartBody.Part titlebody = MultipartBody.Part.createFormData("title",videotitle.getText().toString());
        MultipartBody.Part contentbody = MultipartBody.Part.createFormData("content",videocontent.getText().toString());
        MultipartBody.Part playtimebody = MultipartBody.Part.createFormData("playtime",getPlayTime(currentvideopath));

        File thumbnailfile = new File(currentimagepath);
        Log.e("이미지 절대 경로", currentimagepath);
        Log.e("파일이름", thumbnailfile.getName());

        RequestBody thumbnailBody = RequestBody.create(MediaType.parse("multipart/form-data"), thumbnailfile);
        MultipartBody.Part thumbnailfilebody = MultipartBody.Part.createFormData("thumbnailuploaded_file", thumbnailfile.getName(), thumbnailBody);

        File videofile = new File(currentvideopath);


        RequestBody videoBody = RequestBody.create(MediaType.parse("multipart/form-data"), videofile);
        MultipartBody.Part videofilebody = MultipartBody.Part.createFormData("videouploaded_file", videofile.getName(), videoBody);

        Call<Check> call = apiservice.VideoUpload(thumbnailfilebody, videofilebody, nicknamebody, imagebody, titlebody, contentbody, playtimebody);

        call.enqueue(new Callback<Check>() {
            @Override
            public void onResponse(Call<Check> call, Response<Check> response) {
                Check check = response.body();
//                Log.e("에러", Integer.toString(check.getError()));
//                Log.e("bool", Boolean.toString(check.isBool()));
                if (check.getCheck().equals("ok")) {
                    Toast.makeText(VideoPlusActivity.this, "업로드 성공.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(VideoPlusActivity.this,MainActivity.class);
                    onActivityResult(VIDEO_PLUS,200,intent);
                    aBoolean = true;
                    finish();
                } else {
                    Toast.makeText(VideoPlusActivity.this, "업로드 실패.", Toast.LENGTH_LONG).show();
                    Log.e("결과",check.getCheck());
                }
            }

            @Override
            public void onFailure(Call<Check> call, Throwable t) {
                Toast.makeText(VideoPlusActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_ALBUM:
                if(resultCode == Activity.RESULT_OK){
                    try{
                        photoURI = data.getData();
                        Glide.with(this).load(photoURI).into(thumbnail);
                        currentimagepath = getPath(photoURI);
                        Log.e("이미지 경로", currentimagepath);
                    }catch (Exception e){
                        Log.e("error",e.toString());
                    }
                }
                break;
            case TAKE_VIDEO:
                if(resultCode == Activity.RESULT_OK){
                    try{
                        VideoURI = data.getData();
                        currentvideopath = getPath(VideoURI);
                        videopath.setText(currentvideopath);
                        Log.e("비디오 경로", currentvideopath);
                    }catch (Exception e){
                        Log.e("error",e.toString());
                    }
                }
                break;
        }
    }


    //이미지 절대 경로
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    private String getPlayTime(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong( time );
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);

        if(minutes<10){
            return "0"+minutes+":"+seconds;
        }
        return minutes + " : " + seconds;
    }

    public class UploadAsync extends AsyncTask{

        ProgressDialog asyncDialog = new ProgressDialog(VideoPlusActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            asyncDialog.setMessage("업로드 중 입니다...");
            asyncDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
//            APIservice apiservice = retrofit.create(APIservice.class);
//
//            MultipartBody.Part nicknamebody = MultipartBody.Part.createFormData("nickname",usernickname);
//            MultipartBody.Part imagebody = MultipartBody.Part.createFormData("image",userimage);
//            MultipartBody.Part titlebody = MultipartBody.Part.createFormData("title",videotitle.getText().toString());
//            MultipartBody.Part contentbody = MultipartBody.Part.createFormData("content",videocontent.getText().toString());
//            MultipartBody.Part playtimebody = MultipartBody.Part.createFormData("playtime",getPlayTime(currentvideopath));
//
//
//
//            File thumbnailfile = new File(currentimagepath);
//            Log.e("이미지 절대 경로", currentimagepath);
//            Log.e("파일이름", thumbnailfile.getName());
//
//            RequestBody thumbnailBody = RequestBody.create(MediaType.parse("multipart/form-data"), thumbnailfile);
//            MultipartBody.Part thumbnailfilebody = MultipartBody.Part.createFormData("thumbnailuploaded_file", thumbnailfile.getName(), thumbnailBody);
//
//            File videofile = new File(currentvideopath);
//
//
//            RequestBody videoBody = RequestBody.create(MediaType.parse("multipart/form-data"), videofile);
//            MultipartBody.Part videofilebody = MultipartBody.Part.createFormData("videouploaded_file", videofile.getName(), videoBody);
//
//            Call<Check> call = apiservice.VideoUpload(thumbnailfilebody, videofilebody, nicknamebody, imagebody, titlebody, contentbody, playtimebody);
//
//            call.enqueue(new Callback<Check>() {
//                @Override
//                public void onResponse(Call<Check> call, Response<Check> response) {
//                    Check check = response.body();
////                Log.e("에러", Integer.toString(check.getError()));
////                Log.e("bool", Boolean.toString(check.isBool()));
//                    if (check.getCheck().equals("ok")) {
//                        Toast.makeText(VideoPlusActivity.this, "업로드 성공.", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(VideoPlusActivity.this,MainActivity.class);
//                        onActivityResult(VIDEO_PLUS,200,intent);
//                        finish();
//                    } else {
//                        Toast.makeText(VideoPlusActivity.this, "업로드 실패.", Toast.LENGTH_LONG).show();
//                        Log.e("결과",check.getCheck());
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Check> call, Throwable t) {
//                    Toast.makeText(VideoPlusActivity.this, "통신 실패", Toast.LENGTH_SHORT).show();
//                }
//            });
            videoupload();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
//                asyncDialog.dismiss();

        }
    }
}
