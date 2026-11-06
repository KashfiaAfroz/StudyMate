package com.kashfia.studymate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etEditTaskTitle, etEditTaskDesc, etEditTaskDate;
    private Spinner spinnerEditPriority;
    private Button btnUpdateTask, btnDeleteTask;
    private FirebaseFirestore db;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        etEditTaskTitle = findViewById(R.id.etEditTaskTitle);
        etEditTaskDesc = findViewById(R.id.etEditTaskDesc);
        etEditTaskDate = findViewById(R.id.etEditTaskDate);
        spinnerEditPriority = findViewById(R.id.spinnerEditPriority);
        btnUpdateTask = findViewById(R.id.btnUpdateTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);

        db = FirebaseFirestore.getInstance();

        // Setup Spinner items
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerEditPriority.setAdapter(adapter);

        // Get data passed from Intent
        taskId = getIntent().getStringExtra("taskId");
        String title = getIntent().getStringExtra("taskTitle");
        String desc = getIntent().getStringExtra("taskDesc");
        String date = getIntent().getStringExtra("taskDate");
        String priority = getIntent().getStringExtra("taskPriority");

        // Set existing data to EditText fields
        if (title != null) etEditTaskTitle.setText(title);
        if (desc != null) etEditTaskDesc.setText(desc);
        if (date != null) etEditTaskDate.setText(date);

        // Set spinner selection based on existing priority
        if (priority != null) {
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equalsIgnoreCase(priority)) {
                    spinnerEditPriority.setSelection(i);
                    break;
                }
            }
        }

        // Update Task Button Action
        btnUpdateTask.setOnClickListener(v -> {
            String updatedTitle = etEditTaskTitle.getText().toString().trim();
            String updatedDesc = etEditTaskDesc.getText().toString().trim();
            String updatedDate = etEditTaskDate.getText().toString().trim();
            String updatedPriority = spinnerEditPriority.getSelectedItem().toString();

            if (updatedTitle.isEmpty() || updatedDesc.isEmpty() || updatedDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("Tasks").document(taskId)
                    .update(
                            "taskTitle", updatedTitle,
                            "taskDescription", updatedDesc,
                            "taskDate", updatedDate,
                            "priority", updatedPriority
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Delete Task Button Action
        btnDeleteTask.setOnClickListener(v -> {
            db.collection("Tasks").document(taskId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task Deleted Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}