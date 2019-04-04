package com.example.ju.streaming.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class SignUpActivity extends AppCompatActivity {

    EditText id;
    Button idcheck;
    EditText password;
    EditText passwordcheck;
    EditText nickname;
    EditText email;
    Button signup;

    int check = 0;
    String gender = "성별";

    Retrofit retrofit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        id = (EditText) findViewById(R.id.id);
        idcheck = (Button) findViewById(R.id.idcheck);
        password = (EditText) findViewById(R.id.password);
        passwordcheck = (EditText) findViewById(R.id.passwordcheck);
        nickname = (EditText) findViewById(R.id.nickname);

        email = (EditText) findViewById(R.id.email);
        signup = (Button) findViewById(R.id.signup);


        idcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (id.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(SignUpActivity.this, "아이디를 입력해주세요.", Toast.LENGTH_LONG).show();
                } else {
                    String submitid = id.getText().toString();

                    APIservice apiservice = retrofit.create(APIservice.class);

                    Call<Check> call = apiservice.id(submitid);

                    // 앞서만든 요청을 수행
                    call.enqueue(new Callback<Check>() {
                        @Override
                        // 성공시
                        public void onResponse(Call<Check> call, Response<Check> response) {
                            Check result = response.body();
                            Log.e("check", result.getCheck());

                            if (result.getCheck().equals("ok")) {

                                Toast.makeText(SignUpActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_LONG).show();
                                check = 1;

                            } else {
                                Toast.makeText(SignUpActivity.this, "중복된 아이디입니다.", Toast.LENGTH_LONG).show();
                                check = 0;
                            }

                        }

                        @Override
                        // 실패시
                        public void onFailure(Call<Check> call, Throwable t) {
                            Toast.makeText(SignUpActivity.this, "정보받아오기 실패", Toast.LENGTH_LONG).show();
                            Log.e("error", t.toString());

                        }
                    });
                }

            }
        });


        signup.setOnClickListener(new View.OnClickListener() {  //회원가입 버튼
            @Override
            public void onClick(View v) {
                if ((id.getText().toString().replace(" ", "").equals("") || password.getText().toString().replace(" ", "").equals("")
                        || passwordcheck.getText().toString().replace(" ", "").equals("") || nickname.getText().toString().replace(" ", "").equals("")
                        || email.getText().toString().replace(" ", "").equals(""))) {
                    Toast.makeText(SignUpActivity.this, "빈 칸을 채워주세요", Toast.LENGTH_SHORT).show();
                }// 빈 칸 존재 확인
                else {
                    if (check == 0) {   // 아이디 중복확인
                        Toast.makeText(SignUpActivity.this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (password.getText().toString().equals(passwordcheck.getText().toString())) {
                            APIservice apiservice = retrofit.create(APIservice.class);

                            Call<Check> call = apiservice.sign(id.getText().toString(), password.getText().toString(), nickname.getText().toString(), email.getText().toString());


                            // 앞서만든 요청을 수행
                            call.enqueue(new Callback<Check>() {
                                @Override
                                // 성공시
                                public void onResponse(Call<Check> call, Response<Check> response) {
                                    Check result = response.body();
                                    Log.e("check", result.getCheck());

                                    if (result.getCheck().equals("ok")) {

                                        Toast.makeText(SignUpActivity.this, "회원가입 성공.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(SignUpActivity.this, "회원가입 실패.", Toast.LENGTH_LONG).show();

                                    }

                                }

                                @Override
                                // 실패시
                                public void onFailure(Call<Check> call, Throwable t) {
                                    Toast.makeText(SignUpActivity.this, "정보받아오기 실패", Toast.LENGTH_LONG).show();
                                    Log.e("error", t.toString());

                                }
                            });


                        }
                        else{
                            Toast.makeText(SignUpActivity.this, "비밀번호를 확인 해주세요.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });
    }
}







