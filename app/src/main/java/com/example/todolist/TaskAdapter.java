package com.example.todolist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    interface OnTaskActionListener {
        void onAction(int position, String action);
    }

    private List<Task> tasks;
    private OnTaskActionListener listener;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TaskAdapter(List<Task> tasks, OnTaskActionListener listener, Context context) {
        this.tasks = tasks;
        this.listener = listener;
        this.context = context;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
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
        holder.tvDueDate.setText(formatDueDate(task.getDueDate()));

        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            holder.tvCategory.setText(task.getCategory());
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        if (isOverdue(task) && !task.isCompleted()) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.alert_red));
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.alert_red));
            holder.itemView.setBackgroundResource(R.drawable.bg_overdue_task);
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
            holder.itemView.setBackgroundResource(R.drawable.bg_normal_task);
        }

        holder.btnDelete.setOnClickListener(v ->
                listener.onAction(position, "delete"));
        holder.itemView.setOnClickListener(v ->
                listener.onAction(position, "edit"));
    }

    private boolean isOverdue(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isEmpty()) return false;

        try {
            Date dueDate = dateFormat.parse(task.getDueDate());
            return dueDate != null && dueDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private String formatDueDate(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) return "No deadline";

        try {
            Date date = dateFormat.parse(dueDate);
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return dueDate;
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDueDate, tvCategory;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvCategory = itemView.findViewById(R.id.tv_category); // 新增的TextView
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}