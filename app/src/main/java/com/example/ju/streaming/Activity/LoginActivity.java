package com.example.ju.streaming.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.R;
import com.example.ju.streaming.WebRTC.ConnectActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;


import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


import static com.kakao.auth.Session.getCurrentSession;

public class LoginActivity extends AppCompatActivity {

    EditText id;
    EditText password;
    Button login;
    Button signup;
    TextView search;

    String submitid;
    String submitpassword;

    Retrofit retrofit;
    APIservice apiservice;

    //카카오 로그인 콜백
    SessionCallback sessionCallback;
    //페이스북 로그인 콜맥 매니저
    CallbackManager callbackManager;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(LoginActivity.this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.MODIFY_AUDIO_SETTINGS,Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_NETWORK_STATE
                ,Manifest.permission.BLUETOOTH)
                .check();

        id = (EditText)findViewById(R.id.id);
        password = (EditText)findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        signup = (Button)findViewById(R.id.signup);
        search = (TextView) findViewById(R.id.search);


        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //로그인한 아이디 임시저장 shared
        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //카카오 로그인
        sessionCallback = new SessionCallback();
        getCurrentSession().addCallback(sessionCallback);
//                getCurrentSession().checkAndImplicitOpen();

        //페이스북 로그인
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        //로컬 로그인 버튼 클릭 리스너
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.getText().toString().replace(" ", "").equals("")||password.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                else{
                    //로그인할 아이디 패스워드 edittext에서 받아오기
                    submitid = id.getText().toString();
                    submitpassword = password.getText().toString();

                    apiservice = retrofit.create(APIservice.class);

                    Call<Check> call = apiservice.id(submitid,submitpassword);

                    Log.e("id",submitid);
                    Log.e("password",submitpassword);

                    // 앞서만든 요청을 수행
                    call.enqueue(new Callback<Check>() {
                        @Override
                        // 성공시
                        public void onResponse(Call<Check> call, Response<Check> response) {
                            Check result = response.body();
                            Log.e("check",result.getCheck());

                            if(result.getCheck().equals("ok")) {

                                Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();
                                //로그인 된 아이디 shared 저장
                                editor.putString("id",submitid);
                                editor.commit();
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        // 실패시
                        public void onFailure(Call<Check> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "정보받아오기 실패", Toast.LENGTH_LONG).show();
                            Log.e("error",t.toString());

                        }
                    });

                }
            }
        });

        //회원가입 클릭 리스너
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


        // 페이스북 로그인
//        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile","email"));
//        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                Log.e("id",profile.getId());
                Log.e("이름",profile.getName());
                final String facebookid = profile.getId();

                apiservice = retrofit.create(APIservice.class);
                Call<Check> call = apiservice.facebook(profile.getId(),profile.getName());

                call.enqueue(new Callback<Check>() {
                    @Override
                    public void onResponse(Call<Check> call, Response<Check> response) {
                        Check result = response.body();
                        Log.e("로그인 체크", result.getCheck());

                        if(result.getCheck().equals("ok")){
                            Toast.makeText(LoginActivity.this, "페이스북 로그인 성공", Toast.LENGTH_SHORT).show();
                            editor.putString("id",facebookid);
                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else if(result.getCheck().equals("false")){
                            Toast.makeText(LoginActivity.this, "실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Check> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "정보받아오기 실패", Toast.LENGTH_LONG).show();
                        Log.e("error",t.toString());
                    }
                });

            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 카카오 로그인
    public void request(){
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {

            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.e("이메일",result.getKakaoAccount().getEmail());
                Log.e("닉네임",result.getNickname());

                final String kakaoid = result.getKakaoAccount().getEmail();

                apiservice = retrofit.create(APIservice.class);

                Call<Check> call = apiservice.kakao(result.getKakaoAccount().getEmail(),result.getNickname());

                call.enqueue(new Callback<Check>() {
                    @Override
                    public void onResponse(Call<Check> call, Response<Check> response) {
                        Check result = response.body();
                        Log.e("로그인 체크", result.getCheck());

                        if(result.getCheck().equals("ok")){
                            Toast.makeText(LoginActivity.this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show();
                            editor.putString("id",kakaoid);
                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else if(result.getCheck().equals("false")){
                            Toast.makeText(LoginActivity.this, "실패", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<Check> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "정보받아오기 실패", Toast.LENGTH_LONG).show();
                        Log.e("error",t.toString());
                    }
                });
            }
        });
    }

    private class SessionCallback implements ISessionCallback{
        //access token 발급. 다음액티비티 이동
        @Override
        public void onSessionOpened() {
            request();
        }
        //token 실패
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.d("error", "Session Fail Error is " + exception.getMessage().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(getCurrentSession().handleActivityResult(requestCode, resultCode, data)){
            return ;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LoginManager.getInstance().logOut();
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().logOut();
    }
}






