package com.terryd.androidsimpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.citrix.mvpn.api.MicroVPNSDK;
import com.citrix.mvpn.exception.MvpnException;
import com.citrix.mvpn.exception.NetworkTunnelNotStartedException;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = getIntent().getStringExtra("URL");

        WebView webView = findViewById(R.id.idWebView);
        try {
            WebViewClient webviewClient = new WebViewClient();
            webView.setWebViewClient(webviewClient);
            webView = MicroVPNSDK.enableWebViewObjectForNetworkTunnel(this, webView, webviewClient);
            webView.loadUrl(url);
        } catch(NetworkTunnelNotStartedException nse) {
            Log.e(TAG, "TunnelNotStarted: " + nse.getMessage());
        } catch(MvpnException e) {
            Log.e(TAG, "Mvpn Error: " + e.getMessage());
        }
    }
}
