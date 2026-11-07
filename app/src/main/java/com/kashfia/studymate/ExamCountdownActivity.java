package com.kashfia.studymate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ExamCountdownActivity extends AppCompatActivity {

    private EditText etExamName, etExamDate;
    private Button btnSaveExam;
    private RecyclerView recyclerViewExam;
    private ExamAdapter examAdapter;
    private List<Exam> examList;

    private static final String PREF_NAME = "StudyMateExamListPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_countdown);

        etExamName = findViewById(R.id.etExamName);
        etExamDate = findViewById(R.id.etExamDate);
        btnSaveExam = findViewById(R.id.btnSaveExam);
        recyclerViewExam = findViewById(R.id.recyclerViewExam);

        if (recyclerViewExam != null) {
            recyclerViewExam.setLayoutManager(new LinearLayoutManager(this));
        }
        examList = new ArrayList<>();

        loadExams();

        // অ্যাডাপ্টারে ক্লিক লিসেনার যুক্ত করা হলো (আইটেমে ক্লিক করলে ডিলিট ডায়ালগ আসবে)
        examAdapter = new ExamAdapter(examList, position -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Exam Countdown")
                    .setMessage("Are you sure you want to delete this exam?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        examList.remove(position);
                        saveExams();
                        examAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Exam deleted successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        if (recyclerViewExam != null) {
            recyclerViewExam.setAdapter(examAdapter);
        }

        if (btnSaveExam != null) {
            btnSaveExam.setOnClickListener(v -> {
                String title = etExamName.getText().toString().trim();
                String date = etExamDate.getText().toString().trim();

                if (title.isEmpty() || date.isEmpty()) {
                    Toast.makeText(this, "Please enter both exam name and date", Toast.LENGTH_SHORT).show();
                    return;
                }

                examList.add(new Exam(title, date));
                saveExams();
                examAdapter.notifyDataSetChanged();

                etExamName.setText("");
                etExamDate.setText("");
                Toast.makeText(this, "Exam added to list successfully!", Toast.LENGTH_SHORT).show();
            });
        }

        setupBottomNavigation();
    }

    private void saveExams() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // আগের সব ডেটা ক্লিয়ার করে নতুন লিস্ট সেভ করা হচ্ছে যাতে ডিলিট করা আইটেম আর না থাকে
        editor.clear();
        editor.putInt("exam_count", examList.size());

        for (int i = 0; i < examList.size(); i++) {
            editor.putString("exam_title_" + i, examList.get(i).getTitle());
            editor.putString("exam_date_" + i, examList.get(i).getDate());
        }
        editor.apply();
    }

    private void loadExams() {
        examList.clear();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int count = prefs.getInt("exam_count", 0);

        for (int i = 0; i < count; i++) {
            String title = prefs.getString("exam_title_" + i, "");
            String date = prefs.getString("exam_date_" + i, "");
            if (!title.isEmpty()) {
                examList.add(new Exam(title, date));
            }
        }
    }

    private void setupBottomNavigation() {
        Button navHome = findViewById(R.id.navHome);
        Button navTasks = findViewById(R.id.navTasks);
        Button navProfile = findViewById(R.id.navProfile);
        Button navLogout = findViewById(R.id.navLogout);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
        if (navTasks != null) {
            navTasks.setOnClickListener(v -> startActivity(new Intent(this, TasksActivity.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}