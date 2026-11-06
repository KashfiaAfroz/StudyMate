package com.kashfia.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileEmail;
    private Button btnBackToHome, btnGallery, btnCamera, btnOpenBrowser, btnOpenEmail;
    private ImageView ivProfilePic;
    private FirebaseAuth mAuth;

    private static final int REQUEST_IMAGE_GALLERY = 101;
    private static final int REQUEST_IMAGE_CAMERA = 102;
    private static final String PREF_NAME = "StudyMatePrefs";
    private static final String KEY_PROFILE_IMAGE = "profile_image_base64";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnOpenBrowser = findViewById(R.id.btnOpenBrowser);
        btnOpenEmail = findViewById(R.id.btnOpenEmail);
        ivProfilePic = findViewById(R.id.ivProfilePic);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.getEmail() != null) {
            tvProfileEmail.setText("Email: " + currentUser.getEmail());
        }

        // Load saved profile image from SharedPreferences on start
        loadSavedImage();

        // Gallery Intent Button
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        });

        // Camera Intent Button
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Implicit Intent: Open Browser
        btnOpenBrowser.setOnClickListener(v -> {
            Uri webpage = Uri.parse("https://diu.ac"); // Dhaka International University website
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
        });

        // Implicit Intent: Open Email App
        btnOpenEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@studymate.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "StudyMate Support Query");
            intent.putExtra(Intent.EXTRA_TEXT, "Hello StudyMate Team, \n\n");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Back to Home Intent
        btnBackToHome.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                try {
                    Uri imageUri = data.getData();
                    ivProfilePic.setImageURI(imageUri);

                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    saveImageToPreferences(bitmap);

                    Toast.makeText(this, "Profile picture saved successfully!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivProfilePic.setImageBitmap(imageBitmap);

                    saveImageToPreferences(imageBitmap);

                    Toast.makeText(this, "Camera photo saved successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveImageToPreferences(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_PROFILE_IMAGE, encodedImage);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedImage() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String encodedImage = prefs.getString(KEY_PROFILE_IMAGE, null);
            if (encodedImage != null) {
                byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivProfilePic.setImageBitmap(decodedBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}