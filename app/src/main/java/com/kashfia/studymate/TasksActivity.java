package com.kashfia.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        recyclerView = findViewById(R.id.recyclerViewAllTasks);
        bottomNavigationView = findViewById(R.id.bottomNavigationViewTasks);

        // Set Tasks as selected by default in bottom nav
        bottomNavigationView.setSelectedItemId(R.id.nav_tasks);

        // Bottom Navigation Item Click Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(TasksActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_tasks) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(TasksActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();

        taskAdapter = new TaskAdapter(taskList, task -> {
            Intent intent = new Intent(TasksActivity.this, EditTaskActivity.class);
            intent.putExtra("taskId", task.getTaskId());
            intent.putExtra("taskTitle", task.getTaskTitle());
            intent.putExtra("taskDesc", task.getTaskDescription());
            intent.putExtra("taskDate", task.getTaskDate());
            startActivity(intent);
        });

        recyclerView.setAdapter(taskAdapter);
        db = FirebaseFirestore.getInstance();

        loadAllTasks();
    }

    private void loadAllTasks() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        db.collection("Tasks")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            taskList.add(task);
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TasksActivity.this, "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllTasks();
    }
}
