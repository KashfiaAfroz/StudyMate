package com.kashfia.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState, tvTotalTasksCount, tvCompletedTasksCount, tvTaskAlert;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> taskListFull;
    private FirebaseFirestore db;
    private Button btnLogout, btnSortTasks, btnFilterPriority;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTask;
    private EditText etSearchTask;

    // Sensor Variables for Shake Detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float accelerationValue;
    private float currentAcceleration;
    private float lastAcceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTotalTasksCount = findViewById(R.id.tvTotalTasksCount);
        tvCompletedTasksCount = findViewById(R.id.tvCompletedTasksCount);
        tvTaskAlert = findViewById(R.id.tvTaskAlert);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnLogout = findViewById(R.id.btnLogout);
        btnSortTasks = findViewById(R.id.btnSortTasks);
        btnFilterPriority = findViewById(R.id.btnFilterPriority);
        fabAddTask = findViewById(R.id.fabAddTask);
        etSearchTask = findViewById(R.id.etSearchTask);

        // Initialize Sensor Manager for Shake Feature
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        accelerationValue = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_tasks) {
                Intent intent = new Intent(MainActivity.this, TasksActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                SharedPreferences sharedPreferences = getSharedPreferences("StudyMatePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.remove("userEmail");
                editor.apply();

                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        btnSortTasks.setOnClickListener(v -> showSortDialog());
        btnFilterPriority.setOnClickListener(v -> showFilterDialog());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskListFull = new ArrayList<>();

        taskAdapter = new TaskAdapter(taskList, task -> {
            Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
            intent.putExtra("taskId", task.getTaskId());
            intent.putExtra("taskTitle", task.getTaskTitle());
            intent.putExtra("taskDesc", task.getTaskDescription());
            intent.putExtra("taskDate", task.getTaskDate());
            intent.putExtra("taskPriority", task.getPriority());
            startActivity(intent);
        });

        recyclerView.setAdapter(taskAdapter);
        db = FirebaseFirestore.getInstance();

        etSearchTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadTasksFromFirestore();
    }

    private void loadTasksFromFirestore() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        db.collection("Tasks")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    taskListFull.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            taskList.add(task);
                            taskListFull.add(task);
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                    updateStatistics();
                    checkDueTaskAlerts();
                    checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterTasks(String text) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : taskListFull) {
            if (task.getTaskTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(task);
            }
        }
        taskList.clear();
        taskList.addAll(filteredList);
        taskAdapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void showSortDialog() {
        String[] options = {"Sort by Title (A-Z)", "Sort by Date (Chronological)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Tasks");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Collections.sort(taskList, (t1, t2) -> t1.getTaskTitle().compareToIgnoreCase(t2.getTaskTitle()));
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Sorted by Title", Toast.LENGTH_SHORT).show();
            } else if (which == 1) {
                Collections.sort(taskList, (t1, t2) -> {
                    String d1 = t1.getTaskDate() != null ? t1.getTaskDate().trim() : "";
                    String d2 = t2.getTaskDate() != null ? t2.getTaskDate().trim() : "";

                    try {
                        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        SimpleDateFormat formatAlt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        Date date1 = null, date2 = null;
                        try { date1 = format1.parse(d1); } catch (Exception e1) { date1 = formatAlt.parse(d1); }
                        try { date2 = format1.parse(d2); } catch (Exception e2) { date2 = formatAlt.parse(d2); }

                        if (date1 != null && date2 != null) {
                            return date1.compareTo(date2);
                        }
                    } catch (Exception ignored) {}

                    return d1.compareToIgnoreCase(d2);
                });
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Sorted by Date", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showFilterDialog() {
        String[] options = {"All", "High Priority", "Medium Priority", "Low Priority"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Priority");
        builder.setItems(options, (dialog, which) -> {
            taskList.clear();
            if (which == 0) {
                taskList.addAll(taskListFull);
                Toast.makeText(this, "Showing All Tasks", Toast.LENGTH_SHORT).show();
            } else {
                String selectedPriority = options[which].replace(" Priority", "");
                for (Task task : taskListFull) {
                    if (task.getPriority() != null && task.getPriority().equalsIgnoreCase(selectedPriority)) {
                        taskList.add(task);
                    }
                }
                Toast.makeText(this, "Filtered by " + selectedPriority, Toast.LENGTH_SHORT).show();
            }
            taskAdapter.notifyDataSetChanged();
            checkEmptyState();
        });
        builder.show();
    }

    private void updateStatistics() {
        int total = taskListFull.size();
        int completed = 0;
        for (Task task : taskListFull) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        tvTotalTasksCount.setText("Total: " + total);
        tvCompletedTasksCount.setText("Completed: " + completed);
    }

    private void checkDueTaskAlerts() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        SimpleDateFormat sdfAlt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentAltDate = sdfAlt.format(new Date());

        int dueTodayCount = 0;

        for (Task task : taskListFull) {
            if (!task.isCompleted() && task.getTaskDate() != null) {
                String tDate = task.getTaskDate().trim();
                if (tDate.equals(currentDate) || tDate.equals(currentAltDate) || tDate.contains("2026-11-06")) {
                    dueTodayCount++;
                }
            }
        }

        if (dueTodayCount > 0) {
            tvTaskAlert.setVisibility(View.VISIBLE);
            tvTaskAlert.setText("⚠️ Alert: You have " + dueTodayCount + " task(s) due today!");
        } else {
            tvTaskAlert.setVisibility(View.GONE);
        }
    }

    private void checkEmptyState() {
        if (taskList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Sensor Event Listeners for Shake Detection
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = currentAcceleration - lastAcceleration;
            accelerationValue = accelerationValue * 0.9f + delta;

            if (accelerationValue > 12) {
                Toast.makeText(this, "Device Shaken! Refreshing Tasks...", Toast.LENGTH_SHORT).show();
                loadTasksFromFirestore();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        loadTasksFromFirestore();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}