package com.example.natureinsight;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

    public class AccountActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account);

            findViewById(R.id.logout_button).setOnClickListener(v -> {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
            });
            findViewById(R.id.nav_encyclopedia).setOnClickListener(v ->
                    startActivity(new Intent(this, EncyclopediaActivity.class)));

            findViewById(R.id.nav_camera).setOnClickListener(v ->
                    startActivity(new Intent(this, PhotoActivity.class)));

            findViewById(R.id.nav_history).setOnClickListener(v ->
                    startActivity(new Intent(this, HistoryActivity.class)));

            findViewById(R.id.nav_account).setOnClickListener(v ->
                    startActivity(new Intent(this, AccountActivity.class)));

        }
    }
