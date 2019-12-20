package com.example.nfc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 触屏发送信息
 */
//  NfcAdapter.CreateNdefMessageCallBack 接口 应该是在两个Nfc设备接触的时候 被调用的..
public class Receving_Get_NFC extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    //  NfcAdapter
    private NfcAdapter mNfcAdapter;
    //  EditText

    private Context mContext;

    private ServerSocket server;

    private String SSID;
    private String SSIDKey;



    private Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receving_get_nfc);

        mContext = this;
        checkNFCFunction();


        final Intent intent = getIntent();
        SSID = intent.getStringExtra("SSID");
        SSIDKey = intent.getStringExtra("SSIDKey");
        Toast.makeText(this, SSID +","+ SSIDKey , Toast.LENGTH_LONG).show();



        //  注册事件  并触发自动申请权限
        mNfcAdapter.setNdefPushMessageCallback(this, this);


        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {

                TextView textView;

                int port = 20001;
                try {
                    server = new ServerSocket(port);
                    while (server != null) {
                        try {
                            Socket socket = server.accept();

                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            String content = null;
                            while ((content=bufferedReader.readLine() )!= null) {
                                textView = findViewById(R.id.tvmsg);
                                textView.setText("1");
//                                Toast.makeText(Receving_Get_NFC.this, "接收到消息：" +content, Toast.LENGTH_LONG).show();
                            }

                            //关闭连接
                            bufferedReader.close();
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Toast.makeText(Receving_Get_NFC.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        listener.start();

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

    // 文件接收方法
    public String ReceiveFile() {
        try {
            // 接收文件名
            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();

            // 接收文件数据
            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            File dir = new File("/sdcard/NFCAssitant"); // 创建文件的存储路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String savePath = "/sdcard/NFCAssitant/" + fileName; // 定义完整的存储路径
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
            }
            file.close();
            dataStream.close();
            data.close();
            return fileName + " 接收完成";
        } catch (Exception e) {
            return "接收错误:\n" + e.getMessage();
        }
    }

}
