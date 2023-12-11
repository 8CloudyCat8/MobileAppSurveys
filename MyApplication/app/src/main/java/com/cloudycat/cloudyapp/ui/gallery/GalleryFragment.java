package com.cloudycat.cloudyapp.ui.gallery;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cloudycat.cloudyapp.R;
import com.cloudycat.cloudyapp.SecondActivity;
import com.cloudycat.cloudyapp.databinding.FragmentGalleryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;

        // Добавляем SwipeRefreshLayout к корневому макету
        swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
        swipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Добавляем существующий корневой макет в SwipeRefreshLayout
        swipeRefreshLayout.addView(root);

        // Устанавливаем слушатель обновления для SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Выполнение GET-запроса при обновлении страницы
            fetchData();
        });

        // Выполнение GET-запроса при создании фрагмента
        fetchData();

        // Возвращаем SwipeRefreshLayout в качестве корневого макета
        return swipeRefreshLayout;
    }

    private void fetchData() {
        String url = "http://185.20.225.206/api/v1/surveys";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResponse,
                error -> swipeRefreshLayout.setRefreshing(false)
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void handleResponse(JSONObject response) {
        try {
            binding.buttonContainer.removeAllViews();
            JSONArray jsonArray = response.getJSONArray("surveys");
            setupCategories(jsonArray);
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupCategories(JSONArray jsonArray) throws JSONException {
        List<String> categoriesList = new ArrayList<>();
        List<Integer> categoryColors = new ArrayList<>();
        int[] colorArray = new int[]{R.color.card1Color, R.color.card4Color, R.color.card5Color,
                R.color.card8Color, R.color.card9Color, R.color.card11Color, R.color.card12Color};

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject survey = jsonArray.getJSONObject(i);
            JSONArray surveyTopics = survey.getJSONArray("survey_topics");

            if (surveyTopics.length() == 0) {
                // Если у опроса нет категории, добавляем "Без категории"
                addCategory("Без категории", categoriesList, categoryColors, colorArray);
            } else {
                for (int j = 0; j < surveyTopics.length(); j++) {
                    String category = surveyTopics.getString(j);
                    addCategory(category, categoriesList, categoryColors, colorArray);
                }
            }
        }

        createCategoryViews(categoriesList, categoryColors, jsonArray);
    }

    private void addCategory(String category, List<String> categoriesList, List<Integer> categoryColors, int[] colorArray) {
        if (!categoriesList.contains(category)) {
            categoriesList.add(category);
            int randomColorIndex = (int) (Math.random() * colorArray.length);
            categoryColors.add(ContextCompat.getColor(requireContext(), colorArray[randomColorIndex]));
        }
    }

    private void createCategoryViews(List<String> categoriesList, List<Integer> categoryColors, JSONArray jsonArray) throws JSONException {
        // Mapping of categories to emojis
        Map<String, String> categoryEmojis = new HashMap<>();
        categoryEmojis.put("Без категории", "🌟");
        categoryEmojis.put("Образование", "📚");
        categoryEmojis.put("Здоровье", "⚕️");
        categoryEmojis.put("Документы", "📝");
        categoryEmojis.put("Питание", "🍲");
        categoryEmojis.put("Строительство", "🏗️");
        categoryEmojis.put("Домашнее хозяйство", "🏡");
        categoryEmojis.put("Ремонт", "🔧");
        categoryEmojis.put("Красота", "💅");
        categoryEmojis.put("Стиль", "👗");
        categoryEmojis.put("Мода", "🕶️");
        categoryEmojis.put("Косметика", "💄");
        categoryEmojis.put("Соц.сети", "📱");
        categoryEmojis.put("Реклама", "📢");
        categoryEmojis.put("Технологии", "🔧");
        categoryEmojis.put("Саморазвитие", "📘");
        categoryEmojis.put("Хобби", "🎨");
        categoryEmojis.put("Развлечение", "🎮");
        categoryEmojis.put("Корея", "🇰🇷");
        categoryEmojis.put("Рестораны", "🍽️");
        categoryEmojis.put("Кафе", "☕");
        categoryEmojis.put("Отдых", "🌴");
        categoryEmojis.put("Путешествия", "✈️");
        categoryEmojis.put("Агрегатор опросов", "📊");
        categoryEmojis.put("Триангуляция", "🔼");

        // Default emoji for categories not present in the map
        String defaultEmoji = "⚪";

        for (int k = 0; k < categoriesList.size(); k++) {
            String category = categoriesList.get(k);
            int categoryColor = categoryColors.get(k);

            // Get the emoji for the current category or use the default emoji
            String emoji = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                emoji = categoryEmojis.getOrDefault(category, defaultEmoji);
            }

            // Add emoji to the category name
            String categoryWithEmoji = emoji + " " + category;

            TextView categoryText = createCategoryTextView(categoryWithEmoji, categoryColor);
            binding.buttonContainer.addView(categoryText);

            // Add margin after the category name
            addMarginView();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject survey = jsonArray.getJSONObject(i);
                JSONArray surveyTopics = survey.getJSONArray("survey_topics");

                if ((surveyTopics.length() == 0 && category.equals("Без категории")) ||
                        (surveyTopics.length() > 0 && surveyTopics.toString().contains(category))) {
                    createButtonForSurvey(survey, i, categoryColor);
                    addSeparatorView();
                }
            }

            addFinalMarginView(); // Add additional margin after the last button in the category
        }
    }

    private void addFinalMarginView() {
        int finalMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()); // Увеличиваем отступ
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, finalMarginBottom);
        binding.buttonContainer.addView(new View(requireContext()), params);
    }

    private TextView createCategoryTextView(String category, int categoryColor) {
        TextView categoryText = new TextView(requireContext());
        categoryText.setText(category);
        categoryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        categoryText.setTextColor(categoryColor);
        categoryText.setTypeface(null, Typeface.BOLD);
        return categoryText;
    }

    private void addSeparatorView() {
        int buttonMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, buttonMarginBottom);
        binding.buttonContainer.addView(new View(requireContext()), buttonParams);
    }

    private void addMarginView() {
        int marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()); // Уменьшаем отступ
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, marginBottom);
        binding.buttonContainer.addView(new View(requireContext()), params);
    }


    private void createButtonForSurvey(JSONObject survey, int index, int categoryColor) throws JSONException {
        String surveyName = survey.getString("name");
        String surveyDescription = survey.getString("description");
        Button button = new Button(requireContext());
        String buttonText = surveyName + "\n\n" + surveyDescription;
        SpannableString spannableString = new SpannableString(buttonText);
        spannableString.setSpan(new RelativeSizeSpan(1.4f), 0, surveyName.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, buttonText.length(), 0);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, surveyName.length(), 0);

        int buttonColor = categoryColor;
        int textColor = ColorUtils.blendARGB(buttonColor, Color.WHITE, 0.9f);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[]{20, 20, 20, 20, 20, 20, 20, 20});
        gradientDrawable.setColor(buttonColor);

        spannableString.setSpan(new ForegroundColorSpan(textColor), 0, buttonText.length(), 0);

        button.setText(spannableString);
        button.setBackground(gradientDrawable);

        binding.buttonContainer.addView(button);

        animateButton(button, index);

        button.setOnClickListener(view -> {
            // Get the survey ID from the current survey JSONObject
            String surveyId;
            try {
                surveyId = survey.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            // Log the survey ID before making the network request
            Log.d("SurveyButton", "Survey ID: " + surveyId);

            // Create a new JsonObjectRequest to fetch survey details by ID
            String apiUrl = "http://185.20.225.206/api/v1/surveys/" + surveyId;
            JsonObjectRequest surveyDetailsRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    apiUrl,
                    null,
                    response -> {
                        // Log the survey details before starting SecondActivity
                        Log.d("SurveyButton", "Survey Details Response: " + response.toString());

                        // Handle the successful response and start SecondActivity with the survey details
                        Intent intent = new Intent(requireContext(), SecondActivity.class);
                        try {
                            // Add the survey details to the intent
                            intent.putExtra("surveyData", response.toString());
                            intent.putExtra("buttonColor", categoryColor);

                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        // Log the error if the network request fails
                        Log.e("SurveyButton", "Error fetching survey details: " + error.toString());

                        // Handle the error response, if needed
                        error.printStackTrace();
                    }
            );

            // Add the request to the Volley queue to execute it
            Volley.newRequestQueue(requireContext()).add(surveyDetailsRequest);
        });

    }

    private void animateButton(Button button, int index) {
        // Начальное значение прозрачности
        button.setAlpha(0f);

        // Задержка перед началом анимации каждой кнопки
        int delay = index * 200; // Измените значение, чтобы настроить задержку

        // Определение стороны, с которой вылетает кнопка (чередуется между левой и правой)
        boolean fromLeft = index % 2 == 0;

        // Запуск анимации на главном потоке
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Анимация прозрачности
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
            alphaAnimator.setDuration(500); // Измените значение, чтобы настроить длительность анимации

            // Анимация трансляции (вылетание) - слева или справа
            float startX = fromLeft ? -button.getWidth() : button.getWidth();
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(button, "translationX", startX, 0);
            translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            translationAnimator.setDuration(500); // Измените значение, чтобы настроить длительность анимации

            // Комбинированная анимация
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
        }, delay);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
