package com.xmuhyxx.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

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

        View root = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        // 或者 在界面的根层加入 android:fitsSystemWindows=”true” 这个属性，这样就可以让内容界面从 状态栏 下方开始。
        ViewCompat.setFitsSystemWindows(root, true);
        setContentView(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0 以上 全透明
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
            window.setStatusBarColor(Color.TRANSPARENT);
            // 虚拟导航键
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4 以上 半透明
            Window window = getWindow();
            // 状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 虚拟导航键
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }




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