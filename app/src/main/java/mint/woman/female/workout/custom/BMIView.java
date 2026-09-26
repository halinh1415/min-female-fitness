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
import android.util.TypedValue;
import android.view.View;

import mint.woman.female.workout.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Thanh do BMI dang "pill": cac khoang BMI duoc to mau lien mach,
 * con tro (thumb) truot theo chi so hien tai, ben duoi la cac moc gioi han.
 *
 * Moi kich thuoc deu tinh theo dp/sp nen hien thi giong nhau tren moi mat do man hinh.
 */
public class BMIView extends View {

    private final static int SEVERELY_UNDERWEIGHT = 2;
    private final static int UNDERWEIGHT = 3;
    private final static int NORMAL = 4;
    private final static int OVERWEIGHT = 5;
    private final static int OBESE_CLASS_1 = 6;
    private final static int OBESE_CLASS_2 = 7;
    private final static int OBESE_CLASS_3 = 8;

    private final static float MIN_BMI = 15f;
    private final static float MAX_BMI = 40f;

    // Kich thuoc thiet ke (dp/sp)
    private final static float BAR_HEIGHT_DP = 14f;
    private final static float SIDE_INSET_DP = 14f;
    private final static float VALUE_GAP_DP = 6f;
    private final static float LIMIT_GAP_DP = 7f;
    private final static float THUMB_WIDTH_DP = 7f;
    private final static float THUMB_OVERSHOOT_DP = 5f;
    private final static float THUMB_STROKE_DP = 2.5f;
    private final static float MIN_WIDTH_DP = 240f;
    private final static float VALUE_TEXT_SP = 15f;
    private final static float LIMIT_TEXT_SP = 11f;

    private int severelyUnderweightColor;
    private int underweightColor;
    private int normalColor;
    private int overweightColor;
    private int obeseClass1Color;
    private int obeseClass2Color;
    private int obeseClass3Color;
    private int textColor;
    private int markerColor;
    private int limitTextColor;

    private Paint barPaint;
    private Paint thumbPaint;
    private Paint valuePaint;
    private Paint limitPaint;

    private final Path clipPath = new Path();
    private final RectF barRect = new RectF();
    private final RectF thumbRect = new RectF();

    private List<BodyCategory> bodyCategoryList;
    private int currentBodyCategory = NORMAL;

    private float bmiValue = 0f;
    private float weight = 0f;
    private float heightMeter = 0f;

    // 0 = nam, 1 = nu
    private int gender = 0;

    public BMIView(Context context) {
        this(context, null);
    }

    public BMIView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BMIView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initColors(context, attrs, defStyleAttr);
        initPainting();
        initBodyCategories();
    }

    /** Bang mau "Zen": Thuy (thieu can) -> Moc (can doi) -> Tho/Hoa (thua can). */
    private void initColors(Context context, AttributeSet attrs, int defStyleAttr) {
        severelyUnderweightColor = Color.parseColor("#4DB6AC"); // Teal nhat
        underweightColor = Color.parseColor("#AED581");         // Xanh la nhat
        normalColor = Color.parseColor("#66BB6A");              // Xanh la can bang
        overweightColor = Color.parseColor("#FFD54F");          // Vang dat
        obeseClass1Color = Color.parseColor("#FFB74D");         // Cam am
        obeseClass2Color = Color.parseColor("#E57373");         // Do nhat
        obeseClass3Color = Color.parseColor("#D32F2F");         // Do dam
        textColor = Color.parseColor("#37474F");                // Xam da thay vi den gat
        markerColor = Color.parseColor("#00695C");              // Teal Tai Chi

        if (attrs != null) {
            TypedArray a = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.BMIView, defStyleAttr, 0);
            try {
                severelyUnderweightColor =
                        a.getColor(R.styleable.BMIView_severelyUnderweightColor, severelyUnderweightColor);
                underweightColor = a.getColor(R.styleable.BMIView_underweightColor, underweightColor);
                normalColor = a.getColor(R.styleable.BMIView_normalColor, normalColor);
                overweightColor = a.getColor(R.styleable.BMIView_overweightColor, overweightColor);
                obeseClass1Color = a.getColor(R.styleable.BMIView_obeseClass1Color, obeseClass1Color);
                obeseClass2Color = a.getColor(R.styleable.BMIView_obeseClass2Color, obeseClass2Color);
                obeseClass3Color = a.getColor(R.styleable.BMIView_obeseClass3Color, obeseClass3Color);
                textColor = a.getColor(R.styleable.BMIView_textColor, textColor);
                markerColor = a.getColor(R.styleable.BMIView_markerColor, markerColor);
            } finally {
                a.recycle();
            }
        }
        // Chu so moc lay theo mau chu chinh nhung nhat hon de khong lan at thanh mau.
        limitTextColor = Color.argb(150, Color.red(textColor), Color.green(textColor), Color.blue(textColor));
    }

    private void initPainting() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        valuePaint.setColor(textColor);
        valuePaint.setTextSize(sp(VALUE_TEXT_SP));

        limitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        limitPaint.setTextAlign(Paint.Align.CENTER);
        limitPaint.setColor(limitTextColor);
        limitPaint.setTextSize(sp(LIMIT_TEXT_SP));
    }

    private void initBodyCategories() {
        bodyCategoryList = new ArrayList<>();
        bodyCategoryList.add(new BodyCategory(SEVERELY_UNDERWEIGHT, severelyUnderweightColor,
                getResources().getString(R.string.txt_severely_underweight), 16f, 15f));
        bodyCategoryList.add(new BodyCategory(UNDERWEIGHT, underweightColor,
                getResources().getString(R.string.txt_underweight), 17f, 16f));
        bodyCategoryList.add(new BodyCategory(NORMAL, normalColor,
                getResources().getString(R.string.txt_normal), 18.5f, 17.5f));
        bodyCategoryList.add(new BodyCategory(OVERWEIGHT, overweightColor,
                getResources().getString(R.string.txt_overweight), 25f, 24f));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_1, obeseClass1Color,
                getResources().getString(R.string.txt_obese_class_1), 30f, 29f));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_2, obeseClass2Color,
                getResources().getString(R.string.txt_obese_class_2), 35f, 34f));
        bodyCategoryList.add(new BodyCategory(OBESE_CLASS_3, obeseClass3Color,
                getResources().getString(R.string.txt_obese_class_3), 40f, 39f));

        currentBodyCategory = calculateBodyCategory();
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                getResources().getDisplayMetrics());
    }

    private float textHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    /** Chieu cao toi thieu de khong cat mat chu gia tri o tren va cac moc o duoi. */
    private int desiredHeight() {
        float barBlock = dp(BAR_HEIGHT_DP) + 2 * dp(THUMB_OVERSHOOT_DP);
        return (int) Math.ceil(getPaddingTop()
                + textHeight(valuePaint)
                + dp(VALUE_GAP_DP)
                + barBlock
                + dp(LIMIT_GAP_DP)
                + textHeight(limitPaint)
                + getPaddingBottom());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = (int) Math.ceil(dp(MIN_WIDTH_DP) + getPaddingLeft() + getPaddingRight());
        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /** Doi gia tri BMI thanh toa do X tren thanh. */
    private float bmiToX(float bmi, float barLeft, float barRight) {
        float clamped = Math.max(MIN_BMI, Math.min(MAX_BMI, bmi));
        return barLeft + (barRight - barLeft) * ((clamped - MIN_BMI) / (MAX_BMI - MIN_BMI));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bodyCategoryList == null || bodyCategoryList.isEmpty()) {
            return;
        }

        float thumbOvershoot = dp(THUMB_OVERSHOOT_DP);
        float barHeight = dp(BAR_HEIGHT_DP);
        float barLeft = getPaddingLeft() + dp(SIDE_INSET_DP);
        float barRight = getWidth() - getPaddingRight() - dp(SIDE_INSET_DP);
        if (barRight <= barLeft) {
            return;
        }

        float barTop = getPaddingTop() + textHeight(valuePaint) + dp(VALUE_GAP_DP) + thumbOvershoot;
        float barBottom = barTop + barHeight;

        // --- Thanh mau, bo tron 2 dau thanh hinh vien thuoc ---
        barRect.set(barLeft, barTop, barRight, barBottom);
        float radius = barHeight / 2f;

        clipPath.reset();
        clipPath.addRoundRect(barRect, radius, radius, Path.Direction.CW);

        int saveCount = canvas.save();
        canvas.clipPath(clipPath);
        for (int i = 0; i < bodyCategoryList.size(); i++) {
            // Khoang dau to tu me trai thanh, khoang cuoi keo den me phai.
            float start = (i == 0) ? barLeft
                    : bmiToX(bodyCategoryList.get(i).getLimit(gender), barLeft, barRight);
            float end = (i == bodyCategoryList.size() - 1) ? barRight
                    : bmiToX(bodyCategoryList.get(i + 1).getLimit(gender), barLeft, barRight);
            if (end <= start) {
                continue;
            }
            barPaint.setColor(bodyCategoryList.get(i).color);
            canvas.drawRect(start, barTop, end, barBottom, barPaint);
        }
        canvas.restoreToCount(saveCount);

        // --- Cac moc gioi han ben duoi ---
        float limitBaseline = barBottom + thumbOvershoot + dp(LIMIT_GAP_DP)
                - limitPaint.getFontMetrics().ascent;
        float lastLabelRight = Float.NEGATIVE_INFINITY;
        for (BodyCategory category : bodyCategoryList) {
            String label = formatLimit(category.getLimit(gender));
            float half = limitPaint.measureText(label) / 2f;
            float x = bmiToX(category.getLimit(gender), barLeft, barRight);
            x = Math.max(getPaddingLeft() + half, Math.min(getWidth() - getPaddingRight() - half, x));
            // Bo qua moc bi de len moc truoc do cho do roi mat.
            if (x - half < lastLabelRight) {
                continue;
            }
            canvas.drawText(label, x, limitBaseline, limitPaint);
            lastLabelRight = x + half + dp(2f);
        }

        // Chua co du lieu can nang/chieu cao thi khong ve con tro.
        if (bmiValue <= 0f) {
            return;
        }

        // --- Con tro (thumb) + gia tri BMI ---
        float thumbX = bmiToX(bmiValue, barLeft, barRight);
        float thumbWidth = dp(THUMB_WIDTH_DP);
        float stroke = dp(THUMB_STROKE_DP);
        float thumbRadius = thumbWidth / 2f;
        thumbRect.set(thumbX - thumbRadius, barTop - thumbOvershoot,
                thumbX + thumbRadius, barBottom + thumbOvershoot);

        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(thumbRect, thumbRadius, thumbRadius, thumbPaint);

        thumbPaint.setStyle(Paint.Style.STROKE);
        thumbPaint.setStrokeWidth(stroke);
        thumbPaint.setColor(markerColor);
        thumbRect.inset(stroke / 2f, stroke / 2f);
        canvas.drawRoundRect(thumbRect, thumbRadius, thumbRadius, thumbPaint);

        String value = String.format(Locale.US, "%.1f", bmiValue);
        float half = valuePaint.measureText(value) / 2f;
        float valueX = Math.max(getPaddingLeft() + half,
                Math.min(getWidth() - getPaddingRight() - half, thumbX));
        float valueBaseline = getPaddingTop() - valuePaint.getFontMetrics().ascent;
        canvas.drawText(value, valueX, valueBaseline, valuePaint);
    }

    /** 18.5 -> "18.5", 25.0 -> "25". */
    private String formatLimit(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private int calculateBodyCategory() {
        int category = bodyCategoryList.get(0).bodyCategory;
        for (BodyCategory b : bodyCategoryList) {
            if (b.getLimit(gender) <= bmiValue) {
                category = b.bodyCategory;
            }
        }
        return category;
    }

    /** Tinh lai BMI tu can nang / chieu cao hien tai roi ve lai. */
    private void refresh() {
        bmiValue = (heightMeter <= 0f) ? 0f : weight / (heightMeter * heightMeter);
        currentBodyCategory = calculateBodyCategory();
        invalidate();
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

    /** @param height chieu cao tinh bang met. */
    public BMIView setHeight(float height) {
        this.heightMeter = height;
        refresh();
        return this;
    }

    public float getWeight() {
        return weight;
    }

    /** @param weight can nang tinh bang kg. */
    public BMIView setWeight(float weight) {
        this.weight = weight;
        refresh();
        return this;
    }

    public float getBmiValue() {
        return bmiValue;
    }

    public int getGender() {
        return gender;
    }

    /** @param gender 0 = nam, 1 = nu. */
    public BMIView setGender(int gender) {
        this.gender = gender;
        refresh();
        return this;
    }
}
