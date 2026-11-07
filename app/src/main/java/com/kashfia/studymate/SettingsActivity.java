package com.kashfia.studymate;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity implements SensorEventListener {

    private Button btnShakeSensor, btnOpenBrowser, btnSendEmail, btnBackHome;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private long lastShakeTime = 0; // বারবার ওপেন হওয়া রোধ করতে
    private static final int SHAKE_THRESHOLD = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnShakeSensor = findViewById(R.id.btnShakeSensor);
        btnOpenBrowser = findViewById(R.id.btnOpenBrowser);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnBackHome = findViewById(R.id.btnBackHome);

        // Sensor Manager Initializing
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Shake Sensor Test Button
        if (btnShakeSensor != null) {
            btnShakeSensor.setOnClickListener(v ->
                    Toast.makeText(this, "Shake your phone to open Task List automatically!", Toast.LENGTH_SHORT).show()
            );
        }

        // Open Browser Implicit Intent
        if (btnOpenBrowser != null) {
            btnOpenBrowser.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
                startActivity(intent);
            });
        }

        // Send Email Implicit Intent
        if (btnSendEmail != null) {
            btnSendEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:support@studymate.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "StudyMate Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Hello, I am using StudyMate app.");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Back to Home Button
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> finish());
        }

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    // দুই সেকেন্ডের মধ্যে একবারই কাজ করবে যাতে বারবার পেজ ওপেন না হয়
                    if ((curTime - lastShakeTime) > 2000) {
                        lastShakeTime = curTime;
                        Toast.makeText(this, "Phone Shake Detected! Opening Tasks...", Toast.LENGTH_SHORT).show();

                        // শেইক করলে সরাসরি টাস্ক লিস্ট পেজে চলে যাবে
                        Intent intent = new Intent(SettingsActivity.this, TasksActivity.class);
                        startActivity(intent);
                    }
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
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