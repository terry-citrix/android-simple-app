package com.terryd.androidsimpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = ((EditText)findViewById(R.id.idURL)).getText().toString();
    }

    public void onClickLaunchButton(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
}
