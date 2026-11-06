package com.kashfia.studymate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDesc, etTaskDate;
    private Spinner spinnerPriority;
    private Button btnSaveTask;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDesc = findViewById(R.id.etTaskDesc);
        etTaskDate = findViewById(R.id.etTaskDate);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup Priority Spinner items
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(adapter);

        btnSaveTask.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String desc = etTaskDesc.getText().toString().trim();
            String date = etTaskDate.getText().toString().trim();
            String priority = spinnerPriority.getSelectedItem().toString();
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String taskId = UUID.randomUUID().toString();

            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("taskId", taskId);
            taskMap.put("taskTitle", title);
            taskMap.put("taskDescription", desc);
            taskMap.put("taskDate", date);
            taskMap.put("userId", userId);
            taskMap.put("completed", false);
            taskMap.put("priority", priority);

            db.collection("Tasks").document(taskId)
                    .set(taskMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}