package com.kashfia.studymate;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteClickListener listener;

    // ক্লিক লিসেনার ইন্টারফেস
    public interface OnNoteClickListener {
        void onNoteClick(int position);
    }

    public NoteAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDate.setText(note.getDate());
        holder.tvContent.setText(note.getContent());

        if (note.getImageUri() != null && !note.getImageUri().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            try {
                holder.ivImage.setImageURI(Uri.parse(note.getImageUri()));
            } catch (Exception e) {
                e.printStackTrace();
                holder.ivImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        // নোট আইটেমে ক্লিক করলে এডিট/ডিলিট অপশন ট্রিগার হবে
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent;
        ImageView ivImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            ivImage = itemView.findViewById(R.id.ivNoteImage);
        }
    }
}