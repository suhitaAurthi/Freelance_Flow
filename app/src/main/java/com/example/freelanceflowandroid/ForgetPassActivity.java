package com.example.freelanceflowandroid;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgetPassActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass);

        // The old layout had two password fields; we'll reuse one EditText as email field
        etEmail = findViewById(R.id.editTextTextPassword);
        btnSend = findViewById(R.id.button2);

        // If caller prefilled email, put it in the field
        if (getIntent() != null && getIntent().hasExtra("prefill_email")) {
            String pre = getIntent().getStringExtra("prefill_email");
            if (pre != null && !pre.isEmpty()) etEmail.setText(pre);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Enter a valid email address");
                    return;
                }

                // Simulate sending reset email (replace with Firebase Auth sendPasswordResetEmail when configured)
                Toast.makeText(ForgetPassActivity.this, "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
