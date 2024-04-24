package com.cloudycat.cloudyapp.ui.gallery;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

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

    JSONArray jsonArray = new JSONArray();
    List<Integer> categoryColors = new ArrayList<>();
    private final List<String> categoriesList = new ArrayList<>();
    private final List<String> selectedCategories = new ArrayList<>();

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

        swipeRefreshLayout.addView(root);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchData();
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchData();
            }
        }, 500);

        Button btnFilters = root.findViewById(R.id.btnFilters);
        btnFilters.setOnClickListener(view -> showBottomSheet(categoriesList));

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

        categoriesList.clear();
    }


    private void handleResponse(JSONObject response) {
        try {
            binding.buttonContainer.removeAllViews();
            jsonArray = response.getJSONArray("surveys");
            setupCategories(jsonArray);
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupCategories(JSONArray jsonArray) throws JSONException {
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
        int delayMultiplier = 20; // Delay multiplier for animation
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

            // Animate category text
            animateView(categoryText, k * delayMultiplier);

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

    private void animateView(View view, int delay) {
        view.setAlpha(0f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            alphaAnimator.setDuration(1000);
            alphaAnimator.start();
        }, delay);
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
            String surveyId;
            try {
                surveyId = survey.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            Log.d("SurveyButton", "Survey ID: " + surveyId);

            String apiUrl = "http://185.20.225.206/api/v1/surveys/" + surveyId;
            JsonObjectRequest surveyDetailsRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    apiUrl,
                    null,
                    response -> {
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
                        Log.e("SurveyButton", "Error fetching survey details: " + error.toString());

                        error.printStackTrace();
                    }
            );

            Volley.newRequestQueue(requireContext()).add(surveyDetailsRequest);
        });

    }

    private void animateButton(Button button, int index) {
        button.setAlpha(0f);

        int delay = index * 20;

        boolean fromLeft = index % 2 == 0;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
            alphaAnimator.setDuration(500);

            float startX = fromLeft ? -button.getWidth() : button.getWidth();
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(button, "translationX", startX, 0);
            translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            translationAnimator.setDuration(500);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
        }, delay);
    }

    private void showBottomSheet(List<String> categoriesList) {
        boolean[] checkedItems = new boolean[categoriesList.size()];

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final int buttonsPerRow = 3;
        final int buttonPadding = 10;

        for (int i = 0; i < categoriesList.size(); i += buttonsPerRow) {
            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < buttonsPerRow && (i + j) < categoriesList.size(); j++) {
                LinearLayout itemLayout = new LinearLayout(requireContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f));

                ImageView imageView = new ImageView(requireContext());

                String currentCategory = categoriesList.get(i + j);
                Log.d("Category", "Current category: " + currentCategory);


                if (currentCategory.equals("Образование")) {
                    Picasso.get().load(R.drawable.education).into(imageView);
                } else if (currentCategory.equals("Без категории")) {
                    Picasso.get().load(R.drawable.nocategory).into(imageView);
                } else if (currentCategory.equals("Ремонт")) {
                    Picasso.get().load(R.drawable.tool).into(imageView);
                } else if (currentCategory.equals("Питание")) {
                    Picasso.get().load(R.drawable.food).into(imageView);
                } else if (currentCategory.equals("Здоровье")) {
                    Picasso.get().load(R.drawable.health).into(imageView);
                } else if (currentCategory.equals("Документы")) {
                    Picasso.get().load(R.drawable.docs).into(imageView);
                } else if (currentCategory.equals("Строительство")) {
                    Picasso.get().load(R.drawable.build).into(imageView);
                } else if (currentCategory.equals("Домашнее хозяйство")) {
                    Picasso.get().load(R.drawable.household).into(imageView);
                } else if (currentCategory.equals("Красота")) {
                    Picasso.get().load(R.drawable.beauty).into(imageView);
                } else if (currentCategory.equals("Стиль")) {
                    Picasso.get().load(R.drawable.style).into(imageView);
                } else if (currentCategory.equals("Мода")) {
                    Picasso.get().load(R.drawable.fashion).into(imageView);
                } else if (currentCategory.equals("Косметика")) {
                    Picasso.get().load(R.drawable.cosmetics).into(imageView);
                } else if (currentCategory.equals("Соц.сети")) {
                    Picasso.get().load(R.drawable.socialmedia).into(imageView);
                } else if (currentCategory.equals("Реклама")) {
                    Picasso.get().load(R.drawable.ad).into(imageView);
                } else if (currentCategory.equals("Технологии")) {
                    Picasso.get().load(R.drawable.tech).into(imageView);
                } else if (currentCategory.equals("Саморазвитие")) {
                    Picasso.get().load(R.drawable.improve).into(imageView);
                } else if (currentCategory.equals("Хобби")) {
                    Picasso.get().load(R.drawable.hobby).into(imageView);
                } else if (currentCategory.equals("Развлечение")) {
                    Picasso.get().load(R.drawable.fun).into(imageView);
                } else if (currentCategory.equals("Корея")) {
                    Picasso.get().load(R.drawable.korea).into(imageView);
                } else if (currentCategory.equals("Рестораны")) {
                    Picasso.get().load(R.drawable.restaurants).into(imageView);
                } else if (currentCategory.equals("Кафе")) {
                    Picasso.get().load(R.drawable.coffee).into(imageView);
                } else if (currentCategory.equals("Отдых")) {
                    Picasso.get().load(R.drawable.chill).into(imageView);
                } else if (currentCategory.equals("Путешествия")) {
                    Picasso.get().load(R.drawable.travel).into(imageView);
                } else if (currentCategory.equals("Агрегатор опросов")) {
                    Picasso.get().load(R.drawable.surveys).into(imageView);
                } else if (currentCategory.equals("Триангуляция")) {
                    Picasso.get().load(R.drawable.triangle).into(imageView);
                } else {
                    Picasso.get().load(R.drawable.default_image).into(imageView);
                }

                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);

                int finalI = i;
                int finalJ = j;

                updateFilter(imageView, checkedItems[finalI + finalJ]);

                imageView.setOnClickListener(view -> {
                    checkedItems[finalI + finalJ] = !checkedItems[finalI + finalJ];

                    updateFilter(imageView, checkedItems[finalI + finalJ]);
                });

                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                imageParams.setMargins(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
                imageView.setLayoutParams(imageParams);

                itemLayout.addView(imageView);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMargins(buttonPadding, 0, buttonPadding, 0);
                rowLayout.addView(itemLayout);
            }

            layout.addView(rowLayout);

            LinearLayout textLayout = new LinearLayout(requireContext());
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));


            for (int j = 0; j < buttonsPerRow && (i + j) < categoriesList.size(); j++) {
                TextView textView = new TextView(requireContext());
                textView.setText(categoriesList.get(i + j));
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.card8Color));
                textView.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f);
                textParams.setMargins(buttonPadding, 0, buttonPadding, 0);
                textView.setLayoutParams(textParams);

                textLayout.addView(textView);
            }




            layout.addView(textLayout);
        }

        bottomSheetDialog.setOnShowListener(dialogInterface -> {
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });
            }
        });

        MaterialButton okButton = new MaterialButton(requireContext());
        okButton.setText("OK");
        okButton.setOnClickListener(view -> {
            StringBuilder selectedCategoriesMessage = new StringBuilder("Выбранные категории:\n");
            selectedCategories.clear();

            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    String selectedCategory = categoriesList.get(i);
                    selectedCategories.add(selectedCategory);
                    selectedCategoriesMessage.append("- ").append(selectedCategory).append("\n");
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage(selectedCategoriesMessage.toString());
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                bottomSheetDialog.dismiss();
                refreshButtons();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        MaterialButton cancelButton = new MaterialButton(requireContext());
        cancelButton.setText("Отмена");
        cancelButton.setOnClickListener(view -> bottomSheetDialog.dismiss());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(buttonPadding, buttonPadding, buttonPadding, buttonPadding);

        okButton.setLayoutParams(buttonParams);
        cancelButton.setLayoutParams(buttonParams);

        layout.addView(okButton);
        layout.addView(cancelButton);

        scrollView.addView(layout);

        bottomSheetDialog.setContentView(scrollView);
        bottomSheetDialog.show();
    }


    private void updateFilter(ImageView imageView, boolean isChecked) {
        int[] colorArray = new int[]{R.color.card1Color, R.color.card4Color, R.color.card5Color,
                R.color.card9Color, R.color.card11Color};
        int randomIndex = new Random().nextInt(colorArray.length);
        int randomColor = getResources().getColor(colorArray[randomIndex]);
        if (isChecked) {
            imageView.setColorFilter(Color.parseColor(String.format("#%06X", (0xFFFFFF & randomColor))), PorterDuff.Mode.SRC_ATOP);
        } else {
            imageView.clearColorFilter();
        }
    }

    private void refreshButtons() {
        binding.buttonContainer.removeAllViews();
        try {
            createCategoryViews(selectedCategories, categoryColors, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
