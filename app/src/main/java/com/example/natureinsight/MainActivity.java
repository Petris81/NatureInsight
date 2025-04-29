package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private SupabaseAuth supabaseAuth;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // init SupabaseAuth
        supabaseAuth = SupabaseAuth.getInstance();
        supabaseAuth.init(this);
        
        // init DatabaseManager
        databaseManager = DatabaseManager.getInstance();
        databaseManager.init(this);

        if (supabaseAuth.isAuthenticated()) {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        findViewById(R.id.login_button).setOnClickListener(v -> handleLogin());
        findViewById(R.id.signup_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
        int count = databaseManager.loadFromCSV(this, "data.csv");
        if (count == 0) {
            // TODO : think about how to handle it
        }
    }

    private void handleLogin() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.login_button).setEnabled(false);
        supabaseAuth.signIn(email, password, new SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String token) {
                runOnUiThread(() -> {
                    findViewById(R.id.login_button).setEnabled(true);
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    supabaseAuth.setCurrentUserEmail(email);
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    findViewById(R.id.login_button).setEnabled(true);
                    Toast.makeText(MainActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close databse when we close the app
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}