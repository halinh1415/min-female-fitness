package mint.woman.female.workout.activity.ui.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.activity.ui.weekgoal.WeekGoalFragment;
import mint.woman.female.workout.custom.RoundedBarChart;
import mint.woman.female.workout.databinding.ActivityReportBinding;
import mint.woman.female.workout.fragment.WeightViewModel;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.LanguageHelper;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressLint("NonConstantResourceId")
public class ReportActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    ActivityReportBinding binding;

//    @BindView(R.id.weight_line_chart)
//    LineChart mChartWeight;
//    @BindView(R.id.waist_chart)
//    LineChart mChartWaist;
//    @BindView(R.id.calorie_bar_chart)
//    RoundedBarChart mCaloriesChart;
//    @BindView(R.id.txt_chart_y)
//    TextView mChartYUnit;
//    @BindView(R.id.txt_highest_weight)
//    TextView mHighestWeight;
//    @BindView(R.id.txt_lowest_weight)
//    TextView mLowestWeight;
//    @BindView(R.id.txt_good_weight)
//    TextView mGoodWeight;
//    @BindView(R.id.txt_recent_weight)
//    TextView mRecentWeight;
//    @BindView(R.id.txt_waist_chart_y)
//    TextView mWaistChartYUnit;
//    @BindView(R.id.txt_highest_waist)
//    TextView mHighestWaist;
//    @BindView(R.id.txt_lowest_waist)
//    TextView mLowestWaist;
//    @BindView(R.id.txt_good_waist)
//    TextView mGoodWaist;
//    @BindView(R.id.txt_recent_waist)
//    TextView mRecentWaist;
    ValueFormatter xAxisMonthFormatter;

    HistoryViewModel mHistoryViewModel;
    MyPreference myPreference;
    float goodWeight, goodWaist;
    WeightViewModel mWeightViewModel;
    AdView mAdBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

        requestNewAdBanner();

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

    void requestNewAdBanner() {
        mAdBanner = findViewById(R.id.adView);
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




    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void initView() {
        myPreference = MyPreference.newInstance(this);
        mHistoryViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        mWeightViewModel = new ViewModelProvider(this).get(WeightViewModel.class);

        xAxisMonthFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        };

        initCaloriesChart();
        chartWeightInit();
        chartInitWaist();
        initDataWeight();
        initDataWaist();

        mWeightViewModel.getIsKg().observe(this,isKg -> {
            chartWeightInit();
            initDataWeight();
            chartInitWaist();
            initDataWaist();
        });
        Long nowDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
        mHistoryViewModel.findFromTo(nowDay - 30,nowDay).observe(this, historyItems -> {
            ArrayList<BarEntry> values = new ArrayList<>();
            Map<Long, List<HistoryItem>> groupedItems = new HashMap<>();
            Map<Long,Integer> caloriesMap = new HashMap<>();
            for (HistoryItem item : historyItems) {
                long date = item.getDateId();
                if (!groupedItems.containsKey(date)) {
                    groupedItems.put(date, new ArrayList<>());
                    caloriesMap.put(date,0);
                }
                groupedItems.get(date).add(item);
                int currentCalories = caloriesMap.get(date);
                caloriesMap.put(date,currentCalories+ item.getCalories());
            }

            // Convert to List<List<Item>>
            List<List<HistoryItem>> historyDateWithItems = new ArrayList<>(groupedItems.values());

            for (List<HistoryItem> historyDateWithItem : historyDateWithItems) {
                if (historyDateWithItem.size() >0){
                    long date = historyDateWithItem.get(0).getDateId();
                    values.add(new BarEntry(date, caloriesMap.get(date)));
                }

            }
            BarDataSet set1;

            if (binding.calorieBarChart.getData() != null && binding.calorieBarChart.getData().getDataSetCount() > 0) {
                set1 = (BarDataSet) binding.calorieBarChart.getData().getDataSetByIndex(0);
                set1.setColor(R.color.colorAccent);
                set1.setValues(values);
                binding.calorieBarChart.getData().notifyDataChanged();
                binding.calorieBarChart.notifyDataSetChanged();

            } else {
                set1 = new BarDataSet(values, getResources().getString(R.string.txt_calories_burn));
                set1.setColors(new int[] {Color.RED});
                // set1.setDrawIcons(true);
                //set1.setF
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);
                data.setValueTextSize(10f);
                data.setBarWidth(0.9f);

                binding.calorieBarChart.setData(data);
            }
            binding.calorieBarChart.setVisibleXRangeMaximum(10);
            binding.calorieBarChart.moveViewToX(binding.calorieBarChart.getXChartMax());
        });


    }
    private void initDataWeight() {
        if (binding.weightLineChart.getData() != null) binding.weightLineChart.clearValues();

        float currentHeight = myPreference.getCurrentHeightM();
        goodWeight = currentHeight * currentHeight * 22.9f;
        if (!myPreference.isKg()) {
            goodWeight *= 2.20462f;
        }
        binding.txtChartY.setText(myPreference.getWeightUnit());
        mHistoryViewModel.getAllUsers().observe(this, userObjects -> {
            ArrayList<Entry> values = new ArrayList<>();
            List<Entry> valueGood = new ArrayList<>();
            Long nowDay  = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
            valueGood.add(new Entry(nowDay - 365, goodWeight));
            valueGood.add(new Entry(nowDay, goodWeight));
            float min = 1000.0f, max = 0.0f;
            for (UserObject userObject : userObjects) {
                float y = myPreference.isKg() ? userObject.getWeight() : userObject.getLbs();
                if (y > max) max = y;
                if (y < min) min = y;
                float from = userObject.getDate();
                values.add(new Entry(from, y));
            }
            if (values.size() == 0) {
                values.add(new Entry(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()), 65));
            }
            LineDataSet setConst = new LineDataSet(valueGood,getResources().getString(R.string.txt_target_weight));
            setConst.setColor(getResources().getColor(R.color.Green));
            setConst.setLineWidth(2.f);
            setConst.setFormLineWidth(2f);
            setConst.enableDashedLine(50f,10f,1f);
            setConst.setDrawValues(false);

            setConst.setMode(LineDataSet.Mode.STEPPED);

            setConst.disableDashedHighlightLine();

            setConst.setColor(getResources().getColor(R.color.DeepPink));
            setConst.setDrawCircleHole(false);
            setConst.setDrawCircles(false);


            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_weight_chart));


            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(getResources().getColor(R.color.Green));
            set1.setValueTextColor(getResources().getColor(R.color.colorDarkIcon));
            set1.setLineWidth(3.f);

            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setCircleHoleRadius(3f);
            set1.setCircleRadius(5f);
            set1.setMode(LineDataSet.Mode.LINEAR);
            set1.setFillAlpha(100);

            set1.setFillColor(getResources().getColor(R.color.Red));
            set1.setHighLightColor(getResources().getColor(R.color.DarkGreen));
            set1.setDrawCircleHole(true);
            LineData data = new LineData(set1,setConst);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            binding.weightLineChart.setData(data);
            binding.weightLineChart.setVisibleXRangeMaximum(20);
            binding.weightLineChart.moveViewToX(binding.weightLineChart.getXChartMax());

//update weight min,max
            binding.txtLowestWeight.setText(getString(R.string.txt_lowest_weight) + " " + String.format("%.1f",min) + " " + myPreference.getWeightUnit());
            binding.txtHighestWeight.setText(getString(R.string.txt_highest_weight) + " " + String.format("%.1f",max) + " " + myPreference.getWeightUnit());
            binding.txtRecentWeight.setText(getString(R.string.txt_recent_weight) + " " + String.format("%.1f",myPreference.getCurrentWeight()) + " " + myPreference.getWeightUnit());
        });


        binding.txtGoodWeight.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f", goodWeight) + myPreference.getWeightUnit());
    }



    private void initDataWaist() {
        if (binding.waistChart.getData() != null) binding.waistChart.clearValues();
        binding.txtWaistChartY.setText(myPreference.getHeightUnit());
        float currentHeight = myPreference.getCurrentHeightCm();
        goodWaist = currentHeight *0.45f;
        if (!myPreference.isKg()){
            goodWaist = goodWaist/ 30.48f;
        }

        mHistoryViewModel.getAllUsers().observe(this, userObjects -> {
            List<Entry> values = new ArrayList<>();
            List<Entry> targetValue = new ArrayList<>();
            float min = 200.f, max = 0.0f;

            Long nowDay  = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
            targetValue.add(new Entry(nowDay - 365, goodWaist));
            targetValue.add(new Entry(nowDay, goodWaist));
            for (UserObject userObject : userObjects) {
                float  y = userObject.getWaist();
                if (!myPreference.isKg()){
                    y = userObject.getWaist()/ 30.48f;
                }

                if (y > max) max = y;
                if (y < min) min = y;
                float from = userObject.getDate();
                values.add(new Entry(from, y));
            }
            if (values.size() == 0) {
                values.add(new Entry(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()), 75));
            }
            LineDataSet setConst = new LineDataSet(targetValue,getResources().getString(R.string.txt_target));

            setConst.setColor(getResources().getColor(R.color.Lime));
            setConst.setLineWidth(3.f);
            setConst.setDrawCircleHole(false);
            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_waist_chart));

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(getResources().getColor(R.color.Orange));
            set1.setValueTextColor(getResources().getColor(R.color.Red));
            set1.setLineWidth(2.f);
            set1.setLineWidth(5.f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setCircleRadius(5);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setFillAlpha(100);
            set1.setFillColor(getResources().getColor(R.color.Red));
            set1.setHighLightColor(getResources().getColor(R.color.DarkGreen));
            set1.setDrawCircleHole(true);
            LineData data = new LineData(set1,setConst);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            binding.waistChart.setData(data);
            binding.waistChart.setVisibleXRangeMaximum(20);
            binding.waistChart.moveViewToX(binding.waistChart.getXChartMax());
            float currentWaist = myPreference.getCurrentWaist();
            if (!myPreference.isKg()){
                currentWaist =currentWaist/ 30.48f;
            }

//update weight min,max
            binding.txtLowestWaist.setText(getString(R.string.txt_min) + " " + String.format("%.1f",min) + " " + myPreference.getHeightUnit());
            binding.txtHighestWaist.setText(getString(R.string.txt_max) + " " + String.format("%.1f",max) + " " + myPreference.getHeightUnit());
            binding.txtRecentWaist.setText(getString(R.string.txt_recent) + " " + String.format("%.1f",currentWaist) + " " + myPreference.getHeightUnit());
        });


        binding.txtRecentWaist.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f ", goodWaist) + myPreference.getHeightUnit());
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void chartInitWaist() {// no description text
        binding.txtWaistChartY.setText(myPreference.getHeightUnit());
        binding.waistChart.getDescription().setEnabled(false);
        // enable touch gestures
        binding.waistChart.setTouchEnabled(true);
        binding.waistChart.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        binding.waistChart.setDragEnabled(true);
        binding.waistChart.setScaleEnabled(false);
        binding.waistChart.setDrawGridBackground(false);
        binding.waistChart.setHighlightPerDragEnabled(false);
        // set an alternative background color

        binding.waistChart.setPinchZoom(false);

        binding.waistChart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);


        binding.waistChart.setDragEnabled(true);
        binding.waistChart.setScaleEnabled(false);
        binding.waistChart.setDrawGridBackground(false);
        //  mChartWaist.setBackgroundColor(Color.WHITE);
        // mChartWaist.animateX(500);
        binding.waistChart.invalidate();
        // mChartWaist.animateX(200);
        XAxis xAxis = binding.waistChart.getXAxis();
//        xAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.enableGridDashedLine(20,5,5f);
        // xAxis.setTextColor(Color.RED);
        // xAxis.setDrawAxisLine(false);
        //  xAxis.setTextColor(context.getResources().getColor(R.color.Black));
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1); // only intervals of 1 day
        xAxis.setLabelCount(7);
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/M", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        };
        xAxis.setValueFormatter(xAxisFormatter);
        YAxis leftAxis = binding.waistChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.colorDarkIcon));
        leftAxis.enableGridDashedLine(20,5,5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setYOffset(-9f);
        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis = binding.waistChart.getAxisRight();
        rightAxis.setEnabled(false);
        binding.waistChart.getLegend().setTextColor(R.color.colorDarkIcon);
        binding.waistChart.getLegend().setTextSize(16f);
    }
    private void chartWeightInit() {

        binding.weightLineChart.getDescription().setEnabled(false);
        // enable touch gestures
        binding.weightLineChart.setTouchEnabled(true);
        binding.weightLineChart.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        binding.weightLineChart.setDragEnabled(true);
        binding.weightLineChart.setScaleEnabled(false);
        binding.weightLineChart.setDrawGridBackground(false);

        // mChartWeight.setGridBackgroundColor(getColor(R.color.gray_600));
        binding.weightLineChart.setHighlightPerDragEnabled(false);
        // set an alternative background color

        binding.weightLineChart.setPinchZoom(false);
        binding.weightLineChart.setMinimumWidth(5);

        // chart.setDrawYLabels(false);


        binding.weightLineChart.setDragEnabled(true);
        binding.weightLineChart.setScaleEnabled(false);

        //  mChartWeight.setBackgroundColor(Color.WHITE);
        // mChartWeight.animateX(500);
        binding.weightLineChart.invalidate();
        // mChartWeight.animateX(200);
        XAxis xAxis =  binding.weightLineChart.getXAxis();
        xAxis.enableGridDashedLine(20,5,5f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.colorDarkIcon));
        xAxis.setTextSize(10f);
        // xAxis.setTextColor(Color.RED);
        //  xAxis.setTextColor(context.getResources().getColor(R.color.Black));
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1); // only intervals of 1 day
        xAxis.setLabelCount(7);
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/M", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        };
        xAxis.setValueFormatter(xAxisFormatter);
        YAxis leftAxis =  binding.weightLineChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.colorDarkIcon));
        leftAxis.enableGridDashedLine(20,5,5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setYOffset(-9f);
        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis =  binding.weightLineChart.getAxisRight();
        rightAxis.setEnabled(false);
        //  mChartWeight.getLegend().setTextColor(R.color.colorDarkIcon);
        binding.weightLineChart.getLegend().setTextSize(16f);
    }

    private void initCaloriesChart() {
        binding.calorieBarChart.setOnChartValueSelectedListener(this);

        binding.calorieBarChart.setDrawBarShadow(false);
        binding.calorieBarChart.setDrawValueAboveBar(true);

        binding.calorieBarChart.getDescription().setEnabled(false);


        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        binding.calorieBarChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        binding.calorieBarChart.setPinchZoom(false);

        binding.calorieBarChart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);
        binding.calorieBarChart.setDragEnabled(true);
        binding.calorieBarChart.setScaleEnabled(false);
        // mCaloriesChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = binding.calorieBarChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getResources().getColor(R.color.colorDarkIcon));
        xAxis.enableGridDashedLine(20,5,5f);

        ValueFormatter xAxisFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/M", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        };

        xAxis.setValueFormatter(xAxisFormatter);




        YAxis leftAxis = binding.calorieBarChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.enableGridDashedLine(20,5,5f);
        // leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(getResources().getColor(R.color.colorDarkIcon));
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = binding.calorieBarChart.getAxisRight();
        rightAxis.setEnabled(false);


        Legend l = binding.calorieBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        XYMarkerView mv = new XYMarkerView(this);
        mv.setChartView(binding.calorieBarChart); // For bounds control
        binding.calorieBarChart.setMarker(mv);
        binding.calorieBarChart.setVisibleXRangeMaximum(10);
        binding.calorieBarChart.invalidate();
        binding.calorieBarChart.getLegend().setTextColor(getResources().getColor(R.color.colorDarkIcon));
        binding.calorieBarChart.getLegend().setTextSize(16f);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

}
