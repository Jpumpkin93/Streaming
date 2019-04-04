package com.example.ju.streaming.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.Profile;
import com.example.ju.streaming.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class Profile_EditActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    CircleImageView profileimage;
    TextView imageedit;
    EditText nickname;
    EditText introduce;
    EditText email;
    Button edit;

    String uploadimagePath;

    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;

    static final int REQUEST_TAKE_PHOTO = 2222;
    static final int REQUEST_TAKE_ALBUM = 3333;
    static final int REQUEST_IMAGE_CROP = 4444;

    private String mCurrentPhotoPath;

    Uri imageUri;
    Uri photoURI, albumURI;

    Retrofit retrofit;

    String userid;
    String usernickname;
    String userimage;
    String userintroduce;
    String useremail;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    final int PROFILE_EDIT = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile__edit);

        profileimage = (CircleImageView) findViewById(R.id.profile_image);
        imageedit = (TextView) findViewById(R.id.imageedit);
        nickname = (EditText) findViewById(R.id.nickname);
        introduce = (EditText) findViewById(R.id.introduce);
        email = (EditText) findViewById(R.id.email);
        edit  = (Button)findViewById(R.id.edit);

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sharedPreferences = getSharedPreferences("유저정보",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        userid = sharedPreferences.getString("id","id");
        usernickname = sharedPreferences.getString("nickname", "nickname");
        userimage = sharedPreferences.getString("image", "image");
        userintroduce = sharedPreferences.getString("introduce", "introduce");
        useremail = sharedPreferences.getString("email", "email");

        mCurrentPhotoPath = userimage;

        Glide.with(this).load(userimage).into(profileimage);
        nickname.setText(usernickname);
        introduce.setText(userintroduce);
        email.setText(useremail);

        imageedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileedit();
            }
        });
    }


    public void dialog() {
        final CharSequence[] items = {"카메라", "갤러리"};

        AlertDialog.Builder builder = new AlertDialog.Builder(Profile_EditActivity.this);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    PermissionListener permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        captureCamera();

                    }
                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                    };
                    TedPermission.with(Profile_EditActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setRationaleMessage("카메라,외부저장장치 접근 권한이 필요해요")
                            .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                            .check();
                }
                else if (which == 1) {
                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                            getAlbum();

                        }
                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                        Toast.makeText(Profile_EditActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    TedPermission.with(Profile_EditActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setRationaleMessage("카메라,외부저장장치 접근 권한이 필요해요")
                            .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                            .check();
                    Toast.makeText(Profile_EditActivity.this, "갤러리", Toast.LENGTH_SHORT).show();

                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void captureCamera(){
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch(IOException e){
                    Log.e("error",e.toString());
                }
                if(photoFile != null){

                    Uri providerURI = FileProvider.getUriForFile(this, "com.example.ju.streaming.fileprovider", photoFile);
                    imageUri = providerURI;

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
            else{
                Toast.makeText(this, "저장공간이 접근 불가능한 기기", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+ timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/Pictures", "jungho");
        if(!storageDir.exists()){
            Log.e("mCurrentPhotoPath1",storageDir.toString());
            storageDir.mkdirs();
        }
        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void getAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public  void cropImage(){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        cropIntent.putExtra("outputX",200);
        cropIntent.putExtra("outputY",200);
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("scale",true);
        cropIntent.putExtra("output",albumURI);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);

    }

    public void cropSingleImage(Uri photoUriPath){

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try{
            File cameraFile = null;
            cameraFile = createImageFile();
            albumURI = Uri.fromFile(cameraFile);
        }catch (Exception e){
            Log.e("error",e.toString());
        }

        cropIntent.setDataAndType(photoUriPath, "image/*");
        cropIntent.putExtra("outputX",200);
        cropIntent.putExtra("outputY",200);
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("scale",true);
        cropIntent.putExtra("output",albumURI);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);


//        List list = getPackageManager().queryIntentActivities(cropIntent,0);
//        grantUriPermission(list.get(0).);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode){
                case REQUEST_TAKE_PHOTO :
                    if(resultCode == Activity.RESULT_OK){
                        try{
                            cropSingleImage(imageUri);
//                            Glide.with(Profile_EditActivity.this).load(imageUri).into(profileimage);
//                            Log.e("카메라 찍고 경로",mCurrentPhotoPath);
                        }catch (Exception e){
                            Log.e("에러야",e.toString());
                        }
                    }
                    break;

                case REQUEST_TAKE_ALBUM:
                    if(resultCode == Activity.RESULT_OK){
                        try{
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        }catch (Exception e){
                            Log.e("error",e.toString());
                        }
                    }
                    break;
                case REQUEST_IMAGE_CROP:
                    if(resultCode == Activity.RESULT_OK){

                        galleryAddPic();
                        Glide.with(Profile_EditActivity.this).load(albumURI).into(profileimage);
                        uploadimagePath = albumURI.toString();
                        Log.e("uri경로",albumURI.toString());
                        Log.e("path경로",uploadimagePath);
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

    public void profileedit(){
        APIservice apiservice = retrofit.create(APIservice.class);
        if(mCurrentPhotoPath.equals(userimage)){

            MultipartBody.Part idbody = MultipartBody.Part.createFormData("id",userid);
            MultipartBody.Part nicknamebody = MultipartBody.Part.createFormData("nickname",nickname.getText().toString());
            MultipartBody.Part introducebody = MultipartBody.Part.createFormData("introduce",introduce.getText().toString());
            MultipartBody.Part emailbody = MultipartBody.Part.createFormData("email",email.getText().toString());

            Call<Profile> call = apiservice.NoimageProfileEdit(idbody, nicknamebody, introducebody, emailbody);

            call.enqueue(new Callback<Profile>() {
                @Override
                public void onResponse(Call<Profile> call, Response<Profile> response) {
                    Profile result = response.body();
                    if (result.getCheck().equals("ok")) {

                        Toast.makeText(Profile_EditActivity.this, "프로필 변경 성공.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Profile_EditActivity.this,MainActivity.class);
                        onActivityResult(PROFILE_EDIT,100,intent);
                        finish();


                    } else {
                        Toast.makeText(Profile_EditActivity.this, "프로필 변경 실패.", Toast.LENGTH_LONG).show();
                        Log.e("업로드 결과",result.getCheck());

                    }
                }

                @Override
                public void onFailure(Call<Profile> call, Throwable t) {
                    Toast.makeText(Profile_EditActivity.this, "fail", Toast.LENGTH_SHORT).show();
                }
            });


        }
        else{
            File file = new File(mCurrentPhotoPath);

            MultipartBody.Part idbody = MultipartBody.Part.createFormData("id",userid);
            MultipartBody.Part nicknamebody = MultipartBody.Part.createFormData("nickname",nickname.getText().toString());
            MultipartBody.Part introducebody = MultipartBody.Part.createFormData("introduce",introduce.getText().toString());
            MultipartBody.Part emailbody = MultipartBody.Part.createFormData("email",email.getText().toString());

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part filebody = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

            Call<Profile> call = apiservice.ProfileEdit(idbody, filebody, nicknamebody, introducebody, emailbody);

            call.enqueue(new Callback<Profile>() {
                @Override
                public void onResponse(Call<Profile> call, Response<Profile> response) {
                    Profile result = response.body();
                    if (result.getCheck().equals("ok")) {

                        Toast.makeText(Profile_EditActivity.this, "프로필 변경 성공.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Profile_EditActivity.this,MainActivity.class);
                        onActivityResult(PROFILE_EDIT,100,intent);
                        finish();


                    } else {
                        Toast.makeText(Profile_EditActivity.this, "프로필 변경 실패.", Toast.LENGTH_LONG).show();
                        Log.e("업로드 결과",result.getCheck());

                    }
                }

                @Override
                public void onFailure(Call<Profile> call, Throwable t) {
                    Toast.makeText(Profile_EditActivity.this, "fail", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


}
