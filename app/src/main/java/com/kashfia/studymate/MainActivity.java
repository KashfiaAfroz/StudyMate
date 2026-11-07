package com.kashfia.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnManageTasks, btnPomodoro, btnExamCountdown, btnNotes, btnProfile, btnSettings, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // 1. লেআউটের আইডিগুলোর সাথে কানেক্ট করা হলো
        btnManageTasks = findViewById(R.id.btnManageTasks);
        btnPomodoro = findViewById(R.id.btnPomodoro);
        btnExamCountdown = findViewById(R.id.btnExamCountdown);
        btnNotes = findViewById(R.id.btnNotes);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

        // Task Page Click
        if (btnManageTasks != null) {
            btnManageTasks.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, TasksActivity.class));
            });
        }

        // Pomodoro Page Click
        if (btnPomodoro != null) {
            btnPomodoro.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PomodoroActivity.class));
            });
        }

        // Exam Countdown Page Click
        if (btnExamCountdown != null) {
            btnExamCountdown.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ExamCountdownActivity.class));
            });
        }

        // Notes Page Click
        if (btnNotes != null) {
            btnNotes.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, NotesActivity.class));
            });
        }

        // Profile Page Click
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            });
        }

        // Settings & Sensors Page Click
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }

        // Logout Click
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // নিচের পার্মানেন্ট ফুটার ন্যাভিগেশন কল করা হলো
        setupBottomNavigation();
    }

    // ==========================================================
    // পার্মানেন্ট ফুটার ন্যাভিগেশন লজিক (Home, Tasks, Profile, Logout)
    // ==========================================================
    private void setupBottomNavigation() {
        Button navHome = findViewById(R.id.navHome);
        Button navTasks = findViewById(R.id.navTasks);
        Button navProfile = findViewById(R.id.navProfile);
        Button navLogout = findViewById(R.id.navLogout);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // যেহেতু এটি অলরেডি হোম পেজ, তাই নতুন করে রিফ্রেশ বা কিছু করার প্রয়োজন নেই
            });
        }

        if (navTasks != null) {
            navTasks.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, TasksActivity.class));
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            });
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}