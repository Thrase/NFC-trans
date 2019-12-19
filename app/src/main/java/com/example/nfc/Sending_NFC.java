package com.example.nfc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 触屏发送信息
 */
//  NfcAdapter.CreateNdefMessageCallBack 接口 应该是在两个Nfc设备接触的时候 被调用的..
public class Sending_NFC extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    //  NfcAdapter
    private NfcAdapter mNfcAdapter;
    //  EditText

    private TextView tips;

    private Context mContext;

    private String SSID;
    private String SSIDKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        mContext = this;
        checkNFCFunction();

        tips = findViewById(R.id.tips);

        Intent intent = getIntent();
        SSID = intent.getStringExtra("SSID");
        SSIDKey = intent.getStringExtra("SSIDKey");
        Toast.makeText(this, SSID + SSIDKey , Toast.LENGTH_LONG).show();

        //  注册事件  并触发自动申请权限
        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    //  在Nfc设备相互接触的时候 调用这个方法  进行消息的发送
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String SSIDSK = SSID + "," + SSIDKey;

        NdefMessage message = BobNdefMessage.getNdefMsg_from_RTD_TEXT(SSIDSK.equals("") ? "empty message" : SSIDSK, false, false);
        return message;
    }


    private void checkNFCFunction() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //  机器不支持Nfc功能
        if (mNfcAdapter == null) {
            return;
        } else {
            //  检查机器NFC是否开启
            if (!mNfcAdapter.isEnabled()) {
                //  机器Nfc未开启 提示用户开启 这里采用对话框的方式<PS:这个开启了  可以进行对NFC的信息读写>
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("警告").setMessage("本机NFC功能未开启,是否开启(不开启将无法继续)").setNegativeButton("开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(setNfc);
                    }
                }).setPositiveButton("不开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).create().show();
                return;
            } else {
                //  NFC 已开启  检查NFC_Beam是否开启  只有这个开启了  才能进行p2p的传输
                if (!mNfcAdapter.isNdefPushEnabled()) {
                    // NFC_Beam未开启  点击开启
                    new AlertDialog.Builder(mContext).setTitle("警告!").setMessage("NFC_Beam功能未开启,是否开启(不开启将无法继续)").setNegativeButton("开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent setNfc = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
                            startActivity(setNfc);
                        }
                    }).setPositiveButton("不开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setCancelable(false).create().show();
                    return;
                }
            }
        }
    }

}
