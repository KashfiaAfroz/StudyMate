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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etAddTaskTitle, etAddTaskDesc, etAddTaskDate;
    private Spinner spinnerAddPriority;
    private Button btnSaveNewTask, btnCancelNewTask;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etAddTaskTitle = findViewById(R.id.etAddTaskTitle);
        etAddTaskDesc = findViewById(R.id.etAddTaskDesc);
        etAddTaskDate = findViewById(R.id.etAddTaskDate);
        spinnerAddPriority = findViewById(R.id.spinnerAddPriority);
        btnSaveNewTask = findViewById(R.id.btnSaveNewTask);
        btnCancelNewTask = findViewById(R.id.btnCancelNewTask);

        // স্পিনারে প্রায়োরিটি অপশন সেট করা (High, Medium, Low)
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerAddPriority.setAdapter(adapter);

        // অটোমেটিক আজকের তারিখ DD-MM-YYYY ফরম্যাটে ডেট বক্সে বসিয়ে দেওয়া
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        etAddTaskDate.setText(currentDate);

        if (btnSaveNewTask != null) {
            btnSaveNewTask.setOnClickListener(v -> saveTaskToFirestore());
        }

        if (btnCancelNewTask != null) {
            btnCancelNewTask.setOnClickListener(v -> finish());
        }
    }

    private void saveTaskToFirestore() {
        String title = etAddTaskTitle.getText().toString().trim();
        String description = etAddTaskDesc.getText().toString().trim();
        String date = etAddTaskDate.getText().toString().trim();
        String priority = spinnerAddPriority.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter a date (DD-MM-YYYY)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String taskId = db.collection("tasks").document().getId();

        Task newTask = new Task(taskId, userId, title, description, date, priority, false);

        db.collection("tasks").document(taskId)
                .set(newTask)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task Added Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}