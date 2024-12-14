package com.example.fixmyroom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fixmyroom.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Admin extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private FirebaseFirestore db;
    private LinearLayout complaintContainer;
    private Button deleteButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link complaintContainer to the LinearLayout in your ScrollView
        complaintContainer = findViewById(R.id.complaintContainer);

        // Button to trigger deletion of selected complaints
        deleteButton = findViewById(R.id.deleteButton);  // Assuming a delete button exists in the layout
        deleteButton.setOnClickListener(v -> deleteSelectedComplaints());

        // Fetch complaints from Firestore
        fetchComplaints();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void fetchComplaints() {
        db.collection("Complaints")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String roomNumber = document.getString("RoomNumber");
                                String complaintText = document.getString("ComplaintText");
                                String complaintId = document.getId();  // Get the document ID for deletion

                                // Fetch SapId as an Object and convert it to String if it's not null
                                Object sapIdObject = document.get("SapId");
                                String sapId = sapIdObject != null ? sapIdObject.toString() : "Unknown SAP ID";

                                // Display each complaint
                                addComplaintCard(complaintId, roomNumber, sapId, complaintText);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void addComplaintCard(String complaintId, String roomNumber, String sapId, String complaintText) {
        // Inflate the complaint card layout
        View complaintCard = LayoutInflater.from(this).inflate(R.layout.complaint_card, complaintContainer, false);

        // Set the room number, SAP ID, and complaint text on the card
        TextView roomNoTextView = complaintCard.findViewById(R.id.room_no);
        TextView sapIdTextView = complaintCard.findViewById(R.id.sap_id);
        TextView descriptionTextView = complaintCard.findViewById(R.id.description);
        CheckBox checkBox = complaintCard.findViewById(R.id.checkbox);

        roomNoTextView.setText("Room No: " + roomNumber);
        sapIdTextView.setText("SAP ID: " + sapId);
        descriptionTextView.setText("Description: " + complaintText);

        // Attach the complaint ID to the checkbox as a tag
        checkBox.setTag(complaintId);

        // Add the complaint card to the container
        complaintContainer.addView(complaintCard);
    }

    private void deleteSelectedComplaints() {
        for (int i = 0; i < complaintContainer.getChildCount(); i++) {
            View complaintCard = complaintContainer.getChildAt(i);
            CheckBox checkBox = complaintCard.findViewById(R.id.checkbox);

            if (checkBox.isChecked()) {
                String complaintId = (String) checkBox.getTag(); // Retrieve complaint ID

                // Delete complaint from Firestore
                db.collection("Complaints").document(complaintId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Admin.this, "Complaint deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting complaint: ", e);
                            Toast.makeText(Admin.this, "Error deleting complaint", Toast.LENGTH_SHORT).show();
                        });

                // Remove the complaint card from the UI and reset the loop counter
                complaintContainer.removeView(complaintCard);
                i--; // Adjust index after removal
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Inflate the admin menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            // Handle profile action
            Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_logout) {
            // Handle logout action
            Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to handle logout
    private void logout() {
        // Clear login state in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all saved data, including login state
        editor.apply();

        // Redirect to MainActivity
        Intent intent = new Intent(Admin.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Finish User activity
    }
}