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
    private List<Task> taskList = new ArrayList<>();
    private DBHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUserId = getIntent().getIntExtra("user_id", -1);

        dbHelper = new DBHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showTaskDialog(null));

        loadTasks();
    }

    private void loadTasks() {
        taskList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM tasks WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});

        while (cursor.moveToNext()) {
            Task task = new Task(
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("description")),
                    cursor.getString(cursor.getColumnIndex("due_date"))
            );
            task.setId(cursor.getInt(cursor.getColumnIndex("id")));
            taskList.add(task);
        }
        cursor.close();
        dbHelper.debugDatabaseStructure();

        adapter = new TaskAdapter(taskList, this::onTaskAction);
        recyclerView.setAdapter(adapter);
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
        loadTasks();
    }

    private void onTaskAction(int position, String action) {
        Task task = taskList.get(position);
        if ("delete".equals(action)) {
            dbHelper.getWritableDatabase()
                    .delete("tasks", "id=?", new String[]{String.valueOf(task.getId())});
            loadTasks();
        } else if ("edit".equals(action)) {
            showTaskDialog(task);
        }
    }
}