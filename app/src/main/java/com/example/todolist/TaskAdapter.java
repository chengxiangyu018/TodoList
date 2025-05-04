package com.example.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    interface OnTaskActionListener {
        void onAction(int position, String action);
    }

    private List<Task> tasks;
    private OnTaskActionListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDesc.setText(task.getDescription());
        holder.tvDueDate.setText(task.getFormattedDueDate());

        holder.btnDelete.setOnClickListener(v ->
                listener.onAction(position, "delete"));
        holder.itemView.setOnClickListener(v ->
                listener.onAction(position, "edit"));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDueDate;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
