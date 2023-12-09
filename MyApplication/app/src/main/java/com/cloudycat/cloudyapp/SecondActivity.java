package com.cloudycat.cloudyapp;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SecondActivity extends AppCompatActivity {
    public String Id;
    JSONArray Answers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Получаем данные опроса из интента
        String surveyDataString = getIntent().getStringExtra("surveyData");

        Log.d("SecondActivity", "Survey data: " + surveyDataString);

        try {
            // Разбираем JSON-строку в JSONObject
            JSONObject surveyData = new JSONObject(surveyDataString);

            // Извлекаем основные данные опроса
            String surveyId = surveyData.getString("id");
            Id = surveyId;
            String surveyName = surveyData.getString("name");
            String surveyDescription = surveyData.getString("description");

            // Обновляем TextView с основными данными опроса
            TextView surveyDetailsTextView = findViewById(R.id.surveyDetailsTextView);
            surveyDetailsTextView.setText("ID: " + surveyId + "\nНазвание: " + surveyName + "\nОписание: " + surveyDescription);

            // Используем Gson для красивого форматирования JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String formattedJson = gson.toJson(surveyData);

            // Обновляем TextView отформатированным JSON
            TextView surveyAll = findViewById(R.id.surveyAll);
            surveyAll.setText(formattedJson);

            JSONArray questionsArray = surveyData.getJSONArray("questions");

            // Контейнер для динамически добавляемых вопросов
            LinearLayout questionContainer = findViewById(R.id.questionContainer);

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObject = questionsArray.getJSONObject(i);

                // Извлекаем детали вопроса
                String questionText = questionObject.getString("text");
                String answerType = questionObject.getString("answer_type");
                boolean isRequired = questionObject.getBoolean("required");

                // Проверяем, является ли ответ обязательным
                if (isRequired) {
                    // Если ответ обязателен, добавляем звездочку к имени вопроса
                    questionText = questionText + " (Обязательно)";
                }

                // Создаём новый TextView для вопроса с отступом сверху 20px
                TextView questionTextView = new TextView(this);
                questionTextView.setText(questionText);

                // Устанавливаем параметры макета с отступом сверху
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(10, 50, 0, 0);
                questionTextView.setLayoutParams(layoutParams);

                // Добавляем TextView в questionContainer
                questionContainer.addView(questionTextView);

                // Создаём элементы UI в зависимости от типа ответа
                switch (answerType) {
                    case "single_choice":
                        RadioGroup radioGroup = new RadioGroup(this);
                        JSONArray choicesArray = questionObject.getJSONObject("restrictions").getJSONArray("choices");
                        for (int j = 0; j < choicesArray.length(); j++) {
                            RadioButton radioButton = new RadioButton(this);
                            radioButton.setText(choicesArray.getString(j));
                            radioGroup.addView(radioButton);
                        }
                        questionContainer.addView(radioGroup);
                        break;

                    case "multiple_choice":
                        JSONArray choicesArrayMulti = questionObject.getJSONObject("restrictions").getJSONArray("choices");
                        LinearLayout choicesLayout = new LinearLayout(this);
                        choicesLayout.setOrientation(LinearLayout.VERTICAL); // Устанавливаем вертикальную ориентацию

                        for (int j = 0; j < choicesArrayMulti.length(); j++) {
                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(choicesArrayMulti.getString(j));
                            choicesLayout.addView(checkBox);
                        }

                        questionContainer.addView(choicesLayout);
                        break;

                    case "text":
                        EditText editText = new EditText(this);

                        // Получаем объект ограничений
                        JSONObject restrictionsObject = questionObject.optJSONObject("restrictions");

                        // Устанавливаем максимальную длину для EditText (если "maxLength" равно null, устанавливаем без ограничений)
                        int maxLength = (restrictionsObject != null && !restrictionsObject.isNull("maxLength"))
                                ? restrictionsObject.getInt("maxLength")
                                : Integer.MAX_VALUE; // Set to a large positive value

                        // Устанавливаем максимальную длину для EditText
                        editText.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter(maxLength)
                        });

                        editText.setHint("Введите ваш ответ здесь");

                        // Если maxLength не установлен, не добавляем TextView для отображения количества символов
                        if (maxLength != Integer.MAX_VALUE) {
                            // Создаём TextView для отображения количества символов
                            TextView charCountTextView = new TextView(this);
                            charCountTextView.setText("0/" + maxLength);

                            // Устанавливаем Gravity для центрирования текста в TextView
                            charCountTextView.setGravity(Gravity.CENTER);

                            // Добавляем TextView в questionContainer
                            questionContainer.addView(charCountTextView, new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            ));

                            // Добавляем TextWatcher к EditText
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                    // Обновляем TextView с количеством символов
                                    int currentLength = charSequence.length();
                                    charCountTextView.setText(currentLength + "/" + maxLength);
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                }
                            });
                        }

                        // Добавляем EditText в questionContainer
                        questionContainer.addView(editText);
                        break;


                    case "slider":
                        // Создаём LinearLayout для размещения как SeekBar так и TextView
                        LinearLayout sliderLayout = new LinearLayout(this);
                        sliderLayout.setOrientation(LinearLayout.VERTICAL);

                        // Создаём TextView для отображения текущего значения
                        TextView valueTextView = new TextView(this);
                        valueTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                        // Получаем минимальное значение из ограничений
                        int min = questionObject.getJSONObject("restrictions").getInt("min");

                        // Устанавливаем начальное значение TextView равным минимальному значению
                        valueTextView.setText(String.valueOf(min));

                        // Создаём SeekBar
                        SeekBar seekBar = new SeekBar(this);
                        int max = questionObject.getJSONObject("restrictions").getInt("max");
                        seekBar.setMax(max - min);

                        // Получаем идентификатор цвета из ресурсов
                        int purple200Color = getResources().getColor(R.color.purple_200);

                        // Устанавливаем цвет слайдера
                        seekBar.getProgressDrawable().setColorFilter(purple200Color, PorterDuff.Mode.SRC_IN);
                        seekBar.getThumb().setColorFilter(purple200Color, PorterDuff.Mode.SRC_IN);


                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                // Обновляем TextView текущим значением
                                int currentValue = progress + min;
                                valueTextView.setText(String.valueOf(currentValue));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {}

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {}
                        });

                        // Добавляем TextView с значением над SeekBar
                        sliderLayout.addView(valueTextView);

                        // Добавляем SeekBar в макет
                        sliderLayout.addView(seekBar);

                        // Добавляем макет в основной контейнер
                        questionContainer.addView(sliderLayout);
                        break;
                }

            }
            int childCount = questionContainer.getChildCount();

            // Добавляем кнопку для отправки результатов
            Button submitBtn = findViewById(R.id.submitBtn);


            View.OnClickListener onClickButtonToMain = view -> {
                getAnswers(childCount, questionContainer);
                sendJsonPostRequest();
                finish();
            };

            submitBtn.setOnClickListener(onClickButtonToMain);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SecondActivity", "Error parsing survey data: " + e.getMessage());
            TextView surveyDetailsTextView = findViewById(R.id.surveyDetailsTextView);
            surveyDetailsTextView.setText("Ошибка при разборе данных опроса: " + e.getMessage());
        }
    }
    private void getAnswers(int childCount,LinearLayout container) {
        JSONObject jsonObject = new JSONObject(); // создаем экземпляр JSONObject

        JSONArray answersArray = new JSONArray(); // создаем массив для элементов "answers"

        for (int i = 0; i < childCount; i++) {
            View childView = container.getChildAt(i);

            if (childView instanceof RadioGroup) {
                // Обработка RadioButton
                LinearLayout Layout = (LinearLayout) childView;
                int v = Layout.getChildCount();
                for (int z=0; z<v;z++){
                    RadioButton radioButton = (RadioButton) Layout.getChildAt(z);
                    if (radioButton.isChecked()){
                        int radioButtonValue = z;
                        JSONObject answer4 = new JSONObject();
                        try {
                            answer4.put("choice", radioButtonValue);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        answersArray.put(answer4);
                    }
                }

            }  else if (childView instanceof EditText) {
                // Обработка EditText
                EditText editText = (EditText) childView;
                // Получение значений и выполнение необходимых действий
                String editTextValue = editText.getText().toString();
                JSONObject answer1 = new JSONObject();
                try {
                    answer1.put("text", editTextValue);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                answersArray.put(answer1);

            } else if (childView instanceof LinearLayout) {

                LinearLayout Layout = (LinearLayout) childView;
                int a = Layout.getChildCount();
                if (Layout.getChildAt(0) instanceof CheckBox){
                    ArrayList<Integer> checked = new ArrayList<>();
                    for(int c = 0; c<=Layout.getChildCount() - 1;c++){
                        CheckBox checkBox = (CheckBox) Layout.getChildAt(c);
                        if (checkBox.isChecked()){
                            checked.add(c);
                        }
                    }
                    // Получение значений и выполнение необходимых действий
                    JSONObject answer4 = new JSONObject();
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (int d = 0; d < checked.size(); d++) {
                            jsonArray.put(checked.get(d));
                        }
                        answer4.put("choices", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    answersArray.put(answer4);
                }
                else if (Layout.getChildAt(0) instanceof TextView){
                    TextView valueTextView = (TextView) Layout.getChildAt(0);
                    // Получение значений и выполнение необходимых действий
                    int textViewValue = Integer.parseInt((String) valueTextView.getText());
                    JSONObject answer4 = new JSONObject();
                    try {
                        answer4.put("value", textViewValue);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    answersArray.put(answer4);
                }
            }
        }
        Answers = answersArray;
    }

    private void sendJsonPostRequest(){
        String url = "http://185.20.225.206/api/v1/surveys/" + Id + "/answers";
        try {
            JSONObject jsonParams = new JSONObject();

            jsonParams.put("answers", Answers);
            jsonParams.put("polling_time", 120);

            Log.d("JSON Params", jsonParams.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParams,
                    response -> {

                        try {

                            JSONObject surveyAnswerInfo = (JSONObject) response.get("surveyAnswerInfo");
                            String id = surveyAnswerInfo.get("survey_id").toString();

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Toast toast = Toast.makeText(getApplicationContext(), "Запрос отправлен", Toast.LENGTH_SHORT);

                        toast.show();

                    },
                    error -> {

                        Toast toast = Toast.makeText(getApplicationContext(),
                                error.toString(), Toast.LENGTH_SHORT);
                        toast.show();

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