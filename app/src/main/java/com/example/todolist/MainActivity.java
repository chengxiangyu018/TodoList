package com.example.todolist;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DBHelper dbHelper;
    private int currentUserId;

    private Spinner categorySpinner;
    private CheckBox cbShowOverdue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = getIntent().getIntExtra("user_id", -1);
        dbHelper = new DBHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>(), this::onTaskAction, this); // 初始化空适配器
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showTaskDialog(null));
        setupSearchUI();

        loadTasksWithFilter();
    }

    private void showTaskDialog(Task existingTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_task, null);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDesc = view.findViewById(R.id.et_desc);
        TextView tvDate = view.findViewById(R.id.tv_date);
        Button btnDate = view.findViewById(R.id.btn_pick_date);

        if (existingTask != null) {
            etTitle.setText(existingTask.getTitle());
            etDesc.setText(existingTask.getDescription());
            tvDate.setText(existingTask.getFormattedDueDate());
        }

        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view1, year, month, day) -> {
                String dateStr = String.format("%d-%02d-%02d", year, month + 1, day);
                tvDate.setText("due date: " + dateStr);
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view)
                .setTitle(existingTask == null ? "create task" : "edit task")
                .setPositiveButton("save", (dialog, which) -> {
                    String dueDate = tvDate.getText().toString().replace("due: ", "");
                    Task task = existingTask == null ?
                            new Task(etTitle.getText().toString(), etDesc.getText().toString(), dueDate) :
                            existingTask;

                    saveTask(task);
                })
                .setNegativeButton("cancel", null)
                .show();
    }

    private void saveTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", currentUserId);
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("due_date", task.getDueDate());

        if (task.getId() == 0) {
            db.insert("tasks", null, values);
        } else {
            db.update("tasks", values, "id=?", new String[]{String.valueOf(task.getId())});
        }
        loadTasksWithFilter();
    }

    private void onTaskAction(int position, String action) {
        Task task = adapter.getTaskAt(position);

        if ("delete".equals(action)) {
            dbHelper.getWritableDatabase()
                    .delete("tasks", "id=?", new String[]{String.valueOf(task.getId())});
            loadTasksWithFilter(); // 重新加载数据保持同步
        } else if ("edit".equals(action)) {
            showTaskDialog(task);
        }
    }

    private void setupSearchUI() {
        categorySpinner = findViewById(R.id.spinner_category);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getCategories());
        categorySpinner.setAdapter(categoryAdapter);

        cbShowOverdue = findViewById(R.id.cb_overdue);
        cbShowOverdue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadTasksWithFilter();
        });

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            loadTasksWithFilter();
        });
    }

    private List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        return categories;
    }

    private void loadTasksWithFilter() {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        if (selectedCategory.equals("All")) selectedCategory = null;

        Cursor cursor = dbHelper.getTasksByCategoryWithSort(
                selectedCategory,
                currentUserId,
                cbShowOverdue.isChecked()
        );

        List<Task> filteredTasks = new ArrayList<>();
        while (cursor.moveToNext()) {
            Task task = cursorToTask(cursor);
            filteredTasks.add(task);
        }
        cursor.close();

        adapter.updateTasks(filteredTasks);
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task(
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
        );

        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        task.setCompleted(cursor.getInt(cursor.getColumnIndex("is_completed")) == 1);

        int categoryIndex = cursor.getColumnIndex("category_name");
        if (categoryIndex != -1) {
            task.setCategory(cursor.getString(categoryIndex));
        }

        int priorityIndex = cursor.getColumnIndex("priority");
        if (priorityIndex != -1) {
            task.setPriority(cursor.getInt(priorityIndex));
        }

        return task;
    }
}