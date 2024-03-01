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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.custom.RoundedBarChart;
import mint.woman.female.workout.fragment.WeightViewModel;
import mint.woman.female.workout.model.HistoryDateWithItem;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.MyPreference;


@SuppressLint("NonConstantResourceId")
public class ReportFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {


    @BindView(R.id.weight_line_chart)
    LineChart mChartWeight;
    @BindView(R.id.waist_chart)
    LineChart mChartWaist;
    @BindView(R.id.calorie_bar_chart)
    RoundedBarChart mCaloriesChart;
    @BindView(R.id.txt_chart_y)
    TextView mChartYUnit;
    @BindView(R.id.txt_highest_weight)
    TextView mHighestWeight;
    @BindView(R.id.txt_lowest_weight)
    TextView mLowestWeight;
    @BindView(R.id.txt_good_weight)
    TextView mGoodWeight;
    @BindView(R.id.txt_recent_weight)
    TextView mRecentWeight;
    @BindView(R.id.txt_waist_chart_y)
    TextView mWaistChartYUnit;
    @BindView(R.id.txt_highest_waist)
    TextView mHighestWaist;
    @BindView(R.id.txt_lowest_waist)
    TextView mLowestWaist;
    @BindView(R.id.txt_good_waist)
    TextView mGoodWaist;
    @BindView(R.id.txt_recent_waist)
    TextView mRecentWaist;
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
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        ButterKnife.bind(this, view);
        return view;
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
//        chartWeightInit();
//        chartInitWaist();
//        initDataWeight();
//        initDataWaist();

        mWeightViewModel.getIsKg().observe(getViewLifecycleOwner(), isKg -> {
            chartWeightInit();
            initDataWeight();
            chartInitWaist();
            initDataWaist();
        });
        mHistoryViewModel.getHistoryDate().observe(getViewLifecycleOwner(), historyDateWithItems -> {
            ArrayList<BarEntry> values = new ArrayList<>();
            Long nowDay = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());

            values.add(new BarEntry(nowDay - 30, 0));


            for (HistoryDateWithItem historyDateWithItem : historyDateWithItems) {

                values.add(new BarEntry(historyDateWithItem.date.getDate(), historyDateWithItem.totalCalories()));

            }
            BarDataSet set1;

            if (mCaloriesChart.getData() != null && mCaloriesChart.getData().getDataSetCount() > 0) {
                set1 = (BarDataSet) mCaloriesChart.getData().getDataSetByIndex(0);
                set1.setColor(R.color.colorAccent);
                set1.setValues(values);
                mCaloriesChart.getData().notifyDataChanged();
                mCaloriesChart.notifyDataSetChanged();

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

                mCaloriesChart.setData(data);
            }
            mCaloriesChart.getBarData().setValueTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
            mCaloriesChart.setVisibleXRangeMaximum(10);
            mCaloriesChart.moveViewToX(mCaloriesChart.getXChartMax());
        });
    }

    private void initDataWeight() {
        if (mChartWeight.getData() != null) mChartWeight.clearValues();

        float currentHeight = myPreference.getCurrentHeightM();
        goodWeight = currentHeight * currentHeight * 22.9f;
        if (!myPreference.isKg()) {
            goodWeight *= 2.20462f;
        }
        mChartYUnit.setText(myPreference.getWeightUnit());
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
                mLowestWeight.setText(getString(R.string.txt_lowest_weight) + ": N/A" );
                mHighestWeight.setText(getString(R.string.txt_highest_weight) + ": N/A " );
                mRecentWeight.setText(getString(R.string.txt_recent_weight) + ": N/A ");
            }else{
                mLowestWeight.setText(getString(R.string.txt_lowest_weight) + " " + String.format("%.1f", min) + " " + myPreference.getWeightUnit());
                mHighestWeight.setText(getString(R.string.txt_highest_weight) + " " + String.format("%.1f", max) + " " + myPreference.getWeightUnit());
                mRecentWeight.setText(getString(R.string.txt_recent_weight) + " " + String.format("%.1f", myPreference.getCurrentWeight()) + " " + myPreference.getWeightUnit());
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
            mChartWeight.setData(data);
            mChartWeight.getAxisLeft().setAxisMaximum(data.getYMax());
            mChartWeight.getAxisLeft().setAxisMinimum(data.getYMin()-1);
            mChartWeight.setVisibleXRangeMaximum(20);
            mChartWeight.moveViewToX(mChartWeight.getXChartMax());

//update weight min,max

        });


        mGoodWeight.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f", goodWeight) + myPreference.getWeightUnit());
    }


    private void initDataWaist() {
        if (mChartWaist.getData() != null) mChartWaist.clearValues();
        mWaistChartYUnit.setText(myPreference.getHeightUnit());
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
                mLowestWaist.setText(getString(R.string.txt_min) + ":N/A " );
                mHighestWaist.setText(getString(R.string.txt_max) + ": N/A" );
            }else{
                mLowestWaist.setText(getString(R.string.txt_min) + " " + String.format("%.1f", min) + " " + myPreference.getHeightUnit());
                mHighestWaist.setText(getString(R.string.txt_max) + " " + String.format("%.1f", max) + " " + myPreference.getHeightUnit());
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
            mChartWaist.setData(data);

            mChartWaist.getAxisLeft().setAxisMaximum(data.getYMax());
            mChartWaist.getAxisLeft().setAxisMinimum(data.getYMin());
            mChartWaist.setVisibleXRangeMaximum(20);

            mChartWaist.moveViewToX(mChartWaist.getXChartMax());
            float currentWaist = myPreference.getCurrentWaist();
            if (!myPreference.isKg()) {
                currentWaist = currentWaist / 30.48f;
            }

//update weight min,max

            mRecentWaist.setText(getString(R.string.txt_recent) + " " + String.format("%.1f", currentWaist) + " " + myPreference.getHeightUnit());
        });


        mGoodWaist.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f ", goodWaist) + myPreference.getHeightUnit());
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void chartInitWaist() {// no description text
        mWaistChartYUnit.setText(myPreference.getHeightUnit());
        mChartWaist.getDescription().setEnabled(false);
        // enable touch gestures
        mChartWaist.setTouchEnabled(true);
        mChartWaist.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        mChartWaist.setDragEnabled(true);
        mChartWaist.setScaleEnabled(false);
        mChartWaist.setDrawGridBackground(false);
        mChartWaist.setHighlightPerDragEnabled(false);
        // set an alternative background color

        mChartWaist.setPinchZoom(false);

        mChartWaist.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);


        mChartWaist.setDragEnabled(true);
        mChartWaist.setScaleEnabled(false);
        mChartWaist.setDrawGridBackground(false);
        //  mChartWaist.setBackgroundColor(Color.WHITE);
        // mChartWaist.animateX(500);
        mChartWaist.invalidate();
        // mChartWaist.animateX(200);
        XAxis xAxis = mChartWaist.getXAxis();
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
        YAxis leftAxis = mChartWaist.getAxisLeft();

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
        YAxis rightAxis = mChartWaist.getAxisRight();
        rightAxis.setEnabled(false);
        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView(mChartWaist); // For bounds control
        mChartWaist.setMarker(mv);
        mChartWaist.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
        mChartWaist.getLegend().setTextSize(16f);
    }

    private void chartWeightInit() {

        mChartWeight.getDescription().setEnabled(false);
        // enable touch gestures
        mChartWeight.setTouchEnabled(true);
        mChartWeight.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        mChartWeight.setDragEnabled(true);
        mChartWeight.setScaleEnabled(false);
        mChartWeight.setDrawGridBackground(false);
        mChartWeight.setHighlightPerDragEnabled(false);
        // set an alternative background color

        mChartWeight.setPinchZoom(false);

        mChartWeight.setDrawGridBackground(false);
        // set an alternative background color

       // mChartWeight.setPinchZoom(false);
        mChartWeight.setMinimumWidth(5);

        // chart.setDrawYLabels(false);


      //  mChartWeight.setDragEnabled(true);
      //  mChartWeight.setScaleEnabled(true);

        //  mChartWeight.setBackgroundColor(Color.WHITE);
        // mChartWeight.animateX(500);
        mChartWeight.invalidate();
        // mChartWeight.animateX(200);
        XAxis xAxis = mChartWeight.getXAxis();
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
        YAxis leftAxis = mChartWeight.getAxisLeft();

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
        YAxis rightAxis = mChartWeight.getAxisRight();
        rightAxis.setEnabled(false);

        //  mChartWeight.getLegend().setTextColor(R.color.colorDarkIcon);
        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView(mChartWeight); // For bounds control
        mChartWeight.setMarker(mv);
      //  mChartWeight.setScaleYEnabled(false);
        mChartWeight.getLegend().setTextSize(16f);
        mChartWeight.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
    }

    private void initCaloriesChart() {
        mCaloriesChart.setOnChartValueSelectedListener(this);

        mCaloriesChart.setDrawBarShadow(false);
        mCaloriesChart.setDrawValueAboveBar(true);

        mCaloriesChart.getDescription().setEnabled(false);


        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mCaloriesChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mCaloriesChart.setPinchZoom(false);

        mCaloriesChart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);
        mCaloriesChart.setDragEnabled(true);
        mCaloriesChart.setScaleEnabled(false);
        // mCaloriesChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = mCaloriesChart.getXAxis();

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


        YAxis leftAxis = mCaloriesChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.enableGridDashedLine(20, 5, 5f);
        // leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(getActivity().getResources().getColor(R.color.colorDarkIcon));
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mCaloriesChart.getAxisRight();
        rightAxis.setEnabled(false);


        Legend l = mCaloriesChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        XYMarkerView mv = new XYMarkerView(getActivity());
        mv.setChartView(mCaloriesChart); // For bounds control
        mCaloriesChart.setMarker(mv);
        mCaloriesChart.setVisibleXRangeMaximum(10);
        mCaloriesChart.invalidate();
        mCaloriesChart.getLegend().setTextColor(getContext().getResources().getColor(R.color.colorDarkIcon));
        mCaloriesChart.getLegend().setTextSize(16f);

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
