package com.example.nfc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private String pubSSID, pubSSIDKey;

    private WifiManager wifiManager;
    private NfcManager nfcManager;
    private NfcAdapter nfcAdapter;

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    private Button SendButton;
    private Button RecvButton;
//    private Button TestButton;

    String URI_Path;

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

                if (nfcAdapter != null && nfcAdapter.isEnabled()) {
//                    Toast.makeText(MainActivity.this, "nfc enabled", Toast.LENGTH_SHORT).show();
//                    String SSSSK = turnOnHotspot();
//                    String S[] = SSSSK.split(",");
//                    pubSSID = S[0];
//                    pubSSIDKey = S[1];

                    //跳转到send
                    Intent intent=new Intent(MainActivity.this, Receiving_Main.class);
//                    intent.putExtra("SSID", pubSSID);
//                    intent.putExtra("SSIDKey", pubSSIDKey);
                    startActivity(intent);
                }
                else {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }
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
                    Intent intent=new Intent(MainActivity.this, Sending_Get_NFC.class);
                    startActivity(intent);
                }
                else {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }

//                Toast.makeText(MainActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
                //跳转到recv
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                try {
                    URI_Path = uri.getPath();
                    Toast.makeText(this, "文件路径：" + URI_Path, Toast.LENGTH_SHORT).show();
                }
                catch (NullPointerException e){
                    Toast.makeText(this, "error: "+e, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

}
