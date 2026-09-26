package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


import mint.woman.female.workout.R;

import java.util.ArrayList;
import java.util.List;

public class ProgressLineView extends View {
    int mWidth = 600, mHeight = 15;
    float progress = 5.5f;
    private int parts = 10;
    private Paint paint = null;
    private int fillColor = Color.parseColor("#32cd32");
    private int currentColor = Color.parseColor("#3CB371");
    private int emptyColor = Color.parseColor("#C0C0C0");
    private int separatorColor = Color.parseColor("#FFFFFF");
    private RectF rectFill = null;
    private RectF rectEmpty = null;
    private RectF currentRectF;
    private List<RectF> separators;
    private List<RectF> passParts;

    public ProgressLineView(Context context) {
        super(context);
        init(null, 0);
    }

    public ProgressLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProgressLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ProgressLineView, defStyle, 0);

        parts = a.getInteger(R.styleable.ProgressLineView_part, 1);
        fillColor = a.getColor(
                R.styleable.ProgressLineView_fillColor, Color.GREEN);
        emptyColor = a.getColor(
                R.styleable.ProgressLineView_emptyColor, Color.GRAY);
        currentColor = a.getColor(
                R.styleable.ProgressLineView_currentColor, Color.YELLOW);
        separatorColor = a.getColor(
                R.styleable.ProgressLineView_separatorColor, Color.WHITE);
        progress = a.getFloat(R.styleable.ProgressLineView_progressLineView, 0f);

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries
        //invalidateTextPaintAndMeasurements();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMax(int max) {
        this.parts = max;
        invalidate();
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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int topOfBar = getPaddingTop();
        int botOfBar = getPaddingTop() + 20;
        int rightSideOfBar = mWidth;
        this.paint = new Paint();
        separators = new ArrayList<>();
        passParts = new ArrayList<>();
        this.rectFill = new RectF(0, topOfBar, rightSideOfBar, botOfBar);
        this.rectEmpty = new RectF(0, topOfBar, rightSideOfBar, botOfBar);

        double fraction = progress % 1;//Give you 0.75 as remainder
        int current = (int) progress;
        int spaceBetween = mWidth / 200;
        int widthPart = mWidth / this.parts - (int) (0.9 * spaceBetween);
        int widthItem = widthPart + spaceBetween;
        int startX = widthPart;
        for (int i = 0; i < this.parts; i++) {
            this.separators.add(new RectF(startX, topOfBar, startX + spaceBetween, botOfBar));
            startX += spaceBetween + widthPart;
        }
        int startXItem = 0;
        for (int i = 0; i < current; i++) {
            startXItem = i * widthItem;
            RectF pRectF = new RectF(startXItem, topOfBar, startXItem + widthPart, botOfBar);
            passParts.add(pRectF);
        }
        currentRectF = new RectF(current * widthItem, topOfBar, (float) (current * widthItem + fraction * widthItem), botOfBar);

        // Foreground
        this.paint.setColor(this.fillColor);
        canvas.drawRect(this.rectFill, this.paint);

        // Background
        this.paint.setColor(this.emptyColor);
        canvas.drawRect(this.rectEmpty, this.paint);

        // Separator
        this.paint.setColor(this.separatorColor);
        for (RectF separator : this.separators) {
            canvas.drawRect(separator, this.paint);
        }
        //Pass
        this.paint.setColor(this.fillColor);
        for (RectF passRect : this.passParts) {
            canvas.drawRect(passRect, this.paint);
        }
        //Current
        this.paint.setColor(this.currentColor);
        canvas.drawRect(currentRectF, this.paint);
    }
}
