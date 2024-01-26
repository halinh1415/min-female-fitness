package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import mint.woman.female.workout.R;

/**
 * TODO: document your custom view class.
 */
public class StackedView extends View {
    private int mColor1 = Color.RED;
    private int mColor2 = Color.BLUE;
    private int mColor3 = Color.GREEN;
    private final Float[] progress = new Float[]{0.47f, 0.38f, 0.15f};
    private TextPaint mTextPaint;
    private Paint paint;
    private float mTextWidth;
    private float mTextHeight;
    private final int mMin = 15;
    private final int mMax = 40;
    private final int mWidth = 600;
    private final int mHeight = 230;
    private final float weight = 0;
    private final float height = 0;

    public StackedView(Context context) {
        super(context);
        init(null, 0);
    }

    public StackedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public StackedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StackedView, defStyle, 0);

        mColor1 = a.getColor(
                R.styleable.StackedView_color1, Color.RED);
        mColor2 = a.getColor(
                R.styleable.StackedView_color2, Color.GREEN);
        mColor3 = a.getColor(
                R.styleable.StackedView_color3, Color.YELLOW);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(24);
        mTextPaint.setColor(Color.BLACK);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        int width;
//        int height;
//
//        //Measure Width
//        if (widthMode == MeasureSpec.EXACTLY) {
//            width = widthSize;
//            mWidth = width;
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            width = MeasureSpec.getSize(widthMeasureSpec);
//            mWidth = width;
//        } else {
//            width = mWidth;
//        }
//
//        //Measure Height
//        if (heightMode == MeasureSpec.EXACTLY) {
//            height = heightSize;
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            height = Math.min(mHeight, heightSize);
//        } else {
//            height = mHeight;
//        }
//
//        setMeasuredDimension(width, height);
//    }
    @SuppressLint({"DrawAllocation", "DefaultLocale"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = (getHeight() -paddingTop -paddingRight)/3;

        float w0 = contentWidth * progress[0];
        float w1 = contentWidth * progress[1];
        float w2 = contentWidth * progress[2];
        Log.d("HAHA", String.format("W = %.2f %.2f %.2f", w0, w1, w2));
        this.paint = new Paint();
        // Draw the text.
        this.paint.setColor(this.mColor3);
        canvas.drawRoundRect(new RectF(0, contentHeight, contentWidth, 0), 20, 20, paint);

        mTextPaint.setColor(mColor1);
        canvas.drawText(String.format("%2.0f'", progress[0] * 100) + "%", 0, contentHeight + 25, mTextPaint);


        this.paint.setColor(this.mColor1);

        canvas.drawRoundRect(new RectF(0, contentHeight, w0, 0), 20, 20, paint);
        canvas.drawRect(20, contentHeight, w0, 0, paint);


        mTextPaint.setColor(mColor2);
        canvas.drawText(String.format("%2.0f", progress[1] * 100) + "%", contentWidth * progress[0] + 10, contentHeight + 25, mTextPaint);

        this.paint.setColor(this.mColor2);
        canvas.drawRect(w0, contentHeight, w0 + w1, 0, paint);

        mTextPaint.setColor(mColor3);
        canvas.drawText(String.format("%2.0f", progress[2] * 100) + "%", w0 + w1, contentHeight + 25, mTextPaint);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
          //  canvas.drawRect(new RectF(0, contentWidth/7, contentWidth, 0), paint);
        }
        //canvas.drawRect(0, contentHeight, contentWidth, 0, paint);
    }

}