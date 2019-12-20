package com.example.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Receiving_Main extends Activity {

//    private Button StartSend;
    private Button TestSSID;
    private Button TestNFC;
//    private TextView TextSSID;

    String SSID;
    String SSIDKey;
    String URI_Path;
    boolean hotspotflag =false;

    private ServerSocket server;

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receving_main);

        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {

                int port = 9999;
                while (port > 9000) {
                    try {
                        server = new ServerSocket(port);
                        break;
                    } catch (Exception e) {
                        port--;
                    }
                }
                if (server != null) {
                    while (true) {
                        ReceiveFile();
                    }
                }
            }
        });
        listener.start();

        TestSSID = findViewById(R.id.btn_testSSID);
        TestSSID.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
//              Intent intent = getIntent();
//              SSID = intent.getStringExtra("SSID");
//              SSIDKey = intent.getStringExtra("SSIDKey");
                if (hotspotflag == false) {
                    turnOnHotspot();
                    hotspotflag=true;
                }
                else {
                    Toast.makeText(Receiving_Main.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                }

            }
        });

//        TextSSID = findViewById(R.id.text_ssid);

//        StartSend = findViewById(R.id.btn_startsend);
//        StartSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//无类型限制
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 1);
//            }
//        });

        TestNFC = findViewById(R.id.btn_testNFC);
        TestNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Receiving_Main.this, Receving_Get_NFC.class);
                intent.putExtra("SSID", SSID);
                intent.putExtra("SSIDKey", SSIDKey);
                intent.putExtra("URI_Path", URI_Path);
                startActivity(intent);
            }
        });
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == 1) {
//                Uri uri = data.getData();
//                try {
//                    URI_Path = uri.getPath();
//                    Toast.makeText(this, "文件路径：" + URI_Path, Toast.LENGTH_SHORT).show();
//                }
//                catch (NullPointerException e){
//                    Toast.makeText(this, "error: "+e, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        }
//    }

    @RequiresApi(26)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {

                super.onStarted(reservation);
                SSID = reservation.getWifiConfiguration().SSID;
                SSIDKey = reservation.getWifiConfiguration().preSharedKey;
                Toast.makeText(Receiving_Main.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                mReservation = reservation;
            }
        }, new Handler());

    }

    // 文件接收方法
    public String ReceiveFile() {
        try {
            // 接收文件名
            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();

            // 接收文件数据
            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            File dir = new File("/sdcard/NFCAssitant"); // 创建文件的存储路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String savePath = "/sdcard/NFCAssitant/" + fileName; // 定义完整的存储路径
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
            }
            file.close();
            dataStream.close();
            data.close();
            return fileName + " 接收完成";
        } catch (Exception e) {
            return "接收错误:\n" + e.getMessage();
        }
    }
}