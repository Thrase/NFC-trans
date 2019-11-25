package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button SendButton;
    private Button RecvButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SendButton = findViewById(R.id.btn_send);
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
                //跳转到send
                Intent intent=new Intent(MainActivity.this,SendActivity.class);
                startActivity(intent);

            }
        });

        RecvButton=findViewById(R.id.btn_recv);
        RecvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"请打开WLAN以及NFC",Toast.LENGTH_SHORT).show();
                //跳转到recv
                Intent intent=new Intent(MainActivity.this,RecvActivity.class);
                startActivity(intent);
            }
        });

    }
    }
