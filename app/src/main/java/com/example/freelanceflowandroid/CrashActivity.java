package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        TextView tv = findViewById(R.id.tv_crash);
        String stack = getIntent().getStringExtra("stack");
        if (stack == null) stack = "No stacktrace available.";
        tv.setText(stack);

        Button btn = findViewById(R.id.btn_restart);
        btn.setOnClickListener(v -> {
            Intent i = new Intent(CrashActivity.this, RoleChoice.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}

