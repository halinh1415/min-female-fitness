package mint.woman.female.workout.activity.ui.weekgoal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.ActivitySetGoalBinding;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SetGoalActivity extends AppCompatActivity {
    ActivitySetGoalBinding binding;
    MyPreference myDB;
    private int contentBasePaddingTop;

    int firstDayOfWeek = Calendar.MONDAY, goalOfWeek = 5;
    String[] fistDayList = new String[3];
    // Map index spinner -> giá trị Calendar
    int[] firstDayValues = new int[]{Calendar.MONDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contentBasePaddingTop = binding.getRoot().getPaddingTop();
        binding.getRoot().addOnLayoutChangeListener((view, left, top, right, bottom,
                                                      oldLeft, oldTop, oldRight, oldBottom) ->
                keepContentBelowActionBar());
        FirebaseAnalytics.getInstance(this).logEvent("scr_set_goal",new Bundle());

        setTitle(getString(R.string.txt_week_goal));
        initView();
    }

    /**
     * Android 16 enforces edge-to-edge. On some decor layouts the framework ActionBar can then
     * overlap android.R.id.content. Measure the real overlap so older Android versions and devices
     * whose ActionBar already resizes the content do not receive duplicate top padding.
     */
    private void keepContentBelowActionBar() {
        View actionBarContainer = findViewById(androidx.appcompat.R.id.action_bar_container);
        if (actionBarContainer == null
                || actionBarContainer.getVisibility() != View.VISIBLE
                || actionBarContainer.getHeight() == 0) {
            return;
        }

        int[] actionBarLocation = new int[2];
        int[] contentLocation = new int[2];
        actionBarContainer.getLocationInWindow(actionBarLocation);
        binding.getRoot().getLocationInWindow(contentLocation);

        int actionBarBottom = actionBarLocation[1] + actionBarContainer.getHeight();
        int overlap = Math.max(0, actionBarBottom - contentLocation[1]);
        int expectedTopPadding = contentBasePaddingTop + overlap;
        if (binding.getRoot().getPaddingTop() != expectedTopPadding) {
            binding.getRoot().setPadding(
                    binding.getRoot().getPaddingLeft(),
                    expectedTopPadding,
                    binding.getRoot().getPaddingRight(),
                    binding.getRoot().getPaddingBottom()
            );
        }
    }

    private void initView() {
        myDB = new MyPreference(this);
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        goalOfWeek = myDB.getGoalOfWeek();
        firstDayOfWeek = myDB.getFirstDayOfWeek();
        binding.npkWeekGoal.setMinValue(1);
        binding.npkWeekGoal.setMaxValue(7);
        binding.npkWeekGoal.setValue(goalOfWeek);
        binding.npkWeekGoal.setWrapSelectorWheel(false);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        // Tính tên ngày bằng cách cộng từ Monday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        fistDayList[0] = weekFormat.format(calendar.getTime());

        // Saturday = Monday + 5 ngày
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        fistDayList[1] = weekFormat.format(calendar.getTime());

        // Sunday = Saturday + 1 ngày
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        fistDayList[2] = weekFormat.format(calendar.getTime());

        binding.spFirstDayOfWeek.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, fistDayList));

        // Map giá trị Calendar ngược lại thành index spinner TRƯỚC khi setOnItemSelectedListener
        int spinnerIndex = 0;
        for (int i = 0; i < firstDayValues.length; i++) {
            if (firstDayValues[i] == firstDayOfWeek) {
                spinnerIndex = i;
                break;
            }
        }
        binding.spFirstDayOfWeek.setSelection(spinnerIndex);

        binding.spFirstDayOfWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                firstDayOfWeek = firstDayValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.npkWeekGoal.setOnValueChangedListener((picker, oldVal, newVal) -> goalOfWeek = newVal);
        binding.btnSave.setOnClickListener(view -> saveGoal());

    }


    void saveGoal() {
        Bundle bundleFire = new Bundle();
        bundleFire.putInt("GoalOfWeek",goalOfWeek);
        bundleFire.putInt("FirstDayOf",firstDayOfWeek);
        FirebaseAnalytics.getInstance(this).logEvent("click_save_scr_set_goal",bundleFire);

        myDB.setGoalOfWeek(goalOfWeek);
        myDB.setFirstDayOfWeek(firstDayOfWeek);
        Intent intent = new Intent(); // không trả lại getIntent(): tránh Intent Redirection
        intent.putExtra("RESULT", false);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
           // hideSystemUI();
        } else {

        }
    }

    @Override
    public void onBackPressed() {
//        Intent intent = getIntent();
//        intent.putExtra("RESULT", false);
//        setResult(999, intent);
        super.onBackPressed();
    }



    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // Hide the nav bar and status bar4
        );
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() && lang.length() > 1) {
            lang = lang.substring(0, 2);
        } else {
            lang = Locale.getDefault().getLanguage();
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang));

    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

}
