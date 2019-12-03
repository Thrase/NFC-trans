package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.nfc.NfcManager;
import android.nfc.NfcAdapter;

public class SendActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private NfcManager nfcManager;
    private NfcAdapter nfcAdapter;

    private Button StartSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        nfcManager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        StartSend=findViewById(R.id.btn_startsend);
        StartSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(SendActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
//                int status = wifiManager.getWifiState();
//                if (status==WifiManager.WIFI_STATE_ENABLED) {
//                    wifiManager.setWifiEnabled(false);
//                    Toast.makeText(SendActivity.this, "wifi off", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    wifiManager.setWifiEnabled(true);
//                    Toast.makeText(SendActivity.this, "wifi on", Toast.LENGTH_SHORT).show();
//                }
//
//                if (nfcAdapter != null && nfcAdapter.isEnabled()) {
//                    Toast.makeText(SendActivity.this, "nfc enabled", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(SendActivity.this, "nfc disabled", Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

}