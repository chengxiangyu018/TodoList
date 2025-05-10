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

//        this.deleteDatabase("todo.db");

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

//        builder.setView(view)
//                .setTitle(existingTask == null ? "create task" : "edit task")
//                .setPositiveButton("save", (dialog, which) -> {
//                    String dueDate = tvDate.getText().toString().replace("due: ", "");
//                    Task task = existingTask == null ?
//                            new Task(etTitle.getText().toString(), etDesc.getText().toString(), dueDate) :
//                            existingTask;
//
//                    saveTask(task);
//                })
//                .setNegativeButton("cancel", null)
//                .show();

        Spinner categorySpinner = view.findViewById(R.id.spinner_category);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getCategoryNames());
        categorySpinner.setAdapter(categoryAdapter);

        if (existingTask != null && existingTask.getCategory() != null) {
            int position = categoryAdapter.getPosition(existingTask.getCategory());
            categorySpinner.setSelection(Math.max(position, 0));
        }

        builder.setView(view)
                .setTitle(existingTask == null ? "create task" : "edit task")
                .setPositiveButton("save", (dialog, which) -> {
                    String dueDate = tvDate.getText().toString().replace("due date: ", "");
                    String selectedCategory = categorySpinner.getSelectedItem().toString();

                    Task task = existingTask == null ?
                            new Task(
                                    etTitle.getText().toString(),
                                    etDesc.getText().toString(),
                                    dueDate,
                                    selectedCategory,
                                    0
                            ) :
                            existingTask;

                    task.setCategory(selectedCategory);
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

        try {
            db.beginTransaction();

            long taskId;
            if (task.getId() == 0) {
                taskId = db.insert("tasks", null, values);
                task.setId((int) taskId);
            } else {
                db.update("tasks", values, "id=?", new String[]{String.valueOf(task.getId())});
                taskId = task.getId();
            }

            updateTaskCategory(db, taskId, task.getCategory());

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        loadTasksWithFilter();
    }


    private void updateTaskCategory(SQLiteDatabase db, long taskId, String categoryName) {
        db.delete("task_category", "task_id=?", new String[]{String.valueOf(taskId)});

        if (categoryName != null && !categoryName.equals("No Category")) {
            Cursor cursor = db.rawQuery(
                    "SELECT id FROM categories WHERE name=?",
                    new String[]{categoryName});

            if (cursor.moveToFirst()) {
                int categoryId = cursor.getInt(0);
                ContentValues cv = new ContentValues();
                cv.put("task_id", taskId);
                cv.put("category_id", categoryId);
                db.insert("task_category", null, cv);
            }
            cursor.close();
        }
    }

    private void onTaskAction(int position, String action) {
        Task task = adapter.getTaskAt(position);

        if ("delete".equals(action)) {
            dbHelper.getWritableDatabase()
                    .delete("tasks", "id=?", new String[]{String.valueOf(task.getId())});
            loadTasksWithFilter();
        } else if ("edit".equals(action)) {
            showTaskDialog(task);
        }
    }

    private void setupSearchUI() {
        categorySpinner = findViewById(R.id.spinner_category);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getCategoryNames());
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

        dbHelper.debugDatabaseStructure();
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

    private List<String> getCategoryNames() {
        List<String> categories = new ArrayList<>();
        categories.add("No Category");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories", null);

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0));
        }
        cursor.close();

        return categories;
    }
}



