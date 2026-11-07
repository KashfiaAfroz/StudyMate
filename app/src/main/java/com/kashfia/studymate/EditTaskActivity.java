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

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDesc, etTaskDate;
    private Spinner spinnerPriority;
    private Button btnUpdateTask, btnDeleteTask;

    private FirebaseFirestore db;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        db = FirebaseFirestore.getInstance();

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDesc = findViewById(R.id.etTaskDesc);
        etTaskDate = findViewById(R.id.etTaskDate);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnUpdateTask = findViewById(R.id.btnUpdateTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);

        // Spinner Setup
        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerPriority != null) {
            spinnerPriority.setAdapter(adapter);
        }

        // Intent থেকে আগের ডেটা রিসিভ করে EditText-এ বসানো
        if (getIntent() != null) {
            taskId = getIntent().getStringExtra("taskId");
            String title = getIntent().getStringExtra("title");
            String description = getIntent().getStringExtra("description");
            String date = getIntent().getStringExtra("date");
            String priority = getIntent().getStringExtra("priority");

            if (title != null) etTaskTitle.setText(title);
            if (description != null) etTaskDesc.setText(description);
            if (date != null) etTaskDate.setText(date);

            if (priority != null && spinnerPriority != null) {
                for (int i = 0; i < priorities.length; i++) {
                    if (priorities[i].equalsIgnoreCase(priority)) {
                        spinnerPriority.setSelection(i);
                        break;
                    }
                }
            }
        }

        // Update Button Click
        if (btnUpdateTask != null) {
            btnUpdateTask.setOnClickListener(v -> updateTask());
        }

        // Delete Button Click
        if (btnDeleteTask != null) {
            btnDeleteTask.setOnClickListener(v -> deleteTask());
        }
    }

    private void updateTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDesc.getText().toString().trim();
        String date = etTaskDate.getText().toString().trim();
        String priority = spinnerPriority != null ? spinnerPriority.getSelectedItem().toString() : "Medium";

        if (title.isEmpty()) {
            etTaskTitle.setError("Please enter task title");
            etTaskTitle.requestFocus();
            return;
        }

        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("taskTitle", title);
        taskMap.put("taskDescription", description);
        taskMap.put("taskDate", date);
        taskMap.put("priority", priority);

        db.collection("tasks").document(taskId)
                .update(taskMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteTask() {
        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Task ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Task deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}