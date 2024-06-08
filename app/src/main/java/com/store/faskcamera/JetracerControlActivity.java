package com.store.faskcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class JetracerControlActivity extends AppCompatActivity {
    private EditText inputText;
    private Button clickable;
    private ConnectionManager connectionManager;
    private Button buttonForward, buttonLeft, buttonRight, buttonForwardLeft, buttonForwardRight, buttonReverse, buttonReverseLeft, buttonReverseRight;
    private WebView webView;
    private static final String TAG = "4DBG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jetracer_control);
        buttonForward = findViewById(R.id.buttonForward);
        buttonLeft = findViewById(R.id.buttonLeft);
        buttonRight = findViewById(R.id.buttonRight);
        buttonForwardLeft = findViewById(R.id.buttonForwardLeft);
        buttonForwardRight = findViewById(R.id.buttonForwardRight);
        buttonReverse = findViewById(R.id.buttonReverse);
        buttonReverseRight = findViewById(R.id.buttonReverseRight);
        buttonReverseLeft = findViewById(R.id.buttonReverseLeft);
        connectionManager = new ConnectionManager();

        setupButton(buttonForward, "forward");
        setupButton(buttonLeft, "left");
        setupButton(buttonRight, "right");
        setupButton(buttonForwardLeft, "forward_left");
        setupButton(buttonForwardRight, "forward_right");
        setupButton(buttonReverse, "reverse");
        setupButton(buttonReverseRight,"reverse_right");
        setupButton(buttonReverseLeft,"reverse_left");

        // Kết nối đến server khi khởi tạo
        connectionManager.startConnection("192.168.1.14", Integer.parseInt(Constants.webViewPortControl));


        webView = findViewById(R.id.webViewControl);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setInitialScale(210);

        // Connect to the streaming server
        connectToStream();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButton(Button button, String startCommand) {

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Người dùng nhấn giữ nút
                    connectionManager.sendMessage(startCommand);
                    return true; // Sự kiện đã được xử lý
                case MotionEvent.ACTION_UP:
                    // Người dùng thả nút
                    connectionManager.sendMessage("stop");
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionManager.stopConnection(); // Đóng kết nối khi ứng dụng bị hủy
    }

    // Lớp ConnectionManager sẽ quản lý kết nối
    static class ConnectionManager {
        private Socket socket;
        private PrintWriter printWriter;

        public void startConnection(String ip, int port) {
            new Thread(() -> {
                try {
                    socket = new Socket(ip, port);
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void sendMessage(String message) {
            new Thread(() -> {
                if (printWriter != null) {
                    printWriter.println(message);
                }
            }).start();
        }

        public void stopConnection() {
            new Thread(() -> {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (printWriter != null) {
                        printWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
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

    private void connectToStream() {
        Log.i(TAG, "<strm> CONNECT");

        // Extract saved web address
        SharedPreferences sharedPref = getSharedPreferences("userConf", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("serverIp", "192.168.1.14");
        Log.i(TAG, "http://" + ip + ":" + Constants.flaskVideoFeed);

        // Setup URL
        webView.loadUrl("http://" + ip + ":" + Constants.flaskVideoFeed);
    }
}