package com.example.freelanceflowandroid;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class freelancerDetail extends AppCompatActivity {

    private static final String PREFS = "freelancer_prefs";
    private static final String KEY_RESUME_URI = "resume_uri";
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_RATE = "rate";
    private static final String KEY_FEE = "fee";

    private TextView tvResumeName;
    private ImageView ivPhotoPreview;
    private ImageButton btnRemovePhoto;
    private EditText etRateHour;
    private EditText etFeePercent;
    private TextView tvNetEarnings;
    private Button btnSelectResume;
    private Button btnSelectPhoto;
    private Button btnSubmit;

    private Uri resumeUri;
    private Uri photoUri;

    private ActivityResultLauncher<String[]> resumePickerLauncher;
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freelancer_detail);

        tvResumeName = findViewById(R.id.tv_resume_name);
        ivPhotoPreview = findViewById(R.id.iv_photo_preview);
        btnRemovePhoto = findViewById(R.id.btn_remove_photo);
        etRateHour = findViewById(R.id.et_rate_hour);
        etFeePercent = findViewById(R.id.et_fee_percent);
        tvNetEarnings = findViewById(R.id.tv_net_earnings);
        btnSelectResume = findViewById(R.id.btn_select_resume);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnSubmit = findViewById(R.id.btn_submit);

        resumePickerLauncher =
                registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                    if (uri != null) {
                        resumeUri = uri;
                        try {
                            // persist read permission if available
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        String name = queryDisplayName(uri);
                        tvResumeName.setText(name != null ? name : uri.getLastPathSegment());
                    }
                });

        photoPickerLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        photoUri = uri;
                        ivPhotoPreview.setImageURI(uri);
                        btnRemovePhoto.setVisibility(View.VISIBLE);
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                    }
                });

        btnSelectResume.setOnClickListener(v -> resumePickerLauncher.launch(new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        }));

        btnSelectPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        btnRemovePhoto.setOnClickListener(v -> clearPhoto());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                updateNetEarnings();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etRateHour.addTextChangedListener(watcher);
        etFeePercent.addTextChangedListener(watcher);

        loadSavedState();

        btnSubmit.setOnClickListener(v -> attemptSaveAndProceed());
    }

    private void attemptSaveAndProceed() {
        if (!validateInputs()) return;

        saveState();
        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();

        // Start DashboardFreelancer. Ensure DashboardFreelancer is declared in AndroidManifest.
        Intent intent = new Intent(freelancerDetail.this, DashboardFreelancer.class);
        // If you want to clear the back stack so user can't navigate back to this setup screen:
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // finish this activity so it is removed from the back stack
        finish();
    }

    private boolean validateInputs() {
        if (resumeUri == null) {
            Toast.makeText(this, "Please select a resume.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (photoUri == null) {
            Toast.makeText(this, "Please select a profile photo.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (etRateHour.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter hourly rate.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (etFeePercent.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter platform fee.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveState() {
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS, MODE_PRIVATE).edit();

        if (resumeUri != null) editor.putString(KEY_RESUME_URI, resumeUri.toString());
        if (photoUri != null) editor.putString(KEY_PHOTO_URI, photoUri.toString());
        editor.putString(KEY_RATE, etRateHour.getText().toString().trim());
        editor.putString(KEY_FEE, etFeePercent.getText().toString().trim());
        editor.apply();
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        String resume = prefs.getString(KEY_RESUME_URI, null);
        String photo = prefs.getString(KEY_PHOTO_URI, null);

        if (resume != null) {
            try {
                resumeUri = Uri.parse(resume);
                tvResumeName.setText(queryDisplayName(resumeUri));
            } catch (Exception ignored) {}
        }

        if (photo != null) {
            try {
                photoUri = Uri.parse(photo);
                ivPhotoPreview.setImageURI(photoUri);
                btnRemovePhoto.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {}
        }

        etRateHour.setText(prefs.getString(KEY_RATE, ""));
        etFeePercent.setText(prefs.getString(KEY_FEE, ""));
        updateNetEarnings();
    }

    private void updateNetEarnings() {
        double rate = parseDouble(etRateHour.getText().toString());
        double fee = parseDouble(etFeePercent.getText().toString());
        double net = rate * (1 - fee / 100.0);
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        tvNetEarnings.setText(nf.format(Math.max(net, 0)) + " per hour");
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private String queryDisplayName(Uri uri) {
        if (uri == null) return "";
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    uri, new String[]{OpenableColumns.DISPLAY_NAME},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return uri.getLastPathSegment();
    }

    private void clearPhoto() {
        photoUri = null;
        ivPhotoPreview.setImageResource(android.R.drawable.ic_menu_camera);
        btnRemovePhoto.setVisibility(View.GONE);
    }
}