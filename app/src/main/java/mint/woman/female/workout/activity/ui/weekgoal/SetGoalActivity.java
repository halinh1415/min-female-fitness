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

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetGoalActivity extends AppCompatActivity {
    @BindView(R.id.spnWeekGoal)
    Spinner spWeekGoal;
    @BindView(R.id.spnFirstDayOfWeek)
    Spinner spFirstDayOfWeek;


    int firstDayOfWeek = 0, goalOfWeek = 5;
    String[] fistDayList = new String[3];
    String[] goalOfWeeks = new String[7];
    MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        myPreference = new MyPreference(this);

        SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        goalOfWeek = myPreference.getGoalOfWeek() -1;
        firstDayOfWeek = myPreference.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {

            goalOfWeeks[i] = "" + (i + 1) + " " + getString(R.string.txt_day);
            if (i>0){
                goalOfWeeks[i] = "" + (i + 1) + " " + getString(R.string.txt_days);
            }
        }
        spWeekGoal.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, goalOfWeeks));

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        fistDayList[0] = weekFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        fistDayList[1] = weekFormat.format(calendar.getTime());
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        fistDayList[2] = weekFormat.format(calendar.getTime());
        spFirstDayOfWeek.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, fistDayList));

        spFirstDayOfWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                firstDayOfWeek = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spFirstDayOfWeek.setSelection(firstDayOfWeek);

        spWeekGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                goalOfWeek = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spWeekGoal.setSelection(goalOfWeek);

    }

    @OnClick(R.id.btn_save)
    void saveGoal() {
        Bundle bundleFire = new Bundle();
        bundleFire.putInt("GoalOfWeek", goalOfWeek+1);
        bundleFire.putInt("FirstDayOf", firstDayOfWeek);

        myPreference.setGoalOfWeek(goalOfWeek+1);
        myPreference.setFirstDayOfWeek(firstDayOfWeek);
        Intent intent = getIntent();
        intent.putExtra("RESULT", false);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String lang = preferences.getString("ST_LANGUAGE", "");
        if (!lang.isEmpty() ) {
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