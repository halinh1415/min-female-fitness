package mint.woman.female.workout.activity.ui.history;

import static mint.woman.female.workout.utils.FitnessConstUtils.convertFromCalendar;

import android.content.Context;
import android.content.Intent;
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
import mint.woman.female.workout.activity.ui.preview.PreviewActivity;
import mint.woman.female.workout.common.adapter.EventDecorator;
import mint.woman.female.workout.common.adapter.HistorySection;
import mint.woman.female.workout.custom.HighlightWeekendsDecorator;
import mint.woman.female.workout.databinding.FragmentHistoryBinding;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
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
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class HistoryFragment extends Fragment implements HistorySection.ClickListener{
    private final SimpleDateFormat mFormatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    FragmentHistoryBinding binding;
//    @BindView(R.id.calendarView)
//    MaterialCalendarView calendarView;
//    @BindView(R.id.rc_history)
//    RecyclerView mHistoryRc;
//    @BindView(R.id.progressBarHistory)
//    ProgressBar mProgressHistory;
//    @BindView(R.id.txt_no_workout)
//    TextView mNoWorkoutText;
//    @BindView(R.id.adView)
//    AdView mAdBanner;

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
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        requestNewAdBanner();
    }

    private void initView() {
        mAdapter = new SectionedRecyclerViewAdapter();
        binding.rcHistory.setNestedScrollingEnabled(false);
        binding.rcHistory.setLayoutManager(new GridLayoutManager(getContext(), 1));
        //    DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        //  mHistoryRc.addItemDecoration(divider);
        binding.rcHistory.setAdapter(mAdapter);
        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding.txtNoWorkout.setVisibility(View.GONE);

        binding.progressBarHistory.setVisibility(View.VISIBLE);


        CalendarDay today = CalendarDay.today();
        getHistoryOnMonth(today);

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
        calendar.set(Calendar.MONTH, day.getMonth() - 1);
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
        mHistoryViewModel.findInDay(dateMillis).observe(getActivity(), historyItems -> {
            binding.progressBarHistory.setVisibility(View.GONE);
            if (historyItems != null && historyItems.size() > 0) { // Kiểm tra null và rỗng cho an toàn
                binding.txtNoWorkout.setVisibility(View.GONE);
                mAdapter.removeAllSections();

                // 'dateMillis' đã chính xác là mili giây của 00:00:00 của ngày đó,
                // có thể dùng trực tiếp để định dạng hiển thị
                mAdapter.addSection(new HistorySection(getActivity(),
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
        mHistoryViewModel.findFromTo(from, end).observe(getActivity(), historyItems -> {
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
                    mAdapter.addSection(new HistorySection(getActivity(),
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


    private void restartProgress() {
        AlertDialog.Builder restart = new AlertDialog.Builder(getContext());
        restart.setTitle(getString(R.string.txt_alert_reset));
        restart.setNegativeButton(getString(android.R.string.cancel), null);
        restart.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            mHistoryViewModel.deleteAllHistory();
        });
        restart.show();
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
        binding.adView.setVisibility(View.GONE);
        if (myPreference.isShowAds() && myPreference.getOK()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            binding.adView.loadAd(adRequest);
            binding.adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    binding.adView.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    @Override
    public void onItemRootViewClicked(HistoryItem historyItem) {
        Intent intent = new Intent(getActivity(), PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("PROGRAM_ID", historyItem.getProgramId());
        bundle.putInt("DAY", historyItem.getDayIndex());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
