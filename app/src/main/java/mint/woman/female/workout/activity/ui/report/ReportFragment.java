package mint.woman.female.workout.activity.ui.report;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.custom.DialogWeight;
import mint.woman.female.workout.databinding.FragmentReportBinding;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.notification.AppNotificationHelper;
import mint.woman.female.workout.notification.NotificationPermissionDialog;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.WeeklyProgressResult;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressLint("NonConstantResourceId")
public class ReportFragment extends Fragment implements View.OnClickListener {
    FragmentReportBinding binding;
    HistoryViewModel mHistoryViewModel;
    MyPreference myDB;
    float targetWeight = 70.1f;
    int currentStreak = 0;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDB = MyPreference.newInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHistoryViewModel = new ViewModelProvider(getActivity()).get(HistoryViewModel.class);
        initView();
        initData();
    }

    private void initView() {
        // Refresh weight trends
        mHistoryViewModel.refreshWeightTrends();

        // Refresh calories trends and chart data
        mHistoryViewModel.refreshCaloriesTrends();
        mHistoryViewModel.refreshCaloriesChartData();

        // Refresh today workout stats
        mHistoryViewModel.refreshTodayWorkout();

        // Refresh weekly progress
        mHistoryViewModel.refreshWeeklyProgress(myDB.getGoalOfWeek(), myDB.getFirstDayOfWeek());

        mHistoryViewModel.getUserStreak().observe(getViewLifecycleOwner(), userStreakResult -> {
            if (userStreakResult != null) {
                binding.tvWorkoutStreak.setText("" + userStreakResult.streakDays);
                currentStreak = userStreakResult.streakDays;
            }
        });

        // Observe today workout stats
        mHistoryViewModel.getTodayWorkout().observe(getViewLifecycleOwner(), todayWorkout -> {
            if (todayWorkout != null) {
                binding.tvWorkoutCount.setText(todayWorkout.workoutCount + " " + getString(R.string.txt_exercise));
                binding.tvTodayCalories.setText("" + todayWorkout.totalCalories + " kcal");
                binding.tvWorkoutDuration.setText(formatDurationToMinute(todayWorkout.totalDuration));

                // Cập nhật xu hướng thời gian tập luyện hôm nay
                binding.tvTodayDurationTrend.setText(String.format("%s %s (%+.1f%%)",
                        todayWorkout.durationTrendText,
                        formatDurationToMinute(todayWorkout.durationTrend),
                        todayWorkout.durationTrendPercent));

                // Cập nhật xu hướng calories hôm nay
                binding.tvTodayCaloriesTrend.setText(String.format("%s %d kcal (%+.1f%%)",
                        todayWorkout.caloriesTrendText,
                        todayWorkout.caloriesTrend,
                        todayWorkout.caloriesTrendPercent));
            }
        });

        // Observe weekly progress
        mHistoryViewModel.getWeeklyProgress().observe(getViewLifecycleOwner(), weeklyProgress -> {
            if (weeklyProgress != null) {
                binding.progressWeek.setMax(weeklyProgress.goalDays);
                binding.progressWeek.setProgress(weeklyProgress.completedDays);
                binding.progressWeekText.setText(weeklyProgress.completedDays + "/" + weeklyProgress.goalDays);
                binding.tvWeekCalories.setText("" + weeklyProgress.totalCalories + " kcal");
                binding.tvWeekDuration.setText(formatDurationToMinute(weeklyProgress.totalDuration));

                // Cập nhật trend: Thời lượng luyện tập và Calories
                updateWeeklyTrend(weeklyProgress);
            }
        });

        String stringWeight = String.format("%.1f %s", myDB.getCurrentWeight(), myDB.getWeightUnit());
        SpannableString contentWeight = new SpannableString(stringWeight);
        contentWeight.setSpan(new UnderlineSpan(), 0, stringWeight.length(), 0);
        binding.tvCurrentWeight.setText(contentWeight);

        // Click listener for 7 days
        binding.ll7dayChange.setOnClickListener(v -> {
            mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
                drawWeightChart(userObjects, 7);
            });
            // Cập nhật weight trend 7 days
            mHistoryViewModel.getWeightTrend7Days().observe(getViewLifecycleOwner(), weightTrendResult -> {
                updateWeekChangeDisplay(weightTrendResult, 7);
            });
            updateButtonStyles(7);
        });

        // Click listener for 30 days
        binding.ll30dayChange.setOnClickListener(v -> {
            mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
                drawWeightChart(userObjects, 30);
            });
            // Cập nhật weight trend 30 days
            mHistoryViewModel.getWeightTrend30Days().observe(getViewLifecycleOwner(), weightTrendResult -> {
                updateWeekChangeDisplay(weightTrendResult, 30);
            });
            updateButtonStyles(30);
        });

        // Click listener for 90 days
        binding.ll90dayChange.setOnClickListener(v -> {
            mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
                drawWeightChart(userObjects, 90);
            });
            // Cập nhật weight trend 90 days
            mHistoryViewModel.getWeightTrend90Days().observe(getViewLifecycleOwner(), weightTrendResult -> {
                updateWeekChangeDisplay(weightTrendResult, 90);
            });
            updateButtonStyles(90);
        });

        // ===== CALORIES CHART LISTENERS =====
        // Click listener for 7 days calories
        binding.ll7dayChangeCalories.setOnClickListener(v -> {
            mHistoryViewModel.getCaloriesChartData7Days().observe(getViewLifecycleOwner(), caloriesData -> {
                drawCaloriesChart(caloriesData, 7);
            });
            // Cập nhật calories trend 7 days
            mHistoryViewModel.getCaloriesTrend7Days().observe(getViewLifecycleOwner(), caloriesTrendResult -> {
                updateCaloriesTrendDisplay(caloriesTrendResult, 7);
            });
            updateCaloriesButtonStyles(7);
        });

        // Click listener for 30 days calories
        binding.ll30dayChangeCalories.setOnClickListener(v -> {
            mHistoryViewModel.getCaloriesChartData30Days().observe(getViewLifecycleOwner(), caloriesData -> {
                drawCaloriesChart(caloriesData, 30);
            });
            // Cập nhật calories trend 30 days
            mHistoryViewModel.getCaloriesTrend30Days().observe(getViewLifecycleOwner(), caloriesTrendResult -> {
                updateCaloriesTrendDisplay(caloriesTrendResult, 30);
            });
            updateCaloriesButtonStyles(30);
        });

        // Click listener for 90 days calories
        binding.ll90dayChangeCalories.setOnClickListener(v -> {
            mHistoryViewModel.getCaloriesChartData90Days().observe(getViewLifecycleOwner(), caloriesData -> {
                drawCaloriesChart(caloriesData, 90);
            });
            // Cập nhật calories trend 90 days
            mHistoryViewModel.getCaloriesTrend90Days().observe(getViewLifecycleOwner(), caloriesTrendResult -> {
                updateCaloriesTrendDisplay(caloriesTrendResult, 90);
            });
            updateCaloriesButtonStyles(90);
        });

        // Set default display to 7 days for calories
        updateCaloriesButtonStyles(7);
        mHistoryViewModel.getCaloriesChartData7Days().observe(getViewLifecycleOwner(), caloriesData -> {
            drawCaloriesChart(caloriesData, 7);
        });
        mHistoryViewModel.getCaloriesTrend7Days().observe(getViewLifecycleOwner(), caloriesTrendResult -> {
            if (caloriesTrendResult != null) {
                updateCaloriesTrendDisplay(caloriesTrendResult, 7);
            }
        });

        mHistoryViewModel.getSuggestProgram().observe(getViewLifecycleOwner(), suggestProgram -> {
            if (suggestProgram != null) {
                String programText = suggestProgram.getName() + " - " + getString(R.string.x_minutes, suggestProgram.getTotal());
                String streakText = getString(R.string.streak_x_day_target, currentStreak + 1);

                binding.tvNextProgramSuggestion.setText(programText);
                binding.tvStreakTarget.setText(streakText);

                // ✅ Thêm vào lịch chương trình tiếp theo
                binding.btnScheduleNext.setOnClickListener(v -> {
                    Log.d("🎯 ScheduleNext", "📅 Adding program to schedule: " + suggestProgram.getName());

                    // Thêm vào lịch luyện tập tiếp theo (ngày mai)
                    scheduleNextProgram(suggestProgram);
                });
            }
        });


        binding.bmiView.setGender(myDB.getGender());
        binding.bmiView.setWeight(myDB.getCurrentWeightKG());
        binding.bmiView.setHeight(myDB.getCurrentHeightCm() / 100);
        binding.txtStatusBmi.setText(binding.bmiView.getBodyDescription());
        binding.icEditBmi.setOnClickListener(this::onClick);

        // Observe weight changed event
        mHistoryViewModel.getWeightChanged().observe(getViewLifecycleOwner(), changed -> {
            if (changed) {
                onWeightChange();
            }
        });
    }

    private void initData() {
        if (binding.lineChart.getData() != null) binding.lineChart.clearValues();

        // ===== THÊM DỮ LIỆU TEST 3 THÁNG =====
        // Bỏ comment dòng dưới để thêm dữ liệu test
        //  insertThreeMonthsTestData();
//        mHistoryViewModel.deleteAllUsers();
    //    insertOneMonthTestData();

        // Sau khi insert, đợi 1 giây rồi load dữ liệu
        mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
            // Default display for 7 days
            drawWeightChart(userObjects, 7);
        });

        // Mặc định hiển thị weight trend 7 days khi start
        mHistoryViewModel.getWeightTrend7Days().observe(getViewLifecycleOwner(), weightTrendResult -> {
            if (weightTrendResult != null) {
                updateWeekChangeDisplay(weightTrendResult, 7);
            }
        });

        float currentHeight = myDB.getCurrentHeightM();
        float targetWeight = currentHeight * currentHeight * 22.9f;
        if (!myDB.isKg()) {
            targetWeight *= 2.20462f;
        }
        binding.tvWeekGoal.setText(getString(R.string.target_x_kg, targetWeight, myDB.getWeightUnit()));
    }

    private void drawWeightChart(List<UserObject> userObjects, int days) {
        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<String> dateLabels = new ArrayList<>();
        float min = 1000.0f, max = 0.0f;
        long referenceMillis;
        long dayInMillis = TimeUnit.DAYS.toMillis(1);
        long cutoffMillis = System.currentTimeMillis() - (days * dayInMillis);

        if (userObjects != null && userObjects.size() > 0) {
            referenceMillis = (long) userObjects.get(0).getDate();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            referenceMillis = calendar.getTimeInMillis();
        }

        binding.lineChart.setTag(referenceMillis);

        if (userObjects != null && userObjects.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            int entryIndex = 0;

            for (UserObject userObject : userObjects) {
                long objectTime = (long) userObject.getDate();

                if (objectTime >= cutoffMillis) {
                    float y = myDB.isKg() ? userObject.getWeight() : userObject.getLbs();
                    if (y > max) max = y;
                    if (y < min) min = y;
                    float daysSinceRef = (objectTime - referenceMillis) / (float) dayInMillis;
                    values.add(new Entry(entryIndex, y));
                    String dateLabel = sdf.format(objectTime);
                    dateLabels.add(dateLabel);
                    entryIndex++;
                }
            }
        } else {
            values.add(new Entry(0, 65));
            dateLabels.add("Today");
        }

        LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_weight_chart));
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set1.setColor(getResources().getColor(R.color.colorWorkout));
        set1.setValueTextColor(getResources().getColor(R.color.colorText));
        set1.setLineWidth(2.f);
        set1.setDrawCircles(true);
        set1.setDrawValues(true);
        set1.setFillAlpha(100);
        set1.setHighLightColor(getResources().getColor(R.color.DarkGreen));
        set1.setDrawCircleHole(true);
        LineData data = new LineData(set1);
        data.setValueTextColor(getResources().getColor(R.color.colorText));
        data.setValueTextSize(9f);

        binding.lineChart.setData(data);

        // Xóa description label
        Description description = new Description();
        description.setEnabled(false);
        binding.lineChart.setDescription(description);

        // Cấu hình trục X để hiển thị ngày tháng
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextSize(10f);

        binding.lineChart.setVisibleXRangeMaximum(Math.min(10, dateLabels.size()));
        binding.lineChart.moveViewToX(binding.lineChart.getXChartMax());
    }

    private void updateWeekChangeDisplay(mint.woman.female.workout.utils.WeightTrendResult result, int days) {
        String unit = myDB.getWeightUnit();
        String trendText = "";

        if (result.trend.equals("UP")) {
            trendText = getString(R.string.up_weight_trend, result.weightChange, unit, days);
        } else if (result.trend.equals("DOWN")) {
            trendText = getString(R.string.down_weight_trend, result.weightChange, unit, days);
        } else {
            switch (days) {
                case 7:
                    trendText = getString(R.string.stable_over_past_7d);

                    break;
                case 30:
                    trendText = getString(R.string.stable_over_past_30d);
                    break;
                case 90:
                    trendText = getString(R.string.stable_over_past_90d);
                    break;
            }
        }
        binding.tvWeightStability.setText(trendText);
        binding.tvWeekGoal.setText(String.format("Current: %.1f %s | Target: %.1f %s", myDB.getCurrentWeight(), unit, targetWeight, unit));
    }

    private void drawCaloriesChart(Map<String, Integer> caloriesData, int days) {
        if (binding.caloriesChart == null) {
            return;
        }

        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<String> dateLabels = new ArrayList<>();

        if (caloriesData != null && !caloriesData.isEmpty()) {
            int entryIndex = 0;
            for (Map.Entry<String, Integer> entry : caloriesData.entrySet()) {
                String dateString = entry.getKey(); // Format: yyyy-MM-dd
                int calories = entry.getValue();

                values.add(new BarEntry(entryIndex, calories));

                // Format: dd/MM
                String[] parts = dateString.split("-");
                if (parts.length >= 3) {
                    dateLabels.add(parts[2] + "/" + parts[1]);
                } else {
                    dateLabels.add(dateString);
                }
                entryIndex++;
            }
        } else {
            // Dữ liệu trống
            values.add(new BarEntry(0, 0));
            dateLabels.add("No data");
        }

        BarDataSet set1 = new BarDataSet(values, getString(R.string.txt_calories_burn));
        set1.setColor(getResources().getColor(R.color.colorWorkout));
        set1.setValueTextColor(getResources().getColor(R.color.colorText));
        set1.setDrawValues(true);
        set1.setValueTextSize(9f);

        BarData data = new BarData(set1);
        data.setBarWidth(0.9f);

        binding.caloriesChart.setData(data);

        // Xóa description label
        Description description = new Description();
        description.setEnabled(false);
        binding.caloriesChart.setDescription(description);

        // Cấu hình trục X để hiển thị ngày tháng
        XAxis xAxis = binding.caloriesChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextSize(10f);

        // Cấu hình trục Y
        YAxis leftAxis = binding.caloriesChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0);

        YAxis rightAxis = binding.caloriesChart.getAxisRight();
        rightAxis.setEnabled(false);

        binding.caloriesChart.invalidate();
    }

    private void updateCaloriesTrendDisplay(mint.woman.female.workout.utils.CaloriesTrendResult result, int days) {
        String trendText = "";

        if (result.caloriesTrendText.equals("UP")) {
            trendText = getString(R.string.up_calories_trend, result.caloriesTrend, days, result.caloriesTrendPercent);
        } else if (result.caloriesTrendText.equals("DOWN")) {
            trendText = getString(R.string.down_calories_trend, Math.abs(result.caloriesTrend), days, Math.abs(result.caloriesTrendPercent));
        } else {
            switch (days) {
                case 7:
                    trendText = getString(R.string.stable_over_past_7d);
                    break;
                case 30:
                    trendText = getString(R.string.stable_over_past_30d);
                    break;
                case 90:
                    trendText = getString(R.string.stable_over_past_90d);
                    break;
            }
        }
        binding.tvCaloriesStability.setText(trendText);
    }

    private void updateCaloriesButtonStyles(int selectedDays) {
        // Reset all button styles
        binding.ll7dayChangeCalories.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.ll30dayChangeCalories.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.ll90dayChangeCalories.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tv7dayChangeCalories.setTextColor(getResources().getColor(R.color.colorText));
        binding.tv30dayChangeCalories.setTextColor(getResources().getColor(R.color.colorText));
        binding.tv3mChangeCalories.setTextColor(getResources().getColor(R.color.colorText));

        // Highlight selected button
        switch (selectedDays) {
            case 7:
                binding.ll7dayChangeCalories.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv7dayChangeCalories.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 30:
                binding.ll30dayChangeCalories.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv30dayChangeCalories.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 90:
                binding.ll90dayChangeCalories.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv3mChangeCalories.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }

    private void updateButtonStyles(int selectedDays) {
        // Reset all button styles
        binding.ll7dayChange.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.ll30dayChange.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        binding.ll90dayChange.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tv7dayChange.setTextColor(getResources().getColor(R.color.colorText));
        binding.tv30dayChange.setTextColor(getResources().getColor(R.color.colorText));
        binding.tv3mChange.setTextColor(getResources().getColor(R.color.colorText));

        // Highlight selected button
        switch (selectedDays) {
            case 7:
                binding.ll7dayChange.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv7dayChange.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 30:
                binding.ll30dayChange.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv30dayChange.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case 90:
                binding.ll90dayChange.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                binding.tv3mChange.setTextColor(getResources().getColor(android.R.color.white));
                break;
        }
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 3600;
        int seconds = (totalSeconds % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String formatDurationToMinute(int totalSeconds) {
        int minutes = totalSeconds / 60;
        return getString(R.string.x_minutes, minutes);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ic_edit_bmi:
                DialogWeight mDialogWeight = new DialogWeight();
                mDialogWeight.show(getParentFragmentManager(), "enter_weight");
                break;
        }
    }


    public void onWeightChange() {
        // Cập nhật cân nặng hiện tại
        String stringWeight = String.format("%.1f %s", myDB.getCurrentWeight(), myDB.getWeightUnit());
        SpannableString contentWeight = new SpannableString(stringWeight);
        contentWeight.setSpan(new UnderlineSpan(), 0, stringWeight.length(), 0);
        binding.tvCurrentWeight.setText(contentWeight);

        // Cập nhật mục tiêu cân nặng
        float currentHeight = myDB.getCurrentHeightM();
        float newTargetWeight = currentHeight * currentHeight * 22.9f;
        if (!myDB.isKg()) {
            newTargetWeight *= 2.20462f;
        }
        targetWeight = newTargetWeight;
        binding.tvWeekGoal.setText(getString(R.string.target_x_kg, newTargetWeight, myDB.getWeightUnit()));

        // Refresh dữ liệu và vẽ lại đồ thị
        mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
            drawWeightChart(userObjects, 7);
        });

        // Refresh weight trends
        mHistoryViewModel.refreshWeightTrends();
    }

    /**
     * Thêm dữ liệu test UserObject 3 tháng (45 bản ghi)
     * Mỗi 2 ngày 1 lần, cân nặng giảm từ 85kg → 73.4kg
     * Dùng HistoryViewModel.insertUser() để insert vào database
     */
    private void insertThreeMonthsTestData() {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            // Lùi lại 90 ngày
            calendar.add(Calendar.DAY_OF_YEAR, -90);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            float startWeight = 85.0f;

            // Thêm 45 bản ghi (90 ngày / 2 = 45 lần)
            for (int i = 0; i < 45; i++) {
                // Tính cân nặng giảm dần (0.15kg mỗi 2 ngày)
                float weight = startWeight - (i * 0.15f);

                UserObject userObject = new UserObject();
                userObject.setWeight(weight);
                userObject.setHeight(180.0f);
                userObject.setDate(calendar.getTimeInMillis());

                // Insert vào database thông qua HistoryViewModel
                mHistoryViewModel.insertUser(userObject);

                // Di chuyển đến 2 ngày tiếp theo
                calendar.add(Calendar.DAY_OF_YEAR, 2);
            }
        }).start();
    }

    /**
     * Thêm dữ liệu test UserObject 1 tháng (15 bản ghi)
     * Mỗi 2 ngày 1 lần
     */
    private void insertOneMonthTestData() {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            float startWeight = 80.0f;

            for (int i = 0; i < 15; i++) {
                float weight = 60.0f + (float) Math.random() * 10.0f; // Cân nặng ngẫu nhiên từ 30kg đến 40kg

                UserObject userObject = new UserObject();
                userObject.setWeight(weight);
                userObject.setHeight(180.0f);
                userObject.setDate(calendar.getTimeInMillis());

                mHistoryViewModel.insertUser(userObject);

                calendar.add(Calendar.DAY_OF_YEAR, 3);
            }
        }).start();
    }

    /**
     * Thêm dữ liệu test UserObject 3 tháng với biến động thực tế
     * Mô phỏng lên xuống tự nhiên (hormone, ăn uống, tập luyện)
     */
    private void insertThreeMonthsWithFluctuations() {
        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -90);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            float baseWeight = 75.0f;

            for (int i = 0; i < 45; i++) {
                // Xu hướng chính: giảm 0.15kg mỗi 2 ngày
                float trend = -0.15f * i;

                // Biến động: ±1.5kg (mô phỏng ảnh hưởng hormone, ăn uống)
                float fluctuation = 1.5f * (float) Math.sin(i * 0.5f);

                float weight = baseWeight + trend + fluctuation;

                UserObject userObject = new UserObject();
                userObject.setWeight(weight);
                userObject.setHeight(180.0f);
                userObject.setDate(calendar.getTimeInMillis());

                mHistoryViewModel.insertUser(userObject);

                calendar.add(Calendar.DAY_OF_YEAR, 3);
            }
        }).start();
    }

    /**
     * Cập nhật trend thời lượng luyện tập và calories tuần này
     * Hiển thị trên tv_week_duration_trend và tv_week_calories_trend
     */
    private void updateWeeklyTrend(WeeklyProgressResult weeklyProgress) {
        if (weeklyProgress == null) {
            return;
        }

        // Tính toán trend: so sánh với tuần trước
        // Lấy dữ liệu từ getTodayWorkout để so sánh
        mHistoryViewModel.getTodayWorkout().observe(getViewLifecycleOwner(), todayWorkout -> {
            if (todayWorkout != null) {
                // Tính trend thời lượng
                String durationTrendText = "";
                if (todayWorkout.durationTrendPercent > 0) {
                    durationTrendText = String.format("⬆ + %s (%+.1f%%)",
                            formatDurationToMinute((int) todayWorkout.durationTrend),
                            todayWorkout.durationTrendPercent);
                } else if (todayWorkout.durationTrendPercent < 0) {
                    durationTrendText = String.format("⬇ - %s (%.1f%%)",
                            formatDurationToMinute((int) Math.abs(todayWorkout.durationTrend)),
                            Math.abs(todayWorkout.durationTrendPercent));
                } else {
                    durationTrendText = getString(R.string.stable);
                }

                // Tính trend calories
                String caloriesTrendText = "";
                if (todayWorkout.caloriesTrendPercent > 0) {
                    caloriesTrendText = String.format("⬆ + %d kcal (%+.1f%%)",
                            todayWorkout.caloriesTrend,
                            todayWorkout.caloriesTrendPercent);
                } else if (todayWorkout.caloriesTrendPercent < 0) {
                    caloriesTrendText = String.format("⬇ - %d kcal (%.1f%%)",
                            Math.abs(todayWorkout.caloriesTrend),
                            Math.abs(todayWorkout.caloriesTrendPercent));
                } else {
                    durationTrendText = getString(R.string.stable);
                }

                // Cập nhật TextViews
                binding.tvWeekDurationTrend.setText(durationTrendText);
                binding.tvWeekCaloriesTrend.setText(caloriesTrendText);
            }
        });
    }

    /**
     * 📅 Schedule chương trình cho ngày mai
     * - Kiểm tra quyền POST_NOTIFICATIONS trước
     * - Chỉ tạo thông báo nhắc nhở (KHÔNG thêm vào HistoryItem)
     * - Nhắc nhở người dùng tập ngày mai
     */
    private void scheduleNextProgram(ProgramObject program) {
        // ✅ Kiểm tra quyền POST_NOTIFICATIONS trước tiên
        if (!hasNotificationPermission()) {
            Log.d("🎯 ScheduleNext", "⚠️ Notification permission not granted");
            return;
        }

        // Set thời gian cho ngày mai (8:00 AM)
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 8);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        Log.d("🎯 ScheduleNext", "📅 Creating reminder for tomorrow 8:00 AM: " + program.getName());

        // Hiển thị thông báo nhắc nhở
        showScheduleReminder(program, tomorrow);
    }

    /**
     * ✅ Kiểm tra quyền POST_NOTIFICATIONS
     * Nếu chưa có, hiển thị dialog xin quyền bằng AppNotificationHelper
     * @return true nếu có quyền, false nếu chưa có
     */
    private boolean hasNotificationPermission() {
        // Android 13 (API 33) trở lên cần kiểm tra quyền POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d("🎯 ScheduleNext", "⚠️ Requesting POST_NOTIFICATIONS permission using AppNotificationHelper");

                // ✅ Sử dụng AppNotificationHelper để hiển thị dialog xin quyền
                NotificationPermissionDialog.showPracticeReminderDialog(getActivity());
                return false;
            }
        }
        return true;
    }

    /**
     * 📢 Hiển thị thông báo nhắc nhở tập ngày mai với option chọn thời gian
     */
    private void showScheduleReminder(ProgramObject program, Calendar tomorrow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.schedule_reminder_title))
                .setMessage(getString(
                        R.string.schedule_reminder_message,
                        program.getName(),
                        program.getTotal(),
                        currentStreak
                ))
                .setPositiveButton(getString(R.string.schedule_pick_time), (dialog, which) -> {
                    dialog.dismiss();
                    AppNotificationHelper.scheduleNotificationWithTimePicker(
                            getContext(),
                            program.getName(),
                            program.getTotal(),
                            (hour, minute) -> {
                                Toast.makeText(
                                        getContext(),
                                        getString(R.string.schedule_time_selected_toast, hour, minute),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                    );
                })
                .setNegativeButton(getString(R.string.schedule_default_time_button), (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(
                            getContext(),
                            getString(R.string.schedule_default_time_toast),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

}
