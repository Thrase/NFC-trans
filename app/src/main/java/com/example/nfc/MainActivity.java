package com.example.nfc;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private NfcManager nfcManager;
    private NfcAdapter nfcAdapter;

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    private Button SendButton;
    private Button RecvButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        nfcManager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        SendButton = findViewById(R.id.btn_send);
        SendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                turnOnHotspot();

                if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "nfc enabled", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "nfc disabled", Toast.LENGTH_SHORT).show();
                }
//                Toast.makeText(MainActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
                //跳转到send
                Intent intent=new Intent(MainActivity.this,SendActivity.class);
                startActivity(intent);

            }
        });

        RecvButton=findViewById(R.id.btn_recv);
        RecvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = wifiManager.getWifiState();
                if (status==WifiManager.WIFI_STATE_ENABLED) {
                    Toast.makeText(MainActivity.this, "wifi already on", Toast.LENGTH_SHORT).show();
                }
                else {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(MainActivity.this, "wifi on", Toast.LENGTH_SHORT).show();
                }

                if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this, "nfc enabled", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "nfc disabled", Toast.LENGTH_SHORT).show();
                }

//                Toast.makeText(MainActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
                //跳转到recv
                Intent intent=new Intent(MainActivity.this,RecvActivity.class);
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
                Toast.makeText(MainActivity.this, "wifi hotspot is on", Toast.LENGTH_SHORT).show();
                mReservation = reservation;
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Toast.makeText(MainActivity.this, "wifi hotspot onStopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Toast.makeText(MainActivity.this, "wifi hotspot failed", Toast.LENGTH_SHORT).show();
            }
        }, new Handler());
    }

    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }
}
