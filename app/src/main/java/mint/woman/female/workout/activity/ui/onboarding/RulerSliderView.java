package mint.woman.female.workout.activity.ui.onboarding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import mint.woman.female.workout.R;

/**
 * Horizontal ruler slider with a fixed center marker.
 *
 * <p>The ruler scrolls continuously with the finger (fractional positions are kept while dragging),
 * supports fling with momentum, then settles onto the nearest tick.
 */
public class RulerSliderView extends View {
    public interface OnValueChangeListener {
        void onValueChanged(int value, boolean fromUser);
    }

    /** Thời gian chạy về vạch gần nhất sau khi thả tay. */
    private static final int SETTLE_DURATION_MS = 220;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OverScroller scroller;
    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int minFlingVelocity;
    private int maxFlingVelocity;

    private int minValue = 0;
    private int maxValue = 100;
    private int value = 50;
    private int majorStep = 10;
    private float pixelsPerUnit = 12f;
    private boolean feetAndInches;
    private boolean uniformTicks;
    private boolean showLabels = true;
    private int labelStep = 10;

    /** Giá trị đang nằm giữa thước, để dạng thực nên thước bám sát ngón tay thay vì nhảy từng nấc. */
    private float scrollPosition = 50f;

    private float downX;
    private float lastTouchX;
    private boolean dragging;
    private boolean flinging;
    private boolean settling;
    private OnValueChangeListener listener;

    public RulerSliderView(Context context) { super(context); init(); }
    public RulerSliderView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public RulerSliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setFocusable(true);
        setClickable(true);
        setHapticFeedbackEnabled(true);
        setBackgroundColor(Color.TRANSPARENT);
        paint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.g1_medium));

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        touchSlop = configuration.getScaledTouchSlop();
        minFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new OverScroller(getContext());
        scrollPosition = value;
    }

    // --- PUBLIC API ---

    public void setRange(int minValue, int maxValue, int majorStep) {
        int clampedMax = Math.max(minValue, maxValue);
        int clampedStep = Math.max(1, majorStep);
        // Compose gọi lại setter này ở mỗi lần recomposition; bỏ qua khi không có gì đổi
        // để không phá vị trí thước người dùng đang kéo.
        if (this.minValue == minValue && this.maxValue == clampedMax && this.majorStep == clampedStep) {
            return;
        }
        this.minValue = minValue;
        this.maxValue = clampedMax;
        this.majorStep = clampedStep;
        stopAnimation();
        this.value = clampValue(this.value);
        this.scrollPosition = this.value;
        invalidate();
    }

    /** Use inch-based tick grouping for the ft/in height ruler. */
    public void setFeetAndInches(boolean feetAndInches) {
        if (this.feetAndInches == feetAndInches) return;
        this.feetAndInches = feetAndInches;
        invalidate();
    }

    /** Bề rộng của một đơn vị: quyết định thước dày hay thưa và kéo bao xa thì hết dải. */
    public void setPixelsPerUnit(float pixelsPerUnit) {
        float clamped = Math.max(dp(4), pixelsPerUnit);
        if (this.pixelsPerUnit == clamped) return;
        this.pixelsPerUnit = clamped;
        stopAnimation();
        this.scrollPosition = this.value;
        invalidate();
    }

    /** Makes every tick the same length, unlike the measurement ruler. */
    public void setUniformTicks(boolean uniformTicks) {
        if (this.uniformTicks == uniformTicks) return;
        this.uniformTicks = uniformTicks;
        invalidate();
    }

    /** Controls label density independently from tick density. */
    public void setLabelStep(int labelStep) {
        int clamped = Math.max(1, labelStep);
        if (this.labelStep == clamped) return;
        this.labelStep = clamped;
        invalidate();
    }

    public void setShowLabels(boolean showLabels) {
        if (this.showLabels == showLabels) return;
        this.showLabels = showLabels;
        invalidate();
    }

    public void setValue(int value) {
        int clamped = clampValue(value);
        // Giá trị không đổi nghĩa là caller chỉ đang phát lại giá trị thước vừa báo lên,
        // không được reset vị trí đang trượt.
        if (this.value == clamped) return;
        stopAnimation();
        this.value = clamped;
        this.scrollPosition = clamped;
        invalidate();
    }

    public int getValue() { return value; }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    // --- SCROLLING ---

    private float maxScrollPx() {
        return (maxValue - minValue) * pixelsPerUnit;
    }

    private float currentScrollPx() {
        return (scrollPosition - minValue) * pixelsPerUnit;
    }

    private void scrollToPx(float scrollPx, boolean fromUser) {
        float clamped = Math.max(0f, Math.min(maxScrollPx(), scrollPx));
        scrollPosition = minValue + clamped / pixelsPerUnit;
        notifyValueFromScroll(fromUser);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void notifyValueFromScroll(boolean fromUser) {
        int rounded = clampValue(Math.round(scrollPosition));
        if (rounded == value) return;
        value = rounded;
        if (!fromUser) return;
        if (dragging) {
            // Chỉ rung khi ngón tay còn trên thước, tránh rung liên tục lúc thước đang trôi.
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        }
        if (listener != null) listener.onValueChanged(value, true);
    }

    private void stopAnimation() {
        scroller.forceFinished(true);
        flinging = false;
        settling = false;
    }

    private void fling(float velocityPxPerSecond) {
        flinging = true;
        settling = false;
        scroller.fling(Math.round(currentScrollPx()), 0,
                Math.round(velocityPxPerSecond), 0,
                0, Math.round(maxScrollPx()), 0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void settleToNearestTick() {
        int target = clampValue(Math.round(scrollPosition));
        int start = Math.round(currentScrollPx());
        int distance = Math.round((target - minValue) * pixelsPerUnit) - start;
        flinging = false;
        if (distance == 0) {
            settling = false;
            scrollPosition = target;
            invalidate();
            return;
        }
        settling = true;
        scroller.startScroll(start, 0, distance, 0, SETTLE_DURATION_MS);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollToPx(scroller.getCurrX(), true);
            return;
        }
        if (flinging) {
            flinging = false;
            settleToNearestTick();
        } else if (settling) {
            settling = false;
            scrollPosition = clampValue(Math.round(scrollPosition));
            notifyValueFromScroll(true);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stopAnimation();
                dragging = false;
                downX = event.getX();
                lastTouchX = downX;
                return true;

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                if (!dragging && Math.abs(x - downX) > touchSlop) {
                    dragging = true;
                    // Bỏ qua quãng nằm trong ngưỡng slop để thước không giật một nhịp khi bắt đầu kéo.
                    lastTouchX = x;
                    disallowParentIntercept(true);
                }
                if (dragging) {
                    scrollToPx(currentScrollPx() - (x - lastTouchX), true);
                    lastTouchX = x;
                }
                return true;
            }

            case MotionEvent.ACTION_UP: {
                boolean wasDragging = dragging;
                dragging = false;
                disallowParentIntercept(false);
                if (wasDragging) {
                    velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
                    float velocityX = velocityTracker.getXVelocity();
                    if (Math.abs(velocityX) > minFlingVelocity) {
                        fling(-velocityX);
                    } else {
                        settleToNearestTick();
                    }
                } else {
                    performClick();
                }
                releaseVelocityTracker();
                return true;
            }

            case MotionEvent.ACTION_CANCEL:
                if (dragging) settleToNearestTick();
                dragging = false;
                disallowParentIntercept(false);
                releaseVelocityTracker();
                return true;

            default:
                return true;
        }
    }

    private void disallowParentIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) parent.requestDisallowInterceptTouchEvent(disallow);
    }

    private void releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
        releaseVelocityTracker();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // --- DRAWING ---

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float baseline = getHeight() * 0.62f;

        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(239, 227, 232));
        canvas.drawLine(0, baseline, getWidth(), baseline, paint);

        int firstValue = Math.max(minValue,
                (int) Math.floor(scrollPosition - centerX / pixelsPerUnit) - 1);
        int lastValue = Math.min(maxValue,
                (int) Math.ceil(scrollPosition + (getWidth() - centerX) / pixelsPerUnit) + 1);
        int labelInterval = labelInterval();
        boolean labelsFit = labelInterval * pixelsPerUnit >= dp(feetAndInches ? 42 : 34);

        for (int tickValue = firstValue; tickValue <= lastValue; tickValue++) {
            float x = centerX + (tickValue - scrollPosition) * pixelsPerUnit;
            boolean major = feetAndInches ? tickValue % 12 == 0 : tickValue % majorStep == 0;
            boolean middle = !major && (feetAndInches
                    ? tickValue % 6 == 0
                    : tickValue % Math.max(1, majorStep / 2) == 0);
            float tickHeight = uniformTicks ? dp(18) : major ? dp(25) : middle ? dp(17) : dp(10);
            paint.setColor(major ? Color.rgb(199, 179, 188) : Color.rgb(230, 215, 221));
            paint.setStrokeWidth(dp(major ? 2 : 1));
            canvas.drawLine(x, baseline - tickHeight, x, baseline, paint);

            if (major && showLabels && labelsFit && tickValue % labelInterval == 0) {
                paint.setTextSize(dp(11));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.rgb(160, 147, 155));
                String label = feetAndInches ? (tickValue / 12) + "'" : String.valueOf(tickValue);
                canvas.drawText(label, x, baseline + dp(22), paint);
            }
        }

        paint.setColor(Color.rgb(233, 30, 99));
        paint.setStrokeWidth(dp(3));
        canvas.drawLine(centerX, dp(2), centerX, baseline + dp(5), paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, dp(2), dp(4), paint);
    }

    /** Khoảng cách giữa hai vạch có số, tính theo đơn vị của thước. */
    private int labelInterval() {
        return feetAndInches ? 12 * Math.max(1, labelStep / 10) : labelStep;
    }

    private int clampValue(int value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
