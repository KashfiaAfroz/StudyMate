package com.kashfia.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTasks;
    private TextView tvNoTasks, tvTotalTasks, tvCompletedTasks, tvRemainingTasks;
    private Button btnSortTasks, btnFilterPriority;
    private EditText etSearchTask;
    private FloatingActionButton fabAddTask;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> taskListFull;

    private ListenerRegistration tasksListener;

    private int currentSortOption = -1;
    private String currentFilterPriority = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvRemainingTasks = findViewById(R.id.tvRemainingTasks);

        etSearchTask = findViewById(R.id.etSearchTask);
        btnSortTasks = findViewById(R.id.btnSortTasks);
        btnFilterPriority = findViewById(R.id.btnFilterPriority);
        fabAddTask = findViewById(R.id.fabAddTask);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskListFull = new ArrayList<>();

        taskAdapter = new TaskAdapter(taskList, this);
        recyclerViewTasks.setAdapter(taskAdapter);

        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> {
                Intent intent = new Intent(TasksActivity.this, AddTaskActivity.class);
                startActivity(intent);
            });
        }

        if (etSearchTask != null) {
            etSearchTask.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    processFilterAndSort();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSortTasks != null) {
            btnSortTasks.setOnClickListener(v -> showSortDialog());
        }

        if (btnFilterPriority != null) {
            btnFilterPriority.setOnClickListener(v -> showFilterDialog());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTasksFromFirestore();
    }

    private void fetchTasksFromFirestore() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        if (tasksListener != null) {
            tasksListener.remove();
        }

        tasksListener = db.collection("tasks")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    taskListFull.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Task task = doc.toObject(Task.class);
                            if (task != null) {
                                task.setTaskId(doc.getId());
                                taskListFull.add(task);
                            }
                        }
                    }
                    processFilterAndSort();
                });
    }

    private void processFilterAndSort() {
        List<Task> tempFilteredList = new ArrayList<>();

        for (Task task : taskListFull) {
            boolean matchesPriority = currentFilterPriority.equalsIgnoreCase("All") ||
                    (task.getPriority() != null && task.getPriority().equalsIgnoreCase(currentFilterPriority));

            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (task.getTaskTitle() != null && task.getTaskTitle().toLowerCase(Locale.getDefault()).contains(currentSearchQuery.toLowerCase(Locale.getDefault()))) ||
                    (task.getTaskDescription() != null && task.getTaskDescription().toLowerCase(Locale.getDefault()).contains(currentSearchQuery.toLowerCase(Locale.getDefault())));

            if (matchesPriority && matchesSearch) {
                tempFilteredList.add(task);
            }
        }

        if (currentSortOption == 0) {
            Collections.sort(tempFilteredList, (t1, t2) -> {
                String title1 = t1.getTaskTitle() != null ? t1.getTaskTitle() : "";
                String title2 = t2.getTaskTitle() != null ? t2.getTaskTitle() : "";
                return title1.compareToIgnoreCase(title2);
            });
        } else if (currentSortOption == 1) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Collections.sort(tempFilteredList, (t1, t2) -> {
                try {
                    Date d1 = sdf.parse(t1.getTaskDate() != null ? t1.getTaskDate() : "");
                    Date d2 = sdf.parse(t2.getTaskDate() != null ? t2.getTaskDate() : "");
                    if (d1 != null && d2 != null) {
                        return d1.compareTo(d2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            });
        }

        taskList.clear();
        taskList.addAll(tempFilteredList);

        updateTaskCounters();

        if (taskList.isEmpty()) {
            if (tvNoTasks != null) tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            if (tvNoTasks != null) tvNoTasks.setVisibility(View.GONE);
        }

        taskAdapter.notifyDataSetChanged();
    }

    private void updateTaskCounters() {
        int total = taskListFull.size();
        int completed = 0;

        for (Task task : taskListFull) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        int remaining = total - completed;

        if (tvTotalTasks != null) tvTotalTasks.setText("Total: " + total);
        if (tvCompletedTasks != null) tvCompletedTasks.setText("Done: " + completed);
        if (tvRemainingTasks != null) tvRemainingTasks.setText("Remain: " + remaining);
    }

    private void showSortDialog() {
        String[] options = {"Title (A-Z)", "Date (DD-MM-YYYY)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Tasks By");
        builder.setItems(options, (dialog, which) -> {
            currentSortOption = which;
            processFilterAndSort();
        });
        builder.show();
    }

    private void showFilterDialog() {
        String[] options = {"All", "High", "Medium", "Low"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Priority");
        builder.setItems(options, (dialog, which) -> {
            currentFilterPriority = options[which];
            processFilterAndSort();
        });
        builder.show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(TasksActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        Button navHome = findViewById(R.id.navHome);
        Button navTasks = findViewById(R.id.navTasks);
        Button navProfile = findViewById(R.id.navProfile);
        Button navLogout = findViewById(R.id.navLogout);

        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateToHome());
        }

        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {});
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(TasksActivity.this, ProfileActivity.class)));
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TasksActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tasksListener != null) {
            tasksListener.remove();
        }
    }
}