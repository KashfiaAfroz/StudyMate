package com.kashfia.studymate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList;
    private OnExamClickListener listener;

    // ডিলিট করার জন্য ক্লিক লিসেনার ইন্টারফেস
    public interface OnExamClickListener {
        void onExamClick(int position);
    }

    public ExamAdapter(List<Exam> examList, OnExamClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.tvTitle.setText(exam.getTitle());
        holder.tvDate.setText("Date: " + exam.getDate());

        long daysLeft = calculateDaysLeft(exam.getDate());
        if (daysLeft > 0) {
            holder.tvDaysLeft.setText("⏳ " + daysLeft + " Days Left");
            holder.tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
        } else if (daysLeft == 0) {
            holder.tvDaysLeft.setText("🔥 Exam is Today!");
            holder.tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#E65100"));
        } else {
            holder.tvDaysLeft.setText("⚠️ Exam Date Passed");
            holder.tvDaysLeft.setTextColor(android.graphics.Color.parseColor("#757575"));
        }

        // আইটেমে ক্লিক করলে ডিলিট অপশন ট্রিগার হবে
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExamClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    private long calculateDaysLeft(String examDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date examDate = sdf.parse(examDateStr);
            Date currentDate = new Date();

            String currentDateStr = sdf.format(currentDate);
            currentDate = sdf.parse(currentDateStr);

            if (examDate != null && currentDate != null) {
                long diffInMillis = examDate.getTime() - currentDate.getTime();
                return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvDaysLeft;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExamTitle);
            tvDate = itemView.findViewById(R.id.tvExamDate);
            tvDaysLeft = itemView.findViewById(R.id.tvDaysLeft);
        }
    }
}