package mint.woman.female.workout.activity.ui.history;

import static mint.woman.female.workout.utils.FitnessConstUtils.convertFromCalendar;

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
import mint.woman.female.workout.databinding.ActivityHistoryBinding;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.LanguageHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class HistoryActivity extends AppCompatActivity implements HistorySection.ClickListener{
    ActivityHistoryBinding binding;
    private final SimpleDateFormat mFormatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//    @BindView(R.id.calendarView)
//    MaterialCalendarView calendarView;
//    @BindView(R.id.rc_history)
//    RecyclerView mHistoryRc;
//    @BindView(R.id.progressBarHistory)
//    ProgressBar mProgressHistory;
//    @BindView(R.id.txt_no_workout)
//    TextView mNoWorkoutText;

    SectionedRecyclerViewAdapter mAdapter;

    HistoryViewModel mHistoryViewModel;
    HashMap<Integer, ProgramObject> hashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityHistoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            initView();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView() {
        mAdapter = new SectionedRecyclerViewAdapter();
        setTitle(getString(R.string.mn_history));
        binding.rcHistory.setNestedScrollingEnabled(false);
        hashMap = FitnessApplication.getApplication().mJsonHelper.getAllCategoryHashMap();
        binding.rcHistory.setLayoutManager(new GridLayoutManager(this, 1));
        //    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        //  mHistoryRc.addItemDecoration(divider);
        binding.rcHistory.setAdapter(mAdapter);
        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding.txtNoWorkout.setVisibility(View.GONE);

        binding.progressBarHistory.setVisibility(View.VISIBLE);


        CalendarDay today = CalendarDay.today();
        getHistoryOnMonth(today);
//        calendarView.state().edit()
//                .setMaximumDate(CalendarDay.today())
//                .commit();


        binding.calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                getHistoryOnDate(date);
            }
        });
        binding.calendarView.setOnMonthChangedListener((widget, date) -> {
            getHistoryOnMonth(date);
        });
        binding.calendarView.addDecorators(new HighlightWeekendsDecorator());

    }

    private void getHistoryOnDate(CalendarDay day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, day.getYear());
        calendar.set(Calendar.MONTH, day.getMonth() - 1); // Calendar.MONTH là 0-indexed
        calendar.set(Calendar.DAY_OF_MONTH, day.getDay());

        // --- CẬP NHẬT: Chuẩn hóa Calendar về 00:00:00 của ngày được chọn ---
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 'dateMillis' bây giờ là mili giây của 00:00:00 của ngày được chọn theo múi giờ địa phương
        long dateMillis = calendar.getTimeInMillis();
        Log.d("HAHA", "get history on date (millis):" + dateMillis); // Log giá trị mili giây

        binding.progressBarHistory.setVisibility(View.VISIBLE);

        // MHistoryViewModel.findInDay cần được cập nhật để nhận mili giây, không phải số ngày.
        // Đảm bảo HistoryDao của bạn có một phương thức tương ứng:
        // @Query("SELECT * FROM HistoryItem WHERE dateId = :targetDateStartOfDayMillis ORDER BY start_time ASC")
        // LiveData<List<HistoryItem>> findInDay(long targetDateStartOfDayMillis);
        mHistoryViewModel.findInDay(dateMillis).observe(HistoryActivity.this, historyItems -> {
            binding.progressBarHistory.setVisibility(View.GONE);
            if (historyItems != null && historyItems.size() > 0) { // Kiểm tra null và rỗng cho an toàn
                binding.txtNoWorkout.setVisibility(View.GONE);
                mAdapter.removeAllSections();

                // 'dateMillis' đã chính xác là mili giây của 00:00:00 của ngày đó,
                // có thể dùng trực tiếp để định dạng hiển thị
                mAdapter.addSection(new HistorySection(HistoryActivity.this,
                        mFormatDate.format(dateMillis), // Sử dụng dateMillis trực tiếp để định dạng
                        historyItems, this));
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.removeAllSections();
                mAdapter.notifyDataSetChanged();
                binding.txtNoWorkout.setVisibility(View.VISIBLE);
                Log.d("HAHA", "Get history on date = null or empty"); // Cập nhật log
            }
        });
    }

    private void getHistoryOnMonth(CalendarDay day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, day.getYear());
        calendar.set(Calendar.MONTH, day.getMonth() - 1); // Calendar.MONTH là 0-indexed

        // --- CẬP NHẬT: Tính toán 'from' (mili giây của 00:00:00 đầu tháng) ---
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Đặt về ngày đầu tiên của tháng
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long from = calendar.getTimeInMillis(); // 'from' là mili giây của 00:00:00 của ngày đầu tháng

        // --- CẬP NHẬT: Tính toán 'end' (mili giây của 00:00:00 của ngày ĐẦU TIÊN CỦA THÁNG TIẾP THEO) ---
        // Chúng ta sẽ truy vấn các `dateId` >= `from` VÀ < `end`
        calendar.add(Calendar.MONTH, 1); // Chuyển sang tháng tiếp theo (sẽ tự động về ngày 1 của tháng đó)
        // Không cần calendar.add(Calendar.DATE, -1); nữa
        long end = calendar.getTimeInMillis(); // 'end' là mili giây của 00:00:00 của ngày đầu tháng tiếp theo

        binding.progressBarHistory.setVisibility(View.VISIBLE);

        // MHistoryViewModel.findFromTo cần được cập nhật để nhận mili giây, không phải số ngày.
        // Đảm bảo HistoryDao của bạn có một phương thức tương ứng:
        // @Query("SELECT * FROM HistoryItem WHERE dateId >= :fromMillis AND dateId < :toMillis ORDER BY dateId DESC, start_time DESC")
        // LiveData<List<HistoryItem>> findFromTo(long fromMillis, long toMillis);
        mHistoryViewModel.findFromTo(from, end).observe(HistoryActivity.this, historyItems -> {
            binding.progressBarHistory.setVisibility(View.GONE);
            if (historyItems != null && historyItems.size() > 0) { // Kiểm tra null cho an toàn
                binding.txtNoWorkout.setVisibility(View.GONE);
                mAdapter.removeAllSections();

                // Grouping items by their dateId (mili giây của đầu ngày)
                // TreeMap để đảm bảo các key (dateId) được sắp xếp tự động
                Map<Long, List<HistoryItem>> groupedItems = new TreeMap<>(Collections.reverseOrder()); // Sắp xếp giảm dần theo ngày
                for (HistoryItem item : historyItems) {
                    long dateMillis = item.getDateId(); // dateId bây giờ là mili giây của 00:00:00
                    if (!groupedItems.containsKey(dateMillis)) {
                        groupedItems.put(dateMillis, new ArrayList<>());
                    }
                    groupedItems.get(dateMillis).add(item);
                }

                // Convert to List<List<Item>>
                List<List<HistoryItem>> historyDateWithItems = new ArrayList<>(groupedItems.values());
                // Không cần Collections.reverse(historyDateWithItems); nếu TreeMap đã sắp xếp ngược

                List<CalendarDay> calendarDayList = new ArrayList<>();
                SimpleDateFormat mFormatDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()); // Định dạng ngày cho hiển thị

                for (List<HistoryItem> historyItemList : historyDateWithItems) {
                    if (historyItemList.isEmpty()) continue; // Bảo vệ khỏi danh sách rỗng

                    long dateMillis = historyItemList.get(0).getDateId(); // Lấy dateId (mili giây của 00:00:00)
                    Calendar displayCalendar = Calendar.getInstance();
                    displayCalendar.setTimeInMillis(dateMillis); // Đặt Calendar bằng mili giây của đầu ngày

                    calendarDayList.add(convertFromCalendar(displayCalendar)); // Chuyển đổi Calendar thành CalendarDay
                    mAdapter.addSection(new HistorySection(HistoryActivity.this,
                            mFormatDate.format(dateMillis), // Sử dụng dateMillis trực tiếp cho định dạng
                            historyItemList, this));
                }

                // --- CẬP NHẬT: Xóa các decorators cũ trước khi thêm mới để tránh trùng lặp ---
                binding.calendarView.removeDecorators(); // Xóa tất cả các decorators hiện có
                EventDecorator eventDecorator = new EventDecorator( calendarDayList);
                binding.calendarView.addDecorator(eventDecorator);
                mAdapter.notifyDataSetChanged();
            } else {
                binding.calendarView.removeDecorators();
                mAdapter.removeAllSections();
                mAdapter.notifyDataSetChanged();
                binding.txtNoWorkout.setVisibility(View.VISIBLE);
            }

        });
    }

    // Đảm bảo bạn có phương thức convertFromCalendar nếu nó chưa có
    // Ví dụ:



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