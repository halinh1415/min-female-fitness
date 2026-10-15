package mint.woman.female.workout.activity.ui.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable
import coil.compose.AsyncImage
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.LightMinPalette

private val B_ACCENT = LightMinPalette.pink
// Tông rose của Min thay cho dải vàng của WomenWorkout (tên biến giữ nguyên cho dễ đối chiếu).
private val B_GOLD = LightMinPalette.pink
private val B_GOLD_DARK = Color(0xFFC2185B)
private val B_GOLD_LIGHT = LightMinPalette.pinkLight
private val B_GOLD_BG = LightMinPalette.pinkSoft
private val B_GOLD_DIM = Color(0xFFF8D3E1)
private val B_INK = Color.White
private val B_INK_MUTED = Color(0xFFC2185B)
private val B_CTA = listOf(LightMinPalette.pinkLight, LightMinPalette.pink)

/**
 * Paywall (giống paywall B của WomenWorkout, tông hồng Min) - bố cục theo app Karate: ảnh hero tràn viền, giá quy về mỗi tuần,
 * thẻ giá đặc biệt (chỉ khi Play có base plan), nút CTA "Không cần thanh toán ngay", điều khoản
 * đăng ký. Nút đóng có thể ẩn vài giây đầu ([closeVisible]).
 */
@Composable
fun PaywallScreenB(
    prices: PaywallPrices,
    selected: Int,
    closeVisible: Boolean,
    onSelect: (Int) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onClose: () -> Unit,
    onPromoExpired: () -> Unit = {},
    footer: PaywallFooter = PaywallFooter(),
    /** Đưa gói Trọn đời lên đầu trong dải vàng (lần chào Trọn đời thay cho giảm 34%). */
    featureLifetime: Boolean = false,
) {
    val colors = PwColors
    val trialShown = prices.trialAvailable && selected == PaywallPlan.YEARLY

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Hero()
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    if (prices.special != null) {
                        SpecialCard(prices.special, selected == PaywallPlan.SPECIAL, onPromoExpired) { onSelect(PaywallPlan.SPECIAL) }
                    }
                    if (featureLifetime) {
                        GoldBand(stringResource(R.string.pwb_badge_best), selected == PaywallPlan.LIFETIME, { onSelect(PaywallPlan.LIFETIME) }) {
                            LifetimeRow(prices.lifetime, badge = null, selected = selected == PaywallPlan.LIFETIME) { onSelect(PaywallPlan.LIFETIME) }
                        }
                    }
                    PlanRow(
                        title = if (prices.trialAvailable) stringResource(R.string.pwb_trial_title, prices.trialDays)
                        else stringResource(R.string.pwb_yearly_title),
                        subtitle = prices.yearly?.let { stringResource(R.string.pwb_yearly_price, it) },
                        right = prices.yearlyWeekly?.let { stringResource(R.string.pwb_per_week, it) },
                        // Tag góc như Karate: tiết kiệm so với trả theo tháng, không tính được thì "Giá gốc".
                        badge = prices.savePercent?.let { stringResource(R.string.pwb_save, "$it%") }
                            ?: stringResource(R.string.pwb_badge_original),
                        selected = selected == PaywallPlan.YEARLY,
                        onClick = { onSelect(PaywallPlan.YEARLY) },
                    )
                    PlanRow(
                        title = stringResource(R.string.pwb_monthly_title),
                        subtitle = prices.monthly?.let { stringResource(R.string.pwb_monthly_price, it) },
                        right = prices.monthlyWeekly?.let { stringResource(R.string.pwb_per_week, it) },
                        badge = null,
                        selected = selected == PaywallPlan.MONTHLY,
                        onClick = { onSelect(PaywallPlan.MONTHLY) },
                    )
                    if (!featureLifetime) {
                        LifetimeRow(prices.lifetime, stringResource(R.string.pwb_badge_best), selected == PaywallPlan.LIFETIME) {
                            onSelect(PaywallPlan.LIFETIME)
                        }
                    }
                }
                Text(
                    stringResource(R.string.pwb_terms),
                    color = colors.muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
                Box(Modifier.fillMaxWidth().padding(bottom = 8.dp), contentAlignment = Alignment.Center) { LegalLinks(footer) }
            }

            // Thanh CTA ghim dưới.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(colors.background.copy(alpha = 0.9f), colors.surface)))
                    .navigationBarsPadding()
                    .padding(top = 10.dp, bottom = 10.dp),
            ) {
                Button(
                    onClick = onPurchase,
                    enabled = footer.ctaEnabled,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.8f),
                    ),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(64.dp)
                        .alpha(if (footer.ctaEnabled) 1f else 0.45f),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(B_CTA), RoundedCornerShape(50)),
                    ) {
                        Text(
                            if (trialShown) stringResource(R.string.pwb_cta_trial, prices.trialDays)
                            else stringResource(R.string.pwb_cta_sub),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                        )
                        if (trialShown) {
                            Text("✓ " + stringResource(R.string.pwb_no_payment), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                PriceErrorRow(footer)
                Text(
                    when {
                        trialShown && prices.yearly != null -> stringResource(R.string.pwb_note_trial, prices.trialDays, prices.yearly)
                        selected == PaywallPlan.LIFETIME -> stringResource(R.string.pwb_note_lifetime)
                        selected == PaywallPlan.SPECIAL && prices.special != null ->
                            stringResource(R.string.pwb_yearly_price, prices.special.price) + "\n" + stringResource(R.string.pwb_note_sub)
                        else -> stringResource(R.string.pwb_note_sub)
                    },
                    color = colors.muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
                )
            }
        }

        // Nút đóng + khôi phục nằm đè lên ảnh hero, có nền mờ để luôn đọc được.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            AnimatedVisibility(closeVisible, enter = fadeIn() + scaleIn()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(onClick = onClose),
                ) {
                    Text("✕", color = Color.White, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.pwb_restore),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onRestore)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Hero() {
    val colors = PwColors
    Box(Modifier.fillMaxWidth().height(340.dp)) {
        AsyncImage(
            model = "file:///android_asset/images/img_premium.webp",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )
        // Mờ dần về màu nền để chữ dễ đọc.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.35f to Color.Transparent,
                    0.78f to colors.background.copy(alpha = 0.92f),
                    1f to colors.background,
                ),
            ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
        ) {
            Text(
                stringResource(R.string.pwb_title),
                color = colors.title,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(8.dp))
            listOf(R.string.pwb_check_1, R.string.pwb_check_2, R.string.pwb_check_3).forEach {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(B_GOLD),
                    ) {
                        Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(it), color = colors.title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * Gói ưu đãi bọc trong dải vàng "GIÁ ĐẶC BIỆT" (sáng lên khi được chọn). Đợt khuyến mãi 50% có tag
 * đếm ngược ở góc; hết giờ gọi [onExpired] để quay về giá thường.
 */
@Composable
private fun SpecialCard(special: SpecialOffer, selected: Boolean, onExpired: () -> Unit, onClick: () -> Unit) {
    val colors = PwColors
    val endsAt = special.endsAt
    var remaining by remember(endsAt) { mutableLongStateOf(endsAt?.let { it - System.currentTimeMillis() } ?: 0L) }
    if (endsAt != null) {
        LaunchedEffect(endsAt) {
            while (true) {
                remaining = endsAt - System.currentTimeMillis()
                if (remaining <= 0L) {
                    onExpired()
                    break
                }
                delay(1000L - System.currentTimeMillis() % 1000L)
            }
        }
    }
    GoldBand(stringResource(R.string.pwb_special_band), selected, onClick) {
        PlanRow(
            title = stringResource(R.string.pwb_special_title, "${special.percent}%"),
            subtitle = null,
            subtitleRich = {
                val text = stringResource(R.string.pwb_special_price, special.price, special.original)
                Text(
                    buildAnnotatedString {
                        append(text)
                        // Chỉ gạch đúng con số giá gốc (lần xuất hiện cuối, giá ưu đãi đứng trước).
                        val start = text.lastIndexOf(special.original)
                        if (start >= 0) {
                            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, start + special.original.length)
                        }
                    },
                    color = colors.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            right = special.weekly?.let { stringResource(R.string.pwb_per_week, it) },
            badge = if (endsAt != null) stringResource(R.string.pwb_ends_in, formatCountdown(remaining)) else null,
            badgeIsTimer = true,
            selected = selected,
            onClick = onClick,
        )
    }
}

/** Dải vàng bọc gói đang được chào (sáng lên khi được chọn), nhãn [label] ở trên. */
@Composable
private fun GoldBand(label: String, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(B_GOLD_LIGHT, B_GOLD))
                else Brush.horizontalGradient(listOf(B_GOLD_BG, B_GOLD_BG)),
            )
            .clickable(onClick = onClick)
            .padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
    ) {
        Text(
            label,
            color = if (selected) B_INK else B_INK_MUTED,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        )
        content()
    }
}

@Composable
private fun LifetimeRow(price: String?, badge: String?, selected: Boolean, onClick: () -> Unit) {
    PlanRow(
        title = stringResource(R.string.pwb_lifetime_title),
        subtitle = price?.let { stringResource(R.string.pwb_lifetime_price, it) },
        right = stringResource(R.string.pwb_forever),
        badge = badge,
        selected = selected,
        onClick = onClick,
    )
}

private fun formatCountdown(millis: Long): String {
    val seconds = (millis.coerceAtLeast(0L) + 999L) / 1000L
    return String.format(Locale.US, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
}

@Composable
private fun PlanRow(
    title: String,
    subtitle: String?,
    right: String?,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit,
    subtitleRich: (@Composable () -> Unit)? = null,
    badgeIsTimer: Boolean = false,
) {
    val colors = PwColors
    Box(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) B_ACCENT else colors.border), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(start = 18.dp, end = 18.dp, top = if (badge != null) 22.dp else 16.dp, bottom = 16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = colors.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                when {
                    subtitleRich != null -> subtitleRich()
                    // Chính sách Google Play: giá tổng bị trừ phải nổi bật hơn giá quy đổi (tuần / tháng).
                    subtitle != null -> Text(subtitle, color = colors.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    else -> Text("—", color = colors.muted, fontSize = 16.sp)
                }
            }
            Text(right ?: "", color = colors.muted, fontSize = 12.sp)
        }
        if (badge != null) {
            // Tag góc trên phải: vàng khi gói được chọn, mờ khi không (như Karate).
            Text(
                badge,
                color = if (selected) B_INK else B_INK_MUTED,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                // Chữ số đều nhau để đồng hồ không giật mỗi giây.
                style = if (badgeIsTimer) TextStyle(fontFeatureSettings = "tnum") else TextStyle.Default,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topEnd = 18.dp, bottomStart = 14.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(B_GOLD, B_GOLD_DARK))
                        else Brush.horizontalGradient(listOf(B_GOLD_DIM, B_GOLD_DIM)),
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

/** Màu dùng trong paywall, lấy từ bảng màu của Min. */
/** Paywall luôn dùng bộ màu sáng (kể cả khi app đang giao diện tối) cho đồng nhất với ảnh hero. */
private object PwColors {
    val background = LightMinPalette.background
    val surface = LightMinPalette.surface
    val title = LightMinPalette.textPrimary
    val body = LightMinPalette.textSecondary
    val muted = LightMinPalette.textMuted
    val border = LightMinPalette.track
    val bad = LightMinPalette.danger
    val primary = LightMinPalette.pink
}

/** Vị trí gói giống PremiumViewModel.PLAN_*: 0 trọn đời, 1 năm, 2 tháng, 3 năm giá ưu đãi. */
object PaywallPlan {
    const val LIFETIME = PremiumViewModel.PLAN_LIFETIME
    const val YEARLY = PremiumViewModel.PLAN_YEARLY
    const val MONTHLY = PremiumViewModel.PLAN_MONTHLY
    /** Gói năm giá đặc biệt (base plan giảm 50% / 34% trên Play Console). */
    const val SPECIAL = PremiumViewModel.PLAN_SPECIAL
}

/** Gói năm giá đặc biệt (base plan riêng trên Play Console). */
@Immutable
data class SpecialOffer(
    val price: String,
    val original: String,
    val weekly: String?,
    val percent: Int,
    val basePlanId: String,
    /** Hết hạn đợt khuyến mãi (millis), null = ưu đãi thường không có đồng hồ. */
    val endsAt: Long? = null,
)

/** Giá lấy từ Google Play; null = chưa tải xong. */
@Immutable
data class PaywallPrices(
    val yearly: String? = null,
    val monthly: String? = null,
    val lifetime: String? = null,
    val savePercent: Int? = null,
    val trialAvailable: Boolean = false,
    val trialDays: Int = 7,
    val yearlyWeekly: String? = null,
    val monthlyWeekly: String? = null,
    val special: SpecialOffer? = null,
)

/** Phần dưới nút mua. */
@Immutable
data class PaywallFooter(
    /** Đã có giá của gói đang chọn: chưa có thì khoá nút mua. */
    val ctaEnabled: Boolean = true,
    /** Tải giá quá lâu / lỗi mạng: hiện "Không tải được giá · Thử lại". */
    val showPriceError: Boolean = false,
    val onRetry: () -> Unit = {},
    val termsUrl: String = "",
    val privacyUrl: String = "",
    val onOpenUrl: (String) -> Unit = {},
)

@Composable
private fun PriceErrorRow(footer: PaywallFooter) {
    if (!footer.showPriceError) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
        Text(stringResource(R.string.pwb_prices_error), color = PwColors.bad, fontSize = 13.sp)
        Text(
            "  " + stringResource(R.string.pwb_retry),
            color = PwColors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = footer.onRetry).padding(4.dp),
        )
    }
}

/** Link Điều khoản / Chính sách (rỗng thì không hiện). */
@Composable
private fun LegalLinks(footer: PaywallFooter) {
    val links = listOfNotNull(
        footer.termsUrl.takeIf { it.isNotBlank() }?.let { R.string.pwb_terms_link to it },
        footer.privacyUrl.takeIf { it.isNotBlank() }?.let { R.string.pwb_privacy_link to it },
    )
    if (links.isEmpty()) return
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        links.forEachIndexed { index, (label, url) ->
            if (index > 0) Text("·", color = PwColors.muted, fontSize = 12.sp)
            Text(
                stringResource(label),
                color = PwColors.muted,
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { footer.onOpenUrl(url) }.padding(6.dp),
            )
        }
    }
}
