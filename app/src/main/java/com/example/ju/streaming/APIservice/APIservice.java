package com.example.ju.streaming.APIservice;

import com.example.ju.streaming.ItemClass.ChatListItem;
import com.example.ju.streaming.ItemClass.Check;
import com.example.ju.streaming.ItemClass.ItemCount;
import com.example.ju.streaming.ItemClass.LiveListItem;
import com.example.ju.streaming.ItemClass.Profile;
import com.example.ju.streaming.ItemClass.SubscribItem;
import com.example.ju.streaming.ItemClass.User;
import com.example.ju.streaming.ItemClass.VideoListItem;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIservice {
    // GET/POST/DELETE/PUT 메소드들을 인터페이스에 구현하여 사용할 수 있다.

    @FormUrlEncoded
    @POST("LoginCheck.php")
    Call<Check> id(@Field("id") String id, @Field("password") String password);

    @FormUrlEncoded
    @POST("IdCheck.php")
    Call<Check> id(@Field("id") String id);

    @FormUrlEncoded
    @POST("SignUp.php")
    Call<Check> sign(@Field("id") String id, @Field("password") String password, @Field("nickname") String name, @Field("email") String email);

    @FormUrlEncoded
    @POST("KakaoLogin.php")
    Call<Check> kakao(@Field("id") String id, @Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("FacebookLogin.php")
    Call<Check> facebook(@Field("id") String id, @Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("Profile.php")
    Call<Profile> Profile(@Field("id") String id);

    @Multipart
    @POST("ProfileEdit.php")
    Call<Profile> ProfileEdit(@Part MultipartBody.Part id, @Part MultipartBody.Part File, @Part MultipartBody.Part nickname, @Part MultipartBody.Part introduce, @Part MultipartBody.Part email);

    @Multipart
    @POST("NoimageProfileEdit.php")
    Call<Profile> NoimageProfileEdit(@Part MultipartBody.Part id, @Part MultipartBody.Part nickname, @Part MultipartBody.Part introduce, @Part MultipartBody.Part email);

    @FormUrlEncoded
    @POST("UserList.php")
    Call<List<User>> UserList(@Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("ChatList.php")
    Call<Check> ChatList(@Field("nickname") String nickname, @Field("younickname") String younickname, @Field("lastmessage") String lastmessage, @Field("hit") int hit);

    @FormUrlEncoded
    @POST("ChatListView.php")
    Call<List<ChatListItem>> ChatListView(@Field("mynick") String nickname);

    @FormUrlEncoded
    @POST("ReceiverProfile.php")
    Call<Profile> ReceiverProfile(@Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("HitFresh.php")
    Call<Check> HitFresh(@Field("nickname") String nickname, @Field("younickname") String younickname);

    @FormUrlEncoded
    @POST("LiveList.php")
    Call<Check> LiveList(@Field("action") String action, @Field("id") String id, @Field("nickname") String nickname, @Field("image") String image, @Field("title") String title, @Field("thumbnail") String thumbnail, @Field("liveaddress") String liveaddress);

    @FormUrlEncoded
    @POST("LiveList.php")
    Call<List<LiveListItem>> LiveListView(@Field("action") String action);

    @Multipart
    @POST("VideoUpload.php")
    Call<Check> VideoUpload(@Part MultipartBody.Part thumbnailuploaded_file, @Part MultipartBody.Part videouploaded_file, @Part MultipartBody.Part nickname,
                            @Part MultipartBody.Part image, @Part MultipartBody.Part title, @Part MultipartBody.Part content, @Part MultipartBody.Part playtime);

    @FormUrlEncoded
    @POST("VideoListView.php")
    Call<List<VideoListItem>> VideoListView(@Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("ChannelProfile.php")
    Call<Profile> ChannelProfile(@Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("Subscription.php")
    Call<Check> Subscription(@Field("action") String action, @Field("subscriber") String subscriber, @Field("bjnickname") String bjnickname, @Field("bjimage") String bjimage);

    @FormUrlEncoded
    @POST("SubscripView.php")
    Call<List<SubscribItem>> SubscriptionList(@Field("nickname") String nickname);

    @FormUrlEncoded
    @POST("SubscripVideoView.php")
    Call<List<VideoListItem>> SubscriptionVideoList(@Field("nicknamelist") String nicknamelist);

    @FormUrlEncoded
    @POST("VideoHit.php")
    Call<Check> VideoHit(@Field("nickname") String nickname, @Field("title") String title, @Field("video") String video);

    @FormUrlEncoded
    @POST("Record.php")
    Call<Check> Record(@Field("action") String action, @Field("id") String id, @Field("nickname") String nickname
            , @Field("image") String image,@Field("thumbnail") String thumbnail, @Field("title") String title,  @Field("video") String video, @Field("playtime") String playtime);

    @FormUrlEncoded
    @POST("Record.php")
    Call<List<VideoListItem>> RecordList(@Field("action") String action, @Field("nicknamelist") String nicknamelist);

    @FormUrlEncoded
    @POST("ItemCount.php")
    Call<ItemCount> ItemCount(@Field("action") String action, @Field("nickname") String nickname, @Field("count") String count);



}
