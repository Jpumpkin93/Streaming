package com.example.ju.streaming.Activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ju.streaming.APIservice.APIservice;
import com.example.ju.streaming.ItemClass.ItemCount;
import com.example.ju.streaming.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.ju.streaming.Service.MySocketService.BaseURL;


public class MyItemActivity extends AppCompatActivity {


    private final String APP_SCHEME = "iamportapp";

    WebView webView;

    @BindView(R.id.coincount)
    TextView coincount;
    @BindView(R.id.coinbuy10)
    Button coinbuy10;
    @BindView(R.id.coinbuy50)
    Button coinbuy50;
    @BindView(R.id.coinbuy100)
    Button coinbuy100;
    @BindView(R.id.qrcode)
    Button qrcode;

    Retrofit retrofit;
    String usernickname;

    private IntentIntegrator qrScan;
    IntentIntegrator integrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        usernickname = intent.getStringExtra("유저닉네임");

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ZxingActivity.class);
        integrator.setOrientationLocked(false);


        coinbuy10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MyItemActivity.this, PayActivity.class);
                i.putExtra("가격", "1100");
                i.putExtra("유저닉네임",usernickname);
                startActivity(i);
            }
        });
        coinbuy50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyItemActivity.this, "50개 사기", Toast.LENGTH_SHORT).show();
            }
        });
        coinbuy100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyItemActivity.this, "100개 사기", Toast.LENGTH_SHORT).show();
            }
        });
        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scan option
                integrator.setPrompt("Scanning...");
                //qrScan.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        itemcount("조회",usernickname,"");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = intent.toString();

        if ( url.startsWith(APP_SCHEME) ) {
            // "iamportapp://https://pgcompany.com/foo/bar"와 같은 형태로 들어옴
            String redirectURL = url.substring(APP_SCHEME.length() + "://".length());
            webView.loadUrl(redirectURL);
        }
    }

    public void itemcount(String action, String usernickname, String count){

        APIservice apiservice = retrofit.create(APIservice.class);

        Call<ItemCount> call = apiservice.ItemCount(action,usernickname, count);

        call.enqueue(new Callback<ItemCount>() {
            @Override
            public void onResponse(Call<ItemCount> call, Response<ItemCount> response) {
                ItemCount count = response.body();
                coincount.setText(count.getCount());
            }

            @Override
            public void onFailure(Call<ItemCount> call, Throwable t) {
                Toast.makeText(MyItemActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // QR코드/ 바코드를 스캔한 결과
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // result.getFormatName() : 바코드 종류
        // result.getContents() : 바코드 값
//        qrcontent.setText( result.getContents());

        AlertDialog.Builder builder = new AlertDialog.Builder(MyItemActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialog_layout = inflater.inflate(R.layout.item_gift_layout,null);
        final TextView havestar = (TextView)dialog_layout.findViewById(R.id.havestar);
        havestar.setText("별풍선");

        final TextView coincount = (TextView)dialog_layout.findViewById(R.id.coincount);
        coincount.setText(result.getContents());

        final EditText giftcoincount = (EditText)dialog_layout.findViewById(R.id.giftcoincount);
        giftcoincount.setVisibility(View.GONE);

        final Button sendcoin = (Button)dialog_layout.findViewById(R.id.sendcoin);
        sendcoin.setText("충전");

        builder.setView(dialog_layout);
        final AlertDialog dialog = builder.create();

        sendcoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemcount("쿠폰", usernickname, result.getContents());
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
