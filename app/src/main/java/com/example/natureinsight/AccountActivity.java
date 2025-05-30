package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {
    private SupabaseAuth supabaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supabaseAuth = SupabaseAuth.getInstance();
        if (!supabaseAuth.isAuthenticated()) {
            Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_account);
        TextView usernameDisplay = findViewById(R.id.username_display);
        usernameDisplay.setText(supabaseAuth.getCurrentUserEmail());

        findViewById(R.id.logout_button).setOnClickListener(v -> {
            supabaseAuth.signOut();
            Toast.makeText(this, getString(R.string.logged_out_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_encyclopedia)
                .setOnClickListener(v -> startActivity(new Intent(this, EncyclopediaActivity.class)));

        findViewById(R.id.nav_camera).setOnClickListener(v -> startActivity(new Intent(this, PhotoActivity.class)));

        findViewById(R.id.nav_history).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.nav_account).setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }
}
