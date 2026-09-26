package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.NutritionObject;

import java.util.ArrayList;
import java.util.List;

public class ProgressStackedView extends View {
    private int[] mColors = new int[]{Color.RED, Color.BLUE, Color.GREEN};

    private int emptyColor = Color.parseColor("#C69F9F9F");
    private float mMax = 150.0f;
    private float mRange1 = 56.0f;
    private float mRange2 = 100.0f;
    private float mProgress = 30.0f;
    private boolean mIsShowText;
    private List<RectF> separators;
    private List<RectF> passParts;
    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    public void setNutritionData(NutritionObject nutritionData){
        mColors[0] = Color.parseColor(nutritionData.getColor1());
        mColors[1] = Color.parseColor(nutritionData.getColor2());
        mColors[2] = Color.parseColor(nutritionData.getColor3());
        mMax = nutritionData.getMax();
        mRange1 = nutritionData.getRange1();
        mRange2 = nutritionData.getRange2();
        mProgress = nutritionData.getProgress();
        invalidate();

    }

    public void setIsShowText(boolean mIsShowText) {
        this.mIsShowText = mIsShowText;
    }

    public ProgressStackedView(Context context) {
        super(context);
        init(null, 0);
    }
    public ProgressStackedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProgressStackedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StackedView, defStyle, 0);

        mColors[0] = a.getColor(
                R.styleable.StackedView_color1, Color.RED);
        mColors[1] = a.getColor(
                R.styleable.StackedView_color2, Color.GREEN);
        mColors[2] = a.getColor(
                R.styleable.StackedView_color3, Color.YELLOW);
        mMax = a.getFloat(R.styleable.StackedView_max,100);
        mRange1 = a.getFloat(R.styleable.StackedView_range1,0);
        mRange2 = a.getFloat(R.styleable.StackedView_range2,0);
        mProgress = a.getFloat(R.styleable.StackedView_progress,0);
        mIsShowText = a.getBoolean(R.styleable.StackedView_showText,false);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries
        invalidateTextPaintAndMeasurements();
    }
    private void invalidateTextPaintAndMeasurements() {
        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(24);
        mTextPaint.setColor(Color.BLACK);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @SuppressLint({"DrawAllocation", "DefaultLocale"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight -15;
        int contentHeight = (getHeight() - paddingTop - paddingBottom)/4;
        separators = new ArrayList<>();
        passParts = new ArrayList<>();
        float w0 = (mRange1*contentWidth/mMax);
        float w1 = (mRange2*contentWidth/mMax);
        if (mRange1 > 0.0f && mRange2 > 0.0f ){
            separators.add(new RectF(0, contentHeight, w0, 0));
            separators.add(new RectF(w0+5, contentHeight, w1, 0));
            separators.add(new RectF(w1+5, contentHeight, contentWidth, 0));
            if (mIsShowText)  {
                mTextPaint.setColor(mColors[1]);
                canvas.drawText(String.format("%2.0f", mRange1), w0, contentHeight + 25, mTextPaint);
                mTextPaint.setColor(mColors[2]);
                canvas.drawText(String.format("%2.0f", mRange2), w0, contentHeight + 25, mTextPaint);
            }
            if (mProgress > mRange2){
                passParts.add(new RectF(0, contentHeight, w0, 0));
                passParts.add(new RectF(w0+5, contentHeight, w1, 0));
                passParts.add(new RectF(w1+5, contentHeight, mProgress*contentWidth/mMax, 0));
            }else if (mProgress> mRange1) {
                passParts.add(new RectF(0, contentHeight, w0, 0));
                passParts.add(new RectF(w0+5, contentHeight,mProgress*contentWidth/mMax , 0));
            }else {
                passParts.add(new RectF(0, contentHeight,mProgress*contentWidth/mMax , 0));
            }
        } else{
            if (mIsShowText)  {
                mTextPaint.setColor(mColors[1]);
                canvas.drawText(String.format("%2.0f", mRange1), w0, contentHeight + 25, mTextPaint);
            }
            separators.add(new RectF(0, contentHeight, w0, 0));
            separators.add(new RectF(w0+5, contentHeight, contentWidth, 0));
            if (mProgress> mRange1) {
                passParts.add(new RectF(0, contentHeight, w0, 0));
                passParts.add(new RectF(w0+5, contentHeight,mProgress*contentWidth/mMax , 0));
            }else {
                passParts.add(new RectF(0, contentHeight,mProgress*contentWidth/mMax , 0));
            }
        }


        Paint paint = new Paint();

        for(int i = 0; i < separators.size();i++){
            paint.setColor(emptyColor);

            canvas.drawRoundRect(separators.get(i), 10, 10, paint);
        }
        for(int i = 0; i < passParts.size();i++){
            paint.setColor(mColors[i]);
            canvas.drawRoundRect(passParts.get(i), 10, 10, paint);
        }

    }
}
