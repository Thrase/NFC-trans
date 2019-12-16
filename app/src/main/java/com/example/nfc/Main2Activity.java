package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/* 继承 Activity类，继承Button.OnClickListener接口；*/
@SuppressLint({ "ShowToast", "HandlerLeak" })
public class Main2Activity extends Activity implements Button.OnClickListener {
    /*
     * i*命名的整型变量代表主界面底部三个按钮被按下的次数； startActivityTime为时间值，单位毫秒；
     */
    private int iAll = 0, iDefault = 0, iWhere = 0, startActivityTime = 2000;
    /* 定义各个界面的按钮； */
    private Button btnAll, btnDefault, btnWhere,
            btnOneToMainActivity, btnController;
    /* 定义主界面的三个TextView组件； */
    private TextView textView_all, textView_default, textView_where;
    /* 定义当前设备的NfcAdapter； */
    private NfcAdapter mNfcAdapter;
    /* */
    private IntentFilter[] intentFiltersArray;
    /* */
    private PendingIntent pendingIntent;
    /* */
    private String[][] techListsArray;
    /* 定义从NFC标签读取到的字符串； */
    private String nfcStr = "Ren-gh_ren.gh.1989_3_A1_192.168.1.253_30000";
    private String fenGeFu = "\\*";
    /*
     * 定义Socket通信界面上的两个文本框，一个由用户输入信息， 一个显示服务器发送的消息；
     */
    EditText inputText;
    TextView showText;
    /* 定义Socket通信界面上的一个按钮； */
    Button btnSendText, btnToMainActivity;
    /* 定义Socket通信用到的Handler对象 */
    Handler handler;
    /* 定义与服务器通信的子线程； */
//    ClientThread clientThread;
    int isWifiEnableStatue = 0, isWifiConnectedStatue = 0, nfcStatue;
    public static int socketStatue = 0;

    /**
     * 默认配置，测试时使用！！！
     */
    String mSSID = "RhkySocket", mPASSWORD = "flamingoeda";
    int mTYPE = 3;
    String deviceName = "Test-PC", ipAdress = "192.168.1.109";
    int post = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        /*
         * 在显示开始界面的情况下后台启动主界面， 直到过了startActivityTime时间后显示主界面；
         */
        Handler x = new Handler();
        x.postDelayed(new defaultStartApp(), startActivityTime);

        /* NFC相关; */
        nfcManager();

        /* 记录使用前wifi的状态； */
        if (isWifiEnabled()) {
            isWifiEnableStatue = -1;
        }
        if (isWifiConnect()) {
            isWifiConnectedStatue = -1;
        }

        nfcStatue = 0;
    }

    /* 手动启动本程序调用，后台加载主界面; */
    class defaultStartApp implements Runnable {
        public void run() {
            setContentView(R.layout.activity_main2);
            /* 设置layout_main.xml布局中的按钮和TextView控件; */
            setMainActivityButtonAndTextView();

            /*
             * 无NFC手机测试wifi自动连接使用，需要更改初始值为所用路由器无线信息；
             */
            // AutoWifi(mSSID, mPASSWORD, mTYPE);
            // connectedSocketServer(deviceName, ipAdress, post);
        }
    }

    /*
     * nfc启动程序时调用，根据读取到的信息，后台运行连接wifi网络操作。 不后台运行的话会导致开始界面加载失败;
     */
    class nfcStartApp implements Runnable {
        public void run() {
            sendConfigToAutoWifi(nfcStr);
            /*
             * 将无线网络名称str[0]、无线网络密码str[1]、 无线加密类型str[2]，传递给AutoWifi方法；
             */
            AutoWifi(mSSID, mPASSWORD, mTYPE);
//            connectedSocketServer(deviceName, ipAdress, post);
        }
    }

    /* NFC标签读取; */
    protected void nfcManager() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "本设备不支持NFC功能", Toast.LENGTH_LONG).show();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] { ndef, };
        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
    }

    @Override
    public void onResume() {
        super.onResume();
        /* 启用nfc前台调度，保证程序开启状态下优先处理nfc包含的消息； */
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    intentFiltersArray, techListsArray);
        }
        /* 能够将intent从外部传递到程序； */
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())
                && (nfcStatue == 0)) {
            // 调用ndef消息的处理方法；
            processIntent(getIntent());
        }
    }

    /* 有新的intent时调用； */
    @Override
    public void onNewIntent(Intent intent) {
        /* 调用ndef消息的处理方法； */
        processIntent(intent);
    }

    /* 定义ndef消息的处理方法； */
    private void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        /* 提取NdefMessage消息的正文赋给字符串nfcStr； */
        nfcStr = new String(msg.getRecords()[0].getPayload());
        /* 后台调用nfcStartApp()方法，分析读到的消息并开始wifi连接; */
        Handler x = new Handler();
        x.postDelayed(new nfcStartApp(), startActivityTime);
    }

    /* 将nfc读到的消息分解后传递给AutoWifi方法进行自动连接； */
    public void sendConfigToAutoWifi(String nfcStr) {
        /*
         * nfcStr = "Ren-gh_ren.gh.1989_3_A1_192.168.1.253_30000";
         * 代表含义依次为：无线网络名称, 无线网络密码, 无线加密方式，设备名称，设备IP，设备端口号；
         * 将nfcStr字符串分解为多个字符串存到str字符串数组里；
         */
        String str[];
        str = nfcStr.split(fenGeFu);
        mSSID = str[0];
        mPASSWORD = str[1];
        mTYPE = Integer.valueOf(str[2]);
        deviceName = str[3];
        ipAdress = str[4];
        post = Integer.valueOf(str[5]);
        /* 清空nfcStr字符串 */
        nfcStr = "Ren-gh_ren.gh.1989_3_A1_192.168.1.253_30000";
        ++nfcStatue;
    }

    /* 根据传递过来的三个无线网络参数连接wifi网络； */
    private void AutoWifi(String ssid, String passwd, Integer type) {
        /*
         * 创建对象，打开wifi功能，等到wifi启动完成后将传递来的wifi网络添加进Network，
         * 然后等待连接诶成功后，传递设备名称，设备IP，设备端口号给connectedSocketServer方法，
         * 用来连接远程Socket服务器；Integer.valueOf(str[5])是将字符串转换为整型；
         */
        /**
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
            myWifi.addNetwork(myWifi.CreateWifiInfo(ssid, passwd, type));
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
    }

    /* 检查wifi是否可用；是则返回true； */
    public boolean isWifiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Toast.makeText(this, "检测wifi可用完毕", Toast.LENGTH_LONG).show();
        return mWifi.isAvailable();
    }

    /* 检查wifi是否连接成功；成功则返回true； */
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        Toast.makeText(this, "检查连接wifi成功完毕", Toast.LENGTH_LONG).show();
        if (mWifi.isConnected() == true)
            Toast.makeText(this, "wifi连接：true", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "wifi连接：false", Toast.LENGTH_LONG).show();
        return mWifi.isConnected();
    }

//    /* 根据第二和第三个参数连接远程设备的Socket服务器； */
//    public void connectedSocketServer(String deviceName, String ipAdress,
//                                      int post) {
//        /* 加载socket通信的界面； */
//        setContentView(R.layout.socket);
//
//        inputText = (EditText) findViewById(R.id.inputText);
//        btnSendText = (Button) findViewById(R.id.btnSendText);
//        showText = (TextView) findViewById(R.id.showText);
//        btnToMainActivity = (Button) findViewById(R.id.btnSocketToMainActivity);
//        btnToMainActivity.setOnClickListener(this);
//
//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                // 如果消息来自于子线程
//                if (msg.what == 0x123) {
//                    String recvString = msg.obj.toString();
//                    if (!recvString.equals("0x123 close")) {
//                        // 将读取的内容最佳显示在文本框中
//                        showText.append(recvString + "\n");
//                    }
//                }
//            }
//        };
//
//        clientThread = new ClientThread(handler, ipAdress, post);
//        // 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
//        new Thread(clientThread).start();
//
//        btnSendText.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /*
//                 * 当用户按下发送按钮后，判断用户输入的字符串不是空或 无意义的空格、TAB制表符的时候才进行处理；
//                 * String.trim().isEmpty()去掉前导空白和后导空白，再判断
//                 * 是否为空;非空时将用户输入的数据封装成Message发送给子 线程的Handler;
//                 */
//                try {
//                    String sendString = inputText.getText().toString();
//                    if (!sendString.trim().isEmpty()) {
//                        Message msg = new Message();
//                        msg.what = 0x345;
//                        msg.obj = sendString;
//                        clientThread.recvHandler.sendMessage(msg);
//                        // 清空input文本框
//                        inputText.setText("");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            /* 适时关闭前台调用，避免资源被占用； */
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /* 设置TextView和按钮，为按钮监听事件， */
    private void setMainActivityButtonAndTextView() {
        /* 所有展览点按钮监听; */
        btnAll = (Button) findViewById(R.id.btnAll);
        btnAll.setOnClickListener(this);
        /* 推荐展览点 按钮监听; */
        btnDefault = (Button) findViewById(R.id.btnDefault);
        btnDefault.setOnClickListener(this);
        /* 您的位置 按钮监听; */
        btnWhere = (Button) findViewById(R.id.btnWhere);
        btnWhere.setOnClickListener(this);
        /* 下一页界面按钮监听; */

        textView_all = (TextView) findViewById(R.id.textView_all);
        textView_default = (TextView) findViewById(R.id.textView_default);
        textView_where = (TextView) findViewById(R.id.textView_where);
    }

    /* 关联one.xml界面布局中的“回到首页”按钮，设置监听事件; */
    private void setOneActivityButtonAndTextView() {
        btnOneToMainActivity = (Button) findViewById(R.id.btnOneToMainActivity);
        btnOneToMainActivity.setOnClickListener(this);

        btnController = (Button) findViewById(R.id.btnController);
        btnController.setOnClickListener(this);
    }

    /* 按钮点击事件处理方法; */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAll:
                textView_all.setText("所有展品" + iAll++);
                break;
            case R.id.btnDefault:
                textView_default.setText("推荐展品" + iDefault++);
                break;
            case R.id.btnWhere:
                textView_where.setText("我的位置" + iWhere++);
                break;

            case R.id.btnOneToMainActivity:
                setContentView(R.layout.activity_main2);
                /* 设置layout_main.xml布局中的按钮和TextView控件; */
                setMainActivityButtonAndTextView();
                break;

            case R.id.btnController:
                break;

            default:
                break;
        }
    }

    /* 返回键的监听事件； */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            /*
             * WifiConfig myWifi; myWifi = new WifiConfig(this);
             * if(isWifiEnableStatue == 1) { myWifi.removeWifiNetwork();
             * myWifi.closeWifi(); } // 如果wifi已连接，断开wifi连接并退出
             * if(isWifiConnectedStatue == 1) { myWifi.removeWifiNetwork();
             * myWifi.disconnectWifi(); }
             */
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}