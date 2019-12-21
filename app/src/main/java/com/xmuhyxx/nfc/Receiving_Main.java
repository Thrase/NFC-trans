package com.xmuhyxx.nfc;

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

public class Receiving_Main extends Activity {


    private Button TestSSID;
    private Button TestNFC;

    String SSID;
    String SSIDKey;
    boolean hotspotflag =false;
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receving_main);
        TestSSID = findViewById(R.id.btn_testSSID);
        TestSSID.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (hotspotflag == false) {
                    turnOnHotspot();
                    hotspotflag=true;
                }
                else {
                    Toast.makeText(Receiving_Main.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                }
            }
        });
        TestNFC = findViewById(R.id.btn_testNFC);
        TestNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Receiving_Main.this, Receving_Get_NFC.class);
                intent.putExtra("SSID", SSID);
                intent.putExtra("SSIDKey", SSIDKey);
                startActivity(intent);
            }
        });
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
                Toast.makeText(Receiving_Main.this, "wifi hotspot SSID: "+SSID + " password: " + SSIDKey, Toast.LENGTH_SHORT).show();
                mReservation = reservation;
            }
        }, new Handler());

    }


}