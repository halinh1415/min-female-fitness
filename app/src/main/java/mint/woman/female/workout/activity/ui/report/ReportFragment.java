package mint.woman.female.workout.activity.ui.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

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
import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.history.HistoryViewModel;
import mint.woman.female.workout.custom.RoundedBarChart;
import mint.woman.female.workout.fragment.WeightViewModel;
import mint.woman.female.workout.model.HistoryDateWithItem;
import mint.woman.female.workout.model.UserObject;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


@SuppressLint("NonConstantResourceId")
public class ReportFragment extends Fragment implements  SeekBar.OnSeekBarChangeListener,
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
    Context context;



    public ReportFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        mHistoryViewModel = new ViewModelProvider(getActivity()).get(HistoryViewModel.class);
        mWeightViewModel = new ViewModelProvider(getActivity()).get(WeightViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myPreference = MyPreference.newInstance(getContext());
        initView();
        initDataCalories();
        initDataWeight();
        initDataWaist();

        mWeightViewModel.getIsKg().observe(getActivity(),isKg -> {
            chartWeightInit();
            initDataWeight();
            chartInitWaist();
            initDataWaist();
        });
    }


    private void initDataCalories() {

        mHistoryViewModel.getHistoryDate().observe(getActivity(), historyDateWithItems -> {
            ArrayList<BarEntry> values = new ArrayList<>();


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
                set1 = new BarDataSet(values, context.getResources().getString(R.string.txt_calories_burn));
                set1.setColors(new int[] {Color.RED});
                // set1.setDrawIcons(true);
                //set1.setF
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);
                data.setValueTextSize(10f);
                data.setBarWidth(0.9f);

                mCaloriesChart.setData(data);
            }
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
        mHistoryViewModel.getAllUsers().observe(getActivity(), userObjects -> {
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
            LineDataSet setConst = new LineDataSet(valueGood,context.getResources().getString(R.string.txt_target_weight));
            setConst.setColor(context.getResources().getColor(R.color.Green));
            setConst.setLineWidth(3.f);
            setConst.setDrawCircleHole(false);
            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_weight_chart));

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(context.getResources().getColor(R.color.colorAccent));
            set1.setValueTextColor(context.getResources().getColor(R.color.Red));
            set1.setLineWidth(5.f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setCircleRadius(5);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setFillAlpha(100);
            set1.setFillColor(context.getResources().getColor(R.color.Red));
            set1.setHighLightColor(context.getResources().getColor(R.color.DarkGreen));
            set1.setDrawCircleHole(true);
            LineData data = new LineData(set1,setConst);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            mChartWeight.setData(data);
            mChartWeight.setVisibleXRangeMaximum(20);
            mChartWeight.moveViewToX(mChartWeight.getXChartMax());

//update weight min,max
            mLowestWeight.setText(getString(R.string.txt_lowest_weight) + " " + String.format("%.1f",min) + " " + myPreference.getWeightUnit());
            mHighestWeight.setText(getString(R.string.txt_highest_weight) + " " + String.format("%.1f",max) + " " + myPreference.getWeightUnit());
            mRecentWeight.setText(getString(R.string.txt_recent_weight) + " " + String.format("%.1f",myPreference.getCurrentWeight()) + " " + myPreference.getWeightUnit());
        });


        mGoodWeight.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f", goodWeight) + myPreference.getWeightUnit());
    }



    private void initDataWaist() {
        if (mChartWaist.getData() != null) mChartWaist.clearValues();
        mWaistChartYUnit.setText(myPreference.getHeightUnit());
        float currentHeight = myPreference.getCurrentHeightCm();
        goodWaist = currentHeight *0.45f;
        switch (myPreference.uHeight()){
            case 0:
                break;
            case 1:
                goodWaist = goodWaist/ 30.48f;
                break;
            case 2:
                goodWaist = goodWaist / 2.54f;
                break;
        }
        mHistoryViewModel.getAllUsers().observe(getActivity(), userObjects -> {
            List<Entry> values = new ArrayList<>();
            List<Entry> targetValue = new ArrayList<>();
            float min = 200.f, max = 0.0f;

            Long nowDay  = TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis());
            targetValue.add(new Entry(nowDay - 365, goodWaist));
            targetValue.add(new Entry(nowDay, goodWaist));
            for (UserObject userObject : userObjects) {
                float y = 0f;
                switch (myPreference.uHeight()){
                    case 1:
                        y = userObject.getWaist()/ 30.48f;
                        break;
                    case 2:
                        y = userObject.getWaist()/ 2.54f;
                        break;
                    default:
                        y = userObject.getWaist();


                }
                if (y > max) max = y;
                if (y < min) min = y;
                float from = userObject.getDate();
                values.add(new Entry(from, y));
            }
            if (values.size() == 0) {
                values.add(new Entry(TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis()), 75));
            }
            LineDataSet setConst = new LineDataSet(targetValue,context.getResources().getString(R.string.txt_target));
            setConst.setColor(context.getResources().getColor(R.color.Lime));
            setConst.setLineWidth(3.f);
            setConst.setDrawCircleHole(false);
            LineDataSet set1 = new LineDataSet(values, getString(R.string.txt_waist_chart));

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(context.getResources().getColor(R.color.Orange));
            set1.setValueTextColor(context.getResources().getColor(R.color.Red));
            set1.setLineWidth(2.f);
            set1.setLineWidth(5.f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setCircleRadius(5);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setFillAlpha(100);
            set1.setFillColor(context.getResources().getColor(R.color.Red));
            set1.setHighLightColor(context.getResources().getColor(R.color.DarkGreen));
            set1.setDrawCircleHole(true);
            LineData data = new LineData(set1,setConst);
            data.setValueTextColor(Color.RED);
            data.setValueTextSize(9f);
            // set data
            mChartWaist.setData(data);
            mChartWaist.setVisibleXRangeMaximum(20);
            mChartWaist.moveViewToX(mChartWaist.getXChartMax());
            float currentWaist = myPreference.getCurrentWaist();
            if (myPreference.uHeight() ==1){
                currentWaist =currentWaist/ 30.48f;
            }else if (myPreference.uHeight() ==2){
                currentWaist =currentWaist/ 2.54f;
            }

//update weight min,max
            mLowestWaist.setText(getString(R.string.txt_min) + " " + String.format("%.1f",min) + " " + myPreference.getHeightUnit());
            mHighestWaist.setText(getString(R.string.txt_max) + " " + String.format("%.1f",max) + " " + myPreference.getHeightUnit());
            mRecentWaist.setText(getString(R.string.txt_recent) + " " + String.format("%.1f",currentWaist) + " " + myPreference.getHeightUnit());
        });


        mGoodWaist.setText(getString(R.string.txt_good_weight) + " " + String.format("%.1f ", goodWaist) + myPreference.getHeightUnit());
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void initView() {
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
        chartWeightInit();
        chartInitWaist();


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
        xAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
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
        YAxis leftAxis = mChartWaist.getAxisLeft();
        leftAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        leftAxis.enableGridDashedLine(20,5,5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setYOffset(-9f);
        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis = mChartWaist.getAxisRight();
        rightAxis.setEnabled(false);
        mChartWaist.getLegend().setTextColor(R.color.colorDarkIcon);
        mChartWaist.getLegend().setTextSize(16f);
    }
    private void chartWeightInit() { // no description text
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
        mChartWeight.setMinimumWidth(5);

        mChartWeight.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);


        mChartWeight.setDragEnabled(true);
        mChartWeight.setScaleEnabled(false);
        mChartWeight.setDrawGridBackground(false);

        //  mChartWeight.setBackgroundColor(Color.WHITE);
        // mChartWeight.animateX(500);
        mChartWeight.invalidate();
        // mChartWeight.animateX(200);
        XAxis xAxis = mChartWeight.getXAxis();
        xAxis.enableGridDashedLine(20,5,5f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        xAxis.setTextSize(10f);
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
        YAxis leftAxis = mChartWeight.getAxisLeft();
        leftAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        leftAxis.enableGridDashedLine(20,5,5f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        //leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setYOffset(-9f);
        //  leftAxis.setTextColor(context.getResources().getColor(R.color.Black));
        YAxis rightAxis = mChartWeight.getAxisRight();
        rightAxis.setEnabled(false);
        mChartWeight.getLegend().setTextColor(R.color.colorDarkIcon);
        mChartWeight.getLegend().setTextSize(16f);
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
        mCaloriesChart.setDrawGridBackground(false);
        // mCaloriesChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = mCaloriesChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
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




        YAxis leftAxis = mCaloriesChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.enableGridDashedLine(20,5,5f);
        // leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mCaloriesChart.getAxisRight();
        rightAxis.setDrawGridLines(false);

        rightAxis.setLabelCount(8, false);
        rightAxis.setTextColor(context.getResources().getColor(R.color.colorDarkIcon));
//        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = mCaloriesChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        XYMarkerView mv = new XYMarkerView(getActivity(), xAxisFormatter);
        mv.setChartView(mCaloriesChart); // For bounds control
        mCaloriesChart.setMarker(mv);
        mCaloriesChart.setVisibleXRangeMaximum(10);
        mCaloriesChart.invalidate();
        mCaloriesChart.getLegend().setTextColor(R.color.colorDarkIcon);
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



    @Override
    public void onDetach() {
        super.onDetach();
        this.context= null;
    }

}
