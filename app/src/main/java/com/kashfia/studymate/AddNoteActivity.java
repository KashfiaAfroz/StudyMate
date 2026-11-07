package com.kashfia.studymate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etNoteTitle, etNoteContent;
    private Button btnSaveNote, btnCancelNote, btnAddNoteImage;
    private ImageView ivAddNoteImage;
    private Uri selectedImageUri = null;

    private static final String PREF_NAME = "StudyMateNotesPrefs";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ivAddNoteImage.setVisibility(View.VISIBLE);
                        ivAddNoteImage.setImageURI(selectedImageUri);
                        Toast.makeText(this, "Image attached!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnCancelNote = findViewById(R.id.btnCancelNote);
        btnAddNoteImage = findViewById(R.id.btnAddNoteImage);
        ivAddNoteImage = findViewById(R.id.ivAddNoteImage);

        if (btnAddNoteImage != null) {
            btnAddNoteImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        if (btnSaveNote != null) {
            btnSaveNote.setOnClickListener(v -> {
                String title = etNoteTitle.getText().toString().trim();
                String content = etNoteContent.getText().toString().trim();

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a note title", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
                String imageUriStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                int count = prefs.getInt("note_count", 0);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("note_title_" + count, title);
                editor.putString("note_content_" + count, content);
                editor.putString("note_date_" + count, currentDate);
                editor.putString("note_image_" + count, imageUriStr);
                editor.putInt("note_count", count + 1);
                editor.apply();

                Toast.makeText(this, "Note saved with image successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnCancelNote != null) {
            btnCancelNote.setOnClickListener(v -> finish());
        }
    }
}