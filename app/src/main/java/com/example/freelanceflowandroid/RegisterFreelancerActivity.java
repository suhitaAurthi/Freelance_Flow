package com.example.freelanceflowandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFreelancerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_GOOGLE_ID = "google_id";

    private TextInputEditText firstNameFree;
    private TextInputEditText lastNameFree;
    private TextInputEditText emailFree;
    private TextInputEditText passwordFree;
    private Spinner countryFree;
    private CheckBox checkUpdatesFree;
    private CheckBox checkAgreeFree;
    private MaterialButton btnGoogleFree;
    private MaterialButton btnCreateFree;
    private android.widget.TextView footerLoginFree;

    private SharedPreferences prefs;

    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_freelancer); // ensure this is the layout you posted

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // find views by the IDs you provided in the layout
        firstNameFree = findViewById(R.id.firstNameFree);
        lastNameFree = findViewById(R.id.lastNameFree);
        emailFree = findViewById(R.id.emailFree);
        passwordFree = findViewById(R.id.passwordFree);
        countryFree = findViewById(R.id.countryFree);
        checkUpdatesFree = findViewById(R.id.checkUpdatesFree);
        checkAgreeFree = findViewById(R.id.checkAgreeFree);
        btnGoogleFree = findViewById(R.id.btnGoogleFreelancer);
        btnCreateFree = findViewById(R.id.btnCreateFree);
        footerLoginFree = findViewById(R.id.footerLoginFree);

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult();
                if (account != null) {
                    prefs.edit()
                            .putString(KEY_GOOGLE_ID, account.getId())
                            .putString(KEY_USER_TYPE, "freelancer")
                            .putString("first_name", account.getGivenName())
                            .putString("last_name", account.getFamilyName())
                            .putString("email", account.getEmail())
                            .apply();

                    PrefsManager.getInstance(RegisterFreelancerActivity.this).saveUserRole("freelancer");

                    Toast.makeText(this, "Signed in with Google: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, congratulationAct.class));
                    finish();
                    return;
                }
            } catch (Exception ex) {
                // fallback to simulated flow
            }

            // fallback simulated
            String simulatedGoogleId = "google_" + System.currentTimeMillis();
            prefs.edit()
                    .putString(KEY_GOOGLE_ID, simulatedGoogleId)
                    .putString(KEY_USER_TYPE, "freelancer")
                    .putString("first_name", safeGet(firstNameFree))
                    .putString("last_name", safeGet(lastNameFree))
                    .putString("email", safeGet(emailFree))
                    .putString("country", getSelectedCountry())
                    .putBoolean("receive_updates", checkUpdatesFree.isChecked())
                    .apply();

            PrefsManager.getInstance(RegisterFreelancerActivity.this).saveUserRole("freelancer");
            Toast.makeText(this, "Signed in with Google (simulated).", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, congratulationAct.class));
            finish();
        });

        // Create account button
        btnCreateFree.setOnClickListener(v -> {
            if (validateInputs()) {
                saveFreeData(false /* signedWithGoogle */);
                Toast.makeText(this, "Freelancer details saved successfully", Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(this, congratulationAct.class));
                } catch (Exception ex) {
                    Toast.makeText(this, "Unable to start congratulations screen.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        // Already have account -> LoginActivity
        footerLoginFree.setOnClickListener(v -> {
            try {
                Intent i = new Intent(this, LoginActivity.class);
                i.putExtra("initial_role", "freelancer");
                i.putExtra("show_login_ui", true);
                startActivity(i);
            } catch (Exception ex) {
                Toast.makeText(this, "Unable to open login screen.", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogleFree.setOnClickListener(v -> {
            try {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            } catch (Exception ex) {
                String simulatedGoogleId = "google_" + System.currentTimeMillis();
                prefs.edit()
                        .putString(KEY_GOOGLE_ID, simulatedGoogleId)
                        .putString(KEY_USER_TYPE, "freelancer")
                        .putString("first_name", safeGet(firstNameFree))
                        .putString("last_name", safeGet(lastNameFree))
                        .putString("email", safeGet(emailFree))
                        .putString("country", getSelectedCountry())
                        .putBoolean("receive_updates", checkUpdatesFree.isChecked())
                        .apply();

                PrefsManager.getInstance(RegisterFreelancerActivity.this).saveUserRole("freelancer");

                Toast.makeText(this, "Signed in with Google (simulated).", Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(this, congratulationAct.class));
                } catch (Exception ex2) {
                    Toast.makeText(this, "Unable to start congratulations screen.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private boolean validateInputs() {
        boolean ok = true;

        String first = safeGet(firstNameFree);
        String last = safeGet(lastNameFree);
        String email = safeGet(emailFree);
        String password = safeGet(passwordFree);

        // first name
        if (first.isEmpty()) {
            firstNameFree.setError("First name is required");
            ok = false;
        } else {
            firstNameFree.setError(null);
        }

        // last name
        if (last.isEmpty()) {
            lastNameFree.setError("Last name is required");
            ok = false;
        } else {
            lastNameFree.setError(null);
        }

        // email
        if (email.isEmpty()) {
            emailFree.setError("Email is required");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailFree.setError("Enter a valid email");
            ok = false;
        } else {
            emailFree.setError(null);
        }

        // password
        if (password.length() < 8) {
            passwordFree.setError("Password must be 8 or more characters");
            ok = false;
        } else {
            passwordFree.setError(null);
        }

        // terms checkbox
        if (!checkAgreeFree.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms of Service", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        // optional: country check (if spinner has a placeholder at pos 0, ensure not selected)
        String country = getSelectedCountry();
        if (country == null || country.trim().isEmpty()) {
            // if you require country selection, uncomment the following:
            // Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            // ok = false;
        }

        return ok;
    }

    private void saveFreeData(boolean signedWithGoogle) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_TYPE, "freelancer");
        editor.putString("first_name", safeGet(firstNameFree));
        editor.putString("last_name", safeGet(lastNameFree));
        editor.putString("email", safeGet(emailFree));
        editor.putString("country", getSelectedCountry());
        editor.putBoolean("receive_updates", checkUpdatesFree.isChecked());
        if (signedWithGoogle) {
            // should be set by Google flow
            editor.putString(KEY_GOOGLE_ID, prefs.getString(KEY_GOOGLE_ID, ""));
        }
        editor.apply();
    }

    private String safeGet(TextInputEditText tie) {
        if (tie == null || tie.getText() == null) return "";
        return tie.getText().toString().trim();
    }

    private String getSelectedCountry() {
        if (countryFree == null || countryFree.getSelectedItem() == null) return "";
        return countryFree.getSelectedItem().toString();
    }
}