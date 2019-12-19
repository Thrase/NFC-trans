package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

public class NFCMain extends AppCompatActivity {
    private static final String TAG = "NFCMain";
    /**写入内容的文本框*/
    private EditText mEditText;
    /**显示NFC卡中的内容*/
    private TextView mTextView;

    /**NFC相关*/
    /**NFC适配器*/
    private NfcAdapter mNfcAdapter;
    /**延时意图*/
    private PendingIntent mPendingIntent;
    /**检测的卡类型*/
    private static String[][] sTechArr = null;
    /**意图过滤器*/
    private static IntentFilter[] sFilters = null;
    static {
        try {
            sTechArr = new String[][]{
                    {IsoDep.class.getName()}, {NfcV.class.getName()}, {NfcF.class.getName()},
                    {MifareUltralight.class.getName()}, {NfcA.class.getName()}
            };
            // 如果这里不设置过滤器，就必须要在AndroidManifest.xml中为MainActivity设置过滤器
            // 这里的ACTION在NfcAdapter中有四个选中，个人认为是在设置响应优先级
            /**
             *
             <intent-filter>
             <action android:name="android.nfc.action.NDEF_DISCOVERED" />
             <category android:name="android.intent.category.DEFAULT" />
             <data android:mimeType="text/plain" />
             </intent-filter>
             */
            sFilters = new IntentFilter[]{
                    new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*")
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcmain);

        initView();
        initNfc();
        // 判断当前手机是否支持NFC
        if (null == mNfcAdapter) {
            // 如果为null,则为不支持。
            Toast.makeText(this, "当前设备不支持NFC功能!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * 初始化NFC
     */
    private void initNfc() {
        // 1. 初始化Nfc适配器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // 2. 初始化延时意图
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 3. 允许后台唤醒--此处是指应用处于不可见的状态时，将卡片贴近手机时，唤醒当前App。
        // 如果sFilters为null,则为不过滤任何意图；如果sTechArr为null时，则为接收所有类型的卡片。
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, sFilters, sTechArr);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 4. 禁止后台唤醒
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 5. 重写OnIntent方法, 读取到的数据存储在intent中
        // 获取到意图中的Tag数据
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        TAG: Tech [android.nfc.tech.NfcV, android.nfc.tech.Ndef]
        Log.e(TAG, "onNewIntent: " + tag);
        // 此处判断输入框mEditText的内容是否为空，如果为空，则为读取数据；如果不为空，则为写入数据
        if (TextUtils.isEmpty(mEditText.getText().toString())) {
            // 数据为空，读取数据
            readData(tag);
        } else {
            // 数据不为空，写入数据
            writeData(tag);
        }

    }

    /**
     * 读取数据
     * @param tag Tag
     */
    private void readData(Tag tag) {
        // 从Tag中获取Ndef信息
        Ndef ndef = Ndef.get(tag);
        try {
            // 连接
            try {
                ndef.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            readData: type: android.ndef.unknown, size: 250, message: NdefMessage [NdefRecord tnf=1 type=54 payload=027A68E4BDA0E5A5BD]
            Log.e(TAG, "readData: type: " + ndef.getType() + ", size: " + ndef.getMaxSize() + ", message: " + ndef.getNdefMessage().toString());
            // 获取NdefMessage消息
            NdefMessage ndefMessage = null;
            try {
                ndefMessage = ndef.getNdefMessage();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            // 获取NdefRecord记录
            //ndefMessage.getRecords();
            // 遍历数组
            StringBuilder stringBuilder = new StringBuilder();
            // 文本
            String text;
            for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
                text = parseNdefRecord(ndefRecord);
                if (!TextUtils.isEmpty(text)) {
                    stringBuilder.append(text);
                }
            }
            // 设置文本
            mTextView.setText(stringBuilder.toString());
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析NdefRecord数据
     * @param ndefRecord NdefRecord记录
     */
    private String parseNdefRecord(NdefRecord ndefRecord) {
        // 判断是否为文本格式数据
        if (NdefRecord.TNF_WELL_KNOWN != ndefRecord.getTnf()) {
            return "";
        }
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return "";
        }

        try {
            // 获得字节流
            byte[] payload = ndefRecord.getPayload();
            // 获得编码格式
            String textEncoding = ((payload[0] & 0x80) == 0) ? "utf-8" : "utf-16";
            // 获得语言编码长度
            int languageCodeLength = payload[0] & 0x3f;
            // 语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // 获取文本
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength -1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 写数据
     * @param tag Tag
     */
    private void writeData(Tag tag) {
        // 创建一个NdefMessage消息
        NdefMessage ndefMessage = new NdefMessage(createTextRecord(mEditText.getText().toString()));
        // 写入Tag
        if (writeTag(ndefMessage, tag)) {
            mEditText.setText("");
            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 将NdefMessage消息写入Tag
     * @param ndefMessage Ndef消息
     * @param tag Tag
     */
    private boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        try {
            // 获取Ndef
            Ndef ndef = Ndef.get(tag);
            // 连接
            ndef.connect();
            // 写入数据
            ndef.writeNdefMessage(ndefMessage);
            return true;
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建文本信息
     * @param text 要写入的文本信息
     */
    private NdefRecord createTextRecord(String text) {
        // 设置语言
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        // 设置编码
        Charset utfEncoding = Charset.forName("UTF-8");
        // 要写入的二进制数据
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        // 将语言长度转化对应的字符
        char status = (char)(utfBit + langBytes.length);
        // 创建一个大小为 1(即语言长度转化对应的字符) + 语言字节长度 + 文本字节长度的字节数组
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        // 写入第一位数据
        data[0] = (byte) status;
        // 写入语言字节
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        // 写入文本数据
        System.arraycopy(textBytes, 0, data, langBytes.length + 1, textBytes.length);
        // 创建一个NdefRecord记录
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mEditText = (EditText) this.findViewById(R.id.editText);
        mTextView = (TextView) this.findViewById(R.id.textView);
    }
}

