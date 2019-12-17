package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class RecvActivity extends AppCompatActivity {

    private NfcAdapter mBLNfcAdapter;
    private PendingIntent mPendingIntent;

    private Button TestButton;

    @Override
    protected void onStart() {
        super.onStart();
        mBLNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent=PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBLNfcAdapter!=null) {
            mBLNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBLNfcAdapter!=null)
            mBLNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recv);
        TestButton = findViewById(R.id.btn_test);
        TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RecvActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }
}
