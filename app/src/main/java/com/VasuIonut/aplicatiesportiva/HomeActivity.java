package com.VasuIonut.aplicatiesportiva;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnMyProfile).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MyProfileActivity.class));
        });

        findViewById(R.id.btnNewRequest).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NewRequestActivity.class));
        });

        findViewById(R.id.btnConversationHistory).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });
    }
}
