package com.cloudycat.cloudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    public static String FirstName, LastName, ID, Email, Phone; // Переменные для хранения данных пользователя

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton buttonToLogin = findViewById(R.id.buttonToLogin); // Кнопка для входа
        MaterialButton buttonLogout = findViewById(R.id.buttonLogout); // Кнопка для выхода
        TextView login = findViewById(R.id.login_email); // Поле ввода логина
        TextView password = findViewById(R.id.login_password); // Поле ввода пароля

        buttonToLogin.setOnClickListener(view -> {
            if (login.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                showToast("Данные не заполнены");
                return;
            }
            sendJsonPostRequest(login.getText().toString(), password.getText().toString());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        });

        buttonLogout.setOnClickListener(view -> {
            sendLogoutRequest();
        });
    }

    // Метод для отправки POST-запроса с данными пользователя
    private void sendJsonPostRequest(String login, String password) {
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("username", login);
            jsonParams.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "http://185.20.225.206/api/v1/login",
                    jsonParams,
                    response -> {
                        try {
                            JSONObject userInfo = response.getJSONObject("userInfo");
                            FirstName = userInfo.getString("firstName");
                            LastName = userInfo.getString("lastName");
                            Email = userInfo.getString("email");
                            ID = userInfo.getString("id");

                            // Журналирование информации о пользователе
                            Log.d("LoginActivity", "FirstName: " + FirstName);
                            Log.d("LoginActivity", "LastName: " + LastName);
                            Log.d("LoginActivity", "Email: " + Email);
                            Log.d("LoginActivity", "ID: " + ID);

                            showToast("Привет, " + FirstName + " " + LastName);
                        } catch (JSONException e) {
                            showToast("Ошибка обработки данных");
                        }
                    },
                    error -> {
                        showToast("Неверный логин или пароль");
                        Log.e("LoginActivity", "Error: " + error.toString());
                    }
            );

            Volley.newRequestQueue(getApplicationContext()).add(request);
        } catch (JSONException ex) {
            showToast(ex.toString());
        }
    }

    // Метод для отправки запроса на выход
    private void sendLogoutRequest() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "http://185.20.225.206/api/v1/logout",
                null,
                response -> showToast("Выход успешен"),
                error -> {
                    showToast("Ошибка при выходе");
                    Log.e("LoginActivity", "Logout Error: " + error.toString());
                }
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    // Метод для отображения всплывающего сообщения
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
