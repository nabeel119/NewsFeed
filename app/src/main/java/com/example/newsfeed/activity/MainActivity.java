package com.example.newsfeed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsfeed.R;
import com.example.newsfeed.adapter.MainArticleAdapter;
import com.example.newsfeed.model.Article;
import com.example.newsfeed.model.ResponseModel;
import com.example.newsfeed.model.SQLiteDatabaseHandler;
import com.example.newsfeed.rest.APIInterface;
import com.example.newsfeed.rest.ApiClient;
import com.example.newsfeed.utils.OnRecyclerViewItemClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {
    private static final String API_KEY = "804bd6fd82524583bfd2e6f0583f84f9";
    List<Article> articleList1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        articleList1 =new ArrayList<>();
        final RecyclerView mainRecycler = findViewById(R.id.activity_main_rv);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mainRecycler.setLayoutManager(linearLayoutManager);
        final APIInterface apiService = ApiClient.getClient().create(APIInterface.class);
        Call<ResponseModel> call = apiService.getLatestNews("bitcoin", "2021-01-18","publishedAt",API_KEY);

            call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                FirebaseLanguageIdentification languageIdentifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

                articleList1.addAll(response.body().getArticles());

                for (int i = 0 ; i < articleList1.size() ;i++) {

                    int finalI = i;
                    languageIdentifier.identifyLanguage(String.valueOf(articleList1.get(i).getDescription()))
                            .addOnSuccessListener(
                                    new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(@Nullable String languageCode) {

                                            if (!languageCode.equals("und")) {
                                                FirebaseTranslatorOptions options =
                                                        new FirebaseTranslatorOptions.Builder()
                                                                .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(languageCode))
                                                                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                                                                .build();
                                                FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                                                        .requireWifi()
                                                        .build();
                                                Log.i("Identified", "Language: " + languageCode);
                                                final FirebaseTranslator allTranslate = FirebaseNaturalLanguage.getInstance().getTranslator(options);
                                                allTranslate.downloadModelIfNeeded(conditions)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void v) {
                                                                        // Create  translator:
                                                                        allTranslate.translate(String.valueOf(articleList1.get(finalI).getDescription()))
                                                                                .addOnSuccessListener(
                                                                                        new OnSuccessListener<String>() {
                                                                                            @Override
                                                                                            public void onSuccess(@NonNull String translatedText) {
                                                                                                Log.d("success trans",translatedText);
                                                                                                articleList1.get(finalI).setDescription(String.valueOf(translatedText));
                                                                                                SQLiteDatabaseHandler db;
                                                                                                db = new SQLiteDatabaseHandler(MainActivity.this);
                                                                                                db.addDescription(translatedText);
                                                                                            }
                                                                                        })
                                                                                .addOnFailureListener(
                                                                                        new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                // Error.
                                                                                                Log.d("Not Success trans","Failed");
                                                                                            }
                                                                                        });
                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Model couldn’t be downloaded or other internal error.
                                                                        // ...
                                                                    }
                                                                });

                                            }
                                            else {
                                                Log.i("Not Indentified", "Can't identify language.");
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Model couldn’t be loaded or other internal error.
                                            // ...
                                            Log.i("Fail", "Can't identify language.");
                                        }
                                    });

                }

              //  if (response.body().getStatus().equals("ok")) {
              //         List<Article> articleList = response.body().getArticles();
              //      if (articleList.size() > 0) {
                        final MainArticleAdapter mainArticleAdapter = new MainArticleAdapter(articleList1);
                        mainArticleAdapter.setOnRecyclerViewItemClickListener(MainActivity.this);
                        mainRecycler.setAdapter(mainArticleAdapter);
              //      }
             //   }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e("out", t.toString());
            }
        });
    }

    @Override
    public void onItemClick(int position, View view) {
        switch (view.getId()) {
            case R.id.article_adapter_ll_parent:
                Article article = (Article) view.getTag();
                if (!TextUtils.isEmpty(article.getUrl())) {
                    Log.e("clicked url", article.getUrl());
                    Log.d("check","check  ");
                    Intent webActivity = new Intent(this, WebActivity.class);
                    webActivity.putExtra("url", article.getUrl());
                    startActivity(webActivity);
                }
                break;
        }
    }
}