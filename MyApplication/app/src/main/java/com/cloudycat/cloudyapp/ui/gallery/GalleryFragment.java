package com.cloudycat.cloudyapp.ui.gallery;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        for (int k = 0; k < categoriesList.size(); k++) {
            String category = categoriesList.get(k);
            int categoryColor = categoryColors.get(k);

            TextView categoryText = createCategoryTextView(category, categoryColor);
            binding.buttonContainer.addView(categoryText);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject survey = jsonArray.getJSONObject(i);
                JSONArray surveyTopics = survey.getJSONArray("survey_topics");

                if ((surveyTopics.length() == 0 && category.equals("Без категории")) ||
                        (surveyTopics.length() > 0 && surveyTopics.toString().contains(category))) {
                    createButtonForSurvey(survey, i, categoryColor);
                    addSeparatorView();
                }
            }

            addMarginView();
        }
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
        int buttonMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, buttonMarginBottom);
        binding.buttonContainer.addView(new View(requireContext()), buttonParams);
    }

    private void addMarginView() {
        int marginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
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
            Intent intent = new Intent(requireContext(), SecondActivity.class);
            try {
                intent.putExtra("surveyData", survey.toString());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void animateButton(Button button, int index) {
        // Начальное значение прозрачности
        button.setAlpha(0f);

        // Задержка перед началом анимации каждой кнопки
        int delay = index * 100; // Измените значение, чтобы настроить задержку

        // Запуск анимации на главном потоке
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
            alphaAnimator.setDuration(500); // Измените значение, чтобы настроить длительность анимации
            alphaAnimator.start();
        }, delay);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
