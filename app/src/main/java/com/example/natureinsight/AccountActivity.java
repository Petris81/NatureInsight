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
        
        // Check if user is authenticated
        if (!supabaseAuth.isAuthenticated()) {
            // User is not authenticated, redirect to login
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
