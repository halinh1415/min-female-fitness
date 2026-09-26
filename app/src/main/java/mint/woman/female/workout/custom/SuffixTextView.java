package mint.woman.female.workout.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SuffixTextView  extends TextView {
    private TextPaint mTextPaint;
    private String mSuffix;
    private float mSuffixWidth;
    private Drawable mSuffixDrawable;

    private void initialize(Context context) {
        Resources resources = getResources();

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(getTextSize()/2);
        // Using the same foreground color could
        // be confusing.
        mTextPaint.setColor(getCurrentHintTextColor());
        mTextPaint.setTextAlign(Paint.Align.RIGHT);

        mSuffixDrawable = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mSuffix == null)
                    return;
                canvas.drawText(mSuffix,0,20,mTextPaint);
             //   canvas.drawText(mSuffix, 0, getPaddingTop(), mTextPaint);
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.OPAQUE;
            }
        };
    }

    public SuffixTextView(Context context) {
        super(context);
        initialize(context);
    }

    public SuffixTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SuffixTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void setSuffix(String suffix) {
        mSuffix = suffix;
        setCompoundDrawables(null, null, mSuffixDrawable, null);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSuffix != null)
            mSuffixWidth = mTextPaint.measureText(" " + mSuffix);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int getCompoundPaddingRight() {
        return super.getCompoundPaddingRight() + (int) Math.ceil(mSuffixWidth);
    }
}
