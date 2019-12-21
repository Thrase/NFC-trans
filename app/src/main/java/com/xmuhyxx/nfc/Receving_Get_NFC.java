package com.xmuhyxx.nfc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import java.io.File;
import java.io.FileOutputStream;
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
    private String SSID;
    private String SSIDKey;
    private Button btnSend;
    private TextView textView;

    private Handler handler=null;

    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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

                receiveFile();

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

//        try {
//            server = new ServerSocket(20001);
//            while (server != null) {
//                try {
//                    Socket socket = server.accept();
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                    content = null;
//                    while ((content=bufferedReader.readLine() )!= null) {
//                        handler.post(runnableUI);
//                    }
//
//                    //关闭连接
//                    bufferedReader.close();
//                    socket.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            ServerSocket ss = new ServerSocket(20001);
            while ( ss!=null ) {
                Socket socket = ss.accept();
                InputStream in = socket.getInputStream();
                int CC;
                //装载文件名的数组
                byte[] c = new byte[1024];
                //解析流中的文件名,也就是开头的流
                for (int i = 0; (CC = in.read()) != -1; i++) {
                    if (CC == '#') {
                        System.out.println("File name get.");
                        break;
                    }
                    c[i] = (byte) CC;
                }
                //将byte[]转化为字符,也就是我们需要的文件名
                String FileName = new String(c, "utf-8").trim();

                content = FileName;
                handler.post(runnableUI);

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

                OutputStream outputStream = socket.getOutputStream();
                System.out.println("Get OK!");
                outputStream.write("文件发送成功".getBytes());
                outputStream.flush();
                outputStream.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
