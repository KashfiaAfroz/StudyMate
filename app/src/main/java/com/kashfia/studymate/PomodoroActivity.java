package com.kashfia.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class PomodoroActivity extends AppCompatActivity {

    private TextView tvTimer;
    private Button btnStart, btnReset;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        // লেআউটের সঠিক আইডিগুলোর সাথে কানেক্ট করা হলো
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStartTimer);
        btnReset = findViewById(R.id.btnResetTimer);

        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                if (timerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            });
        }

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetTimer());
        }

        updateCountDownText();
        setupBottomNavigation();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btnStart.setText("Start");
            }
        }.start();

        timerRunning = true;
        btnStart.setText("Pause");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        btnStart.setText("Start");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        timeLeftInMillis = 25 * 60 * 1000;
        updateCountDownText();
        btnStart.setText("Start");
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        if (tvTimer != null) {
            tvTimer.setText(timeFormatted);
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