package com.kashfia.studymate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotes;
    private Button btnAddNote;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private static final String PREF_NAME = "StudyMateNotesPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        btnAddNote = findViewById(R.id.btnAddNote);

        if (recyclerViewNotes != null) {
            recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        }
        noteList = new ArrayList<>();

        loadNotes();

        // নোট আইটেমে ক্লিক করলে এডিট বা ডিলিট করার অপশন ডায়ালগ আসবে
        noteAdapter = new NoteAdapter(noteList, position -> {
            Note note = noteList.get(position);

            CharSequence[] options = {"Edit Note", "Delete Note"};
            new AlertDialog.Builder(this)
                    .setTitle("Note Options")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showEditNoteDialog(position, note);
                        } else if (which == 1) {
                            deleteNote(position);
                        }
                    })
                    .show();
        });

        if (recyclerViewNotes != null) {
            recyclerViewNotes.setAdapter(noteAdapter);
        }

        if (btnAddNote != null) {
            btnAddNote.setOnClickListener(v -> {
                startActivity(new Intent(NotesActivity.this, AddNoteActivity.class));
            });
        }

        setupBottomNavigation();
    }

    private void showEditNoteDialog(int position, Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Note");

        // সাধারণ নোট অ্যাপের মতো ফ্লেক্সিবল ও স্ক্রোলযোগ্য লেআউট তৈরি
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("Note Title");
        etTitle.setText(note.getTitle());
        etTitle.setTextSize(16f);
        layout.addView(etTitle);

        final EditText etContent = new EditText(this);
        etContent.setHint("Write your detailed notes here...");
        etContent.setText(note.getContent());
        etContent.setTextSize(15f);
        // ডায়নামিক ও ফ্লেক্সিবল করার জন্য মিনিমাম উচ্চতা এবং মাল্টিলাইন সেট করা হলো
        etContent.setMinLines(8);
        etContent.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        layout.addView(etContent);

        scrollView.addView(layout);
        builder.setView(scrollView);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newContent = etContent.getText().toString().trim();

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // নোট অবজেক্ট আপডেট করা
            note.setTitle(newTitle);
            note.setContent(newContent);

            saveNotes();
            noteAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Note Updated Successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteNote(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    noteList.remove(position);
                    saveNotes();
                    noteAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Note Deleted Successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        }
    }

    private void loadNotes() {
        noteList.clear();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int count = prefs.getInt("note_count", 0);

        for (int i = 0; i < count; i++) {
            String title = prefs.getString("note_title_" + i, "");
            String content = prefs.getString("note_content_" + i, "");
            String date = prefs.getString("note_date_" + i, "");
            String imageUri = prefs.getString("note_image_" + i, "");
            if (!title.isEmpty()) {
                noteList.add(new Note(title, content, date, imageUri));
            }
        }
    }

    private void saveNotes() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // আগের সব ডেটা মুছে ফ্রেশ সেভ করা হচ্ছে
        editor.putInt("note_count", noteList.size());

        for (int i = 0; i < noteList.size(); i++) {
            editor.putString("note_title_" + i, noteList.get(i).getTitle());
            editor.putString("note_content_" + i, noteList.get(i).getContent());
            editor.putString("note_date_" + i, noteList.get(i).getDate());
            editor.putString("note_image_" + i, noteList.get(i).getImageUri());
        }
        editor.apply();
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