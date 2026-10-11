package mint.woman.female.workout.activity.ui.premium

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import mint.woman.female.workout.admodule.MaticAd
import mint.woman.female.workout.admodule.MaticAdCallBack
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Locale

/**
 * Vòng quay may mắn (giống Home Workout / WomenWorkout): hiện dạng popup đè lên paywall (activity
 * nền trong suốt, PaywallActivity vẫn nằm phía sau) khi đóng paywall ở cuối onboarding mà chưa mua.
 * Vòng quay mặc định dừng ở ô JACKPOT rồi hiện vé ưu đãi [DiscountDialogFragment] (giá base plan
 * yearly-special-50-off). Đóng vé / bỏ qua: quảng cáo inter (nếu có) rồi đóng, PaywallActivity
 * nhận kết quả và đóng theo.
 */
class LuckyDrawActivity : AppCompatActivity(), DiscountDialogFragment.DismissListener {

    private var closing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FirebaseAnalytics.getInstance(this).logEvent("lucky_draw_view", null)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = close()
        })
        setContent {
            LuckyWheelOverlay(
                colors = WHEEL_COLORS,
                jackpotLabel = "50%",
                onWon = {
                    FirebaseAnalytics.getInstance(this).logEvent("lucky_draw_won", null)
                    // Mở đợt 50% (2 tiếng) dùng chung đồng hồ với paywall, tính vào bậc thang PaywallPromo.
                    val end = PaywallPromo.startNow(this)
                    if (!isFinishing && supportFragmentManager.findFragmentByTag("discount") == null) {
                        DiscountDialogFragment.newInstance(end - System.currentTimeMillis()).show(supportFragmentManager, "discount")
                    }
                },
                onSkip = ::close,
            )
        }
    }

    /** Đóng vé ưu đãi mà chưa mua: cho xem quảng cáo rồi mới đóng (như bản cũ). */
    override fun onDiscountDialogDismissed() = close()

    private fun close() {
        if (closing) return
        closing = true
        if (MyPreference(this).isPremiumMember || !MaticAd.getInstance().isAvailableInter) {
            finish()
            return
        }
        MaticAd.getInstance().showPriorityInterstitialAd(this, object : MaticAdCallBack() {
            override fun onNextAction() {
                super.onNextAction()
                finish()
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.length > 1) lang.substring(0, 2) else Locale.getDefault().language),
        )
    }

    private companion object {
        /** Màu theo app: viền hồng thương hiệu, ô kem / vàng, jackpot tím mận, nút ĐI hồng. */
        val WHEEL_COLORS = LuckyWheelColors(
            rim = listOf(Color(0xFFFF6FA0), Color(0xFFE91E63), Color(0xFF8E0F3E)),
            segmentA = Color(0xFFFFF4DC),
            segmentB = Color(0xFFFFD873),
            jackpot = Color(0xFF7B1FA2),
            text = Color(0xFFC2185B),
            jackpotText = Color.White,
            button = listOf(Color(0xFFFF6699), Color(0xFFE91E63)),
        )
    }
}
