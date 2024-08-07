package mint.woman.female.workout.activity.ui.history;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.R;
import mint.woman.female.workout.common.adapter.EventDecorator;
import mint.woman.female.workout.common.adapter.HistorySection;
import mint.woman.female.workout.custom.HighlightWeekendsDecorator;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.MyPreference;
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

public class HistoryFragment extends Fragment implements HistorySection.ClickListener{
    private final SimpleDateFormat mFormatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    @BindView(R.id.calendarView)
    MaterialCalendarView calendarView;
    @BindView(R.id.rc_history)
    RecyclerView mHistoryRc;
    @BindView(R.id.progressBarHistory)
    ProgressBar mProgressHistory;
    @BindView(R.id.txt_no_workout)
    TextView mNoWorkoutText;
    @BindView(R.id.adView)
    AdView mAdBanner;

    SectionedRecyclerViewAdapter mAdapter;

    MyPreference myPreference;
    HistoryViewModel mHistoryViewModel;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.mn_history);
        mContext = getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        requestNewAdBanner();
    }

    private void initView() {
        mAdapter = new SectionedRecyclerViewAdapter();
        mHistoryRc.setNestedScrollingEnabled(false);
        mHistoryRc.setLayoutManager(new GridLayoutManager(getContext(), 1));
        //    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        //  mHistoryRc.addItemDecoration(divider);
        mHistoryRc.setAdapter(mAdapter);
        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        mNoWorkoutText.setVisibility(View.GONE);

        mProgressHistory.setVisibility(View.VISIBLE);


        CalendarDay today = CalendarDay.today();
        getHistoryOnMonth(today);

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
        mProgressHistory.setVisibility(View.VISIBLE);
        mHistoryViewModel.findInDay(dayLong).observe(getActivity(), historyItems -> {
            mProgressHistory.setVisibility(View.GONE);
            if (historyItems != null) {
                mNoWorkoutText.setVisibility(View.GONE);
                mAdapter.removeAllSections();
                long millis = TimeUnit.DAYS.toMillis(dayLong);
                mAdapter.addSection(new HistorySection(getContext(),mFormatDate.format(millis), historyItems,this));
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
        mHistoryViewModel.findFromTo(from, end).observe(getActivity(), historyItems -> {
            mProgressHistory.setVisibility(View.GONE);
            if (historyItems.size() > 0) {
                mNoWorkoutText.setVisibility(View.GONE);
                mAdapter.removeAllSections();
                Map<Long, List<HistoryItem>> groupedItems = new HashMap<>();
                for (HistoryItem item : historyItems) {
                    long date = item.getDate();
                    if (!groupedItems.containsKey(date)) {
                        groupedItems.put(date, new ArrayList<>());
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
                    mAdapter.addSection(new HistorySection(getContext(),mFormatDate.format(millis),historyItemList,this));
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


    private void restartProgress() {
        AlertDialog.Builder restart = new AlertDialog.Builder(getContext());
        restart.setTitle(getString(R.string.txt_alert_reset));
        restart.setNegativeButton(getString(android.R.string.cancel), null);
        restart.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            mHistoryViewModel.deleteAllHistory();
        });
        restart.show();
    }

    @Override
    public void onItemRootViewClicked(HistoryItem historyItem) {

    }


//    private class EventDecorator implements DayViewDecorator {
//        private final HashSet<CalendarDay> dates;
//
//        EventDecorator(List<CalendarDay> dates) {
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
//            view.addSpan(new DotSpan(20, mContext.getResources().getColor(R.color.colorAccent)));
//        }
//    }
//
//    CalendarDay convertFromCalendar(Calendar calendar) {
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
//        return CalendarDay.from(year, month + 1, dayOfMonth);
//    }

    void requestNewAdBanner() {
        myPreference = new MyPreference(getContext());
        mAdBanner.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdBanner.loadAd(adRequest);
            mAdBanner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdBanner.setVisibility(View.VISIBLE);
                }
            });
        }
    }


}
