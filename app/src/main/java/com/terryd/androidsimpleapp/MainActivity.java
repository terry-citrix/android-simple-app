package com.terryd.androidsimpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.citrix.mvpn.api.MicroVPNSDK;
import com.citrix.mvpn.api.MvpnDefaultHandler;

public class MainActivity extends AppCompatActivity implements TunnelHandler.Callback {
    private static final String TAG = "MainActivity";

    private String url;

    private MvpnDefaultHandler mvpnHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = ((EditText)findViewById(R.id.idURL)).getText().toString();

        if (mvpnHandler == null) {
            mvpnHandler = new TunnelHandler(this);
        }
        Log.i(TAG, "Before calling startTunnel()");
        try {
            MicroVPNSDK.startTunnel(this, new Messenger(mvpnHandler));
        } catch (Exception e) {
            Log.e(TAG, "Failed to start tunnel: " + e.getMessage());
        }
    }

    public void onClickLaunchButton(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }

    @Override
    public void onTunnelStarted() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Started tunnel!", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onError(boolean isSessionExpired) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error with tunnel!", Toast.LENGTH_LONG).show();
        });
    }
}
