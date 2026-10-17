package mint.woman.female.workout.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

import com.google.android.material.chip.Chip;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bản đồ cơ vẽ bằng vector path (assets/muscle/muscle_map.json: hình nữ trace từ app Women Workout bằng tools/trace_muscle_map.py của app đó,
 * viền đổi sang hồng nhẹ)
 * thay cho ảnh cơ thể xám + 28 ảnh bitmap nhóm cơ. Cơ chính tô đỏ, cơ phụ tô hồng; nhấn chip hoặc nhấn thẳng vào nhóm cơ trên hình
 * → nhóm đó nhấp nháy viền sáng vàng, phần còn lại giữ nguyên.
 */
public class MuscleMapView extends View {
    private static final int COLOR_FILL = 0xFF000000;
    private static final int COLOR_HIGHLIGHT = 0xFFFFC233;
    private static final long PULSE_DURATION = 1800;
    private static final int PULSES = 3;

    private static final ExecutorService LOADER = Executors.newSingleThreadExecutor();
    private static volatile MapData sData;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint majorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final Matrix inverse = new Matrix();
    private final float[] touch = new float[2];

    /** muscle key → true nếu là cơ chính. */
    private final SparseBooleanArray shown = new SparseBooleanArray();
    private final SparseArray<Chip> chips = new SparseArray<>();
    private final SparseArray<Region> regions = new SparseArray<>();

    private int highlightKey = -1;
    private float pulse;
    private ValueAnimator animator;
    private Chip activeChip;

    public MuscleMapView(Context context) {
        this(context, null);
    }

    public MuscleMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        fillPaint.setColor(COLOR_FILL);
        highlightPaint.setColor(COLOR_HIGHLIGHT);
        glowPaint.setColor(COLOR_HIGHLIGHT);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);
        if (sData == null) load(context.getApplicationContext().getAssets());
    }

    private void load(AssetManager assets) {
        LOADER.execute(() -> {
            if (sData == null) {
                try {
                    sData = MapData.read(assets);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                requestLayout();
                updateMatrix();
                invalidate();
            });
        });
    }

    /** Nhóm cơ theo id trong dữ liệu bài tập (customfields muscleMajor / muscleMinor). */
    public void setMuscleGroups(@Nullable List<String> major, @Nullable List<String> minor) {
        shown.clear();
        regions.clear();
        // Thêm cơ phụ trước để cơ chính ghi đè khi trùng nhóm.
        for (int key : keys(minor)) shown.put(key, false);
        for (int key : keys(major)) shown.put(key, true);
        invalidate();
    }

    /** Nhấn chip → highlight nhóm cơ tương ứng; nhấn vào hình → chip tương ứng được chọn. */
    public void bindChip(Chip chip, String group) {
        int key = toKey(group);
        if (key <= 0) return;
        chips.put(key, chip);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setOnClickListener(v -> highlight(key));
    }

    public void highlight(int key) {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
        }
        setActiveChip(chips.get(key));
        highlightKey = key;
        float startPulse = pulse;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(PULSE_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            // Nhịp sáng/tối dạng cos: bắt đầu và kết thúc ở 0 nên không giật; đổi nhóm giữa chừng thì chuyển dần.
            float wave = (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * PULSES * t));
            pulse = lerp(startPulse, wave, smooth(t / 0.1f));
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pulse = 0f;
                highlightKey = -1;
                setActiveChip(null);
                invalidate();
            }
        });
        animator.start();
    }

    private void setActiveChip(@Nullable Chip chip) {
        if (activeChip != null && activeChip != chip) activeChip.setChecked(false);
        activeChip = chip;
        if (chip != null) chip.setChecked(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = Math.round(width * mapHeight() / mapWidth());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateMatrix();
    }

    private void updateMatrix() {
        int w = getWidth() - getPaddingLeft() - getPaddingRight();
        int h = getHeight() - getPaddingTop() - getPaddingBottom();
        if (w <= 0 || h <= 0) return;
        float scale = Math.min(w / mapWidth(), h / mapHeight());
        matrix.setScale(scale, scale);
        matrix.postTranslate(getPaddingLeft() + (w - mapWidth() * scale) / 2f,
                getPaddingTop() + (h - mapHeight() * scale) / 2f);
        matrix.invert(inverse);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        MapData data = sData;
        if (data == null) return;
        linePaint.setColor(data.lineColor);
        majorPaint.setColor(data.majorColor);
        minorPaint.setColor(data.minorColor);
        canvas.save();
        canvas.concat(matrix);

        canvas.drawPath(data.silhouette, linePaint);
        canvas.drawPath(data.fill, fillPaint);
        for (int i = 0; i < shown.size(); i++) {
            Path path = data.muscles.get(shown.keyAt(i));
            if (path != null) canvas.drawPath(path, shown.valueAt(i) ? majorPaint : minorPaint);
        }

        Path focus = highlightKey > 0 ? data.muscles.get(highlightKey) : null;
        if (focus != null && pulse > 0.01f) {
            // Viền sáng mềm: 2 lớp stroke trong suốt dần + lớp tô, đều chạy được bằng phần cứng.
            glowPaint.setStrokeWidth(12f * mapWidth() / 600f);
            glowPaint.setAlpha(Math.round(70 * pulse));
            canvas.drawPath(focus, glowPaint);
            glowPaint.setStrokeWidth(6f * mapWidth() / 600f);
            glowPaint.setAlpha(Math.round(140 * pulse));
            canvas.drawPath(focus, glowPaint);
            highlightPaint.setAlpha(Math.round(255 * pulse));
            canvas.drawPath(focus, highlightPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (sData == null || shown.size() == 0) return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (event.getAction() != MotionEvent.ACTION_UP) return super.onTouchEvent(event);
        touch[0] = event.getX();
        touch[1] = event.getY();
        inverse.mapPoints(touch);
        int key = hitTest(Math.round(touch[0]), Math.round(touch[1]));
        if (key > 0) {
            highlight(key);
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int hitTest(int x, int y) {
        // Ưu tiên cơ chính khi hai vùng chồng nhau; nới 6px quanh điểm chạm cho dễ nhấn các nhóm nhỏ.
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < shown.size(); i++) {
                if (shown.valueAt(i) != (pass == 0)) continue;
                Region region = region(shown.keyAt(i));
                if (region != null && near(region, x, y)) {
                    return shown.keyAt(i);
                }
            }
        }
        return -1;
    }

    private static boolean near(Region region, int x, int y) {
        for (int dx = -6; dx <= 6; dx += 3) {
            for (int dy = -6; dy <= 6; dy += 3) {
                if (region.contains(x + dx, y + dy)) return true;
            }
        }
        return false;
    }

    @Nullable
    private Region region(int key) {
        Region region = regions.get(key);
        if (region == null && sData != null) {
            Path path = sData.muscles.get(key);
            if (path == null) return null;
            region = new Region();
            region.setPath(path, new Region(0, 0, (int) mapWidth(), (int) mapHeight()));
            regions.put(key, region);
        }
        return region;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
        pulse = 0f;
        highlightKey = -1;
        super.onDetachedFromWindow();
    }

    private static List<Integer> keys(@Nullable List<String> groups) {
        List<Integer> keys = new ArrayList<>();
        if (groups == null) return keys;
        for (String group : groups) {
            int key = toKey(group);
            if (key > 0) keys.add(key);
        }
        return keys;
    }

    private static int toKey(String group) {
        try {
            return groupToKey(Integer.parseInt(group.trim()));
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    /** Id nhóm cơ trong dữ liệu bài tập → khoá trong muscle_map.json (giống FitnessConstUtils.getMuscle). */
    private static int groupToKey(int group) {
        switch (group) {
            case 1:
            case 13:
            case 14:
                return 1; // abs
            case 15:
            case 16:
                return 15; // adductors
            case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 17:
                return group;
            default:
                return -1;
        }
    }

    private static float smooth(float x) {
        x = Math.max(0f, Math.min(1f, x));
        return x * x * (3 - 2 * x);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float mapWidth() {
        return sData != null ? sData.width : 1000f;
    }

    private static float mapHeight() {
        return sData != null ? sData.height : 1000f;
    }

    private static final class MapData {
        float width;
        float height;
        int lineColor;
        int majorColor;
        int minorColor;
        final Path silhouette;
        final Path fill;
        final SparseArray<Path> muscles = new SparseArray<>();

        private MapData(Path silhouette, Path fill) {
            this.silhouette = silhouette;
            this.fill = fill;
        }

        static MapData read(AssetManager assets) throws Exception {
            String json;
            try (InputStream in = assets.open("muscle/muscle_map.json")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                json = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
            JSONObject root = new JSONObject(json);
            MapData data = new MapData(path(root.getString("silhouette")), path(root.getString("dark")));
            data.width = (float) root.getDouble("width");
            data.height = (float) root.getDouble("height");
            data.lineColor = Color.parseColor(root.getString("lineColor"));
            data.majorColor = Color.parseColor(root.getString("majorColor"));
            data.minorColor = Color.parseColor(root.getString("minorColor"));
            JSONObject muscles = root.getJSONObject("muscles");
            for (Iterator<String> it = muscles.keys(); it.hasNext(); ) {
                String key = it.next();
                data.muscles.put(Integer.parseInt(key), path(muscles.getString(key)));
            }
            return data;
        }

        private static Path path(String d) {
            Path path = PathParser.createPathFromPathData(d);
            // potrace xuất các lỗ (khoảng giữa các bó cơ) theo chiều ngược: dùng even-odd để giữ lỗ.
            path.setFillType(Path.FillType.EVEN_ODD);
            return path;
        }
    }
}
