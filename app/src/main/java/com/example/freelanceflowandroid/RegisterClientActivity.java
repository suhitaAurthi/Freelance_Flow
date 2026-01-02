package com.example.freelanceflowandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterClientActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_GOOGLE_ID = "google_id";

    private TextInputEditText firstNameClient;
    private TextInputEditText lastNameClient;
    private TextInputEditText emailClient;
    private TextInputEditText passwordClient;
    private Spinner countryClient;
    private CheckBox checkUpdatesClient;
    private CheckBox checkAgreeClient;
    private MaterialButton btnGoogleClient;
    private MaterialButton btnCreateClient;
    private android.widget.TextView footerLoginClient;

    private SharedPreferences prefs;

    // Google SignIn
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_client); // ensure this is the layout you posted

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // find views by the IDs you provided in the layout
        firstNameClient = findViewById(R.id.firstNameClient);
        lastNameClient = findViewById(R.id.lastNameClient);
        emailClient = findViewById(R.id.emailClient);
        passwordClient = findViewById(R.id.passwordClient);
        countryClient = findViewById(R.id.countryClient);
        checkUpdatesClient = findViewById(R.id.checkUpdatesClient);
        checkAgreeClient = findViewById(R.id.checkAgreeClient);
        btnGoogleClient = findViewById(R.id.btnGoogleClient);
        btnCreateClient = findViewById(R.id.btnCreateClient);
        footerLoginClient = findViewById(R.id.footerLoginClient);

        // Configure Google Sign-In (request email & id)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult();
                        if (account != null) {
                            // save account info
                            prefs.edit()
                                    .putString(KEY_GOOGLE_ID, account.getId())
                                    .putString(KEY_USER_TYPE, "client")
                                    .putString("first_name", account.getGivenName())
                                    .putString("last_name", account.getFamilyName())
                                    .putString("email", account.getEmail())
                                    .apply();

                            PrefsManager.getInstance(RegisterClientActivity.this).saveUserRole("client");

                            Toast.makeText(this, "Signed in with Google: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, congratulationAct.class));
                            finish();
                            return;
                        }
                    } catch (Exception ex) {
                        // fallback to simulated flow below
                    }
                    // Fallback: simulated google id
                    String simulatedGoogleId = "google_" + System.currentTimeMillis();
                    prefs.edit()
                            .putString(KEY_GOOGLE_ID, simulatedGoogleId)
                            .putString(KEY_USER_TYPE, "client")
                            .putString("first_name", safeGet(firstNameClient))
                            .putString("last_name", safeGet(lastNameClient))
                            .putString("email", safeGet(emailClient))
                            .putString("country", getSelectedCountry())
                            .putBoolean("receive_updates", checkUpdatesClient.isChecked())
                            .apply();

                    PrefsManager.getInstance(RegisterClientActivity.this).saveUserRole("client");

                    Toast.makeText(this, "Signed in with Google (simulated).", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, congratulationAct.class));
                    finish();
                }
        );

        // Create account button
        btnCreateClient.setOnClickListener(v -> {
            if (validateInputs()) {
                saveClientData(false /* signedWithGoogle */);
                Toast.makeText(this, "Client details saved successfully", Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(this, congratulationAct.class));
                } catch (Exception ex) {
                    Toast.makeText(this, "Unable to start congratulations screen.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        // Already have account -> LoginActivity
        footerLoginClient.setOnClickListener(v -> {
            try {
                // pass initial role so LoginActivity can immediately redirect to client dashboard
                Intent i = new Intent(this, LoginActivity.class);
                i.putExtra("initial_role", "client");
                i.putExtra("show_login_ui", true);
                startActivity(i);
            } catch (Exception ex) {
                Toast.makeText(this, "Unable to open login screen.", Toast.LENGTH_SHORT).show();
            }
        });

        // Continue with Google
        btnGoogleClient.setOnClickListener(v -> {
            try {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            } catch (Exception ex) {
                // if Google Play Services not available or other issue, fallback to simulated flow
                String simulatedGoogleId = "google_" + System.currentTimeMillis();
                prefs.edit()
                        .putString(KEY_GOOGLE_ID, simulatedGoogleId)
                        .putString(KEY_USER_TYPE, "client")
                        .putString("first_name", safeGet(firstNameClient))
                        .putString("last_name", safeGet(lastNameClient))
                        .putString("email", safeGet(emailClient))
                        .putString("country", getSelectedCountry())
                        .putBoolean("receive_updates", checkUpdatesClient.isChecked())
                        .apply();

                PrefsManager.getInstance(RegisterClientActivity.this).saveUserRole("client");

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

        String first = safeGet(firstNameClient);
        String last = safeGet(lastNameClient);
        String email = safeGet(emailClient);
        String password = safeGet(passwordClient);

        // first name
        if (first.isEmpty()) {
            firstNameClient.setError("First name is required");
            ok = false;
        } else {
            firstNameClient.setError(null);
        }

        // last name
        if (last.isEmpty()) {
            lastNameClient.setError("Last name is required");
            ok = false;
        } else {
            lastNameClient.setError(null);
        }

        // email
        if (email.isEmpty()) {
            emailClient.setError("Email is required");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailClient.setError("Enter a valid email");
            ok = false;
        } else {
            emailClient.setError(null);
        }

        // password
        if (password.length() < 8) {
            passwordClient.setError("Password must be 8 or more characters");
            ok = false;
        } else {
            passwordClient.setError(null);
        }

        // terms checkbox
        if (!checkAgreeClient.isChecked()) {
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

    private void saveClientData(boolean signedWithGoogle) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_TYPE, "client");
        editor.putString("first_name", safeGet(firstNameClient));
        editor.putString("last_name", safeGet(lastNameClient));
        editor.putString("email", safeGet(emailClient));
        editor.putString("country", getSelectedCountry());
        editor.putBoolean("receive_updates", checkUpdatesClient.isChecked());
        if (signedWithGoogle) {
            // should be set by Google flow
            editor.putString(KEY_GOOGLE_ID, prefs.getString(KEY_GOOGLE_ID, ""));
        }
        editor.apply();

        // Also save role centrally for the app flow
        PrefsManager.getInstance(this).saveUserRole("client");
    }

    private String safeGet(TextInputEditText tie) {
        if (tie == null || tie.getText() == null) return "";
        return tie.getText().toString().trim();
    }

    private String getSelectedCountry() {
        if (countryClient == null || countryClient.getSelectedItem() == null) return "";
        return countryClient.getSelectedItem().toString();
    }
}