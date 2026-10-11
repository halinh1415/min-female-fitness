package mint.woman.female.workout.utils;

import android.app.Activity;
import android.app.Application;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import mint.woman.female.workout.R;

/**
 * Android 15/16 (targetSdk 35+/36) force edge-to-edge and ignore
 * {@code windowOptOutEdgeToEdgeEnforcement}. Most screens of this app were designed for the
 * legacy "fit system windows" layout, so this helper restores that behaviour globally:
 * <ul>
 *     <li>pads the root content view by the status/navigation bar, display-cutout and IME insets</li>
 *     <li>paints the status bar / navigation bar areas with the colors declared in the theme</li>
 *     <li>chooses light/dark system bar icons based on those colors</li>
 * </ul>
 * Screens that draw behind the system bars themselves (paywall, new onboarding...) implement
 * {@link HandlesInsets} and are skipped.
 */
public final class EdgeToEdgeHelper {

    /** Marker for activities that handle window insets on their own. */
    public interface HandlesInsets {
    }

    private static final String APP_PACKAGE = "mint.woman.female.workout";

    private EdgeToEdgeHelper() {
    }

    public static void register(@NonNull Application application) {
        // Only needed where the platform enforces edge-to-edge.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (shouldApply(activity)) {
                    applyLegacyInsets(activity);
                }
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    private static boolean shouldApply(Activity activity) {
        if (activity instanceof HandlesInsets) return false;
        // Never touch third-party activities (AdMob, Facebook, FirebaseUI, Play Billing...).
        return activity.getClass().getName().startsWith(APP_PACKAGE);
    }

    /**
     * Pads legacy content and places real views behind the system bars. On targetSdk 35+
     * {@code statusBarColor} is ignored, so a theme color alone cannot paint this area.
     */
    public static void applyLegacyInsets(@NonNull Activity activity) {
        View decor = activity.getWindow().getDecorView();
        if (!(decor instanceof ViewGroup)) return;
        View content = decor.findViewById(android.R.id.content);
        if (content == null) return;

        View root = content;
        ViewParent parent = root.getParent();
        while (parent instanceof View && parent != decor) {
            root = (View) parent;
            parent = root.getParent();
        }
        if (parent != decor) return;

        int themedStatusColor = resolveColor(
                activity,
                android.R.attr.statusBarColor,
                Color.TRANSPARENT
        );
        int statusColor = resolveColor(
                activity,
                R.attr.statusBarProtectionColor,
                themedStatusColor
        );
        int navColor = resolveColor(activity, android.R.attr.navigationBarColor, Color.TRANSPARENT);
        Drawable windowBackground = decor.getBackground();
        int windowBgColor = windowBackground instanceof ColorDrawable
                ? ((ColorDrawable) windowBackground).getColor()
                : resolveColor(activity, android.R.attr.colorBackground, Color.WHITE);

        // Collapsing image headers (AppBarLayout fitsSystemWindows) draw behind the status bar
        // themselves; a solid protection band above the image looks detached.
        final boolean headerBehindStatusBar = hasFitsSystemWindowsAppBar(content);
        // Theme có action bar (của AppCompat, không phải Toolbar trong layout).
        final boolean hasDecorActionBar = activity instanceof androidx.appcompat.app.AppCompatActivity
                && ((androidx.appcompat.app.AppCompatActivity) activity).getSupportActionBar() != null;
        if (headerBehindStatusBar) {
            statusColor = Color.TRANSPARENT;
        }

        View statusProtection = createProtectionView(activity, statusColor, Gravity.TOP);
        View navigationProtection = createProtectionView(activity, navColor, Gravity.BOTTOM);
        ViewGroup decorGroup = (ViewGroup) decor;
        decorGroup.addView(statusProtection);
        decorGroup.addView(navigationProtection);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(activity.getWindow(), decor);
        controller.setAppearanceLightStatusBars(!headerBehindStatusBar && isLight(statusColor, windowBgColor));
        controller.setAppearanceLightNavigationBars(isLight(navColor, windowBgColor));

        final View target = root;
        ViewCompat.setOnApplyWindowInsetsListener(target, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets tappable = insets.getInsets(WindowInsetsCompat.Type.tappableElement());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(bars.bottom, ime.bottom);
            v.setPadding(bars.left, headerBehindStatusBar ? 0 : bars.top, bars.right, bottom);
            setProtectionHeight(statusProtection, bars.top);
            // Gesture navigation remains transparent; only 3-button navigation needs protection.
            setProtectionHeight(navigationProtection, tappable.bottom);
            statusProtection.bringToFront();
            navigationProtection.bringToFront();
            if (headerBehindStatusBar) {
                // Pass only the top inset down so fitsSystemWindows views offset the toolbar.
                return new WindowInsetsCompat.Builder()
                        .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, bars.top, 0, 0))
                        .build();
            }
            if (hasDecorActionBar) {
                // Để ActionBarOverlayLayout vẫn nhận insets: nó cộng chiều cao action bar vào insets
                // gửi cho khung nội dung (xem fitBelowActionBar).
                return insets.inset(bars.left, bars.top, bars.right, bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        if (hasDecorActionBar && content != target) {
            fitBelowActionBar(content);
        }
        if (target instanceof ViewGroup) {
            ((ViewGroup) target).setClipToPadding(true);
        }
        ViewCompat.requestApplyInsets(target);
    }

    /**
     * Cửa sổ edge-to-edge mang cờ LAYOUT_STABLE nên ActionBarOverlayLayout của AppCompat không đẩy
     * nội dung xuống dưới action bar nữa (action bar đè lên nội dung, vd màn Achievements), mà chỉ báo
     * chiều cao action bar qua insets gửi cho khung nội dung. Đệm khung nội dung theo insets đó.
     */
    private static void fitBelowActionBar(@NonNull View contentFrame) {
        final int startLeft = contentFrame.getPaddingLeft();
        final int startTop = contentFrame.getPaddingTop();
        final int startRight = contentFrame.getPaddingRight();
        final int startBottom = contentFrame.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(contentFrame, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(startLeft + bars.left, startTop + bars.top,
                    startRight + bars.right, startBottom + bars.bottom);
            return windowInsets.inset(bars.left, bars.top, bars.right, bars.bottom);
        });
    }

    private static boolean hasFitsSystemWindowsAppBar(View view) {
        if (view instanceof com.google.android.material.appbar.AppBarLayout) {
            return view.getFitsSystemWindows();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (hasFitsSystemWindowsAppBar(group.getChildAt(i))) return true;
            }
        }
        return false;
    }

    private static View createProtectionView(Activity activity, int color, int gravity) {
        View protection = new View(activity);
        protection.setBackgroundColor(color);
        protection.setClickable(false);
        protection.setFocusable(false);
        protection.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        protection.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                gravity
        ));
        return protection;
    }

    private static void setProtectionHeight(View protection, int height) {
        ViewGroup.LayoutParams params = protection.getLayoutParams();
        if (params.height != height) {
            params.height = height;
            protection.setLayoutParams(params);
        }
    }

    private static boolean isLight(int barColor, int fallbackColor) {
        int color = Color.alpha(barColor) < 32 ? fallbackColor : barColor;
        return ColorUtils.calculateLuminance(color | 0xFF000000) > 0.5;
    }

    private static int resolveColor(Activity activity, int attr, int fallback) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } catch (Exception e) {
            return fallback;
        } finally {
            a.recycle();
        }
    }

}
