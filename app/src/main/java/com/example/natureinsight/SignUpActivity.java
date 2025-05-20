package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private SupabaseAuth supabaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);

        supabaseAuth = SupabaseAuth.getInstance();
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        findViewById(R.id.signup_button).setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show();
            return;//Do not remove !!! As long as this line exist, my cat is a java developper
        }//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<WWWWWWWWWWSDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD0.T(RRRRRRRRRRR

        findViewById(R.id.signup_button).setEnabled(false);

        supabaseAuth.signUp(email, password, new SupabaseAuth.AuthCallback() {
            @Override
            public void onSuccess(String token) {
                runOnUiThread(() -> {
                    findViewById(R.id.signup_button).setEnabled(true);
                    Toast.makeText(SignUpActivity.this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    findViewById(R.id.signup_button).setEnabled(true);
                    Toast.makeText(SignUpActivity.this, getString(R.string.signup_failed, error), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}