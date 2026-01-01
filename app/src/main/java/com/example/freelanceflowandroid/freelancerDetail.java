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

import androidx.activity.result.ActivityResultCallback;
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

        resumePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            resumeUri = uri;
                            try {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (Exception ignored) {}
                            String name = queryDisplayName(uri);
                            tvResumeName.setText(name != null ? name : uri.getLastPathSegment());
                        }
                    }
                }
        );

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            photoUri = uri;
                            ivPhotoPreview.setImageURI(uri);
                            btnRemovePhoto.setVisibility(View.VISIBLE);
                            try {
                                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (Exception ignored) {}
                        }
                    }
                }
        );

        btnSelectResume.setOnClickListener(v -> resumePickerLauncher.launch(new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        }));

        btnSelectPhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        btnRemovePhoto.setOnClickListener(v -> clearPhoto());

        TextWatcher computeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { updateNetEarnings(); }
            @Override public void afterTextChanged(Editable editable) { }
        };
        etRateHour.addTextChangedListener(computeWatcher);
        etFeePercent.addTextChangedListener(computeWatcher);

        loadSavedState();

        btnSubmit.setOnClickListener(v -> attemptSaveAndProceed());
    }

    private void updateNetEarnings() {
        double rate = parseDouble(etRateHour.getText().toString());
        double fee = parseDouble(etFeePercent.getText().toString());
        if (Double.isNaN(rate)) rate = 0;
        if (Double.isNaN(fee)) fee = 0;
        double net = rate * (1.0 - fee / 100.0);
        if (net < 0) net = 0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        tvNetEarnings.setText(nf.format(net) + " per hour");
    }

    private double parseDouble(String s) {
        if (s == null) return Double.NaN;
        s = s.trim().replace("$", "");
        if (s.isEmpty()) return Double.NaN;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return Double.NaN; }
    }

    private String queryDisplayName(Uri uri) {
        if (uri == null) return null;
        String displayName = null;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) displayName = cursor.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return displayName;
    }

    private void attemptSaveAndProceed() {
        if (!validateInputs()) return;
        saveState();
        Toast.makeText(this, "All data provided. Saved and proceeding.", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(freelancerDetail.this, DashboardClient.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private boolean validateInputs() {
        if (resumeUri == null) { Toast.makeText(this, "Please select a resume before continuing.", Toast.LENGTH_LONG).show(); return false; }
        if (photoUri == null) { Toast.makeText(this, "Please select a profile photo before continuing.", Toast.LENGTH_LONG).show(); return false; }
        String rateStr = etRateHour.getText().toString().trim();
        if (rateStr.isEmpty()) { Toast.makeText(this, "Please enter your hourly rate.", Toast.LENGTH_LONG).show(); return false; }
        String feeStr = etFeePercent.getText().toString().trim();
        if (feeStr.isEmpty()) { Toast.makeText(this, "Please enter the platform fee percentage.", Toast.LENGTH_LONG).show(); return false; }
        double rate = parseDouble(rateStr);
        double fee = parseDouble(feeStr);
        if (Double.isNaN(rate) || rate < 0) { Toast.makeText(this, "Please enter a valid hourly rate.", Toast.LENGTH_LONG).show(); return false; }
        if (Double.isNaN(fee) || fee < 0) { Toast.makeText(this, "Please enter a valid fee percentage.", Toast.LENGTH_LONG).show(); return false; }
        return true;
    }

    private void saveState() {
        String rateStr = etRateHour.getText().toString().trim();
        String feeStr = etFeePercent.getText().toString().trim();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (resumeUri != null) editor.putString(KEY_RESUME_URI, resumeUri.toString()); else editor.remove(KEY_RESUME_URI);
        if (photoUri != null) editor.putString(KEY_PHOTO_URI, photoUri.toString()); else editor.remove(KEY_PHOTO_URI);
        editor.putString(KEY_RATE, rateStr);
        editor.putString(KEY_FEE, feeStr);
        editor.apply();
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String resume = prefs.getString(KEY_RESUME_URI, null);
        String photo = prefs.getString(KEY_PHOTO_URI, null);
        String rate = prefs.getString(KEY_RATE, "");
        String fee = prefs.getString(KEY_FEE, "");

        if (resume != null) {
            try { resumeUri = Uri.parse(resume); String name = queryDisplayName(resumeUri); tvResumeName.setText(name != null ? name : resumeUri.getLastPathSegment()); } catch (Exception ignored) {}
        }
        if (photo != null) {
            try { photoUri = Uri.parse(photo); ivPhotoPreview.setImageURI(photoUri); btnRemovePhoto.setVisibility(View.VISIBLE); } catch (Exception ignored) {}
        } else {
            ivPhotoPreview.setImageResource(android.R.drawable.ic_menu_camera);
            btnRemovePhoto.setVisibility(View.GONE);
        }
        etRateHour.setText(rate);
        etFeePercent.setText(fee);
        updateNetEarnings();
    }

    private void clearPhoto() {
        photoUri = null;
        ivPhotoPreview.setImageResource(android.R.drawable.ic_menu_camera);
        btnRemovePhoto.setVisibility(View.GONE);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_PHOTO_URI).apply();
        Toast.makeText(this, "Photo removed. Please add a photo to proceed.", Toast.LENGTH_SHORT).show();
    }
}