package com.example.ju.streaming.Activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.ju.streaming.Adapter.NewsAdapter;
import com.example.ju.streaming.ItemClass.NewsItem;
import com.example.ju.streaming.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsActivity extends AppCompatActivity {

    @BindView(R.id.newscontents)
    RecyclerView newscontents;
    RecyclerView.LayoutManager newlayoutmanager;
    ArrayList<NewsItem> newsItemArrayList;
    NewsAdapter newsAdapter;

    String htmlPageUrl = "http://www.yonhapnews.co.kr/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);

        newscontents.setHasFixedSize(true);
        newlayoutmanager = new LinearLayoutManager(this);
        newscontents.setLayoutManager(newlayoutmanager);
        newsItemArrayList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsItemArrayList, this);

        newscontents.setAdapter(newsAdapter);

        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
            ProgressDialog asyncDialog = new ProgressDialog(NewsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            asyncDialog.setMessage("데이터를 불러오는 중입니다...");
            asyncDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Document doc = Jsoup.connect(htmlPageUrl).get();

                Elements titles= doc.select("div.news-con h1.tit-news");
                Elements images = doc.select("div.img-con");

                ArrayList<String> titlearray = new ArrayList<>();
                ArrayList<String> imagearray = new ArrayList<>();
                ArrayList<String> urlarray = new ArrayList<>();

                titlearray.add(titles.text());

                for(int i = 0; i <12; i++){
                    String newsimage = images.get(i).getElementsByTag("img").attr("src");
                    String newsurl = images.get(i).getElementsByTag("a").attr("href");
                    imagearray.add(newsimage);
                    urlarray.add(newsurl);
                }

                for(int i = 0; i <11; i++){
                    Elements smalltitles= doc.select("div.news-con h2.tit-news");
                    String newstitle = smalltitles.get(i).getElementsByTag("h2").text();
                    titlearray.add(newstitle);
                }

                for(int i = 0; i<12; i++){
                    NewsItem itemlist = new NewsItem(imagearray.get(i),titlearray.get(i),urlarray.get(i));
                    newsItemArrayList.add(itemlist);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        newsAdapter.notifyDataSetChanged();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
        }
    }
}
