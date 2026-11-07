package com.kashfia.studymate;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskTitle.setText(task.getTaskTitle() != null ? task.getTaskTitle() : "");
        holder.tvTaskDesc.setText(task.getTaskDescription() != null ? task.getTaskDescription() : "");
        holder.tvTaskDate.setText("Date: " + (task.getTaskDate() != null ? task.getTaskDate() : ""));

        // প্রায়োরিটি কালার কোডিং
        String priority = task.getPriority() != null ? task.getPriority() : "Medium";
        holder.tvTaskPriority.setText(priority);

        if (priority.equalsIgnoreCase("High")) {
            holder.tvTaskPriority.setTextColor(Color.parseColor("#B71C1C"));
            holder.tvTaskPriority.setBackgroundColor(Color.parseColor("#FFCDD2"));
        } else if (priority.equalsIgnoreCase("Medium")) {
            holder.tvTaskPriority.setTextColor(Color.parseColor("#E65100"));
            holder.tvTaskPriority.setBackgroundColor(Color.parseColor("#FFE0B2"));
        } else {
            holder.tvTaskPriority.setTextColor(Color.parseColor("#1B5E20"));
            holder.tvTaskPriority.setBackgroundColor(Color.parseColor("#C8E6C9"));
        }

        // আজকের ডেট হলে রেড অ্যালার্ট একদম ওপরে বোল্ড ও উজ্জ্বলভাবে শো করবে
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        if (task.getTaskDate() != null && task.getTaskDate().equals(currentDate)) {
            holder.tvRedAlert.setVisibility(View.VISIBLE);
        } else {
            holder.tvRedAlert.setVisibility(View.GONE);
        }

        holder.cbTaskComplete.setOnCheckedChangeListener(null);
        holder.cbTaskComplete.setChecked(task.isCompleted());

        holder.cbTaskComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            if (task.getTaskId() != null && !task.getTaskId().isEmpty()) {
                FirebaseFirestore.getInstance().collection("tasks")
                        .document(task.getTaskId())
                        .update("completed", isChecked);
            }
        });

        // টাস্ক কার্ডে ক্লিক করলে এডিট এবং ডিলিট অপশন পপ-আপ হয়ে আসবে
        holder.cardTaskItem.setOnClickListener(v -> {
            AlertDialog.Builder optionsDialog = new AlertDialog.Builder(context);
            optionsDialog.setTitle("Task Options");
            String[] options = {"Edit Task", "Delete Task"};

            optionsDialog.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showEditDialog(task);
                } else if (which == 1) {
                    deleteTask(task);
                }
            });
            optionsDialog.show();
        });
    }

    private void showEditDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task Details");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(context);
        etTitle.setHint("Task Title");
        etTitle.setText(task.getTaskTitle());
        layout.addView(etTitle);

        final EditText etDesc = new EditText(context);
        etDesc.setHint("Detailed Description & Topics");
        etDesc.setText(task.getTaskDescription());
        etDesc.setMinLines(4);
        etDesc.setGravity(android.view.Gravity.TOP);
        layout.addView(etDesc);

        final EditText etDate = new EditText(context);
        etDate.setHint("Date (DD-MM-YYYY)");
        etDate.setText(task.getTaskDate());
        layout.addView(etDate);

        // প্রায়োরিটি পরিবর্তন করার জন্য স্পিনার (Dropdown) যুক্ত করা হলো
        TextView tvPriorityLabel = new TextView(context);
        tvPriorityLabel.setText("Select Priority:");
        tvPriorityLabel.setPadding(0, 15, 0, 5);
        layout.addView(tvPriorityLabel);

        final Spinner spinnerPriority = new Spinner(context);
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(adapter);

        // আগের প্রায়োরিটি সিলেক্টেড দেখানোর জন্য
        if (task.getPriority() != null) {
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equalsIgnoreCase(task.getPriority())) {
                    spinnerPriority.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerPriority);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDesc = etDesc.getText().toString().trim();
            String newDate = etDate.getText().toString().trim();
            String newPriority = spinnerPriority.getSelectedItem().toString();

            if (newTitle.isEmpty()) {
                Toast.makeText(context, "Title cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task.getTaskId() != null && !task.getTaskId().isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("taskTitle", newTitle);
                map.put("taskDescription", newDesc);
                map.put("taskDate", newDate);
                map.put("priority", newPriority); // ফায়ারস্টোরে প্রায়োরিটি আপডেট করা হচ্ছে

                FirebaseFirestore.getInstance().collection("tasks")
                        .document(task.getTaskId())
                        .update(map)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Task Updated Successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteTask(Task task) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (task.getTaskId() != null && !task.getTaskId().isEmpty()) {
                        FirebaseFirestore.getInstance().collection("tasks")
                                .document(task.getTaskId())
                                .delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Task Deleted Successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskDesc, tvTaskDate, tvTaskPriority, tvRedAlert;
        CheckBox cbTaskComplete;
        CardView cardTaskItem;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDesc = itemView.findViewById(R.id.tvTaskDesc);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            tvTaskPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvRedAlert = itemView.findViewById(R.id.tvRedAlert);
            cbTaskComplete = itemView.findViewById(R.id.cbTaskComplete);
            cardTaskItem = itemView.findViewById(R.id.cardTaskItem);
        }
    }
}