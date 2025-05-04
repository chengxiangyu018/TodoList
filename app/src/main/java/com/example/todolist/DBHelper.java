package com.example.todolist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

    private static final String CREATE_TASKS_TABLE =
            "CREATE TABLE tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "due_date TEXT," +
                    "is_completed INTEGER DEFAULT 0," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES users(id))";

    private static final String CREATE_CATEGORIES_TABLE =
            "CREATE TABLE categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE NOT NULL," +
                    "color TEXT DEFAULT '#6200EE')";

    private static final String CREATE_TASK_CATEGORY_JUNCTION =
            "CREATE TABLE task_category (" +
                    "task_id INTEGER," +
                    "category_id INTEGER," +
                    "PRIMARY KEY (task_id, category_id)," +
                    "FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE CASCADE," +
                    "FOREIGN KEY(category_id) REFERENCES categories(id))";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_TASKS_TABLE);
            db.execSQL(CREATE_CATEGORIES_TABLE);
            db.execSQL(CREATE_TASK_CATEGORY_JUNCTION);
            db.execSQL("CREATE INDEX idx_due_date ON tasks(due_date)");
            Log.d("DB", "create success");
        } catch (SQLiteException e) {
            Log.e("DB", "Create failed", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            onCreate(db);
        }
    }

    public void debugDatabaseStructure() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor tableCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'", null);

        Log.d("DB_DEBUG", "===== database structure =====");
        while (tableCursor.moveToNext()) {
            String tableName = tableCursor.getString(0);
            Log.d("DB_DEBUG", "table_title: " + tableName);

            printTableContent(db, tableName);
        }
        tableCursor.close();
    }

    private static void printTableContent(SQLiteDatabase db, String tableName) {
        Log.d("DB_CONTENT", "═════════ table: " + tableName + " ═════════");

        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
        String[] columns = cursor.getColumnNames();
        cursor.close();

        StringBuilder header = new StringBuilder();
        for (String col : columns) {
            header.append(String.format("| %-15s ", col));
        }
        Log.d("DB_CONTENT", header.toString());
        Log.d("DB_CONTENT", "|-----------------".repeat(columns.length));

        cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        while (cursor.moveToNext()) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < columns.length; i++) {
                String value;
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL:
                        value = "NULL";
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        value = "[BLOB]";
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        value = String.format("%.2f", cursor.getDouble(i));
                        break;
                    default:
                        value = cursor.getString(i);
                }
                row.append(String.format("| %-15s ", value));
            }
            Log.d("DB_CONTENT", row.toString());
        }
        cursor.close();
        Log.d("DB_CONTENT", "\n");
    }
}
