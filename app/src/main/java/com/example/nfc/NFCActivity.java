package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;

public class NFCActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //开启前台调度系统
        NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //关闭前台调度系统
        NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //当该Activity接收到NFC标签时，运行该方法
        //调用工具方法，读取NFC数据
        try {
            String str = NfcUtils.readNFCFromTag(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void initData() {
//        //nfc初始化设置
//        NfcUtils nfcUtils = new NfcUtils(this);
//    }
}
