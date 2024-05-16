package com.cloudycat.cloudyapp;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText nameEditText, surnameEditText, ageEditText, locationEditText, incomeEditText, editTextBirthday;
    private TextView genderTextView, maritalStatusTextView, educationTextView, dateEditText;
    private Button editButton, saveButton, reButton;

    private RadioGroup gender;
    private Spinner educationSpinner, familyStatusSpinner;

    private JSONObject birthDate;

    private String name, surname, age,Date, genderValue, location,familyStatus,education;
    private int income;

    private Button btnDatePicker;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private String [] data = new String[10];
    private JSONObject  responseData;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        genderTextView = findViewById(R.id.genderTextView);
        gender = findViewById(R.id.genderRadioGroup);
        dateEditText = findViewById(R.id.dateEditText);
        btnDatePicker = findViewById(R.id.btnDatePicker);

        locationEditText = findViewById(R.id.locationEditText);
        maritalStatusTextView = findViewById(R.id.maritalStatusTextView);
        educationTextView = findViewById(R.id.educationTextView);
        incomeEditText = findViewById(R.id.incomeEditText);
        educationSpinner = findViewById(R.id.education_spinner);
        familyStatusSpinner = findViewById(R.id.familyStatus_spinner);

        editButton = findViewById(R.id.editButton);

        saveButton = findViewById(R.id.saveButton);
        reButton = findViewById(R.id.reButton);
        reButton.setVisibility(View.GONE);



        // Установка кнопки "Редактировать" на видимую, а кнопки "Сохранить" и поля на невидимые
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        btnDatePicker.setVisibility(View.GONE);

        setFieldsEditable(false);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.education_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        educationSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterFamilyStatus = ArrayAdapter.createFromResource(this,
                R.array.familyStatus_array, android.R.layout.simple_spinner_item);
        adapterFamilyStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        familyStatusSpinner.setAdapter(adapterFamilyStatus);

        educationSpinner.setOnItemSelectedListener(this);




        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar newCalendar = Calendar.getInstance();
                datePickerDialog = new DatePickerDialog(EditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        String date = dateFormatter.format(selectedDate.getTime());
                        dateEditText.setText("Дата рождения - " + date);
                        Date = date;
                        Toast.makeText(EditProfileActivity.this, "Выбранная дата: " + date, Toast.LENGTH_SHORT).show();
                    }

                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();
            }
        });




        getRequestValue();
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // По нажатию на кнопку "Редактировать"
                editButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
                btnDatePicker.setVisibility(View.VISIBLE);

                setFieldsEditable(true);
                getRequestValue();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getValues();

                // По нажатию на кнопку "Сохранить"
                String date = Date.toString();

                String[] parts = date.split("-");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                birthDate = new JSONObject();
                try {
                    birthDate.put("day", day);
                    birthDate.put("month", month);
                    birthDate.put("year", year);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {

                    sendPatchRequest();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                editButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
                setFieldsEditable(false);
            }
        });







    }


    //String name, String surname, JSONObject birthDate, String gender, String region, String familyStatus, String educationStatus, int income
    public void setText(JSONObject additionalDetails) throws JSONException {
        //name, surname, age,Date, genderValue, location,familyStatus,education
        name = additionalDetails.getString("firstName");
        String lastName = additionalDetails.getString("lastName");
        int birthDay = additionalDetails.getJSONObject("birthDate").getInt("day");
        int birthMonth = additionalDetails.getJSONObject("birthDate").getInt("month");
        int birthYear = additionalDetails.getJSONObject("birthDate").getInt("year");
        String gender = additionalDetails.getString("gender");
        String region = additionalDetails.getString("region");
        String familyStatus = additionalDetails.getString("familyStatus");
        String educationStatus = additionalDetails.getString("educationStatus");
        int income = additionalDetails.getInt("income");
        responseData = additionalDetails;
    }
    public void sendPatchRequest() throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = "http://185.20.225.206/api/v1/me/respondent_details";

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("firstName", name);
        jsonBody.put("lastName", surname);
        jsonBody.put("birthDate", birthDate);
        jsonBody.put("gender", genderValue);
        jsonBody.put("region", location);
        jsonBody.put("familyStatus", familyStatus);
        jsonBody.put("educationStatus", education);
        jsonBody.put("income", income);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Запрос отправлен", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Обработка ошибки
            }
        });

        queue.add(request);
    }

    public void getValues(){
        name = nameEditText.getText().toString();
        surname = surnameEditText.getText().toString();
        age = ageEditText.getText().toString();

        int selectedId = gender.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        genderValue = getAnotherValue(radioButton.getText().toString());

        location = locationEditText.getText().toString();

        familyStatus = getAnotherValue(familyStatusSpinner.getSelectedItem().toString());
        education = getAnotherValue(educationSpinner.getSelectedItem().toString());

        income = Integer.parseInt(incomeEditText.getText().toString());

    }

    private void getRequestValue() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://185.20.225.206/api/v1/me";


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String firstName,lastName,gender,region,familyStatus,educationStatus;

                        int birthDay,birthMonth,birthYear, income;
                        try {
                            JSONObject userInfo = (JSONObject) response.get("userInfo");
                            JSONObject additionalDetails = userInfo.getJSONObject("additionalDetails");

                            firstName = additionalDetails.getString("firstName");
                            lastName = additionalDetails.getString("lastName");
                            birthDay = additionalDetails.getJSONObject("birthDate").getInt("day");
                            birthMonth = additionalDetails.getJSONObject("birthDate").getInt("month");
                            birthYear = additionalDetails.getJSONObject("birthDate").getInt("year");
                            gender = additionalDetails.getString("gender");
                            region = additionalDetails.getString("region");
                            familyStatus = additionalDetails.getString("familyStatus");
                            educationStatus = additionalDetails.getString("educationStatus");
                            income = additionalDetails.getInt("income");

                            nameEditText.setText(firstName);
                            surnameEditText.setText(lastName);
                            ageEditText.setText(String.valueOf(calculateAge(birthDay, birthMonth, birthYear)));
                            dateEditText.setText(String.format("%d.%d.%d", birthDay, birthMonth, birthYear));
                            locationEditText.setText(region);
                            incomeEditText.setText(String.valueOf(income));

                            if (gender.equalsIgnoreCase("male")) {
                                RadioButton maleRadioButton = findViewById(R.id.maleRadioButton);
                                maleRadioButton.setChecked(true);
                            } else if (gender.equalsIgnoreCase("female")) {
                                RadioButton femaleRadioButton = findViewById(R.id.femaleRadioButton);
                                femaleRadioButton.setChecked(true);
                            }

                            setSpinnerSelection(familyStatusSpinner, getAnotherValue(familyStatus));
                            setSpinnerSelection(educationSpinner, getAnotherValue(educationStatus));

                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        queue.add(request);

    }

    private int calculateAge(int birthDay, int birthMonth, int birthYear) {
        // Получить текущую дату
        Calendar currentDate = Calendar.getInstance();

        // Получить дату рождения
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthYear, birthMonth - 1, birthDay); // Месяцы в Calendar считаются с 0, поэтому вычетаем 1

        // Вычислить разницу в годах
        int age = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // Проверить, прошел ли уже день рождения в текущем году
        if (birthDate.get(Calendar.MONTH) > currentDate.get(Calendar.MONTH) ||
                (birthDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                        birthDate.get(Calendar.DAY_OF_MONTH) > currentDate.get(Calendar.DAY_OF_MONTH))) {
            age--; // Скорректировать возраст, если день рождения еще не наступил
        }

        return age;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }





    // Функция для установки полей доступными/недоступными для редактирования
    private void setFieldsEditable(boolean editable) {
        nameEditText.setEnabled(editable);
        surnameEditText.setEnabled(editable);
        ageEditText.setEnabled(editable);
        locationEditText.setEnabled(editable);
        incomeEditText.setEnabled(editable);
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public static String getAnotherValue(String input) {
        switch (input) {
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
            case "Не замужем / Не женат":
                return "not_married";
            case "Замужем / Женат":
                return "married";
            case "Разведен / Разведена":
                return "divorced";
            case "Высшая Вдовец / Вдова":
                return "widowed";
            case "Мужской":
                return "male";
            case "Женский":
                return "female";
            case "none":
                return "Нет образования";
            case "basic_general":
                return "Основное общее";
            case "secondary_general":
                return "Среднее общее";
            case "secondary_professional":
                return "Среднее профессиональное";
            case "bachelor_degree":
                return "Высшее, бакалавр";
            case "master_degree":
                return "Высшее, магистр";
            case "highest_qualification":
                return "Высшая квалификация";
            case "not_married":
                return "Не замужем / Не женат";
            case "married":
                return "Замужем / Женат";
            case "divorced":
                return "Разведен / Разведена";
            case "widowed":
                return "Высшая Вдовец / Вдова";
            default:
                return "";
        }
    }



}