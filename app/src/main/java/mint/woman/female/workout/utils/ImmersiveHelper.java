package mint.woman.female.workout.utils;

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
}
