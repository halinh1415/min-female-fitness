package mint.woman.female.workout.utils;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import mint.woman.female.workout.R;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Toàn màn hình như WorkoutActivity của Tai Chi: ẩn thanh trạng thái + thanh điều hướng, vuốt từ mép để hiện
 * tạm. Hộp thoại có cửa sổ riêng nên cũng phải ẩn trên cửa sổ đó, nếu không thanh hệ thống hiện lại mỗi khi mở hộp thoại.
 */
public final class ImmersiveHelper {

    private ImmersiveHelper() {
    }

    public static void hideSystemBars(@Nullable Window window) {
        if (window == null) return;
        WindowCompat.setDecorFitsSystemWindows(window, false);
        // Nền thanh hệ thống trong suốt (như EdgeToEdgeUtils bên Tai Chi): khi hộp thoại giữ focus, Activity tưởng
        // thanh hệ thống đang hiện và tô màu nền (đen) ở mép trên/dưới.
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /** Gọi ngay sau dialog.show(). */
    public static void keepDialog(@Nullable Dialog dialog) {
        if (dialog != null) hideSystemBars(dialog.getWindow());
    }

    /** Mọi DialogFragment mở trong [fm] (kể cả từ fragment con) đều giữ toàn màn hình. */
    public static void keepDialogFragments(@NonNull FragmentManager fm) {
        fm.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentStarted(@NonNull FragmentManager manager, @NonNull Fragment f) {
                if (f instanceof DialogFragment) keepDialog(((DialogFragment) f).getDialog());
            }
        }, true);
    }

    /**
     * Màn tập vẽ tràn cả vùng camera/tai thỏ. Chỉ thanh trên cùng ([topBar]: nút back, số bài, đồng hồ, thanh
     * tiến độ) và nút / chữ trong màn con (fragment trong [containerId]) lùi ra khỏi vùng đó, nền vẫn tràn viền.
     * Màn con được lùi thêm chiều cao [progress] vì trước đây chúng nằm ngay dưới thanh tiến độ.
     */
    public static void installCutoutInsets(@NonNull FragmentActivity activity, @NonNull View root,
                                           @NonNull View topBar, @NonNull View progress, @IdRes int containerId) {
        final Insets[] cutout = {Insets.NONE};
        final Runnable insetContent = () -> {
            Fragment f = activity.getSupportFragmentManager().findFragmentById(containerId);
            if (f != null && f.getView() != null) insetFragment(f.getView(), cutout[0], progress);
        };
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            cutout[0] = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            setPadding(topBar, cutout[0].left, cutout[0].top, cutout[0].right, 0);
            insetContent.run();
            return insets;
        });
        // Chiều cao thanh tiến độ chỉ có sau lần layout đầu.
        topBar.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> insetContent.run());
        activity.getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View view, @Nullable Bundle state) {
                if (f.getId() == containerId) insetFragment(view, cutout[0], progress);
            }
        }, false);
        ViewCompat.requestApplyInsets(root);
    }

    private static void insetFragment(View view, Insets cutout, View progress) {
        insetForCutout(view, Insets.of(cutout.left, cutout.top + progress.getHeight(), cutout.right, cutout.bottom));
    }

    /**
     * Lùi nút / chữ của [view] ra khỏi [inset] mà nền vẫn tràn viền. Với ConstraintLayout: con nào bám mép
     * (top/bottom/start/end tới parent) được cộng margin ở mép đó; con phủ kín cả 4 mép (ảnh nền, lớp phủ, video
     * nền...) hoặc căn giữa thì giữ nguyên. Khung bọc trong suốt chỉ có một con (fragment tự dựng lại layout khi
     * xoay màn hình) thì áp cho con bên trong, cả con mới thay vào sau này. Layout khác thì đệm padding cho cả view.
     */
    public static void insetForCutout(@NonNull View view, @NonNull Insets inset) {
        if (view instanceof ViewGroup && !(view instanceof ConstraintLayout) && view.getBackground() == null
                && ((ViewGroup) view).getChildCount() <= 1) {
            ViewGroup wrapper = (ViewGroup) view;
            if (wrapper.getTag(R.id.tag_cutout_inset) == null) {
                wrapper.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                    @Override
                    public void onChildViewAdded(View parent, View child) {
                        insetForCutout(child, (Insets) parent.getTag(R.id.tag_cutout_inset));
                    }

                    @Override
                    public void onChildViewRemoved(View parent, View child) {
                    }
                });
            }
            wrapper.setTag(R.id.tag_cutout_inset, inset);
            if (wrapper.getChildCount() == 1) insetForCutout(wrapper.getChildAt(0), inset);
            return;
        }
        if (!(view instanceof ConstraintLayout)) {
            int[] base = basePadding(view);
            setPaddingIfChanged(view, base[0] + inset.left, base[1] + inset.top, base[2] + inset.right, base[3] + inset.bottom);
            return;
        }
        ViewGroup group = (ViewGroup) view;
        boolean rtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (!(child.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) continue;
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) child.getLayoutParams();
            int parent = ConstraintLayout.LayoutParams.PARENT_ID;
            boolean top = lp.topToTop == parent;
            boolean bottom = lp.bottomToBottom == parent;
            boolean start = lp.startToStart == parent || (rtl ? lp.rightToRight : lp.leftToLeft) == parent;
            boolean end = lp.endToEnd == parent || (rtl ? lp.leftToLeft : lp.rightToRight) == parent;
            if (top && bottom && start && end) continue;
            int[] base = (int[]) child.getTag(R.id.tag_cutout_base_margins);
            if (base == null) {
                base = new int[]{lp.getMarginStart(), lp.topMargin, lp.getMarginEnd(), lp.bottomMargin};
                child.setTag(R.id.tag_cutout_base_margins, base);
            }
            int startInset = rtl ? inset.right : inset.left;
            int endInset = rtl ? inset.left : inset.right;
            boolean fillsHeight = top && bottom;
            int s = base[0] + (start ? startInset : 0);
            int t = base[1] + (top && !fillsHeight ? inset.top : 0);
            int e = base[2] + (end ? endInset : 0);
            int b = base[3] + (bottom && !fillsHeight ? inset.bottom : 0);
            if (lp.getMarginStart() != s || lp.topMargin != t || lp.getMarginEnd() != e || lp.bottomMargin != b) {
                lp.setMarginStart(s);
                lp.setMarginEnd(e);
                lp.topMargin = t;
                lp.bottomMargin = b;
                child.setLayoutParams(lp);
            }
        }
    }

    private static void setPadding(View view, int left, int top, int right, int bottom) {
        int[] base = basePadding(view);
        setPaddingIfChanged(view, base[0] + left, base[1] + top, base[2] + right, base[3] + bottom);
    }

    private static int[] basePadding(View view) {
        int[] base = (int[]) view.getTag(R.id.tag_cutout_base_padding);
        if (base == null) {
            base = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()};
            view.setTag(R.id.tag_cutout_base_padding, base);
        }
        return base;
    }

    private static void setPaddingIfChanged(View view, int left, int top, int right, int bottom) {
        if (view.getPaddingLeft() != left || view.getPaddingTop() != top
                || view.getPaddingRight() != right || view.getPaddingBottom() != bottom) {
            view.setPadding(left, top, right, bottom);
        }
    }
}
