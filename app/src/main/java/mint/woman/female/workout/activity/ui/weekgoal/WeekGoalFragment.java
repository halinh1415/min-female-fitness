package mint.woman.female.workout.activity.ui.weekgoal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.activity.ui.history.HistoryActivity;
import mint.woman.female.workout.activity.ui.home.HomeViewModel;
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity;
import mint.woman.female.workout.activity.ui.workout.ProgramActivity;
import mint.woman.female.workout.custom.ProgramClickListener;
import mint.woman.female.workout.custom.SpanningLinearLayoutManager;
import mint.woman.female.workout.databinding.FragmentWeekGoalBinding;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WeekGoalFragment extends Fragment implements ProgramClickListener, IDayClickListener {
    int[] firstDayOfWeeks = new int[]{Calendar.MONDAY, Calendar.SATURDAY, Calendar.SUNDAY, Calendar.THURSDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY};
    FragmentWeekGoalBinding binding;
//    @BindView(R.id.rc_week_goal)
//    RecyclerView rcWeekGoal;
//    @BindView(R.id.rc_program_goal)
//    RecyclerView rcProgramGoal;
//    @BindView(R.id.txt_goal_progress)
//    TextView tvGoalProgress;
//    @BindView(R.id.txt_total_time)
//    TextView mTotalTime;
//    @BindView(R.id.txt_workout_number)
//    TextView mWorkouts;
//    @BindView(R.id.txt_calories)
//    TextView mCalories;
//    @BindView(R.id.lnSetGoal)
//    View lnSetGoal;
//    @BindView(R.id.lnHistory)
//    View lnHistory;
//    @BindView(R.id.txt_records)
//    TextView mRecords;
//    @BindView(R.id.txt_goal_title)
//    TextView mGoalTitle;
    MyPreference myPreference;
    HomeViewModel homeViewModel;

    DayAdapter dayAdapter;
    ProgramGoalAdapter programGoalAdapter;
    List<Integer> mProgramHistory;
    boolean isWeek = true;

    public WeekGoalFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWeekGoalBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        myPreference = new MyPreference(getContext());
        if (isWeek) {
            binding.lnHistory.setVisibility(View.GONE);
            binding.lnSetGoal.setVisibility(View.VISIBLE);
            // mGoalTitle.setText(getString(R.string.txt_this_week));
        } else {
            binding.lnHistory.setVisibility(View.VISIBLE);
            binding.lnSetGoal.setVisibility(View.GONE);
            // mGoalTitle.setText(getString(R.string.txt_this_month));
        }
        initWeekGoal();

    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isWeek) {
//            lnHistory.setVisibility(View.GONE);
//            lnSetGoal.setVisibility(View.VISIBLE);
//            mGoalTitle.setText(getString(R.string.txt_this_week));
//        } else {
//            lnHistory.setVisibility(View.VISIBLE);
//            lnSetGoal.setVisibility(View.GONE);
//            mGoalTitle.setText(getString(R.string.txt_this_month));
//        }
//        initWeekGoal();
    }

    public void setIsWeek(boolean isWeek) {
        this.isWeek = isWeek;
        if (isWeek) {
            binding.lnHistory.setVisibility(View.GONE);
            binding.lnSetGoal.setVisibility(View.VISIBLE);
        } else {
            binding.lnHistory.setVisibility(View.VISIBLE);
            binding.lnSetGoal.setVisibility(View.GONE);
        }
        Calendar now = Calendar.getInstance();


        long last = TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis());
        now.set(Calendar.DAY_OF_MONTH, 1);
        long first = TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis());

        homeViewModel.findHistoryFromTo(first, last).observe(getViewLifecycleOwner(), historyItemList -> {
            int times = 0, calories = 0, total = 0, count = 0;
            for (HistoryItem historyItem : historyItemList) {
                times += historyItem.getDuration();
                calories += historyItem.getCalories();

                int programId = historyItem.getProgramId();
                if (!mProgramHistory.contains(programId)) {
                    mProgramHistory.add(programId);
                }

            }
            int sizeProgram = mProgramHistory.size();
            if (sizeProgram > 0) {
                int maxSub = Math.max(0, sizeProgram - 3);
                programGoalAdapter.setProgram(getProgram(mProgramHistory.subList(maxSub, sizeProgram)));
            }

            int minute = (times) / 60;
            binding.txtWorkoutNumber.setText(String.valueOf(historyItemList.size()));
            binding.txtTotalTime.setText(String.format("%d", minute));
            binding.txtCalories.setText(String.format("%d", calories));
        });

    }

    @SuppressLint("DefaultLocale")
    private void initWeekGoal() {
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        cal.setFirstDayOfWeek(firstDayOfWeeks[myPreference.getFirstDayOfWeek()]);
        int startDay = cal.get(Calendar.DAY_OF_WEEK);

        int firstDayOfWeek = cal.getFirstDayOfWeek();
        int addDay = -(startDay - firstDayOfWeek % 7);
        if (addDay > 0) addDay = -(7 - addDay);
        cal.add(Calendar.DATE, addDay);
        List<String> dayOfMonth = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        boolean[] status = new boolean[7];
        long first = TimeUnit.MILLISECONDS.toDays(cal.getTimeInMillis());

        int current = 0;
        for (int i = 0; i < 7; i++) {
            labels.add(weekFormat.format(cal.getTime()));
            dayOfMonth.add(dayFormat.format(cal.getTime()));
            if (cal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                current = i;
            }
            cal.add(Calendar.DATE, 1);
            status[i] = false;

        }
        dayAdapter = new DayAdapter(labels, dayOfMonth, this);
        dayAdapter.setNow(current);
        binding.rcWeekGoal.setAdapter(dayAdapter);
        binding.rcWeekGoal.setLayoutManager(new SpanningLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mProgramHistory = new ArrayList<>();
        programGoalAdapter = new ProgramGoalAdapter(getContext(), this);
        binding.rcProgramGoal.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcProgramGoal.setAdapter(programGoalAdapter);
        binding.rcProgramGoal.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


        homeViewModel.findHistoryFromTo(first, first + 7).observe(getViewLifecycleOwner(), historyItemList -> {
            int times = 0, calories = 0, count = 0;
            for (HistoryItem historyItem : historyItemList) {
                times += historyItem.getDuration();
                calories += historyItem.getCalories();
                int programId = historyItem.getProgramId();
                if (!mProgramHistory.contains(programId)) {
                    mProgramHistory.add(programId);
                }
                int index = (int) (historyItem.getDate() - first);
                if (index < 7) {
                    status[index] = true;
                }
            }
            int sizeProgram = mProgramHistory.size();
            if (sizeProgram > 0) {
                int maxSub = Math.max(0, sizeProgram - 3);
                programGoalAdapter.setProgram(getProgram(mProgramHistory.subList(maxSub, sizeProgram)));
            }

            int minute = (times) / 60;
            binding.txtWorkoutNumber.setText(String.valueOf(historyItemList.size()));
            binding.txtTotalTime.setText(String.format("%d", minute));
            binding.txtCalories.setText(String.format("%d", calories));
            for (Boolean dayStatus : status) {
                if (dayStatus) {
                    count++;
                }
            }
            dayAdapter.setStatus(status);
            binding.txtGoalProgress.setText(String.format("%d/%d", count, myPreference.getGoalOfWeek()));
        });
        binding.rcWeekGoal.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HistoryActivity.class));
        });
        binding.txtTotalTime.setOnClickListener(this::goHistory);
        binding.lnWeek.setOnClickListener(this::goHistory);
        binding.txtMoreHistory.setOnClickListener(this::goHistory);
        binding.txtRecords.setOnClickListener(this::goHistory);
        binding.txtSetGoal.setOnClickListener(view -> {
            startActivityForResult(new Intent(getActivity(), SetGoalActivity.class), 999);
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


    void goHistory(View view) {
        startActivity(new Intent(getActivity(), HistoryActivity.class));
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

    @Override
    public void goDay() {
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }


    @Override
    public void onProgramClick(ProgramObject programObject) {
        if (programObject.getType() == 1) {
            Intent intent = new Intent(getActivity(), ChallengeActivity.class);
            Bundle bundle = new Bundle();
            //bundle.putParcelable("PLAN", programObject);
            bundle.putInt("PROGRAM_ID", programObject.getId());
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (programObject.getType() == 2) {
            Intent intent2 = new Intent(getActivity(), PreviewActivity.class);
            Bundle bundle = new Bundle();
           // bundle.putParcelable("PLAN", programObject);
            bundle.putInt("PROGRAM_ID", programObject.getId());
            bundle.putInt("DAY_NUMBER", 0);
            intent2.putExtras(bundle);
            startActivity(intent2);
        }
    }
}