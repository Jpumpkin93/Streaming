package com.example.ju.streaming.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.Adapter.UserListAdapter;
import com.example.ju.streaming.ItemClass.User;
import com.example.ju.streaming.ItemClass.UserListItem;
import com.example.ju.streaming.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class UserListActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    UserListAdapter userListAdapter;

    Retrofit retrofit;

    String usernickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<UserListItem> userListItems = new ArrayList<>();
        userListAdapter = new UserListAdapter(userListItems, this);

        mRecyclerView.setAdapter(userListAdapter);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userlist();

    }

    public void userlist(){

        Log.e("시작", "시작됨");

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<List<User>> call = apiservice.UserList(usernickname);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(response.body() !=null){
                    List<User> data = response.body();
                    for(int i = 0; i<data.size(); i++){
                        String image = data.get(i).getImage();
                        if(image.equals("")){
                            image = "./UserProfile/profile.jpg";
                        }
                        String nickname = data.get(i).getNickname();
                        userListAdapter.add(image,nickname);
                    }
                    userListAdapter.notifyDataSetChanged();
                }
                else{
                    Log.e("상태","no");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(UserListActivity.this, "정보 받아오기 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러",t.getMessage());
            }
        });
    }
}
