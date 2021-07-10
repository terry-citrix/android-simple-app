package com.terry.androidsimpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = getIntent().getStringExtra("URL");

        WebView webView = findViewById(R.id.idWebView);
        WebViewClient webViewClient = new WebViewClient();
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);
    }
}
