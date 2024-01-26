package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import mint.woman.female.workout.R;

import java.util.ArrayList;

public class BMIView extends View {

    private final static int VERY_SEVERELY_UNDERWEIGHT = 1;
    private final static int SEVERELY_UNDERWEIGHT = 2;
    private final static int UNDERWEIGHT = 3;
    private final static int NORMAL = 4;
    private final static int OVERWEIGHT = 5;
    private final static int OBESE_CLASS_1 = 6;
    private final static int OBESE_CLASS_2 = 7;
    private final static int OBESE_CLASS_3 = 8;

    private int colorNeutral = Color.parseColor("#727272");
    private int verySeverelyUnderweightColor;
    private int severelyUnderweightColor;
    private int underweightColor;
    private int normalColor;
    private int overweightColor;
    private int obeseClass1Color;
    private int obeseClass2Color;
    private int obeseClass3Color;
    private int textColor;


    private Paint mPaint;
    private int currentBodyCategory;
    private ArrayList<BodyCategory> bodyCategoryList;
    private float bmiValue = 0f;
    private int mMin = 15, mMax = 40,
            mWidth = 600, mHeight = 190;
    private float weight = 0;
    private float height = 0;

    // 0 = male, 1 = female
    private int gender = 0;

    public BMIView(Context context) {
        super(context);
        initPainting();
        initBodyCategories();
    }

    public BMIView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Apply all attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BMIView,
                0, 0);
        try {
            verySeverelyUnderweightColor = a.getColor(R.styleable.BMIView_verySeverelyUnderweightColor, Color.parseColor("#ff6f69"));
            severelyUnderweightColor = a.getColor(R.styleable.BMIView_severelyUnderweightColor, Color.parseColor("#ffcc5c"));
            underweightColor = a.getColor(R.styleable.BMIView_underweightColor, Color.parseColor("#ffeead"));
            normalColor = a.getColor(R.styleable.BMIView_normalColor, Color.parseColor("#88d8b0"));
            overweightColor = a.getColor(R.styleable.BMIView_overweightColor, Color.parseColor("#ffeead"));
            obeseClass1Color = a.getColor(R.styleable.BMIView_obeseClass1Color, Color.parseColor("#ffcc5c"));
            obeseClass2Color = a.getColor(R.styleable.BMIView_obeseClass2Color, Color.parseColor("#ff6f69"));
            obeseClass3Color = a.getColor(R.styleable.BMIView_obeseClass3Color, Color.parseColor("#ff6f69"));
            textColor = context.getResources().getColor(R.color.colorDarkIcon);
        } finally {
            a.recycle();
        }
        initPainting();
        initBodyCategories();
    }

    private void initPainting() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(colorNeutral);

        // Scale the desired text size to match screen density
        // mPaint.setTextSize(mFontSize * getResources().getDisplayMetrics().density);
        mPaint.setStrokeWidth(2f);
        setPadding(5, 5, 5, 5);
    }

    private void initBodyCategories() {
        bodyCategoryList = new ArrayList<>();

        //bodyCategoryList.add(new BodyCategory(VERY_SEVERELY_UNDERWEIGHT, verySeverelyUnderweightColor,getResources().getString(R.string.txt_very_severely_underweight), mMin, mMin));
        bodyCategoryList.add(new BodyCategory(SEVERELY_UNDERWEIGHT, severelyUnderweightColor, getResources().getString(R.string.txt_severely_underweight), 16, 15));
        bodyCategoryList.add(new BodyCategory(UNDERWEIGHT, underweightColor, getResources().getString(R.string.txt_underweight), 17, 16));
        bodyCategoryList.add(new BodyCategory(NORMAL, normalColor, getResources().getString(R.string.txt_normal), 18.5f, 17.5f));
        bodyCategoryList.add(new BodyCategory(OVERWEIGHT, overweightColor, getResources().getString(R.string.txt_overweight), 25, 24));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_1, obeseClass1Color, getResources().getString(R.string.txt_obese_class_1), 30, 29));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_2, obeseClass2Color, getResources().getString(R.string.txt_obese_class_2), 35, 34));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_3, obeseClass3Color, getResources().getString(R.string.txt_obese_class_3), 40, 49));

        currentBodyCategory = NORMAL;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            mWidth = width;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            mWidth = width;
        } else {
            width = mWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(mHeight, heightSize);
        } else {
            height = mHeight;
        }

        setMeasuredDimension(width, height);
    }

    @SuppressLint({"DrawAllocation", "DefaultLocale"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int topOfBar = getPaddingTop() + 50;
        int botOfBar = getPaddingTop() + 100;
        int rightSideOfBar = mWidth - 15;

        float valueOfBar = rightSideOfBar;
        if (bmiValue <= mMax) {
            // - mMin moves the value to the left. The min value should be at the left border
            valueOfBar = rightSideOfBar * ((bmiValue - mMin) / (float) (mMax - mMin));
        }
        Paint.Style oldStyle = mPaint.getStyle();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Draws the categories
        for (int i = 0; i < bodyCategoryList.size() - 1; i++) {
            mPaint.setColor(bodyCategoryList.get(i).color);
            float bodyValue1 = rightSideOfBar * ((bodyCategoryList.get(i).getLimit(gender) - mMin) / (float) (mMax - mMin));
            float bodyValue2 = rightSideOfBar * ((bodyCategoryList.get(i + 1).getLimit(gender) - mMin) / (float) (mMax - mMin));
            canvas.drawRoundRect(bodyValue1, topOfBar, bodyValue2, botOfBar,20,20, mPaint);
        }
        //Draws the last category
        mPaint.setColor(bodyCategoryList.get(bodyCategoryList.size() - 1).color);
        float bodyValueOfBar = rightSideOfBar * ((bodyCategoryList.get(bodyCategoryList.size() - 1).getLimit(gender) - mMin) / (float) (mMax - mMin));
        canvas.drawRect(bodyValueOfBar, topOfBar, rightSideOfBar, botOfBar, mPaint);

        mPaint.setShader(null);
        mPaint.setColor(colorNeutral);

        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        // Draws the marker
        canvas.drawLine(valueOfBar, topOfBar, valueOfBar, botOfBar, mPaint);
        canvas.drawText(String.format("%.1f", bmiValue), valueOfBar - 25, topOfBar - 17, paint);

        //Draws the arrows
        mPaint.setColor(Color.BLACK);
        Path path = new Path();
//        path.moveTo(valueOfBar, topOfBar);
//        path.lineTo(valueOfBar - 12, topOfBar - 12);
//        path.lineTo(valueOfBar + 12, topOfBar - 12);
//        path.lineTo(valueOfBar, topOfBar);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, mPaint);
        RectF rectF = new RectF(valueOfBar - 3, topOfBar -10, valueOfBar + 3, botOfBar+10);
        canvas.drawRoundRect(rectF,3,3, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(colorNeutral);
        mPaint.setStyle(oldStyle);
        // Draws the categories
        paint.setTextSize(25);
        for (int i = 0; i < bodyCategoryList.size() - 1; i++) {
            mPaint.setColor(bodyCategoryList.get(i).color);
            float bodyValue1 = rightSideOfBar * ((bodyCategoryList.get(i).getLimit(gender) - mMin) / (float) (mMax - mMin));
            canvas.drawText(String.format("%2.0f", bodyCategoryList.get(i).getLimit(gender)), bodyValue1 - 10, botOfBar + 25, paint);
        }
        float temp = rightSideOfBar * ((bodyCategoryList.get(bodyCategoryList.size() - 1).getLimit(gender) - mMin) / (float) (mMax - mMin));
        canvas.drawText(String.format("%2.0f", bodyCategoryList.get(bodyCategoryList.size() - 1).getLimit(gender)), temp - 20, botOfBar + 25, paint);

    }

    private int calculateBodyCategory() {
        int category = 1;
        for (BodyCategory b : bodyCategoryList) {
            if (b.getLimit(gender) <= getBmiValue()) {
                category = b.bodyCategory;
            }
        }
        return category;
    }

    @Override
    public void invalidate() {
        if (height == 0f) {
            bmiValue = 0;
        } else {
            bmiValue = weight / (height * height);
        }

        if (bmiValue < mMin) {
            bmiValue = mMin;
        }
        currentBodyCategory = calculateBodyCategory();

        super.invalidate();
    }

    private BodyCategory getBodyCategory(int bodyCategory) {
        for (BodyCategory b : bodyCategoryList) {
            if (b.bodyCategory == bodyCategory) {
                return b;
            }
        }
        return bodyCategoryList.get(0);
    }

    public String getBodyDescription() {
        return getBodyCategory(currentBodyCategory).text;
    }

    public BMIView setHeight(float height) {
        this.height = height;
        invalidate();
        return this;
    }

    public float getWeight() {
        return weight;
    }

    public BMIView setWeight(float weight) {
        this.weight = weight;
        invalidate();
        return this;
    }

    public float getBmiValue() {
        return bmiValue;
    }

    public int getGender() {
        return gender;
    }

    public BMIView setGender(int gender) {
        this.gender = gender;
        invalidate();
        return this;
    }
}