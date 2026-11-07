package com.kashfia.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private EditText etName, etFatherName, etMotherName, etHobby;
    private EditText etEmail, etPhone, etBirthday, etBloodGroup, etParentContact, etPresentAddress, etPermanentAddress, etBatch, etRoll, etDepartment, etUniversity;
    private Button btnSaveProfile;

    private static final String PREF_NAME = "StudyMateProfilePrefs";
    private Uri profileImageUri = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    if (profileImageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(profileImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ivProfileImage.setImageURI(profileImageUri);
                        saveImageUriToPrefs(profileImageUri.toString());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfileImage = findViewById(R.id.ivProfileImage);

        // New Fields
        etName = findViewById(R.id.etProfileName);
        etFatherName = findViewById(R.id.etProfileFatherName);
        etMotherName = findViewById(R.id.etProfileMotherName);
        etHobby = findViewById(R.id.etProfileHobby);

        // Existing Fields
        etEmail = findViewById(R.id.etProfileEmail);
        etPhone = findViewById(R.id.etProfilePhone);
        etBirthday = findViewById(R.id.etProfileBirthday);
        etBloodGroup = findViewById(R.id.etProfileBloodGroup);
        etParentContact = findViewById(R.id.etProfileParentContact);
        etPresentAddress = findViewById(R.id.etProfilePresentAddress);
        etPermanentAddress = findViewById(R.id.etProfilePermanentAddress);
        etBatch = findViewById(R.id.etProfileBatch);
        etRoll = findViewById(R.id.etProfileRoll);
        etDepartment = findViewById(R.id.etProfileDepartment);
        etUniversity = findViewById(R.id.etProfileUniversity);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadProfileData();

        ivProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                saveProfileData();
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            });
        }

        setupBottomNavigation();
    }

    private void saveProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("name", etName.getText().toString().trim());
        editor.putString("fatherName", etFatherName.getText().toString().trim());
        editor.putString("motherName", etMotherName.getText().toString().trim());
        editor.putString("hobby", etHobby.getText().toString().trim());

        editor.putString("email", etEmail.getText().toString().trim());
        editor.putString("phone", etPhone.getText().toString().trim());
        editor.putString("birthday", etBirthday.getText().toString().trim());
        editor.putString("blood", etBloodGroup.getText().toString().trim());
        editor.putString("parentContact", etParentContact.getText().toString().trim());
        editor.putString("presentAddress", etPresentAddress.getText().toString().trim());
        editor.putString("permanentAddress", etPermanentAddress.getText().toString().trim());
        editor.putString("batch", etBatch.getText().toString().trim());
        editor.putString("roll", etRoll.getText().toString().trim());
        editor.putString("department", etDepartment.getText().toString().trim());
        editor.putString("university", etUniversity.getText().toString().trim());
        editor.apply();
    }

    private void saveImageUriToPrefs(String uriStr) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString("imageUri", uriStr).apply();
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etName.setText(prefs.getString("name", ""));
        etFatherName.setText(prefs.getString("fatherName", ""));
        etMotherName.setText(prefs.getString("motherName", ""));
        etHobby.setText(prefs.getString("hobby", ""));

        etEmail.setText(prefs.getString("email", ""));
        etPhone.setText(prefs.getString("phone", ""));
        etBirthday.setText(prefs.getString("birthday", ""));
        etBloodGroup.setText(prefs.getString("blood", ""));
        etParentContact.setText(prefs.getString("parentContact", ""));
        etPresentAddress.setText(prefs.getString("presentAddress", ""));
        etPermanentAddress.setText(prefs.getString("permanentAddress", ""));
        etBatch.setText(prefs.getString("batch", ""));
        etRoll.setText(prefs.getString("roll", ""));
        etDepartment.setText(prefs.getString("department", ""));
        etUniversity.setText(prefs.getString("university", ""));

        String savedUriStr = prefs.getString("imageUri", "");
        if (!savedUriStr.isEmpty()) {
            try {
                profileImageUri = Uri.parse(savedUriStr);
                ivProfileImage.setImageURI(profileImageUri);
            } catch (Exception e) {
                e.printStackTrace();
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
            navProfile.setOnClickListener(v -> {
                // Already in Profile
            });
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