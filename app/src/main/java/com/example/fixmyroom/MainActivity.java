package com.example.fixmyroom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText sapIdEditText, passwordEditText;
    Spinner roleSpinner;
    Button bt1;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login state
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String role = sharedPreferences.getString("ROLE", "");

        if (isLoggedIn) {
            // Redirect to the appropriate activity based on role
            if ("User".equalsIgnoreCase(role)) {
                Intent userIntent = new Intent(MainActivity.this, User.class);
                startActivity(userIntent);
            } else if ("Admin".equalsIgnoreCase(role)) {
                Intent adminIntent = new Intent(MainActivity.this, Admin.class);
                startActivity(adminIntent);
            }
            finish(); // Close MainActivity to prevent going back to the login screen
        } else {
            setContentView(R.layout.activity_main);

            // Initialize Firestore and UI components as usual
            db = FirebaseFirestore.getInstance();
            sapIdEditText = findViewById(R.id.editText1);
            passwordEditText = findViewById(R.id.editText2);
            roleSpinner = findViewById(R.id.spinner);
            bt1 = findViewById(R.id.bt1);

            // Set login button click listener
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sapId = sapIdEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();

                    if (TextUtils.isEmpty(sapId) || TextUtils.isEmpty(password)) {
                        Toast.makeText(MainActivity.this, "Please enter SAP ID and password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    authenticateUser(sapId, password, role);
                }
            });
        }
    }


    void authenticateUser(String sapId, String password, String role) {
        // Ensure SAP ID is numeric
        if (!sapId.matches("\\d+")) {
            Toast.makeText(MainActivity.this, "Invalid SAP ID format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert SAP ID to a Long
        Long sapIdInt = Long.parseLong(sapId);

        // Query Firestore for a user with the matching "Sap Id" field
        db.collection("Users")
                .whereEqualTo("Sap Id", sapIdInt)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String storedPassword = document.getString("Password");
                        String storedRole = document.getString("Role");

                        // Validate password and role
                        if (password.equals(storedPassword) && role.equalsIgnoreCase(storedRole)) {
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Save login state in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putLong("SAP_ID", sapIdInt);
                            editor.putString("ROLE", storedRole);
                            editor.apply();

                            // Redirect to the appropriate activity based on role
                            if (storedRole.equalsIgnoreCase("User")) {
                                Intent i1 = new Intent(MainActivity.this, User.class);
                                i1.putExtra("SAP_ID", sapIdInt);
                                i1.putExtra("ROLE", storedRole);
                                startActivity(i1);
                                finish();
                            } else {
                                Intent i2 = new Intent(MainActivity.this, Admin.class);
                                startActivity(i2);
                                finish();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid password or role", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error accessing database: ", e);
                    Toast.makeText(MainActivity.this, "Error accessing database", Toast.LENGTH_SHORT).show();
                });
    }
}
