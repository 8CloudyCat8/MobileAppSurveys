package com.cloudycat.cloudyapp.ui.home;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cloudycat.cloudyapp.R;
import com.cloudycat.cloudyapp.databinding.FragmentHomeBinding;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final String[] daysOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

    private Calendar firstDayOfMonthCalendar;
    private Calendar calendar;
    private Calendar currentCalendar; // Переменная для хранения текущей даты
    private TextView currentDateTextView; // Переменная для доступа к текстовому представлению текущей даты

    String selectDayOfMonth = "01";
    String selectMonthNumber = "01";
    String jsonString = "";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Запускаем fetchInterviews() с задержкой
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchInterviews();
            }
        }, 1000);

        currentDateTextView = binding.weekTextView; // Присваиваем текстовое представление текущей даты

        calendar = Calendar.getInstance();
        currentCalendar = Calendar.getInstance(); // Инициализируем текущую дату
        setupNavigationPanel();
        setupDaysOfWeekPanel();
        setCurrentDate();

        binding.previousWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, -7); // Переключаемся на предыдущую неделю
                updateWeek();
                updateCurrentCalendar(); // Обновляем текущую дату
            }
        });

        binding.nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, 7); // Переключаемся на следующую неделю
                updateWeek();
                updateCurrentCalendar(); // Обновляем текущую дату
            }
        });
    }

    private void fetchInterviews() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "http://185.20.225.206/api/v1/interviews/page",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Получаем объект "page"
                            JSONObject pageObject = response.getJSONObject("page");
                            // Получаем массив "content"
                            JSONArray contentArray = pageObject.getJSONArray("content");

                            // Создаем новый JSON объект с ключом "interviews"
                            JSONObject newResponse = new JSONObject();
                            newResponse.put("status", response.getInt("status"));
                            newResponse.put("message", response.get("message"));
                            JSONArray interviewsArray = new JSONArray();

                            // Перемещаем содержимое массива "content" внутрь "interviews"
                            for (int i = 0; i < contentArray.length(); i++) {
                                JSONObject interviewObject = contentArray.getJSONObject(i);
                                interviewsArray.put(interviewObject);
                            }
                            newResponse.put("interviews", interviewsArray);

                            jsonString = newResponse.toString();

                            // Здесь вы можете добавить код для обработки нового JSON
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Выводим сообщение об ошибке в Toast
                        String errorMessage = "Ошибка при получении данных: ";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            errorMessage += new String(error.networkResponse.data);
                        } else {
                            errorMessage += error.toString();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

        // Добавляем запрос в очередь запросов Volley
        Volley.newRequestQueue(requireContext()).add(request);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupNavigationPanel() {
        // Можете добавить настройку внешнего вида кнопок переключения недели, если это необходимо
    }

    private void setupDaysOfWeekPanel() {
        updateWeek();

        // Добавляем обработчик для первого элемента в панели навигации
        View firstDayOfWeek = binding.daysOfWeekPanel.getChildAt(0);
        firstDayOfWeek.performClick(); // Эмулируем клик на первый элемент
    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        Calendar startOfWeek = (Calendar) currentCalendar.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
        String startDate = dateFormat.format(startOfWeek.getTime());

        Calendar endOfWeek = (Calendar) currentCalendar.clone();
        endOfWeek.set(Calendar.DAY_OF_WEEK, endOfWeek.getFirstDayOfWeek() + 6);
        String endDate = dateFormat.format(endOfWeek.getTime());

        String weekRange = startDate + " - " + endDate;
        currentDateTextView.setText(weekRange); // Устанавливаем текст текущей даты
    }

    private void updateWeek() {
        binding.daysOfWeekPanel.removeAllViews(); // Очищаем панель дней недели
        binding.blockSchedule.removeAllViews(); // Очищаем блок расписания

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

        // Определяем текущую дату для сравнения с днями недели
        Calendar currentDateCalendar = Calendar.getInstance();
        currentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentDateCalendar.set(Calendar.MINUTE, 0);
        currentDateCalendar.set(Calendar.SECOND, 0);
        currentDateCalendar.set(Calendar.MILLISECOND, 0);

        // Добавляем дни недели и даты на панель
        for (int i = 0; i < daysOfWeek.length; i++) {
            // Создаем новый объект tempCalendar для каждого дня недели
            Calendar tempCalendar = (Calendar) calendar.clone();
            tempCalendar.set(Calendar.DAY_OF_WEEK, tempCalendar.getFirstDayOfWeek()); // Устанавливаем начальный день недели
            tempCalendar.add(Calendar.DAY_OF_MONTH, i); // Добавляем смещение для получения текущего дня недели

            String dayOfWeek = daysOfWeek[i];
            String date = dateFormat.format(tempCalendar.getTime());
            TextView textView = createTextView(dayOfWeek + "\n" + date);

            // Убираем подсветку для всех дней недели
            textView.setBackgroundColor(Color.TRANSPARENT);

            // Если дата совпадает с текущей датой, подсвечиваем ее другим цветом
            Calendar compareCalendar = (Calendar) tempCalendar.clone();
            compareCalendar.set(Calendar.HOUR_OF_DAY, 0);
            compareCalendar.set(Calendar.MINUTE, 0);
            compareCalendar.set(Calendar.SECOND, 0);
            compareCalendar.set(Calendar.MILLISECOND, 0);
            if (compareCalendar.equals(currentDateCalendar)) {
                int highlightColor = ContextCompat.getColor(requireContext(), R.color.card13Color);
                textView.setBackgroundColor(highlightColor);
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // При выборе дня сначала убираем подсветку для всех дней недели
                    for (int j = 0; j < binding.daysOfWeekPanel.getChildCount(); j++) {
                        TextView dayTextView = (TextView) binding.daysOfWeekPanel.getChildAt(j);
                        dayTextView.setBackgroundColor(Color.TRANSPARENT);
                    }

                    // При выборе дня изменяем цвет фона
                    v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_300));

                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                    SimpleDateFormat monthNumberFormat = new SimpleDateFormat("MM", Locale.getDefault());

                    String dayOfMonth = dayFormat.format(tempCalendar.getTime());
                    String monthNumber = monthNumberFormat.format(tempCalendar.getTime());

                    selectDayOfMonth = dayOfMonth;
                    selectMonthNumber = monthNumber;

                    updateScheduleBlock();
                }
            });
            binding.daysOfWeekPanel.addView(textView);
        }

        // Получаем первый день месяца и его день недели
        firstDayOfMonthCalendar = (Calendar) calendar.clone();
        firstDayOfMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
    }


    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(18);
        textView.setPadding(50, 16, 16, 16);
        return textView;
    }

    private void updateCurrentCalendar() {
        currentCalendar = (Calendar) calendar.clone(); // Обновляем текущую дату
        setCurrentDate(); // Обновляем текст текущей даты
    }

    private void updateScheduleBlock() {
        // Очистить содержимое блока расписания
        binding.blockSchedule.removeAllViews();

        try {
            // Преобразовать JSON-строку в JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Получить массив 'interviews' из JSON
            JSONArray interviews = jsonObject.getJSONArray("interviews");

            for (int i = 0; i < interviews.length(); i++) {
                JSONObject interview = interviews.getJSONObject(i);
                String startDateString = interview.getString("startDate");
                String endDateString = interview.getString("endDate");

                // Разобрать даты
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                Date startDate = sdf.parse(startDateString);
                Date endDate = sdf.parse(endDateString);

                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedStartDate = outputDateFormat.format(startDate);
                String formattedEndDate = outputDateFormat.format(endDate);

                // Получить день и месяц начала интервью
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
                String day = dayFormat.format(startDate);
                String month = monthFormat.format(startDate);

                if ((day.equals(selectDayOfMonth)) && (month.equals(selectMonthNumber))) {
                    String interviewName = interview.getString("name");
                    String description = interview.getString("description");

                    // Создать текстовое представление с данными об интервью
                    TextView textView = new TextView(requireContext());
                    textView.setText(interviewName + "\n\n" +
                            description + "\n\n" +
                            "Дата начала: " + formattedStartDate + "\n" +
                            "Дата окончания: " + formattedEndDate + "\n" +
                            "Показать больше информации...");
                    textView.setTextSize(20);
                    textView.setTextColor(Color.WHITE);
                    textView.setPadding(16, 16, 16, 16);
                    textView.setGravity(Gravity.RIGHT); // Выравнивание текста слева
                    int backgroundColor = ContextCompat.getColor(requireContext(), R.color.purple_300);
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setColor(backgroundColor);
                    gradientDrawable.setCornerRadius(20f);
                    textView.setBackground(gradientDrawable);

                    // Устанавливаем жирный шрифт для наименований
                    SpannableStringBuilder builder = new SpannableStringBuilder(textView.getText());
                    builder.setSpan(new StyleSpan(Typeface.BOLD), 0, interviewName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), builder.toString().indexOf("Дата начала:"), builder.toString().indexOf("Дата начала:") + "Дата начала:".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), builder.toString().indexOf("Дата окончания:"), builder.toString().indexOf("Дата окончания:") + "Дата окончания:".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    textView.setText(builder, TextView.BufferType.SPANNABLE);

                    // Добавить слушатель нажатия на текстовое представление
                    textView.setOnClickListener(new View.OnClickListener() {
                        boolean isAdditionalInfoShown = false;

                        @Override
                        public void onClick(View v) {
                            // Создаем и настраиваем кастомное диалоговое окно
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Дополнительная информация");
                            builder.setMessage(getAdditionalInformation(interview));

                            // Добавляем кнопку "Закрыть"
                            builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            // Создаем и отображаем диалоговое окно
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                    });

                    // Добавляем кнопку записи на слот
                    int card13Color = ContextCompat.getColor(requireContext(), R.color.green_300);
                    ColorDrawable buttonColor = new ColorDrawable(card13Color);

                    // Создаем кнопку записи на слот
                    Button recordButton = new Button(requireContext());
                    recordButton.setText("Записаться");

                    // Устанавливаем цвет фона кнопки
                    recordButton.setBackground(buttonColor);

                    // Обработчик нажатия на кнопку записи на слот
                    recordButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Создаем и настраиваем кастомное диалоговое окно
                            Dialog dialog = new Dialog(requireContext());

                            // Создаем элементы управления программно
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                            TextView slotsInfoTextView = new TextView(requireContext());
                            slotsInfoTextView.setText("Доступно 5 слотов. Вы уверены, что хотите записаться?");
                            slotsInfoTextView.setPadding(20, 20, 20, 20);
                            slotsInfoTextView.setTextSize(18);
                            slotsInfoTextView.setLayoutParams(params);

                            // Создаем RadioGroup для выбора временного слота
                            RadioGroup radioGroup = new RadioGroup(requireContext());
                            radioGroup.setOrientation(RadioGroup.VERTICAL);
                            radioGroup.setLayoutParams(params);

                            // Задаем массив данных о слотах (например, время начала каждого слота)
                            String[] timeSlots = {"10:00", "11:00", "12:00", "13:00", "14:00"};

                            // Проходимся по массиву данных о слотах
                            for (String timeSlot : timeSlots) {
                                // Создаем RadioButton для каждого слота
                                RadioButton radioButton = new RadioButton(requireContext());
                                radioButton.setText(timeSlot);
                                radioButton.setLayoutParams(params);

                                // Генерируем случайные числа для количества занятых и доступных слотов
                                int totalSlots = (int) (Math.random() * 10) + 1; // Случайное количество слотов от 1 до 10
                                int occupiedSlots = (int) (Math.random() * totalSlots);
                                int freeSlots = totalSlots - occupiedSlots;

                                // Создаем TextView для отображения информации о свободных и занятых слотах
                                TextView slotInfoTextView = new TextView(requireContext());
                                slotInfoTextView.setLayoutParams(params);
                                slotInfoTextView.setText("Свободно: " + freeSlots + " из " + totalSlots);

                                // Добавляем RadioButton и TextView в RadioGroup
                                radioGroup.addView(radioButton);
                                radioGroup.addView(slotInfoTextView);
                            }

                            Button confirmButton = new Button(requireContext());
                            confirmButton.setText("Подтвердить");
                            confirmButton.setBackground(buttonColor);
                            confirmButton.setPadding(20, 20, 20, 20);
                            confirmButton.setTextSize(18);
                            confirmButton.setLayoutParams(params);
                            confirmButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            // Добавляем созданные элементы управления в диалоговое окно
                            LinearLayout dialogLayout = new LinearLayout(requireContext());
                            dialogLayout.setOrientation(LinearLayout.VERTICAL);
                            dialogLayout.addView(slotsInfoTextView);
                            dialogLayout.addView(radioGroup);
                            dialogLayout.addView(confirmButton);

                            // Устанавливаем макет в диалоговое окно
                            dialog.setContentView(dialogLayout);

                            // Отобразить кастомное диалоговое окно
                            dialog.show();
                        }
                    });


                    // Создаем вертикальный LinearLayout для размещения текстового представления и кнопки записи на слот
                    LinearLayout container = new LinearLayout(requireContext());
                    container.setOrientation(LinearLayout.VERTICAL);
                    container.addView(textView);
                    container.addView(recordButton);

                    // Добавляем созданный контейнер в блок расписания
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 20); // Добавляем отступ снизу
                    container.setLayoutParams(params);
                    binding.blockSchedule.addView(container);
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    // Метод для создания TextView с дополнительной информацией
    private void startColorAnimation(final GradientDrawable gradientDrawable) {
        // Определяем начальный и конечный цвета
        int startColor = Color.parseColor("#98d550");
        int endColor = Color.parseColor("#75bc49");

        // Создаем объект ObjectAnimator для анимации цвета
        ObjectAnimator colorAnimation = ObjectAnimator.ofArgb(gradientDrawable, "color", startColor, endColor);
        colorAnimation.setDuration(200); // Продолжительность анимации в миллисекундах
        colorAnimation.setRepeatCount(4); // Повторять анимацию 3 раза (0, 1, 2 = 3 повтора)
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE); // Анимация будет менять цвет в обратном порядке при достижении конечного цвета

        // Запускаем анимацию
        colorAnimation.start();
    }

    // Метод для получения дополнительной информации об ограничениях для респондентов
    private String getAdditionalInformation(JSONObject interview) {
        try {
            String minAge = interview.getJSONObject("respondentRestrictions").isNull("minAge") ? "Не указано" : interview.getJSONObject("respondentRestrictions").getString("minAge");
            String maxAge = interview.getJSONObject("respondentRestrictions").isNull("maxAge") ? "Не указано" : interview.getJSONObject("respondentRestrictions").getString("maxAge");
            JSONArray allowedGendersArray = interview.getJSONObject("respondentRestrictions").isNull("allowedGenders") ? new JSONArray() : interview.getJSONObject("respondentRestrictions").getJSONArray("allowedGenders");
            String allowedGenders = allowedGendersArray.length() == 0 ? "Не указано" : allowedGendersArray.join(", ").replaceAll("\"", "");
            JSONArray allowedRegionsArray = interview.getJSONObject("respondentRestrictions").isNull("allowedRegions") ? new JSONArray() : interview.getJSONObject("respondentRestrictions").getJSONArray("allowedRegions");
            String allowedRegions = allowedRegionsArray.length() == 0 ? "Не указано" : allowedRegionsArray.join(", ").replaceAll("\"", "");
            JSONArray allowedFamilyStatusArray = interview.getJSONObject("respondentRestrictions").isNull("allowedFamilyStatus") ? new JSONArray() : interview.getJSONObject("respondentRestrictions").getJSONArray("allowedFamilyStatus");
            String allowedFamilyStatus = allowedFamilyStatusArray.length() == 0 ? "Не указано" : allowedFamilyStatusArray.join(", ").replaceAll("\"", "");
            JSONArray allowedEducationArray = interview.getJSONObject("respondentRestrictions").isNull("allowedEducation") ? new JSONArray() : interview.getJSONObject("respondentRestrictions").getJSONArray("allowedEducation");
            String allowedEducation = allowedEducationArray.length() == 0 ? "Не указано" : allowedEducationArray.join(", ").replaceAll("\"", "");
            String minIncome = interview.getJSONObject("respondentRestrictions").isNull("minIncome") ? "Не указано" : String.valueOf(interview.getJSONObject("respondentRestrictions").getInt("minIncome"));
            String maxIncome = interview.getJSONObject("respondentRestrictions").isNull("maxIncome") ? "Не указано" : String.valueOf(interview.getJSONObject("respondentRestrictions").getInt("maxIncome"));

            return "Ограничения для респондентов:\n" +
                    "Минимальный возраст: " + minAge + "\n" +
                    "Максимальный возраст: " + maxAge + "\n" +
                    "Разрешенные гендеры: " + allowedGenders + "\n" +
                    "Разрешенные регионы: " + allowedRegions + "\n" +
                    "Разрешенные семейные статусы: " + allowedFamilyStatus + "\n" +
                    "Разрешенные уровни образования: " + allowedEducation + "\n" +
                    "Минимальный доход: " + minIncome + "\n" +
                    "Максимальный доход: " + maxIncome;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}