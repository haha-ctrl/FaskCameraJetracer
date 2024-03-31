package com.store.faskcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class LiveCameraJetracer extends AppCompatActivity {

    private static final String TAG = "4DBG";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera_jetracer);


        webView = findViewById(R.id.streamWebView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setInitialScale(110);

        // Connect to the streaming server
        connectToStream();
    }




    private void connectToStream() {
        Log.i(TAG, "<strm> CONNECT");

        // Extract saved web address
        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.12");
        Log.i(TAG, "http://" + ip + ":5000");

        // Setup URL
        webView.loadUrl("http://" + ip + ":5000");
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnectFromStream();
    }

    private void disconnectFromStream() {
        Log.i(TAG, "<strm> DISCONNECT");
        webView.loadUrl("");
    }
}