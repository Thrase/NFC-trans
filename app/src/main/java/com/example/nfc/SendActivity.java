package com.example.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


public class SendActivity extends Activity {

    private Button StartSend;
    private Button TestSSID;
    private Button TestNFC;
//    private TextView TextSSID;

    String SSID;
    String SSIDKey;
    boolean hotspotflag =false;

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

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
                    Toast.makeText(SendActivity.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                }

            }
        });

//        TextSSID = findViewById(R.id.text_ssid);

        StartSend = findViewById(R.id.btn_startsend);
        StartSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        TestNFC = findViewById(R.id.btn_testNFC);
        TestNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendActivity.this, Sending_NFC.class);
                intent.putExtra("SSID", SSID);
                intent.putExtra("SSIDKey", SSIDKey);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                try {
                    Toast.makeText(this, "文件路径：" + uri.getPath(), Toast.LENGTH_SHORT).show();
                }
                catch (NullPointerException e){
                    Toast.makeText(this, "error: "+e, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @RequiresApi(26)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {

                super.onStarted(reservation);
                SSID = reservation.getWifiConfiguration().SSID;
                SSIDKey = reservation.getWifiConfiguration().preSharedKey;
                Toast.makeText(SendActivity.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                mReservation = reservation;
            }
        }, new Handler());

    }
}