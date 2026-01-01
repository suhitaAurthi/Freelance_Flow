package com.example.freelanceflowandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * clientDetail activity (full interactive version)
 */
public class clientDetail extends AppCompatActivity {

    private static final String PREFS = "client_prefs";
    private static final String KEY_LOGO_URI = "logo_uri";
    private static final String KEY_NAME = "display_name";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PHONE_VERIFIED = "phone_verified";
    private static final String KEY_CITY = "city";
    private static final String KEY_PAYMENT_ON_FILE = "payment_on_file";
    private static final String KEY_BUDGET_MIN = "budget_min";
    private static final String KEY_BUDGET_MAX = "budget_max";
    private static final String KEY_PROJECTS = "projects_json";

    private ImageView ivLogoPreview;
    private ImageButton btnRemoveLogo;
    private Button btnSelectLogo;
    private EditText etDisplayName;
    private Spinner spAccountType;
    private EditText etPhone;
    private Button btnVerifyPhone;
    private TextView tvPhoneVerified;
    private EditText etCity;
    private Switch switchPayment;
    private EditText etBudgetMin;
    private EditText etBudgetMax;
    private TextView tvBudgetSample;
    private Button btnSubmit;

    private TextView tvNoProjects;
    private RecyclerView rvProjects;
    private Button btnAddProject;

    private Uri logoUri;
    private boolean phoneVerified = false;

    private ActivityResultLauncher<String> logoPickerLauncher;

    private final List<JSONObject> projects = new ArrayList<>();
    private ProjectAdapter projectAdapter;

    // helper: safely read text from EditText
    private String getTextSafe(EditText e) {
        if (e == null) return "";
        CharSequence cs = e.getText();
        return cs == null ? "" : cs.toString();
    }

    // helper: safely parse double from EditText
    private double parseDoubleSafe(EditText e) {
        if (e == null) return Double.NaN;
        return parseDouble(getTextSafe(e));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.client_detail);

            ivLogoPreview = findViewById(R.id.iv_logo_preview);
            btnRemoveLogo = findViewById(R.id.btn_remove_logo);
            btnSelectLogo = findViewById(R.id.btn_select_logo);
            etDisplayName = findViewById(R.id.et_display_name);
            spAccountType = findViewById(R.id.sp_account_type);
            etPhone = findViewById(R.id.et_phone);
            btnVerifyPhone = findViewById(R.id.btn_verify_phone);
            tvPhoneVerified = findViewById(R.id.tv_phone_verified);
            etCity = findViewById(R.id.et_city);
            switchPayment = findViewById(R.id.switch_payment);
            etBudgetMin = findViewById(R.id.et_budget_min);
            etBudgetMax = findViewById(R.id.et_budget_max);
            tvBudgetSample = findViewById(R.id.tv_budget_sample);
            btnSubmit = findViewById(R.id.btn_submit);

            tvNoProjects = findViewById(R.id.tv_no_projects);
            rvProjects = findViewById(R.id.rv_projects);
            btnAddProject = findViewById(R.id.btn_add_project);

            // Defensive check: ensure required views exist in the loaded layout; if not, surface a readable error and stop initialization.
            StringBuilder missingViews = new StringBuilder();
            if (ivLogoPreview == null) missingViews.append("ivLogoPreview\n");
            if (btnSelectLogo == null) missingViews.append("btnSelectLogo\n");
            if (etDisplayName == null) missingViews.append("etDisplayName\n");
            if (spAccountType == null) missingViews.append("spAccountType\n");
            if (etPhone == null) missingViews.append("etPhone\n");
            if (btnVerifyPhone == null) missingViews.append("btnVerifyPhone\n");
            if (tvPhoneVerified == null) missingViews.append("tvPhoneVerified\n");
            if (etCity == null) missingViews.append("etCity\n");
            if (switchPayment == null) missingViews.append("switchPayment\n");
            if (etBudgetMin == null) missingViews.append("etBudgetMin\n");
            if (etBudgetMax == null) missingViews.append("etBudgetMax\n");
            if (tvBudgetSample == null) missingViews.append("tvBudgetSample\n");
            if (btnSubmit == null) missingViews.append("btnSubmit\n");
            if (tvNoProjects == null) missingViews.append("tvNoProjects\n");
            if (rvProjects == null) missingViews.append("rvProjects\n");
            if (btnAddProject == null) missingViews.append("btnAddProject\n");

            if (missingViews.length() > 0) {
                String msg = "Layout is missing required views:\n" + missingViews;
                Log.e("clientDetail", msg);
                Toast.makeText(this, "Internal layout mismatch — check Logcat. App may not work correctly.", Toast.LENGTH_LONG).show();
                // stop initialization to avoid NullPointerExceptions
                return;
            }

            ArrayAdapter<String> accAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Select account type", "Individual", "Company"});
            accAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spAccountType.setAdapter(accAdapter);

            logoPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            if (uri != null) {
                                logoUri = uri;
                                ivLogoPreview.setImageURI(uri);
                                btnRemoveLogo.setVisibility(View.VISIBLE);
                                try {
                                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
            );

            btnSelectLogo.setOnClickListener(view -> logoPickerLauncher.launch("image/*"));

            btnRemoveLogo.setOnClickListener(view -> confirmRemoveLogo());

            etPhone.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String digitsOnly = s.toString().replaceAll("\\D+", "");
                    if (isPhoneFormatValid(digitsOnly)) {
                        if (!phoneVerified) {
                            tvPhoneVerified.setTextColor(ContextCompat.getColor(clientDetail.this, android.R.color.holo_green_dark));
                            tvPhoneVerified.setText("Phone looks valid ✔");
                        }
                    } else {
                        if (!phoneVerified) {
                            tvPhoneVerified.setText("");
                        } else {
                            if (!isPhoneFormatValid(digitsOnly)) {
                                phoneVerified = false;
                                tvPhoneVerified.setTextColor(ContextCompat.getColor(clientDetail.this, android.R.color.holo_red_dark));
                                tvPhoneVerified.setText("Phone needs re-verification");
                            }
                        }
                    }
                }
            });

            btnVerifyPhone.setOnClickListener(view -> {
                String raw = etPhone.getText().toString().trim();
                String digitsOnly = raw.replaceAll("\\D+", "");
                if (!isPhoneFormatValid(digitsOnly)) {
                    phoneVerified = false;
                    tvPhoneVerified.setTextColor(ContextCompat.getColor(clientDetail.this, android.R.color.holo_red_dark));
                    tvPhoneVerified.setText("Invalid phone number (must be 11 digits or start with 88+11)");
                    Toast.makeText(clientDetail.this, "Invalid phone number. Please enter 11 digits or include country code 88.", Toast.LENGTH_SHORT).show();
                    return;
                }
                phoneVerified = true;
                tvPhoneVerified.setTextColor(ContextCompat.getColor(clientDetail.this, android.R.color.holo_green_dark));
                tvPhoneVerified.setText("Phone verified ✔");
                Toast.makeText(clientDetail.this, "Phone verified (simulated)", Toast.LENGTH_SHORT).show();
            });

            android.text.TextWatcher budgetWatcher = new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable e) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateBudgetSample();
                }
            };
            etBudgetMin.addTextChangedListener(budgetWatcher);
            etBudgetMax.addTextChangedListener(budgetWatcher);

            projectAdapter = new ProjectAdapter(projects);
            rvProjects.setLayoutManager(new LinearLayoutManager(this));
            rvProjects.setAdapter(projectAdapter);

            btnAddProject.setOnClickListener(view -> showAddProjectDialogInteractive());

            loadSavedState();

            btnSubmit.setOnClickListener(view -> attemptSave());
        } catch (Throwable t) {
            Log.e("clientDetail", "onCreate failed", t);
            // Rethrow so our global uncaught handler saves the stacktrace to crash_log.txt
            throw t;
        }
    }

    private void confirmRemoveLogo() {
        new AlertDialog.Builder(this)
                .setTitle("Remove logo")
                .setMessage("Are you sure you want to remove the logo?")
                .setPositiveButton("Remove", (dialog, which) -> clearLogo())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearLogo() {
        logoUri = null;
        ivLogoPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        btnRemoveLogo.setVisibility(View.GONE);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().remove(KEY_LOGO_URI).apply();
        Toast.makeText(this, "Logo removed", Toast.LENGTH_SHORT).show();
    }

    private boolean isPhoneFormatValid(String digitsOnly) {
        if (digitsOnly == null) return false;
        if (digitsOnly.length() == 11) return true;
        if (digitsOnly.length() == 13 && digitsOnly.startsWith("88")) return true;
        return digitsOnly.length() >= 7 && digitsOnly.length() <= 15;
    }

    private void updateBudgetSample() {
        double min = parseDoubleSafe(etBudgetMin);
        double max = parseDoubleSafe(etBudgetMax);

        if (Double.isNaN(min) && Double.isNaN(max)) {
            tvBudgetSample.setText("");
            return;
        }

        if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) {
            tvBudgetSample.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            tvBudgetSample.setText("Invalid budget: Min should be ≤ Max");
            return;
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String minText = Double.isNaN(min) ? "?" : nf.format(min);
        String maxText = Double.isNaN(max) ? "?" : nf.format(max);
        tvBudgetSample.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tvBudgetSample.setText("Typical budget: " + minText + " — " + maxText);
    }

    private double parseDouble(String s) {
        if (s == null) return Double.NaN;
        s = s.trim().replace(",", "").replace("$", "");
        if (s.isEmpty()) return Double.NaN;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private void attemptSave() {
        StringBuilder missing = new StringBuilder();

        String name = getTextSafe(etDisplayName).trim();
        if (name.isEmpty()) missing.append("• Display name\n");

        int accountPos = (spAccountType == null) ? 0 : spAccountType.getSelectedItemPosition();
        if (accountPos <= 0) missing.append("• Account type\n");

        String city = getTextSafe(etCity).trim();
        if (city.isEmpty()) missing.append("• City\n");

        if (switchPayment == null || !switchPayment.isChecked()) missing.append("• Add a payment method on file\n");

        double min = parseDoubleSafe(etBudgetMin);
        double max = parseDoubleSafe(etBudgetMax);
        if (Double.isNaN(min)) missing.append("• Budget min\n");
        if (Double.isNaN(max)) missing.append("• Budget max\n");
        if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) missing.append("• Budget min should be ≤ max\n");

        if (logoUri == null) missing.append("• Company logo / profile photo\n");

        String rawPhone = getTextSafe(etPhone).trim();
        String digitsOnly = rawPhone.replaceAll("\\D+", "");
        if (!isPhoneFormatValid(digitsOnly)) missing.append("• Valid phone number (11 digits or +88 + 11 digits)\n");
        if (!phoneVerified) missing.append("• Phone not verified\n");

        if (missing.length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Please complete required fields")
                    .setMessage("The following are required before proceeding:\n\n" + missing)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        saveState();
        Toast.makeText(this, "Client details saved", Toast.LENGTH_SHORT).show();
    }

    private void showAddProjectDialogInteractive() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (12 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Project title (required)");
        root.addView(etTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Short description (optional)");
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = padding / 2;
        root.addView(etDesc, descLp);

        LinearLayout budgetRow = new LinearLayout(this);
        budgetRow.setOrientation(LinearLayout.HORIZONTAL);
        budgetRow.setWeightSum(2f);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = padding / 2;

        final EditText etMin = new EditText(this);
        etMin.setHint("Min budget");
        etMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams minLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        budgetRow.addView(etMin, minLp);

        final EditText etMax = new EditText(this);
        etMax.setHint("Max budget");
        etMax.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams maxLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        maxLp.leftMargin = padding / 2;
        budgetRow.addView(etMax, maxLp);

        root.addView(budgetRow, rowLp);

        final TextView tvValidation = new TextView(this);
        tvValidation.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        tvValidation.setPadding(0, padding / 2, 0, 0);
        root.addView(tvValidation);

        TextWatcher validator = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable e) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String title = etTitle.getText().toString().trim();
                double min = parseDouble(etMin.getText().toString());
                double max = parseDouble(etMax.getText().toString());
                StringBuilder msg = new StringBuilder();
                if (title.isEmpty()) msg.append("Title is required. ");
                if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) msg.append("Min should be ≤ Max.");
                tvValidation.setText(msg.toString());
            }
        };
        etTitle.addTextChangedListener(validator);
        etMin.addTextChangedListener(validator);
        etMax.addTextChangedListener(validator);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Project (sample)")
                .setView(root)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addBtn.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                double min = parseDouble(etMin.getText().toString());
                double max = parseDouble(etMax.getText().toString());

                if (title.isEmpty()) {
                    etTitle.setError("Title required");
                    etTitle.requestFocus();
                    return;
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) {
                    etMin.setError("Min must be ≤ Max");
                    etMin.requestFocus();
                    return;
                }

                JSONObject p = new JSONObject();
                try {
                    p.put("title", title);
                    p.put("description", desc);
                    if (!Double.isNaN(min)) p.put("budget_min", min);
                    if (!Double.isNaN(max)) p.put("budget_max", max);
                    p.put("created_at", System.currentTimeMillis());
                } catch (JSONException ex) {}

                projects.add(0, p);
                saveState();
                refreshProjectsView();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.VH> {
        private final List<JSONObject> items;
        ProjectAdapter(List<JSONObject> items) { this.items = items; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            final JSONObject obj = items.get(position);
            String title = obj.optString("title", "Untitled");
            String desc = obj.optString("description", "");
            double min = obj.has("budget_min") ? obj.optDouble("budget_min", Double.NaN) : Double.NaN;
            double max = obj.has("budget_max") ? obj.optDouble("budget_max", Double.NaN) : Double.NaN;

            String subtitle = desc;
            if (Double.isNaN(min) && Double.isNaN(max)) {}
            else {
                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
                String minText = Double.isNaN(min) ? "?" : nf.format(min);
                String maxText = Double.isNaN(max) ? "?" : nf.format(max);
                String budget = "Budget: " + minText + " — " + maxText;
                subtitle = desc.isEmpty() ? budget : desc + " • " + budget;
            }

            holder.title.setText(title);
            holder.subtitle.setText(subtitle);

            holder.itemView.setOnClickListener(v -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Title: ").append(title).append("\n\n");
                if (!desc.isEmpty()) sb.append("Description: ").append(desc).append("\n\n");
                if (!Double.isNaN(min) || !Double.isNaN(max)) {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    String minText = Double.isNaN(min) ? "?" : nf.format(min);
                    String maxText = Double.isNaN(max) ? "?" : nf.format(max);
                    sb.append("Budget: ").append(minText).append(" — ").append(maxText).append("\n\n");
                }
                sb.append("Created: ").append(java.text.DateFormat.getDateTimeInstance().format(obj.optLong("created_at", System.currentTimeMillis())));
                new AlertDialog.Builder(clientDetail.this)
                        .setTitle("Project details")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Edit", (d, w) -> showEditProjectDialog(position))
                        .show();
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(clientDetail.this)
                        .setTitle("Remove project")
                        .setMessage("Remove project \"" + title + "\"?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            items.remove(position);
                            saveState();
                            refreshProjectsView();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void showEditProjectDialog(final int index) {
        if (index < 0 || index >= projects.size()) return;
        final JSONObject existing = projects.get(index);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (12 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Project title (required)");
        etTitle.setText(existing.optString("title", ""));
        root.addView(etTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Short description (optional)");
        etDesc.setText(existing.optString("description", ""));
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = padding / 2;
        root.addView(etDesc, descLp);

        LinearLayout budgetRow = new LinearLayout(this);
        budgetRow.setOrientation(LinearLayout.HORIZONTAL);
        budgetRow.setWeightSum(2f);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = padding / 2;

        final EditText etMin = new EditText(this);
        etMin.setHint("Min budget");
        etMin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (existing.has("budget_min")) etMin.setText(String.valueOf(existing.optDouble("budget_min")));
        LinearLayout.LayoutParams minLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        budgetRow.addView(etMin, minLp);

        final EditText etMax = new EditText(this);
        etMax.setHint("Max budget");
        etMax.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (existing.has("budget_max")) etMax.setText(String.valueOf(existing.optDouble("budget_max")));
        LinearLayout.LayoutParams maxLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        maxLp.leftMargin = padding / 2;
        budgetRow.addView(etMax, maxLp);

        root.addView(budgetRow, rowLp);

        final TextView tvValidation = new TextView(this);
        tvValidation.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        tvValidation.setPadding(0, padding / 2, 0, 0);
        root.addView(tvValidation);

        android.text.TextWatcher validator = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable e) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String title = etTitle.getText().toString().trim();
                double min = parseDouble(etMin.getText().toString());
                double max = parseDouble(etMax.getText().toString());
                StringBuilder msg = new StringBuilder();
                if (title.isEmpty()) msg.append("Title is required. ");
                if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) msg.append("Min should be ≤ Max.");
                tvValidation.setText(msg.toString());
            }
        };
        etTitle.addTextChangedListener(validator);
        etMin.addTextChangedListener(validator);
        etMax.addTextChangedListener(validator);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Project")
                .setView(root)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> {
                String title = etTitle.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                double min = parseDouble(etMin.getText().toString());
                double max = parseDouble(etMax.getText().toString());

                if (title.isEmpty()) {
                    etTitle.setError("Title required");
                    etTitle.requestFocus();
                    return;
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && min > max) {
                    etMin.setError("Min must be ≤ Max");
                    etMin.requestFocus();
                    return;
                }

                try {
                    existing.put("title", title);
                    existing.put("description", desc);
                    if (!Double.isNaN(min)) existing.put("budget_min", min); else existing.remove("budget_min");
                    if (!Double.isNaN(max)) existing.put("budget_max", max); else existing.remove("budget_max");
                } catch (JSONException ex) {}

                saveState();
                refreshProjectsView();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void saveState() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (logoUri != null) editor.putString(KEY_LOGO_URI, logoUri.toString()); else editor.remove(KEY_LOGO_URI);
        editor.putString(KEY_NAME, getTextSafe(etDisplayName).trim());
        editor.putInt(KEY_ACCOUNT_TYPE, spAccountType == null ? 0 : spAccountType.getSelectedItemPosition());
        editor.putString(KEY_PHONE, getTextSafe(etPhone).trim());
        editor.putBoolean(KEY_PHONE_VERIFIED, phoneVerified);
        editor.putString(KEY_CITY, getTextSafe(etCity).trim());
        editor.putBoolean(KEY_PAYMENT_ON_FILE, switchPayment != null && switchPayment.isChecked());
        editor.putString(KEY_BUDGET_MIN, getTextSafe(etBudgetMin).trim());
        editor.putString(KEY_BUDGET_MAX, getTextSafe(etBudgetMax).trim());

        JSONArray arr = new JSONArray();
        for (JSONObject p : projects) arr.put(p);
        editor.putString(KEY_PROJECTS, arr.toString());

        editor.apply();
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String logo = prefs.getString(KEY_LOGO_URI, null);
        if (logo != null) {
            try {
                logoUri = Uri.parse(logo);
                ivLogoPreview.setImageURI(logoUri);
                btnRemoveLogo.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {}
        } else {
            ivLogoPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            btnRemoveLogo.setVisibility(View.GONE);
        }

        if (etDisplayName != null) etDisplayName.setText(prefs.getString(KEY_NAME, ""));
        int accPos = prefs.getInt(KEY_ACCOUNT_TYPE, 0);
        if (spAccountType != null) spAccountType.setSelection(accPos);

        if (etPhone != null) etPhone.setText(prefs.getString(KEY_PHONE, ""));
        phoneVerified = prefs.getBoolean(KEY_PHONE_VERIFIED, false);
        if (phoneVerified) {
            tvPhoneVerified.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            tvPhoneVerified.setText("Phone verified ✔");
        } else {
            tvPhoneVerified.setText("");
        }

        if (etCity != null) etCity.setText(prefs.getString(KEY_CITY, ""));
        if (switchPayment != null) switchPayment.setChecked(prefs.getBoolean(KEY_PAYMENT_ON_FILE, false));
        if (etBudgetMin != null) etBudgetMin.setText(prefs.getString(KEY_BUDGET_MIN, ""));
        if (etBudgetMax != null) etBudgetMax.setText(prefs.getString(KEY_BUDGET_MAX, ""));
        updateBudgetSample();

        projects.clear();
        String projectsJson = prefs.getString(KEY_PROJECTS, null);
        if (projectsJson != null) {
            try {
                JSONArray arr = new JSONArray(projectsJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject p = arr.optJSONObject(i);
                    if (p != null) projects.add(p);
                }
            } catch (JSONException ignored) {}
        }
        refreshProjectsView();
    }

    private void refreshProjectsView() {
        if (projects.isEmpty()) {
            rvProjects.setVisibility(View.GONE);
            tvNoProjects.setVisibility(View.VISIBLE);
        } else {
            rvProjects.setVisibility(View.VISIBLE);
            tvNoProjects.setVisibility(View.GONE);
        }
        if (projectAdapter != null) projectAdapter.notifyDataSetChanged();
    }
}

