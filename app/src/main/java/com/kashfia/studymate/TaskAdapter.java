package com.kashfia.studymate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener onTaskClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.onTaskClickListener = listener;
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
        holder.tvTitle.setText(task.getTaskTitle());
        holder.tvDesc.setText(task.getTaskDescription());
        holder.tvDate.setText(task.getTaskDate());

        String priority = task.getPriority() != null ? task.getPriority() : "Medium";
        holder.tvPriority.setText(priority);

        // Color coding based on priority
        if (priority.equalsIgnoreCase("High")) {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#D32F2F")); // Red
        } else if (priority.equalsIgnoreCase("Medium")) {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#F57C00")); // Orange
        } else {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#388E3C")); // Green
        }

        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(task.isCompleted());

        holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);

            FirebaseFirestore.getInstance().collection("Tasks")
                    .document(task.getTaskId())
                    .update("completed", isChecked)
                    .addOnFailureListener(e -> {
                        Toast.makeText(buttonView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    });
        });

        holder.itemView.setOnClickListener(v -> {
            if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDate, tvPriority;
        CheckBox cbComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDesc = itemView.findViewById(R.id.tvTaskDesc);
            tvDate = itemView.findViewById(R.id.tvTaskDate);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            cbComplete = itemView.findViewById(R.id.cbTaskComplete);
        }
    }
}