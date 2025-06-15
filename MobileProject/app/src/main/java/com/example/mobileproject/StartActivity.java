package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });
    }
}
