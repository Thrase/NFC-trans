package com.example.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Sending_Get_NFC extends AppCompatActivity {
    private static final String TAG = "NFC";
    //  NfcAdapter
    private NfcAdapter mNfcAdapter;

    private TextView textView;
    private TextView textView_IP;
    private TextView textView_SE;
    private Button btnSend;
    private Button btnFile;
    private EditText editText;

    private String SSID;
    private String SSIDKey;
    private String URI_Path;
    private String serverAddress;
    private String IPAd_String;

    int isWifiEnableStatue = 0, isWifiConnectedStatue = 0, nfcStatue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_get_nfc);

        checkNFCFunction();
        textView = (TextView) findViewById(R.id.tv);
        textView_IP = findViewById(R.id.tv2);
        textView_SE = findViewById(R.id.tv3);
        btnSend = findViewById(R.id.btnSend);
        btnFile = findViewById(R.id.btnFile);
        editText = findViewById(R.id.et);

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread sender = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
//                            Toast.makeText(Sending_Get_NFC.this, serverAddress, Toast.LENGTH_SHORT).show();
                            Socket socket = new Socket(serverAddress, 20001);
                            socket.setSoTimeout(10000);
//
                            String str = editText.getText().toString();
//                            Toast.makeText(Sending_Get_NFC.this, str, Toast.LENGTH_SHORT).show();
                            //给服务端发送消息
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                            printWriter.write(str + "\r\n");
                            printWriter.flush();

                            //关闭资源
                            printWriter.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
                sender.start();
            }
        });

    }
    //  * * * * * * * * * * * * * * * * * * * * * * * NFC start * * * * * * * * * * * * * * * * * * * * * * * ↓

    //  回调,当NFC消息过来的时候自动调用.
    @Override
    protected void onNewIntent(Intent intent) {
        //  接收到消息的第一步
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  需要从Intent中读出信息
        //  消息判别
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            resolveIntent(getIntent());
        }
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

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            NdefMessage[] messages = null;
            Parcelable[] rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsg != null) {
                messages = new NdefMessage[rawMsg.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = (NdefMessage) rawMsg[i];
                }
            } else {
                //  未知Action
                byte[] empty = new byte[]{};
                NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage ndefMessage = new NdefMessage(ndefRecord);
                messages = new NdefMessage[]{ndefMessage};
            }
            //  将Message中的Record解析出来
            progressNdefMessage(messages);
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

        } else {
            return;
        }
    }


    private void progressNdefMessage(NdefMessage[] messages) {
        if (messages == null || messages.length == 0) {
            return;
        }
        for (int i = 0; i < messages.length; i++) {
            NdefRecord records[] = messages[i].getRecords();
            for (NdefRecord record : records) {
                if (isTextUri(record)) {
                    parseTextUri(record);
                }
            }
        }
    }

    private boolean isTextUri(NdefRecord record) {
        if (NdefRecord.TNF_WELL_KNOWN == record.getTnf()) {
            if (Arrays.equals(NdefRecord.RTD_TEXT, record.getType())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void parseTextUri(NdefRecord record) {

        //  读出所有的PayLoad
        String payLoadStr = "";
        byte[] payloads = record.getPayload();
        byte statusByte = payloads[0];
        //  得到编码方式
        String textEncoding = ((statusByte & 0200) == 0) ? "UTF-8" : "UTF-16";
        //  获取语言码的长度
        int languageCodeLength = statusByte & 0077;
        //  真正的解析
        payLoadStr = new String(payloads, languageCodeLength + 1, payloads.length - languageCodeLength - 1, Charset.forName(textEncoding));
        //  解析完成- -NFC阶段结束
        textView.setText(payLoadStr);
        String S[] = payLoadStr.split(",");
        SSID = S[0];
        SSIDKey = S[1];

        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        int SEAd = dhcpinfo.serverAddress;
        serverAddress = (SEAd & 0xFF) + "." + ((SEAd >> 8) & 0xFF) + "." + ((SEAd >> 16) & 0xFF) + "." + (SEAd >> 24 & 0xFF);
        textView_SE.setText("SE Address: " + serverAddress);

        int IPAd = AutoWifi(SSID, SSIDKey);
        IPAd_String = (IPAd & 0xFF) + "." + ((IPAd >> 8) & 0xFF) + "." + ((IPAd >> 16) & 0xFF) + "." + (IPAd >> 24 & 0xFF);
        textView_IP.setText("IP Address: " + IPAd_String);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    new AlertDialog.Builder(this).setTitle("警告!").setMessage("NFC_Beam功能未开启,是否开启(不开启将无法继续)").setNegativeButton("开启", new DialogInterface.OnClickListener() {
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

    /* 根据传递过来的三个无线网络参数连接wifi网络； */
    private int AutoWifi(String ssid, String passwd) {
        /*
         * 创建对象，打开wifi功能，等到wifi启动完成后将传递来的wifi网络添加进Network，
         * 然后等待连接成功后，传递设备名称，设备IP，设备端口号给connectedSocketServer方法，
         * 用来连接远程Socket服务器；Integer.valueOf(str[5])是将字符串转换为整型；
         */
        /*
         * 定义AutoWifiConfig对象，通过该对象对wifi进行操作； WifiConfig myWifi = new
         * WifiConfig(this); 不能用作全局，不然会出现刷nfc连接wifi，连接到socket，再刷nfc时程序卡死的情况；
         */
        WifiConfig myWifi = new WifiConfig(this);
        Boolean b;
        if (!isWifiEnabled()) {
            isWifiEnableStatue = 1;
            myWifi.openWifi();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                b = isWifiEnabled();
            } while (!b);
        }
        if (!isWifiConnect() || !myWifi.getSSID().equals(ssid)) {
            myWifi.addNetwork(myWifi.CreateWifiInfo(ssid, passwd, 3));
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                b = isWifiConnect();
            } while (!b);
            isWifiConnectedStatue = 1;
        }
        return myWifi.getIPAddress();
    }

    /* 检查wifi是否可用；是则返回true； */
    public boolean isWifiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        Toast.makeText(this, "检测wifi可用完毕", Toast.LENGTH_LONG).show();
        return mWifi.isAvailable();
    }

    /* 检查wifi是否连接成功；成功则返回true； */
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        Toast.makeText(this, "检查连接wifi成功完毕", Toast.LENGTH_LONG).show();
        return mWifi.isConnected();
    }

    public String SendFile(String fileName, String path, String ipAddress, int port) {
        try {
            Socket name = new Socket(ipAddress, port);
            OutputStream outputName = name.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            name.close();

            Socket data = new Socket(ipAddress, port);
            OutputStream outputData = data.getOutputStream();
            FileInputStream fileInput = new FileInputStream(path);
            int size = -1;
            byte[] buffer = new byte[1024];
            while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
                outputData.write(buffer, 0, size);
            }
            outputData.close();
            fileInput.close();
            data.close();
            return fileName + " 发送完成";
        } catch (Exception e) {
            return "发送错误:\n" + e.getMessage();
        }
    }


}
