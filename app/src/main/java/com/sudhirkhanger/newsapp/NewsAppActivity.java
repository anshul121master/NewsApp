package com.sudhirkhanger.newsapp;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sudhirkhanger.newsapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class NewsAppActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final String NEWS_REQUEST_URL =
            "http://content.guardianapis.com/search?q=debate&tag=politics/politics&from-date=2014-01-01&api-key=test&page-size=20&show-tags=contributor";
    private static final String LOG_TAG = NewsAppActivity.class.getSimpleName();
    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter mNewsAdapters;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.emptyView.setText(getResources().getText(R.string.no_stories));
        binding.newsListView.setEmptyView(binding.emptyView);

        mNewsAdapters = new NewsAdapter(this, new ArrayList<News>());

        binding.newsListView.setAdapter(mNewsAdapters);

        binding.newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = mNewsAdapters.getItem(position);

                Uri newsUri = Uri.parse(currentNews.getWebUrl());
                Log.d(LOG_TAG, newsUri.toString());

                if (currentNews.getWebUrl() == null || TextUtils.isEmpty(currentNews.getWebUrl())) {
                    Toast.makeText(NewsAppActivity.this, NewsAppActivity.this.getResources().getString(R.string.no_link_found), Toast.LENGTH_SHORT).show();
                } else {
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                    startActivity(websiteIntent);
                }
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getSupportLoaderManager();

            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            binding.emptyView.setText(R.string.no_internet);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        return new NewsLoader(this, NEWS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        binding.loadingIndicator.setVisibility(View.GONE);

        binding.emptyView.setText(R.string.no_stories);

        mNewsAdapters.clear();

        if (data != null && !data.isEmpty()) {
            mNewsAdapters.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mNewsAdapters.clear();
    }
}
