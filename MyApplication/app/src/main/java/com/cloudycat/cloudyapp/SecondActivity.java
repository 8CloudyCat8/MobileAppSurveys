package com.cloudycat.cloudyapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Получаем данные опроса из интента
        String surveyDataString = getIntent().getStringExtra("surveyData");

        try {
            // Разбираем JSON-строку в JSONObject
            JSONObject surveyData = new JSONObject(surveyDataString);

            // Извлекаем необходимую информацию из JSONObject
            JSONObject surveyInfo = surveyData.getJSONObject("surveyInfo");
            String surveyId = surveyInfo.getString("id");
            String surveyName = surveyInfo.getString("name");
            String surveyDescription = surveyInfo.getString("description");

            // Обновляем TextView деталями опроса
            TextView surveyDetailsTextView = findViewById(R.id.surveyDetailsTextView);
            surveyDetailsTextView.setText("ID: " + surveyId + "\nНазвание: " + surveyName + "\nОписание: " + surveyDescription);

            // Используем Gson для красивого форматирования JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String formattedJson = gson.toJson(surveyInfo);

            // Обновляем TextView отформатированным JSON
            TextView surveyAll = findViewById(R.id.surveyAll);
            surveyAll.setText(formattedJson);

            // Получаем массив вопросов
            JSONArray questionsArray = surveyInfo.getJSONArray("questions");

            // Контейнер для динамически добавляемых вопросов
            LinearLayout questionContainer = findViewById(R.id.questionContainer);

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObject = questionsArray.getJSONObject(i);

                // Извлекаем детали вопроса
                String questionText = questionObject.getString("text");
                String answerType = questionObject.getString("answer_type");

                // Создаем новый TextView для вопроса
                TextView questionTextView = new TextView(this);
                questionTextView.setText(questionText);
                questionContainer.addView(questionTextView);

                // Создаем элементы UI в зависимости от типа ответа
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
                        for (int j = 0; j < choicesArrayMulti.length(); j++) {
                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(choicesArrayMulti.getString(j));
                            questionContainer.addView(checkBox);
                        }
                        break;

                    case "text":
                        EditText editText = new EditText(this);

                        // Получаем значение maxLength из ограничений
                        int maxLength = questionObject.getJSONObject("restrictions").getInt("maxLength");

                        // Устанавливаем максимальную длину для EditText
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

                        editText.setHint("Введите ваш ответ здесь");

                        // Создаем TextView для отображения количества символов
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

                        // Добавляем EditText в questionContainer
                        questionContainer.addView(editText);
                        break;

                    case "slider":
                        // Создаем LinearLayout для размещения как SeekBar, так и TextView с значением
                        LinearLayout sliderLayout = new LinearLayout(this);
                        sliderLayout.setOrientation(LinearLayout.VERTICAL);

                        // Создаем TextView для отображения текущего значения
                        TextView valueTextView = new TextView(this);
                        valueTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Центрируем текст
                        valueTextView.setText("0"); // Начальное значение

                        // Создаем SeekBar
                        SeekBar seekBar = new SeekBar(this);
                        JSONObject restrictionsObject = questionObject.getJSONObject("restrictions");
                        int min = restrictionsObject.getInt("min");
                        int max = restrictionsObject.getInt("max");
                        seekBar.setMax(max - min);

                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                // Обновляем TextView текущим значением
                                int currentValue = progress + min;
                                valueTextView.setText(String.valueOf(currentValue));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
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

            // Добавляем кнопку для отправки результатов
            Button submitBtn = findViewById(R.id.submitBtn);
            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            // Обрабатываем JSONException (например, выводим сообщение об ошибке)
            TextView surveyDetailsTextView = findViewById(R.id.surveyDetailsTextView);
            surveyDetailsTextView.setText("Ошибка при разборе данных опроса");
        }
    }
}