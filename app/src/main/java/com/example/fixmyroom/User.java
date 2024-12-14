package com.example.fixmyroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class User extends AppCompatActivity {

    private FirebaseFirestore db;
    private Button submitButton;

    private EditText editTextRoomNumber, editTextComplaint;
    private Spinner spinnerHostel;
    private CheckBox checkboxFurniture, checkboxElectrical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve intent extras for SAP ID and role
        long sapId = getIntent().getIntExtra("SAP_ID", 0);
        String role = getIntent().getStringExtra("ROLE");

        // Initialize views
        editTextRoomNumber = findViewById(R.id.editTextRoomNumber);
        editTextComplaint = findViewById(R.id.editTextComplaint);
        spinnerHostel = findViewById(R.id.spinner);
        checkboxFurniture = findViewById(R.id.checkbox1);
        checkboxElectrical = findViewById(R.id.checkbox2);
        submitButton = findViewById(R.id.submitbtn);

        // Set up submit button listener
        submitButton.setOnClickListener(v -> submitComplaint(sapId, role));

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void submitComplaint(long sapId, String role) {
        String hostel = spinnerHostel.getSelectedItem().toString();
        String roomNumber = editTextRoomNumber.getText().toString().trim();
        String complaintText = editTextComplaint.getText().toString().trim();
        boolean isFurnitureChecked = checkboxFurniture.isChecked();
        boolean isElectricalChecked = checkboxElectrical.isChecked();

        if (roomNumber.isEmpty() || complaintText.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create complaint data
        Map<String, Object> complaint = new HashMap<>();
        complaint.put("SapId", sapId);
        complaint.put("Role", role);
        complaint.put("Hostel", hostel);
        complaint.put("RoomNumber", roomNumber);
        complaint.put("Furniture", isFurnitureChecked);
        complaint.put("Electrical", isElectricalChecked);
        complaint.put("ComplaintText", complaintText);
        complaint.put("Timestamp", System.currentTimeMillis());

        // Add complaint to Firestore
        db.collection("Complaints").add(complaint)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(User.this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(User.this, "Failed to submit complaint", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu); // Inflate the user menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Use if-else instead of switch
        if (item.getItemId() == R.id.menu_profile) {
            // Handle profile action
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.menu_logout) {
            // Handle logout action
            logout();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        // Clear login state in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all saved data, including login state
        editor.apply();

        // Redirect to MainActivity
        Intent intent = new Intent(User.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Finish User activity
    }
}
