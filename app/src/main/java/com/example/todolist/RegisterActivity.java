package com.example.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNewUsername, etNewPassword, etConfirmPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);
        etNewUsername = findViewById(R.id.et_new_username);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        Button btnRegister = findViewById(R.id.btn_complete_register);

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = etNewUsername.getText().toString().trim();
        String password = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Username and password cannot be empty");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (isUsernameExists(username)) {
            etNewUsername.setError("Username already taken");
            return;
        }

        if (registerUser(username, password)) {
            startMainActivity(username);
        }
    }

    private boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT username FROM users WHERE username = ?",
                new String[]{username}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private boolean registerUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        try {
            long result = db.insertOrThrow("users", null, values);
            if (result != -1) {
                showToast("Registration successful");
                return true;
            }
        } catch (SQLException e) {
            Log.e("REGISTER", "Database error", e);
        }
        showToast("Registration failed");
        return false;
    }

    private void startMainActivity(String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
