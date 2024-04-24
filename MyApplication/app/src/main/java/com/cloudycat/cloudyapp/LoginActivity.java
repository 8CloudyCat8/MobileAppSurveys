package com.cloudycat.cloudyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;

public class LoginActivity extends AppCompatActivity {
    public static String FirstName;
    public String LastName;
    public String ID;
    public String Email;
    public String Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialButton buttonOnMain = findViewById(R.id.buttonToLogin);
        MaterialButton buttonReg = findViewById(R.id.buttonRegister);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedEmail = preferences.getString("email", "");
        String savedPassword = preferences.getString("password", "");

        TextView login = findViewById(R.id.login_email);
        TextView password = findViewById(R.id.login_password);
        login.setText(savedEmail);
        password.setText(savedPassword);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupDialog();
            }
        });

        View.OnClickListener onClickButtonToMain = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login.getText().toString().isEmpty() && password.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Данные не заполнены", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                String url2 = "http://185.20.225.206/api/v1/login";

                sendJsonPostRequest(login.getText().toString(), password.getText().toString());

                // Save login credentials
                saveLoginCredentials(login.getText().toString(), password.getText().toString());

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        };
        buttonOnMain.setOnClickListener(onClickButtonToMain);

    }

    private void saveLoginCredentials(String email, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private String getGenderKey(String selectedStatus) {
        switch (selectedStatus) {
            case "Мужской":
                return "male";
            case "Женский":
                return "female";
            default:
                return "";
        }
    }


    private String getFamilyStatusKey(String selectedStatus) {
        switch (selectedStatus) {
            case "Не замужем / Не женат":
                return "not_married";
            case "Замужем / Женат":
                return "married";
            case "Разведен / Разведена":
                return "divorced";
            case "Вдовец / Вдова":
                return "widowed";
            default:
                return "";
        }
    }

    private String getEducationStatusKey(String selectedStatus) {
        switch (selectedStatus) {
            case "Нет образования":
                return "none";
            case "Основное общее":
                return "basic_general";
            case "Среднее общее":
                return "secondary_general";
            case "Среднее профессиональное":
                return "secondary_professional";
            case "Высшее, бакалавр":
                return "bachelor_degree";
            case "Высшее, магистр":
                return "master_degree";
            case "Высшая квалификация":
                return "highest_qualification";
            default:
                return "";
        }
    }

    private void showPopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_layout, null);

        final EditText editText1 = dialogView.findViewById(R.id.editText1);
        final EditText editText2 = dialogView.findViewById(R.id.editText2);
        final EditText editText3 = dialogView.findViewById(R.id.editText3);
        final EditText editText4 = dialogView.findViewById(R.id.editText4);
        final EditText editText5 = dialogView.findViewById(R.id.editText5);
        final EditText editText8 = dialogView.findViewById(R.id.editText8);
        final EditText editText11 = dialogView.findViewById(R.id.editText11);
        final DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

        final Spinner spinnerMaritalStatus = dialogView.findViewById(R.id.spinnerMaritalStatus);
        final Spinner spinnerEducation = dialogView.findViewById(R.id.spinnerEducation);
        final RadioGroup radioGroupGender = dialogView.findViewById(R.id.radioGroupGender);

        RadioButton radioButtonMale = dialogView.findViewById(R.id.radioButtonMale);
        radioButtonMale.setChecked(true);

        builder.setView(dialogView)
                .setTitle("Enter Details")
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();
                String text3 = editText3.getText().toString();
                String text4 = editText4.getText().toString();
                String text5 = editText5.getText().toString();
                String text8 = editText8.getText().toString();
                String text11 = editText11.getText().toString();

                if (text1.isEmpty() || text2.isEmpty() || text3.isEmpty() || text4.isEmpty() || text5.isEmpty() || text8.isEmpty() || text11.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                } else if (text4.length() == 10) {
                    // Добавлено условие для проверки длины пароля
                    if (text5.length() < 8) {
                        Toast.makeText(LoginActivity.this, "Пароль должен содержать не менее 8 символов", Toast.LENGTH_SHORT).show();
                    } else {
                        alertDialog.dismiss();
                        String selectedMaritalStatus = spinnerMaritalStatus.getSelectedItem().toString();
                        String selectedEducationStatus = spinnerEducation.getSelectedItem().toString();

                        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();

                        if (selectedGenderId != -1) {
                            RadioButton selectedGenderRadioButton = dialogView.findViewById(selectedGenderId);
                            String selectedGender = selectedGenderRadioButton.getText().toString();
                            sendRegistrationRequest(text1, text2, text3, text4, text5, selectedMaritalStatus, selectedEducationStatus, selectedGender, datePicker, text8);
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Телефон должен содержать 10 цифр", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void sendRegistrationRequest(String firstName, String lastName, String email, String phoneNumber, String password,
                                         String selectedMaritalStatus, String selectedEducationStatus, String selectedGender, DatePicker datePicker, String region) {
        try {
            JSONObject birthDate = new JSONObject();

            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();

            birthDate.put("day", day );
            birthDate.put("month", month);
            birthDate.put("year", year);

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("firstName", firstName);
            jsonParams.put("lastName", lastName);
            jsonParams.put("birthDate", birthDate);
            jsonParams.put("gender", getGenderKey(selectedGender));
            jsonParams.put("region", region);
            jsonParams.put("familyStatus", getFamilyStatusKey(selectedMaritalStatus));
            jsonParams.put("educationStatus", getEducationStatusKey(selectedEducationStatus));
            jsonParams.put("income", 100000);
            jsonParams.put("email", email);
            jsonParams.put("phoneNumber", "+7" + phoneNumber);
            jsonParams.put("password", password);

            Log.d("RegistrationRequest", "Sending registration request with parameters: " + jsonParams.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    "http://185.20.225.206/api/v1/register",
                    jsonParams,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("RegistrationResponse", "Registration successful. Response: " + response.toString());
                            Toast.makeText(LoginActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                            TextView login = findViewById(R.id.login_email);
                            TextView pass = findViewById(R.id.login_password);
                            login.setText(email);
                            pass.setText(password);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("RegistrationError", "Error during registration. Error: " + error.toString());
                            Toast.makeText(LoginActivity.this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show();
                        }
                    });

            Volley.newRequestQueue(getApplicationContext()).add(request);

        } catch (JSONException ex) {
            Log.e("RegistrationError", "JSON Exception: " + ex.toString());
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }




    private void sendJsonPostRequest(String login, String password){
        try {
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
            // Make new json object and put params in it
            JSONObject jsonParams = new JSONObject();

            jsonParams.put("username", login);
            jsonParams.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,

                    "http://185.20.225.206/api/v1/login",
                    jsonParams,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            String firstName;
                            String lastName;
                            String email;
                            String id;
                            String phone;
                            JSONObject userInfo;
                            JSONObject additionalDetails;
                            JSONObject additionalDetails1;
                            try {
                                userInfo = (JSONObject) response.get("userInfo");
                                additionalDetails = (JSONObject) userInfo.get("additionalDetails");


                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                firstName = additionalDetails.get("firstName").toString();
                                lastName = additionalDetails.get("lastName").toString();
                                email = userInfo.get("email").toString();
                                id = userInfo.get("id").toString();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            FirstName= firstName;
                            LastName= lastName;
                            Email = email;
                            ID = id;

                            Toast toast = Toast.makeText(getApplicationContext(), "Привет, "+FirstName + " " + LastName, Toast.LENGTH_SHORT);

                            toast.show();
                        }
                    },

                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Неверный логин или пароль", Toast.LENGTH_SHORT);
                            toast.show();

                        }
                    });


            Volley.newRequestQueue(getApplicationContext()).
                    add(request);



        } catch(JSONException ex){
            Toast toast = Toast.makeText(getApplicationContext(),
                    ex.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}