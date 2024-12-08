package mint.woman.female.workout.activity.ui.report;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.databinding.FragmentReportBinding;
import mint.woman.female.workout.fragment.WeightViewModel;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.MyPreference;


@SuppressLint("NonConstantResourceId")
public class ReportFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    
    FragmentReportBinding binding;

    ValueFormatter xAxisMonthFormatter;

    HistoryViewModel mHistoryViewModel;
    MyPreference myPreference;
    float goodWeight, goodWaist;
    WeightViewModel mWeightViewModel;
    //   Context context;


    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // context = getContext();


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
        myPreference = MyPreference.newInstance(getContext());
        mHistoryViewModel = new ViewModelProvider(getActivity()).get(HistoryViewModel.class);
        mWeightViewModel = new ViewModelProvider(getActivity()).get(WeightViewModel.class);
        myPreference = new MyPreference(getContext());
        xAxisMonthFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        };

        initCaloriesChart();
        mWeightViewModel.getIsKg().observe(getViewLifecycleOwner(), isKg -> {
            chartWeightInit();
            initDataWeight();
            chartInitWaist();
            initDataWaist();
        });
        Long nowDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
        mHistoryViewModel.findFromTo(nowDay - 30,nowDay).observe(getViewLifecycleOwner(), historyItems -> {
            ArrayList<BarEntry> values = new ArrayList<>();
            Map<Long, List<HistoryItem>> groupedItems = new HashMap<>();
            Map<Long,Integer> caloriesMap = new HashMap<>();
            for (HistoryItem item : historyItems) {
                long date = item.getDate();
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
                    long date = historyDateWithItem.get(0).getDate();
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
                set1 = new BarDataSet(values, getActivity().getResources().getString(R.string.txt_calories_burn));
                set1.setColors(new int[]{Color.RED});
                // set1.setDrawIcons(true);
                //set1.setF
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);
                data.setValueTextSize(10f);
                data.setBarWidth(0.9f);
                data.setDrawValues(false);
                data.setDrawValues(false);

                binding.calorieBarChart.setData(data);
            }
            binding.calorieBarChart.getBarData().setValueTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
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
        mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
            ArrayList<Entry> values = new ArrayList<>();
            List<Entry> valueGood = new ArrayList<>();
            Long nowDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
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
                max = 70;
                min = 50;
                binding.txtLowestWeight.setText(getString(R.string.txt_lowest_weight) + ": N/A" );
                binding.txtHighestWeight.setText(getString(R.string.txt_highest_weight) + ": N/A " );
                binding.txtRecentWeight.setText(getString(R.string.txt_recent_weight) + ": N/A ");
            }else{
                binding.txtLowestWeight.setText(getString(R.string.txt_lowest_weight) + " " + String.format("%.1f", min) + " " + myPreference.getWeightUnit());
                binding.txtHighestWeight.setText(getString(R.string.txt_highest_weight) + " " + String.format("%.1f", max) + " " + myPreference.getWeightUnit());
                binding.txtRecentWeight.setText(getString(R.string.txt_recent_weight) + " " + String.format("%.1f", myPreference.getCurrentWeight()) + " " + myPreference.getWeightUnit());
            }
            LineDataSet setConst = new LineDataSet(valueGood, getActivity().getResources().getString(R.string.txt_target_weight));
            setConst.setColor(getActivity().getResources().getColor(R.color.Green));
            setConst.setLineWidth(1.f);
            setConst.enableDashedLine(20f, 10f, 5f);
            setConst.setDrawValues(false);

            setConst.setMode(LineDataSet.Mode.STEPPED);

            setConst.disableDashedHighlightLine();

            setConst.setColor(getActivity().getResources().getColor(R.color.Lime));
            setConst.setDrawCircleHole(false);
            setConst.setDrawCircles(false);


            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_weight_chart));


            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(getActivity().getResources().getColor(R.color.Green));
            set1.setValueTextColor(getActivity().getResources().getColor(R.color.colorDarkIcon));
            set1.setLineWidth(3.f);

            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setDrawCircleHole(true);
            set1.setCircleRadius(5);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawFilled(true);
            set1.setFillDrawable(ContextCompat.getDrawable(getContext(), R.drawable.chart_fill));
          //  set1.setFillAlpha(50);
            set1.setHighLightColor(getActivity().getResources().getColor(R.color.DarkGreen));
            LineData data = new LineData( setConst,set1);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            binding.weightLineChart.setData(data);
            binding.weightLineChart.getAxisLeft().setAxisMaximum(data.getYMax());
            binding.weightLineChart.getAxisLeft().setAxisMinimum(data.getYMin()-1);
            binding.weightLineChart.setVisibleXRangeMaximum(20);
            binding.weightLineChart.moveViewToX( binding.weightLineChart.getXChartMax());

//update weight min,max

        });


        binding.txtGoodWeight.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f", goodWeight) + myPreference.getWeightUnit());
    }


    private void initDataWaist() {
        if (binding.waistChart.getData() != null) binding.waistChart.clearValues();
        binding.txtWaistChartY.setText(myPreference.getHeightUnit());
        float currentHeight = myPreference.getCurrentHeightCm();
        goodWaist = currentHeight * 0.45f;
        if (!myPreference.isKg()) {
            goodWaist = goodWaist / 30.48f;
        }

        mHistoryViewModel.getAllUsers().observe(getViewLifecycleOwner(), userObjects -> {
            List<Entry> values = new ArrayList<>();
            List<Entry> targetValue = new ArrayList<>();

            Long nowDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
            targetValue.add(new Entry(nowDay - 365, goodWaist));
            targetValue.add(new Entry(nowDay, goodWaist));

            float min = 1000.0f, max = 0.0f;
            for (UserObject userObject : userObjects) {
                float y = userObject.getWaist();
                if (!myPreference.isKg()) {
                    y = userObject.getWaist() / 30.48f;
                }

                if (y > max) max = y;
                if (y < min) min = y;
                float from = userObject.getDate();
                values.add(new Entry(from, y));
            }
            if (values.size() == 0) {
                values.add(new Entry(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()), 75));
                binding.txtLowestWaist.setText(getString(R.string.txt_min) + ":N/A " );
                binding.txtHighestWaist.setText(getString(R.string.txt_max) + ": N/A" );
            }else{
                binding.txtLowestWaist.setText(getString(R.string.txt_min) + " " + String.format("%.1f", min) + " " + myPreference.getHeightUnit());
                binding.txtHighestWaist.setText(getString(R.string.txt_max) + " " + String.format("%.1f", max) + " " + myPreference.getHeightUnit());
            }
            LineDataSet setConst = new LineDataSet(targetValue, getActivity().getResources().getString(R.string.txt_target));

            setConst.setColor(getActivity().getResources().getColor(R.color.Pink));
            setConst.setLineWidth(1);
            setConst.setDrawCircleHole(false);
            setConst.setDrawCircles(false);
            setConst.setDrawValues(false);
            setConst.enableDashedLine(20f, 5f, 1f);
            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_waist_chart));

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(getActivity().getResources().getColor(R.color.DeepPink));
            set1.setValueTextColor(getContext().getResources().getColor(R.color.Red));
            set1.setLineWidth(2f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setDrawCircleHole(true);
            set1.setCircleRadius(5);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawFilled(true);
            set1.setFillDrawable(ContextCompat.getDrawable(getContext(), R.drawable.chart_fill_waist));
            set1.setFillAlpha(50);
            set1.setHighLightColor(getContext().getResources().getColor(R.color.DarkGreen));

            LineData data = new LineData(set1, setConst);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            binding.waistChart.setData(data);

            binding.waistChart.getAxisLeft().setAxisMaximum(data.getYMax());
            binding.waistChart.getAxisLeft().setAxisMinimum(data.getYMin());
            binding.waistChart.setVisibleXRangeMaximum(20);

            binding.waistChart.moveViewToX( binding.waistChart.getXChartMax());
            float currentWaist = myPreference.getCurrentWaist();
            if (!myPreference.isKg()) {
                currentWaist = currentWaist / 30.48f;
            }

//update weight min,max

            binding.txtRecentWaist.setText(getString(R.string.txt_recent) + " " + String.format("%.1f", currentWaist) + " " + myPreference.getHeightUnit());
        });


        binding.txtGoodWaist.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f ", goodWaist) + myPreference.getHeightUnit());
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
        XAxis xAxis =  binding.waistChart.getXAxis();
//        xAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.enableGridDashedLine(20, 5, 5f);
        // xAxis.setTextColor(Color.RED);
        // xAxis.setDrawAxisLine(false);
        //  xAxis.setTextColor(context.getResources().getColor(R.color.Black));
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
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
        YAxis leftAxis =  binding.waistChart.getAxisLeft();

        leftAxis.setTextColor(getActivity().getResources().getColor(R.color.colorDarkIcon));
        leftAxis.enableGridDashedLine(20, 5, 5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        //leftAxis.setGranularity(4);
        leftAxis.setYOffset(-9f);
        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis =  binding.waistChart.getAxisRight();
        rightAxis.setEnabled(false);
        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView( binding.waistChart); // For bounds control
        binding.waistChart.setMarker(mv);
        binding.waistChart.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
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
        binding.weightLineChart.setHighlightPerDragEnabled(false);
        // set an alternative background color

        binding.weightLineChart.setPinchZoom(false);

        binding.weightLineChart.setDrawGridBackground(false);
        // set an alternative background color

       // mChartWeight.setPinchZoom(false);
        binding.weightLineChart.setMinimumWidth(5);

        // chart.setDrawYLabels(false);


      //  mChartWeight.setDragEnabled(true);
      //  mChartWeight.setScaleEnabled(true);

        //  mChartWeight.setBackgroundColor(Color.WHITE);
        // mChartWeight.animateX(500);
        binding.weightLineChart.invalidate();
        // mChartWeight.animateX(200);
        XAxis xAxis = binding.weightLineChart.getXAxis();
        xAxis.enableGridDashedLine(20, 5, 5f);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
        xAxis.setTextSize(10f);
        // xAxis.setTextColor(Color.RED);
        //  xAxis.setTextColor(context.getResources().getColor(R.color.Black));
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
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
        YAxis leftAxis = binding.weightLineChart.getAxisLeft();

        leftAxis.setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
       // leftAxis.enableGridDashedLine(20, 5, 5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

//        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
      //  leftAxis.setGranularity(4);
        leftAxis.setGranularityEnabled(true);
//        leftAxis.setAxisMaximum(200f);
//        leftAxis.setAxisMinimum(0f);
       // leftAxis.setYOffset(-4f);

        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis = binding.weightLineChart.getAxisRight();
        rightAxis.setEnabled(false);

        //  mChartWeight.getLegend().setTextColor(R.color.colorDarkIcon);
        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView(binding.weightLineChart); // For bounds control
        binding.weightLineChart.setMarker(mv);
      //  mChartWeight.setScaleYEnabled(false);
        binding.weightLineChart.getLegend().setTextSize(16f);
        binding.weightLineChart.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
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
        // binding.calorieBarChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = binding.calorieBarChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(getActivity().getResources().getColor(R.color.colorDarkIcon));
        xAxis.enableGridDashedLine(20, 5, 5f);

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
        leftAxis.enableGridDashedLine(20, 5, 5f);
        // leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(getActivity().getResources().getColor(R.color.colorDarkIcon));
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

        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView(binding.calorieBarChart); // For bounds control
        binding.calorieBarChart.setMarker(mv);
        binding.calorieBarChart.setVisibleXRangeMaximum(10);
        binding.calorieBarChart.invalidate();
        binding.calorieBarChart.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
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
