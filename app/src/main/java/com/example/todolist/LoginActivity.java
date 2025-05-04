package com.example.todolist;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);

        // 登录验证
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateLogin(username, password)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // 跳转注册
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private boolean validateLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        String[] selectionArgs = {username, password};

        try (android.database.Cursor cursor = db.rawQuery(query, selectionArgs)) {
            return cursor.getCount() > 0;
        }
    }
}
