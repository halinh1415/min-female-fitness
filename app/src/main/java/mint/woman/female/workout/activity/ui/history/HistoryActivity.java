package mint.woman.female.workout.activity.ui.history;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.EventDecorator;
import mint.woman.female.workout.common.adapter.HistorySection;
import mint.woman.female.workout.custom.HighlightWeekendsDecorator;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class HistoryActivity extends AppCompatActivity implements HistorySection.ClickListener{
    private final SimpleDateFormat mFormatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    @BindView(R.id.calendarView)
    MaterialCalendarView calendarView;
    @BindView(R.id.rc_history)
    RecyclerView mHistoryRc;
    @BindView(R.id.progressBarHistory)
    ProgressBar mProgressHistory;
    @BindView(R.id.txt_no_workout)
    TextView mNoWorkoutText;

    SectionedRecyclerViewAdapter mAdapter;

    HistoryViewModel mHistoryViewModel;
    HashMap<Integer, ProgramObject> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mAdapter = new SectionedRecyclerViewAdapter();
        setTitle(getString(R.string.mn_history));
        mHistoryRc.setNestedScrollingEnabled(false);
        hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        mHistoryRc.setLayoutManager(new GridLayoutManager(this, 1));
        //    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        //  mHistoryRc.addItemDecoration(divider);
        mHistoryRc.setAdapter(mAdapter);
        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        mNoWorkoutText.setVisibility(View.GONE);

        mProgressHistory.setVisibility(View.VISIBLE);


        CalendarDay today = CalendarDay.today();
        getHistoryOnMonth(today);
//        calendarView.state().edit()
//                .setMaximumDate(CalendarDay.today())
//                .commit();


        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                getHistoryOnDate(date);
            }
        });
        calendarView.setOnMonthChangedListener((widget, date) -> {
            getHistoryOnMonth(date);
        });
        calendarView.addDecorators(new HighlightWeekendsDecorator());

    }

    private void getHistoryOnDate(CalendarDay day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, day.getYear());
        calendar.set(Calendar.MONTH, day.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day.getDay());
        long dayLong = TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis());
        Log.d("HAHA", "get history on :" + dayLong);
        mProgressHistory.setVisibility(View.VISIBLE);
        mHistoryViewModel.findInDay(dayLong).observe(HistoryActivity.this, historyItems -> {
            mProgressHistory.setVisibility(View.GONE);
            if (historyItems != null) {
                mNoWorkoutText.setVisibility(View.GONE);
                mAdapter.removeAllSections();
                long millis = TimeUnit.DAYS.toMillis(dayLong);
                mAdapter.addSection(new HistorySection(HistoryActivity.this,mFormatDate.format(millis), historyItems,this));
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.removeAllSections();
                mAdapter.notifyDataSetChanged();
                mNoWorkoutText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getHistoryOnMonth(CalendarDay day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, day.getYear());
        calendar.set(Calendar.MONTH, day.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long from = TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        long end = TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis());
        mProgressHistory.setVisibility(View.VISIBLE);
        mHistoryViewModel.findFromTo(from, end).observe(HistoryActivity.this, historyItems -> {
            mProgressHistory.setVisibility(View.GONE);
            if (historyItems.size() > 0) {
                mNoWorkoutText.setVisibility(View.GONE);
                mAdapter.removeAllSections();
                Map<Long, List<HistoryItem>> groupedItems = new HashMap<>();
                for (HistoryItem item : historyItems) {
                    long date = item.getDate();
                    if (!groupedItems.containsKey(date)) {
                        groupedItems.put(date, new ArrayList<HistoryItem>());
                    }
                    groupedItems.get(date).add(item);
                }

                // Convert to List<List<Item>>
                List<List<HistoryItem>> historyDateWithItems = new ArrayList<>(groupedItems.values());
                Collections.reverse(historyDateWithItems);
                List<CalendarDay> calendarDayList = new ArrayList<>();

                for (List<HistoryItem> historyItemList : historyDateWithItems) {
                    long millis = Calendar.getInstance().getTimeInMillis();
                    if (historyItemList.size()>0){
                        millis = TimeUnit.DAYS.toMillis(historyItemList.get(0).getDate());
                    }
                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(millis);
                    calendarDayList.add(FitnessConstUtils.convertFromCalendar(today));
                    mAdapter.addSection(new HistorySection(HistoryActivity.this,mFormatDate.format(millis),historyItemList,this));
                }
                EventDecorator eventDecorator = new EventDecorator(calendarDayList);
                calendarView.addDecorator(eventDecorator);
                mAdapter.notifyDataSetChanged();
            } else {
                calendarView.removeDecorators();
                mAdapter.removeAllSections();
                mAdapter.notifyDataSetChanged();
                mNoWorkoutText.setVisibility(View.VISIBLE);
            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Home Button!
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_reset:
                restartProgress();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restartProgress() {
        AlertDialog.Builder restart = new AlertDialog.Builder(this);
        restart.setTitle(getString(R.string.txt_alert_reset));
        restart.setNegativeButton(getString(android.R.string.cancel), null);
        restart.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            mHistoryViewModel.deleteAllHistory();
        });
        restart.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);

        return true;
    }



    @Override
    public void onItemRootViewClicked(HistoryItem historyItem) {
        ProgramObject programObject = hashMap.get(historyItem.getProgramId());
        if (programObject != null){
            Intent intent = new Intent(HistoryActivity.this, PreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("PROGRAM_ID", historyItem.getProgramId());
            bundle.putInt("DAY_NUMBER", historyItem.getDayIndex());
            intent.putExtras(bundle);
            startActivity(intent);
        }
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


//    private class EventDecorator implements DayViewDecorator {
//        private final HashSet<CalendarDay> dates;
//
//        public EventDecorator(List<CalendarDay> dates) {
//            this.dates = new HashSet<>(dates);
//        }
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            return dates.contains(day);
//        }
//
//        @SuppressLint("UseCompatLoadingForDrawables")
//        @Override
//        public void decorate(DayViewFacade view) {
//            view.addSpan(new DotSpan(20, getResources().getColor(R.color.colorAccent)));
//        }
//    }

//    CalendarDay convertFromCalendar(Calendar calendar) {
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
//        return CalendarDay.from(year, month + 1, dayOfMonth);
//    }

}