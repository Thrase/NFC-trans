package com.example.nfc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private TextView textView;

    private Handler handler=null;

    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receving_get_nfc);

        handler=new Handler();

        textView = findViewById(R.id.tvmsg);

        mContext = this;
        checkNFCFunction();

        final Intent intent = getIntent();
        SSID = intent.getStringExtra("SSID");
        SSIDKey = intent.getStringExtra("SSIDKey");
        Toast.makeText(this, SSID +","+ SSIDKey , Toast.LENGTH_LONG).show();

        //  注册事件  并触发自动申请权限
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        new Thread() {
            @Override
            public void run() {

                int port = 20001;
                try {
                    server = new ServerSocket(port);
//                    while (server != null) {
                        receiveFile();

//                        try {
//                            Socket socket = server.accept();
//
//                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                            content = null;
//                            while ((content=bufferedReader.readLine() )!= null) {
//
//                                handler.post(runnableUI);
//                            }
//
//                            //关闭连接
//                            bufferedReader.close();
//                            socket.close();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }


//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    Runnable runnableUI = new Runnable() {
        @Override
        public void run() {
            textView.setText(content);
        }
    };


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


    public synchronized void receiveFile() {
        try {
            ServerSocket ss = new ServerSocket(20001);
            while (true) {
                Socket socket = ss.accept();
                InputStream in = socket.getInputStream();
                int content;
                //装载文件名的数组
                byte[] c = new byte[1024];
                //解析流中的文件名,也就是开头的流
                for (int i = 0; (content = in.read()) != -1; i++) {
                    //表示文件名已经读取完毕
                    if (content == '#') {
                        System.out.println("File name get.");
                        break;
                    }
                    c[i] = (byte) content;
                }
                //将byte[]转化为字符,也就是我们需要的文件名
                String FileName = new String(c, "utf-8").trim();

                System.out.println(FileName);

                //创建一个文件,指定保存路径和刚才传输过来的文件名
                OutputStream saveFile = new FileOutputStream(
                        new File(Environment.getExternalStorageDirectory().toString(), FileName));
                byte[] buf = new byte[1024];
                int len;
                //判断是否读到文件末尾
                while ((len = in.read(buf)) != -1) {
                    saveFile.write(buf, 0, len);
                }
                saveFile.flush();
                saveFile.close();
                //告诉发送端我已经接收完毕
                OutputStream outputStream = socket.getOutputStream();
                System.out.println("Get OK!");
                outputStream.write("文件接收成功".getBytes());
                outputStream.flush();
                outputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
