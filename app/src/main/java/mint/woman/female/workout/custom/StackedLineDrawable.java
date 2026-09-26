package mint.woman.female.workout.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import java.util.List;

public class StackedLineDrawable extends View {

    private int parts;
    private Paint paint = null;
    private int color1 = Color.parseColor("#0C7310");
    private int color2 = Color.parseColor("#4CAF50");
    private int color3 = Color.parseColor("#CDF69D");
    private RectF currentRectF;
    private List<RectF> separators = null;
    private List<RectF> passParts = null;
    private Float[] progress = new Float[]{0.34f,0.47f,0.19f};
    private int mMin = 10, mMax = 40,
            mWidth = 600, mHeight = 30;

    public StackedLineDrawable(Context context) {
        super(context);
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

        this.paint.setColor(this.color3);
        canvas.drawRoundRect(new RectF(0, mHeight, mWidth, 0), 20, 20, paint);
        canvas.drawText(String.format("%2.0f", progress[0]*100), 0, mHeight + 25, paint);
        this.paint.setColor(this.color1);
        canvas.drawRoundRect(new RectF(0, mHeight, mWidth*progress[0], 0),20,20, paint);
        canvas.drawText(String.format("%2.0f", progress[1]*100), mWidth*progress[0], mHeight + 25, paint);

        this.paint.setColor(this.color2);
        canvas.drawRect(new RectF(mWidth*progress[0], mHeight, mWidth*progress[1], 0), paint);
        canvas.drawText(String.format("%2.0f", progress[2]*100), mWidth*progress[0], mHeight + 25, paint);

    }


}
