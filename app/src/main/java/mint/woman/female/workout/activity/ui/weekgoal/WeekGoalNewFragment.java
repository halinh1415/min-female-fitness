package mint.woman.female.workout.activity.ui.weekgoal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryActivity;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.custom.ProgramClickListener;
import mint.woman.female.workout.custom.SpanningLinearLayoutManager;
import mint.woman.female.workout.databinding.FragmentWeekGoalNewBinding;
import mint.woman.female.workout.model.CategoryObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.MyPreference;
import mint.woman.female.workout.utils.ProgramRecommenderV3;
import mint.woman.female.workout.utils.UserStreakResult;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class WeekGoalNewFragment extends Fragment implements IDayClickListener, ProgramClickListener {

    FragmentWeekGoalNewBinding binding;

    HistoryViewModel historyViewModel;
    MyPreference myDB;
    List<ProgramObject> programList;
    int currentAvgDuration = 10;
    List<Integer> mProgramHistory;
    DayAdapter dayAdapter;
   ProgramGoalAdapter programGoalAdapter;
    ProgramObject currentSuggestedProgram;
    Set<Integer> recentIds = new HashSet<>();


    public WeekGoalNewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWeekGoalNewBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myDB = new MyPreference(getContext());
        programList = FitnessApplication.getApplication().mJsonHelper.getAllCategory();
        historyViewModel = new ViewModelProvider(getActivity()).get(HistoryViewModel.class);
        historyViewModel.getAvgDuration().observe(getViewLifecycleOwner(), avg -> {
            if (avg == null) return;
            currentAvgDuration = avg;
            Log.d("HAHA", "Average duration: " + avg + " minutes");
        });
        initWeekGoal();
        historyViewModel.getRecentProgramIds().observe(getViewLifecycleOwner(), ids -> {
            if (ids != null) {
                recentIds.clear();
                recentIds.addAll(ids);
                Log.d("HAHA", "Recent program IDs updated: " + recentIds);
            }
        });
        historyViewModel.getUserStreak().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

//            binding.txtCoachMessage.setText(
//                    getString(R.string.streak_days, result.streakDays)
//            );
            binding.txtCoachMessage.setText(getStreakMessage(result));
            getSuggestedProgram(result.streakDays);
//            tvStreak.setText("🔥 " + result.streakDays + " ngày liên tiếp");
//            binding.txtCoachMessage.setText(getStreakMessage(result));
        });
        binding.txtSetGoal.setOnClickListener(view1 -> setGoal());
        binding.btnStartNow.setOnClickListener(view1 -> goSuggestion());


    }

    void goSuggestion() {
        if (currentSuggestedProgram.getType() == 1) {
            Intent intent = new Intent(getActivity(), ChallengeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", currentSuggestedProgram.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (currentSuggestedProgram.getType() == 2) {
            Intent intent2 = new Intent(getActivity(), PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", currentSuggestedProgram.getId());
            bundle.putInt("DAY_NUMBER", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }

    private void initWeekGoal() {
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 1);
        long nowStartOfDayMillis = now.getTimeInMillis();

        SimpleDateFormat weekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        int firstDayOfWeek = myDB.getFirstDayOfWeek();
        cal.setFirstDayOfWeek(firstDayOfWeek);
        cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);

        // Fix: Nếu ngày đầu tuần tính ra nằm SAU hôm nay, lùi lại 7 ngày
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() > nowStartOfDayMillis) {
            cal.add(Calendar.DAY_OF_MONTH, -7);
        }

        long firstDayOfWeekMillis = cal.getTimeInMillis();

        List<String> dayOfMonth = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        boolean[] status = new boolean[7]; // Mảng trạng thái cho 7 ngày trong tuần
        int currentDayIndex = 0; // Vị trí của ngày hiện tại trong mảng 7 ngày

        for (int i = 0; i < 7; i++) {
            labels.add(weekFormat.format(cal.getTime()));
            dayOfMonth.add(dayFormat.format(cal.getTime()));
            // So sánh mili giây của 00:00:00 để tìm ngày hiện tại
            if (cal.getTimeInMillis() == nowStartOfDayMillis) {
                currentDayIndex = i;
            }
            cal.add(Calendar.DATE, 1);
            status[i] = false;
        }

        long lastDayOfWeekMillis = cal.getTimeInMillis();
        dayAdapter = new DayAdapter(labels, dayOfMonth, this);
        dayAdapter.setNow(currentDayIndex); // Đặt ngày hiện tại để tô sáng
        binding.rcWeekGoal.setAdapter(dayAdapter);
        binding.rcWeekGoal.setLayoutManager(new SpanningLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mProgramHistory = new ArrayList<>();
        programGoalAdapter = new ProgramGoalAdapter(getContext(), this);
        binding.rcProgramGoal.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcProgramGoal.setAdapter(programGoalAdapter);
        binding.rcProgramGoal.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Refresh weekly progress data
        historyViewModel.refreshWeeklyProgress(myDB.getGoalOfWeek(), myDB.getFirstDayOfWeek());

        // Observe weekly progress từ HistoryViewModel
        historyViewModel.getWeeklyProgress().observe(getViewLifecycleOwner(), weeklyProgress -> {
            if (weeklyProgress == null) return;

            // Đặt lại status về false cho mỗi lần dữ liệu mới được tải
            for (int i = 0; i < status.length; i++) {
                status[i] = false;
            }
            mProgramHistory.clear();

            // Cập nhật status[] từ completedDaysList
            if (weeklyProgress.completedDaysList != null) {
                for (Long completedDateId : weeklyProgress.completedDaysList) {
                    long diffMillis = completedDateId - firstDayOfWeekMillis;
                    int index = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);

                    if (index >= 0 && index < 7) {
                        status[index] = true;
                    }
                }
            }

            // Lấy danh sách program từ history items
            historyViewModel.findFromTo(firstDayOfWeekMillis, lastDayOfWeekMillis).observe(getViewLifecycleOwner(), historyItemList -> {
                if (historyItemList != null) {
                    for (HistoryItem historyItem : historyItemList) {
                        int programId = historyItem.getProgramId();
                        if (recentIds.size() < 2 && !recentIds.contains(programId)) {
                            recentIds.add(programId);
                        }
                        if (!mProgramHistory.contains(programId)) {
                            mProgramHistory.add(programId);
                        }
                    }
                }

                int sizeProgram = mProgramHistory.size();
                if (sizeProgram > 0) {
                    int maxSub = Math.max(0, sizeProgram - 3);
                    programGoalAdapter.setProgram(getProgram(mProgramHistory.subList(maxSub, sizeProgram)));
                } else {
                    programGoalAdapter.setProgram(new ArrayList<>());
                }
            });

            dayAdapter.setStatus(status);
            binding.txtGoalProgress.setText(String.format("%d/%d", weeklyProgress.completedDays, weeklyProgress.goalDays));
        });

        // ...existing code...

        binding.rcWeekGoal.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HistoryActivity.class));
        });
    }

    private List<ProgramObject> getProgram(List<Integer> list) {

        HashMap<Integer, ProgramObject> hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        List<ProgramObject> programs = new ArrayList<>();
        for (int id : list
        ) {
            ProgramObject programObject = hashMap.get(id);
            if (programObject != null) programs.add(programObject);
        }

        return programs;
    }


    void goHistory() {
        FirebaseAnalytics.getInstance(getContext()).logEvent("click_layout_this_week_scr_home", new Bundle());
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }


    void setGoal() {
        FirebaseAnalytics.getInstance(getContext()).logEvent("click_txt_set_goal_scr_home", new Bundle());

        startActivityForResult(new Intent(getActivity(), SetGoalActivity.class), 999);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (resultCode == Activity.RESULT_OK) {
                initWeekGoal();
            } else {
                Log.d("HAHA", "NO Change WeekGOAL!!");
            }
        }
    }

    public void getSuggestedProgram(int streakScore) {
        ProgramRecommenderV3 recommender = new ProgramRecommenderV3();
        String timeOfDay = getTimeOfDayBased();
        int dayOff = getDayOffSafe(myDB.getLastWorkout());
        ProgramObject suggested = recommender.suggest(
                programList,
                streakScore,
                true,
                timeOfDay,
                recentIds,
                dayOff,
                currentAvgDuration,
                null,//myDB.getUserTargetFocus(),   // Không có mục tiêu cụ thể
                false//myDB.requiresSeated()   // Không bắt buộc phải ngồi
        );

        if (suggested != null) {
            currentSuggestedProgram = suggested;
            historyViewModel.setSuggestProgram(currentSuggestedProgram);
            bindProgramToUI(suggested, streakScore);
        }
    }
    public String getTimeOfDayBased() {
        java.time.LocalTime now = java.time.LocalTime.now();
        int hour = now.getHour();

        if (hour >= 5 && hour < 12) {
            return ProgramRecommenderV3.TIME_MORNING;
        } else if (hour >= 12 && hour < 17) {
            return ProgramRecommenderV3.TIME_AFTERNOON;
        } else {
            return ProgramRecommenderV3.TIME_EVENING;
        }
    }
    public int getDayOffSafe(long lastWorkoutTime) {
        if (lastWorkoutTime <= 0) return 999;

        java.time.LocalDate lastDate = java.time.Instant.ofEpochMilli(lastWorkoutTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        java.time.LocalDate today = java.time.LocalDate.now();

        return (int) java.time.temporal.ChronoUnit.DAYS.between(lastDate, today);
    }

    public void bindProgramToUI(ProgramObject todayWorkout, int streakScore) {
//        binding.btnStartNow.setText(""+program.name);
        binding.tvProgramName.setText(todayWorkout.getName());
        String premiumIcon = (todayWorkout.getPremium() == 1) ? "👑 " : "";

        // 3. Dịch Level từ số (1, 2, 3) sang chữ Tiếng Việt
        String levelText = "";
        switch (todayWorkout.getLevel()) {
            case 1:
                levelText = getString(R.string.beginner);
                break;
            case 2:
                levelText =  getString(R.string.intermediate);
                break;
            case 3:
                levelText =getString(R.string.advanced);
                break;
            default:
                levelText = getString(R.string.any_level);
        }

        // 4. Lấy mục tiêu (Focus) - Có thể tự viết thêm switch-case để dịch nếu muốn
        String focusText = todayWorkout.getFocus(); // Sẽ lấy chữ "Energy Flow"

        // 5. GHÉP CHUỖI LẠI VỚI NHAU
        // Kết quả mong muốn: "👑 ⏱ 15 phút  •  Cơ bản  •  Energy Flow"
        String infoText = premiumIcon + "⏱ " +getString(R.string.x_minutes,todayWorkout.getTotal())+ "  •  " + levelText + "  •  " +"🧘‍"+ focusText;
        binding.tvProgramInfo.setText(infoText);
    }

    public String getStreakMessage(UserStreakResult result) {
        switch (result.messageType) {
            case START_NEW:
                return getString(R.string.streak_start_new);

            case KEEP_TODAY:
                return getString(R.string.streak_kept_today);

            case NEED_TODAY:
                return getString(R.string.streak_need_today);

            case BROKEN_RECENTLY:
                return getString(R.string.streak_broken_recently);

            case RESUMED:
                return getString(R.string.streak_resumed);

            case MILESTONE_ACHIEVED:
                // Use streakDays to format the milestone message
                return getString(R.string.streak_milestone, result.streakDays);

            case AT_RISK:
                return getString(R.string.streak_at_risk);

            case PAUSED:
                return getString(R.string.streak_paused);

            case UNKNOWN:
            default:
                return getString(R.string.streak_unknown);
        }
    }

    @Override
    public void goDay() {
        FirebaseAnalytics.getInstance(getContext()).logEvent("click_day_in_week_goal_scr_home", new Bundle());

        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }


    @Override
    public void onProgramClick(ProgramObject programObject) {
        if (programObject.getType() == 1) {
            Intent intent = new Intent(getActivity(), ProgramActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", programObject.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (programObject.getType() == 2) {
            Intent intent2 = new Intent(getActivity(), PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", programObject.getId());
            bundle.putInt("DAY_NUMBER", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }
}