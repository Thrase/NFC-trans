package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.net.Uri;


public class SendActivity extends AppCompatActivity {

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    private Button StartSend;
//    private TextView TextSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

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
}