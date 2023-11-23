package com.cloudycat.cloudyapp.ui.gallery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.cloudycat.cloudyapp.SecondActivity;
import com.cloudycat.cloudyapp.databinding.FragmentGalleryBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;

        // Выполнение GET-запроса
        String url = "http://185.20.225.206/api/v1/surveys";
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Обработка успешного ответа
                    try {
                        JSONArray jsonArray = response;

                        // Вывод в лог количества опросов
                        Log.d("GalleryFragment", "Количество опросов: " + jsonArray.length());

                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonArray formattedJsonArray = gson.fromJson(jsonArray.toString(), JsonArray.class);
                        String formattedJson = gson.toJson(formattedJsonArray);

                        Log.d("GalleryFragment", "formattedJson: " + formattedJson);

                        // Вывод в лог id каждого опроса
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject surveyInfo = jsonArray.getJSONObject(i).getJSONObject("surveyInfo");
                            String surveyId = surveyInfo.getString("id");
                            String surveyName = surveyInfo.getString("name");
                            String surveyDescription = surveyInfo.getString("description");

                            Log.d("GalleryFragment", "Id опроса " + (i + 1) + ": " + surveyId);

                            // Создание новой кнопки
                            Button button = new Button(requireContext());

                            // Создание SpannableString для установки различных стилей текста
                            SpannableString spannableString = new SpannableString(surveyName + "\n" + surveyDescription);

                            // Установка цвета текста имени опроса
                            spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, surveyName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Установка цвета текста описания опроса
                            spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), surveyName.length() + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Установка меньшего размера текста для описания
                            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyName.length() + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            button.setText(spannableString);
                            button.setId(View.generateViewId()); // Установка уникального ID для кнопки

                            // Настройка параметров макета для кнопки
                            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                            // Добавление кнопки в контейнер
                            binding.buttonContainer.addView(button, buttonLayoutParams);

                            final int index = i;  // Создание финальной переменной для хранения значения i

                            button.setOnClickListener(view -> {
                                // Использование финальной переменной внутри лямбда-выражения
                                Intent intent = new Intent(requireContext(), SecondActivity.class);
                                try {
                                    intent.putExtra("surveyData", jsonArray.getJSONObject(index).toString());
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                startActivity(intent);
                            });
                        }
                        textView.setText(formattedJson);
                    } catch (Exception e) {
                        e.printStackTrace();
                        textView.setText("Ошибка обработки данных");
                    }
                },
                error -> {
                    // Обработка ошибки
                    textView.setText("Ошибка запроса");
                }
        );

        // Добавление запроса в очередь
        Volley.newRequestQueue(requireContext()).add(request);

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}