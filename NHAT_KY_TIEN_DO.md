# Nhật ký tiến độ

> Mới nhất ở trên.

## 2026-10-17 – Highlight nhóm cơ: bỏ làm mờ nền ✅ (yêu cầu Telegram, chưa push)
- `MuscleMapView`: bỏ `baseAlpha` / `DIM_ALPHA` / `saveLayerAlpha` (giống Home Workout) – hình cơ thể giữ nguyên độ sáng,
  chỉ nhóm cơ được chọn nhấp nháy vàng.
- Build OK. Test S8+: bấm chip nhóm cơ, nền không mờ, chỉ vùng cơ nhấp nháy, không crash.

## 2026-10-17 – Màn Lịch sử không hiện ảnh chương trình ✅ (yêu cầu Telegram, chưa push)
- Nguyên nhân: `DoWorkoutActivity.createHistory()` không bao giờ lưu `image` → `HistoryActivity.imageUrl()` ghép
  "file:///android_asset/images/" + "" → URL chỉ là thư mục, ảnh trống.
- `HistoryActivity.imageUrl()`: lấy ảnh hiện tại của chương trình (`imageBase()` + `image`, bài discover lấy từ server),
  nếu không tra được chương trình thì dùng tên ảnh đã lưu, cuối cùng ảnh mặc định `custom_workout.webp`.
- `HistoryScreen`: `errorUrl` = ảnh mặc định khi tải lỗi. `DoWorkoutActivity`: lưu `image` cho lịch sử mới.
- Build OK. Test S8+: tập xong "Tập luyện văn phòng" → Lịch sử hiện đúng ảnh chương trình, không crash.
  (Trên máy test còn lại 1 dòng lịch sử + huy hiệu "Buổi tập đầu tiên" từ lần test này.)

## 2026-10-17 – Dịch Cài đặt + màn Thành tích sang 26 ngôn ngữ ✅ (yêu cầu Telegram, đã push)
- `res/xml/settings_preference.xml`, `sound_settings.xml`: bỏ chữ tiếng Anh cứng (General, Music, Voice Guide, TTS Engine,
  Privacy Policy, Version) → string resource (`pref_category_general`, `pref_tts_engine`, `pref_version` mới; dùng lại
  `txt_music`, `txt_voice_guide`, `pwb_privacy_link`).
- 20 chuỗi `badge_*` + 7 plurals đơn vị (đúng quy tắc số nhiều từng ngôn ngữ: ar 6 dạng, ru/pl/cs few/many, hr, lv...)
  + 3 chuỗi Cài đặt → `res/values-<lang>/strings_badges.xml`.
- Tên / mô tả / gợi ý 29 huy hiệu + 6 nhóm → `assets/badges/badges-<tag>.json` (loader có sẵn tự ghép; thêm bản trùng cho
  `id`, `nb`, `zh` vì Locale có thể trả mã khác).
- Nguồn: `tools/badge_i18n/<lang>.txt` (Claude dịch), sinh lại bằng `python3 tools/build_badge_translations.py`
  (`--template` in danh sách câu tiếng Anh). Script kiểm đủ khoá và số placeholder.
- Build debug + bundleRelease OK. Test S8+ tiếng Việt: Cài đặt hiện "Thành tích – Đã đạt 0/29", "Âm nhạc"; màn Thành tích
  toàn bộ tiếng Việt. Chữ in sẵn trên ảnh huy hiệu (FIRST WORKOUT...) vẫn tiếng Anh vì nằm trong ảnh.

## 2026-10-17 – Dọn file không dùng ✅ (yêu cầu Telegram, chưa push)
- Quét bằng lint `UnusedResources` (thêm tạm khối `lint { checkOnly ... }` rồi bỏ) + kiểm tra tham chiếu động: app không
  dùng `getIdentifier` cho drawable/string (chỉ `raw video_` và dimen hệ thống), ảnh chương trình nằm trong `assets/images`.
- Xoá 241 file resource (drawable/layout/anim/menu/raw, ~3,6 MB nguồn), 3 color state list `ob_*` chỉ dùng trong style
  thừa, 10 style không dùng, ~6.000 mục giá trị (string ở mọi ngôn ngữ, color, dimen, integer, array).
- Xoá 11 class không được tham chiếu: `BottomNavigationViewBehavior`, `CustomAppBarLayoutBehavior`, `GridSpacingItemDecoration`,
  `ProgressStackedView`, `StackedLineDrawable`, `SuffixEditText`, `TipObject`, `DialogUtils`, `DownloadVideoWorker`,
  `RecipeDatabase`, `XORDecryptionInputStream`. Xoá asset `breath.json`, `slot_machine.json`.
- Giữ: mipmap launcher, id trong layout (vô hại), video `assets/i/g|v` (bài tập lấy theo id lúc chạy, file plan mã hoá).
- AAB release 72,1 MB → 68,5 MB. assembleDebug + bundleRelease OK. Test S8+: 5 tab, preview, tải bài, màn tập chạy bình
  thường, không crash / thiếu resource.
- Có sẵn từ trước (không do dọn): nhóm cài đặt "General" và "Achievements" còn chữ tiếng Anh cứng.

## 2026-10-17 – Bản 1.1.30 (130) ✅ đã push
- Gồm: vector muscle map viền hồng + highlight nhóm cơ.
- assembleDebug + bundleRelease OK (AAB chưa ký, anh tự ký).

## 2026-10-17 – Bản đồ cơ vector (hình nữ, viền hồng nhẹ) + highlight nhóm cơ ✅ (yêu cầu Telegram, chưa push)
- Thay ảnh cơ thể xám dạng giải phẫu (`img_body.png` + 28 ảnh `img_<cơ>_1/2.png`) bằng bản đồ cơ vector dùng chung với
  Women Workout: `assets/muscle/muscle_map.json` lấy từ app đó (trace bằng `WomenWorkout/tools/trace_muscle_map.py`),
  đổi màu: viền `#F8BBD0` (hồng nhẹ, thay xanh lá), cơ chính `#F20C0D`, cơ phụ `#F1696C` (khớp chấm `ic_red_1/2` trên chip).
- Mới `custom/MuscleMapView.java` (port từ Women Workout): nhấn chip hoặc nhấn thẳng vào nhóm cơ trên hình → nhấp nháy vàng.
- `ExerciseDetailActivity`, `DialogDemoWorkout`, 3 layout dùng `muscle_map`; bỏ code ghép bitmap ở luồng nền (trước đây gọi
  `getResources()` trong luồng nền của dialog, có thể crash khi dialog đã đóng). Xoá `FitnessConstUtils.getMuscle`.
- `chip_item.xml` + `color/chip_muscle_bg|text`: màu chip chọn theo accent hồng.
- Build OK. Test S8+: màn chi tiết + dialog (i) lúc tập (Bật nhảy): vẽ đúng, chip + nhấn vào mông / bắp chân / ngực / vai
  highlight đúng, không crash. Có video.

## 2026-10-17 – Bản 1.1.29 (129): chống ANR TTS ✅
- `versionCode 129`, `versionName "1.1.29"`. assembleRelease + bundleRelease OK (chưa ký).
- Bản release ký key debug trên emulator: mở app, Preview → Coach voice → nghe thử Default có yêu cầu đọc, không crash/ANR.

## 2026-10-17 – Chống ANR TextToSpeech (rà theo lỗi ANR Muay Thai) ✅
- `CoachOptionActivity`: tạo engine, setLanguage/getDefaultVoice, nút nghe thử, shutdown chạy trên `TtsExecutor` (trước gọi trên
  main thread → nguy cơ ANR). Test emulator: mở Coach voice, nghe thử Default gửi được yêu cầu đọc, không ANR.

## 2026-10-16 – Chính sách Subscriptions lần 2: ghi rõ điều khoản gói đăng ký – 1.1.28 (128) ✅ (yêu cầu Telegram, chưa commit)
- Google vẫn từ chối bản 125 ("Terms of subscription offer are unclear"), ảnh kèm: vé giảm 50% ("Claim Now!") và thẻ Flash sale ở Home
  "Get it" mở thẳng hộp thanh toán Google Play – trong app không ghi tự gia hạn / chu kỳ trừ tiền / có bắt buộc đăng ký không.
- Mới `values*/strings_sub_terms.xml` (27 ngôn ngữ): `sub_terms_yearly` / `sub_terms_monthly` / `sub_terms_trial` (giá, trừ tiền mỗi năm/tháng,
  tự gia hạn đến khi huỷ, huỷ trên Google Play) + `sub_terms_optional` (không bắt buộc đăng ký, bài miễn phí vẫn dùng được).
- `layout_dialog_discount.xml`, `DiscountDialogFragment.java`: dưới nút Claim Now! thêm dòng điều khoản (giá ưu đãi/năm, tự gia hạn, không bắt buộc).
- `HomeFragment.kt`: Flash sale "Get it" mở vé ưu đãi (có điều khoản) thay vì mở thẳng hộp thanh toán; hết đợt thì vào paywall.
- `PaywallScreenB.kt`: dòng dưới nút CONTINUE theo gói đang chọn (năm / tháng / ưu đãi / dùng thử) ghi giá + chu kỳ + tự gia hạn + không bắt buộc
  (trước gói năm / tháng chỉ ghi "Cancel anytime via Google Play"), chữ đậm hơn.
- `PaywallActivity.java` (paywall onboarding): dòng thanh toán dùng cùng chuỗi + thêm câu không bắt buộc.
- Nâng 1.1.28 (128). Build OK. Test S8+: paywall gói năm ưu đãi / tháng; Home → Flash sale → vé hiện điều khoản dưới nút. Đã tắt màn hình.
- Lưu ý phát hành: Google báo lỗi bản 125 vì 125 còn nằm ở một track (thường là Production) – phải đưa bản mới vào chính track đó
  (bản 127 chỉ ở beta không thay được 125).

## 2026-10-15 – Sửa vi phạm chính sách Subscriptions (giá chia nhỏ nổi bật) – 1.1.27 (127) ✅
- Google từ chối bản 125: vé giảm giá hiện "Total …/year" và "Only …/month" cùng 16sp → giá tổng không nổi bật hơn giá theo tháng;
  ký hiệu ₱ ở giá tổng hiện ô vuông (font đậm của app thiếu ký tự).
- `layout_dialog_discount.xml`: giá tổng 16sp đậm màu đậm (font hệ thống), giá gốc gạch ngang dòng riêng 14sp nhạt, giá tháng 13sp nhạt.
- `PaywallScreenB.kt`, `item_paywall_plan.xml`: giá tổng 16sp đậm, giá/tuần 12sp nhạt (trước cùng 14sp).
- Test S8+ (bản debug): vé sau Lucky Draw và paywall B hiện đúng. Nâng 1.1.27 (127).
- Sửa thêm: gói năm ở paywall B hiện / mua nhầm giá base plan giảm giá (`baseOffer()` lấy offer không offerId đầu tiên của
  premium_yearly – có 3 base plan). Thêm `PaywallOffers.yearlyBaseOffer()` (base plan premium-yearly-base); PremiumActivity và
  `PremiumViewModel.getDefaultOffer()` (cả dùng thử) dùng base plan này. S8+: Yearly 1.000.000 ₫ (trước 752.000 ₫), ưu đãi
  "48% Off (was 1.000.000 ₫)" (trước "31% … was 752.000"). Bản debug không mở được Google Play nên chưa thấy hộp mua.

## 2026-10-13 – SetGoalActivity chuyển sang Jetpack Compose (yêu cầu Telegram, chưa commit)
- Mới `activity/ui/weekgoal/SetGoalActivity.kt` (thay `SetGoalActivity.java` + `res/layout/activity_set_goal.xml`, đã xoá):
  thanh trên ← + "Week Goal", icon mục tiêu trong vòng hồng nhạt, tiêu đề + mô tả, thẻ "Số ngày tập" 7 ô tròn 1–7
  (thay NumberPicker), thẻ "Ngày đầu tuần" 3 nút Thứ Hai / Thứ Bảy / Chủ Nhật theo ngôn ngữ app (thay Spinner), nút Save
  hồng. Lưu MyPreference goalOfWeek / firstDayOfWeek + trả RESULT_OK như cũ; giữ lựa chọn khi xoay / bị kill.
  Bỏ AdView không bao giờ được nạp quảng cáo trong layout cũ.
- `AndroidManifest.xml`: SetGoalActivity dùng theme `AppTheme.Main` (không action bar, như các màn Compose khác).
- Build OK. Test máy ảo Android 16: Home ✎ → chọn 3 ngày + Chủ Nhật → Save → Home hiện 1/3, tuần bắt đầu CN.

## 2026-10-13 – Làm lại card Fitness Coach (AI) đầu Home (yêu cầu Telegram, chưa commit)
- `activity/ui/home/HomeScreen.kt` (`CoachCard`): thẻ ảnh tràn viền 210dp bo 24dp (cùng kiểu thẻ thử thách): ảnh nền +
  người mẫu, gradient tối dưới đáy, nhãn "✨ Gợi ý hôm nay" dạng chip mờ góc trên, tên bài 22sp + thông tin chữ trắng,
  nút viên thuốc trắng "▶ Bắt đầu" chữ hồng; bấm cả thẻ cũng mở bài. Bỏ nhãn xanh lá "Fitness Coach (AI)" + nhãn cam,
  nửa dưới trắng và nút hồng to in hoa. Dòng nhắc streak phía trên nhẹ hơn (15sp, Medium).
- Build OK. Test máy ảo Android 16: hiển thị đúng, bấm "Start now" mở PreviewActivity.

## 2026-10-13 – Home bớt chói (yêu cầu Telegram, chưa commit)
- `activity/ui/home/HomeScreen.kt` (PersonalPlanCard): gradient tím → mận → hồng đậm chữ trắng → nền pastel hồng nhạt →
  tím nhạt, viền tím mờ, chữ tối; nút Create/Continue tím đặc chữ trắng; thanh tiến độ tím → hồng.
- `activity/ui/premium/FlashSaleCard.kt`: gradient hồng đậm → nền hồng nhạt (PinkSoft) + viền hồng, tiêu đề hồng, giờ chữ
  xám, nút "Nhận ngay" hồng đặc. Nút START NOW giữ là điểm nhấn hồng đậm duy nhất. Dùng màu MinColor nên chế độ tối tự đổi.
- Build OK. Ảnh trước/sau trên máy ảo Android 16.

## 2026-10-13 – Report sau khi tập xong, bấm Home không quay về Home ✅ (yêu cầu Telegram, chưa commit)
- Nguyên nhân: `YogaActivity.onNewIntent` (IS_REPORT) gọi `navController.navigate(nav_report)` → Report bị đẩy lên back
  stack của tab Home; bottom nav (multiple back stacks) bấm Home lại khôi phục stack đã lưu của Home = Report.
- Sửa `activity/ui/main/YogaActivity.kt`: chuyển tab bằng `nav_view.selectedItemId = nav_report` (giống bấm tab).
- Build OK. Test máy ảo Android 16: tập Morning warmup → Done → Report (tab Report sáng) → Home về đúng Home; đổi
  tab Settings ↔ Home bình thường. Có video.

## 2026-10-13 – Bản địa hóa paywall / vé ưu đãi / Flash Sale / Home (yêu cầu Telegram, chưa commit)
- Rà các chuỗi dùng ở paywall (PaywallActivity, PaywallScreenB), vé ưu đãi, thẻ Flash Sale, Home: 26 ngôn ngữ thiếu
  tổng cộng ~900 chuỗi (cs/hi/hr/lv/no thiếu cả paywall cũ; da/fa/th/fi/sv thiếu pw_*; mọi ngôn ngữ thiếu
  discount_* và chữ "Loading price…").
- `res/layout/layout_dialog_discount.xml`: bỏ chữ viết cứng "Special Discount", "off", "Claim Now!" →
  `discount_title`, `discount_off`, `discount_claim` (tiếng Anh trong `values/strings_flash_sale.xml`).
- Mới `res/values-*/strings_l10n_premium.xml` (26 ngôn ngữ): 283 chuỗi lấy nguyên từ bản dịch đã có của các app anh em
  (cùng key, cùng tiếng Anh), phần còn lại tự dịch; kiểm tra placeholder khớp tiếng Anh. Sau sửa: 0 chuỗi thiếu.
- Build OK. Test máy ảo Android 16 ở tiếng Pháp, Bồ Đào Nha, Hindi: thẻ Flash Sale, vé ưu đãi ("de desconto" dài
  nhất vẫn vừa), paywall đều hiện đúng ngôn ngữ, không tràn chữ.

## 2026-10-13 – Flash Sale "Nhận ngay" mở thanh toán, Settings mở vé ưu đãi, vé ưu đãi nền mờ (yêu cầu Telegram, chưa commit)
- `HomeFragment.kt`: nút "Nhận ngay" của thẻ Flash Sale mở thẳng hộp thanh toán Google Play gói năm base plan
  `yearly-special-50-off` (PremiumViewModel dùng chung activity, tạo sớm để có giá); Play chưa trả giá / không có base
  plan → mở vé ưu đãi; hết đợt → paywall. Mua xong ẩn thẻ.
- `SettingFragment.java`: thẻ Go Premium khi đang có đợt 50% mở `DiscountDialogFragment` thay cho paywall.
- `DiscountDialogFragment.java`: thêm `showIfPromoActive(activity)`; tự đóng khi mua xong; nền vé trong suốt ~80%
  (`offer_dialog_scrim`, thấy mờ màn phía sau như Women Workout) + làm nhoè phía sau trên Android 12+.
- Build OK. Test máy ảo Android 16 (S8+ đang bị dùng qua Android Studio mirroring): thẻ Flash Sale đếm ngược,
  "Nhận ngay" (debug không có base plan → vé ưu đãi), Settings → Go Premium → vé ưu đãi, nền mờ thấy màn sau.
  Chưa test được hộp thanh toán thật (cần bản ký đúng key cài từ Play).

## 2026-10-13 – Giá vé ưu đãi 50% + thẻ Flash Sale đếm ngược ở Home (yêu cầu Telegram, chưa commit)
- `activity/ui/premium/DiscountDialogFragment.java`: giá theo tháng định dạng bằng `NumberFormat` theo mã tiền tệ
  (trước đây ghép ký hiệu + "%.2f" → "₫42416.67" thay vì "42.417 ₫"); hiện dòng giá tháng khi đã có giá.
- `res/layout/layout_dialog_discount.xml`: bỏ giá mẫu cứng (509.000 ₫ / 1.550.000 ₫ / 42.417 ₫) hiện trước khi Play
  trả giá → "Loading price…" (`discount_price_loading`), giá gốc / giá tháng ẩn tới khi có giá thật.
- Mới `activity/ui/premium/FlashSaleCard.kt` (`FlashSaleBanner`, port Women Workout) + `HomeScreen.kt` / `HomeFragment.kt`:
  thẻ "⚡ Flash sale: Premium giảm 50%" kèm đếm ngược tới `PaywallPromo.activeEndTime`, bấm mở PremiumActivity
  (source flash_sale) với giá 50%; hết giờ / đã Premium thì ẩn.
- Mới `res/values*/strings_flash_sale.xml` (fs_title_percent, fs_ends, fs_cta) lấy từ Women Workout cho 21 ngôn ngữ;
  cs, hi, hr, lv, no, th chưa có bản dịch (dùng tiếng Anh).
- Build OK. Test S8+ dở dang: vé ưu đãi Lucky Draw đếm ngược đúng (01:59:56), giá hiện "Loading price…" vì bản debug
  không lấy được base plan yearly-special-50-off. Dừng test vì S8+ đang bị điều khiển song song qua Android Studio
  Running Devices (screen-sharing-agent từ 09:36: tự mở luồng mua 09:38, mở Cài đặt → Play Store → Bộ nhớ 09:49).

## 2026-10-13 – Banner PaywallActivity dùng ảnh img_premium.webp ✅ (yêu cầu Telegram, chưa commit)
- `activity/ui/premium/PaywallActivity.java`: `iv_hero` nạp `file:///android_asset/images/img_premium.webp` bằng Glide
  (thay nền sóng `background_1`), giữ centerCrop.
- `res/layout/activity_paywall.xml` + `res/drawable/bg_paywall_hero_fade.xml`: lớp mờ chân ảnh cao 96dp → 190dp, thêm
  điểm giữa trắng 85% để tiêu đề và 3 dòng quyền lợi đè lên ảnh vẫn đọc rõ.
- Build OK. Test máy ảo Android 16: onboarding → Get my plan → PaywallActivity hiện banner, chữ rõ.

## 2026-10-13 – Banner PaywallScreenB dùng ảnh img_premium.webp ✅ (yêu cầu Telegram, chưa commit)
- `activity/ui/premium/PaywallScreenB.kt` (`Hero`): ảnh nền đổi từ `onboards/onboard_intro_1.png` sang
  `file:///android_asset/images/img_premium.webp` (1202x745, giữ Crop + TopCenter và lớp mờ dần sẵn có).
- Build OK. Test máy ảo Android 16: Home → nút kim cương → PremiumActivity hiện banner mới, chữ / dấu tick vẫn rõ.
- Lưu ý: PaywallActivity (paywall sau onboarding, nền sóng) là màn khác, không đổi.

## 2026-10-11 – Vừa mở app (Splash) đã hiện thông báo "smart" ✅ (yêu cầu Telegram, chưa commit)
- Nguyên nhân: `FitnessApplication` đặt job định kỳ `SmartNotificationWorker` (6 giờ) mỗi lần mở; job định kỳ MỚI chạy ngay
  lần đầu → máy mới cài / xoá dữ liệu nhận thông báo (id 1000, kênh smart_practice_high) sau ~5-8 giây, lúc đang ở Splash.
  Tái hiện S8+: pm clear → mở app → 8s có thông báo.
- `notification/NotificationScheduler.java`: `setInitialDelay(6, HOURS)` (máy đã có job giữ nguyên nhờ KEEP).
- `notification/SmartNotificationWorker.java`: bỏ qua khi chưa xong lần mở đầu (`isFirstOpen`) hoặc app đang hiển thị
  (`getMyMemoryState` ≤ IMPORTANCE_VISIBLE) - không nhắc tập khi người dùng đang trong app.
- Build OK. Test S8+: pm clear → mở app 25s không có thông báo; JobScheduler: Minimum latency +5h59m.
- Cùng lỗi ở 10 app khác dùng chung code (Tai Chi, Muay Thai, Karate, Taekwondo, Home, Women, Kickboxing, Wing Chun, BajiQuan,
  Capoeira) - chưa sửa, chờ chủ app.

## 2026-10-11 – Onboarding: thêm bản dịch tiếng Việt ✅ (yêu cầu Telegram, chưa commit)
- `res/values-vi/strings_onboard_flow.xml` (274 string + plurals; 5 mốc % mỡ không cần dịch). Các đoạn tô hồng (*_accent) nằm
  đúng trong câu tiêu đề tiếng Việt.
- `OnboardScreens.kt` CalendarCell: nhãn 1 dòng, chữ nhỏ dần theo độ dài (bản dịch dài như "Thành công" bị xuống dòng tràn ô);
  tiếng Việt dùng "Về đích".
- Build OK. Test S8+ (cài sạch, chọn Tiếng Việt): đủ 8 màn nhánh rút gọn, ngày "31 thg 10", thứ CN/T2…, kết quả → Paywall.
  Quay video gửi chủ app.

## 2026-10-11 – Onboarding: sửa "weigh", tách toàn bộ chữ ra strings (chuẩn bị bản địa hóa) ✅ (yêu cầu Telegram, chưa commit)
- Câu cân nặng: "How much do you weigh now?" (bản đang chạy bị đổi thành "weight" - sai ngữ pháp).
- `res/values/strings_onboard_flow.xml` (mới, 274 string + 2 plurals, tiền tố `obf_`): mọi chữ hiển thị của onboarding mới
  (cả 3 nhánh 8 / 26 / 46 màn). Chủ app cho người khác dịch → chỉ có bản tiếng Anh; ngôn ngữ chưa dịch tự dùng tiếng Anh.
  Lưu ý: file `strings_onboarding.xml` cũ (pw_*, ob_* của paywall / onboard cũ) giữ nguyên - không đặt trùng tên.
- `OnboardModel.kt`: `ONBOARD_STEPS` → `onboardSteps(res: Resources)` lấy chữ từ resource (Activity đã áp ngôn ngữ app);
  `cta` mặc định rỗng → `OnboardStep.ctaText()` = R.string.obf_next. `OnboardingStepFactory.create(variant, all)`,
  `OnboardState.configure(variant, all)` (tra nhãn lựa chọn qua `allSteps`), `OnboardFlowActivity` truyền `onboardSteps(resources)`.
- `OnboardScreens.kt`, `OnboardUi.kt`: chữ trong composable → `stringResource`; danh sách cố định (testimonial, mốc mỡ, thông điệp
  tạo kế hoạch, tuần mẫu, BMI) → @StringRes. Câu có số dùng tham số (%1$s / %1$d), "x ngày sớm hơn" dùng plurals.
  Thứ trong tuần ở lịch tập lấy từ ICU theo ngôn ngữ (trước cố định S M T W T F S). `OnboardState.formatDate*` theo ngôn ngữ app
  (trước Locale.ENGLISH "MMM dd").
- Build OK. Test S8+ (tiếng Anh, cài sạch): đủ 8 màn nhánh rút gọn, chữ đúng, kết quả kế hoạch cuộn hết (Designed for you, Plan preview).
  Chưa đi hết nhánh 26 / 46 màn trên máy (chỉ biên dịch).

## 2026-10-11 – Short flow thêm màn vùng tập (Focus area), chip đúng đầu đường kẻ như FocusFragment ✅ (yêu cầu Telegram, chưa commit)
- `OnboardingStepFactory.kt`: QUICK_DESIGNS thêm `step6` → 8 màn (giữ tên nhánh quick_7 cho Remote Config / analytics).
- `OnboardScreens.kt` (FocusBodyScreen, dùng chung cho mọi nhánh): bỏ danh sách thẻ bên trái; ảnh bg_problem_area giữ đúng tỉ lệ
  528x780 (trước FillBounds bị méo), chip ARM / BELLY / BUTT / LEG đặt ngay trên đầu tự do của 4 đường kẻ cam vẽ sẵn trong ảnh
  (đo toạ độ theo ảnh, như guideline của fragment_focus_area), FULL BODY góc trên trái. Chip chọn: nền hồng + ✓. Chấm đánh dấu
  chuyển về đúng chấm cam cuối đường kẻ. Đáy ảnh (trắng) hoà dần về nền ObColor.Background.
- Test S8+: bấm từng chip + bấm lên cơ thể đều chọn đúng, Next bật khi đã chọn. Quay video tới màn chiều cao thì một phiên khác
  mở Wing Chun qua thông báo trên cùng máy → dừng test, cắt video trước đoạn đó.

## 2026-10-11 – Onboarding mặc định bản rút gọn quick_7 ✅ (yêu cầu Telegram, chưa commit)
- `remote_config_defaults.xml`: `onboarding_experiment_variant` full_4x → quick_7.
- `OnboardingVariant.kt`: thêm `DEFAULT = QUICK_7`; giá trị Remote Config trống / lạ → QUICK_7 (trước là FULL).
- Máy đã chia nhánh giữ nhánh cũ (lưu local theo EXPERIMENT_ID). Nếu Firebase console có đặt key này thì console thắng → cần sửa / xoá trên console.
- Test S8+ (debug, pm clear): Ngôn ngữ → màn chào (TEST: quick_7) → Mục tiêu → Chiều cao → Cân nặng → Cân nặng mục tiêu → Building plan → Kết quả
  → Paywall → Lucky Spin → quảng cáo → Home. Quay video gửi chủ app. Lưu ý: Home vẫn hiện "Create" ở Personal plan (kế hoạch chỉ tạo
  trong PersonalPlanActivity, như các nhánh khác); câu "How much do you weight now?" sai chính tả (weigh).

## 2026-10-11 – Hiệu năng màn Discover (Training) + Report ✅ (yêu cầu Telegram, chưa commit)
- `ExploreScreen.kt` (DiscoverScreen), `ReportScreen.kt`: LazyColumn → Column + verticalScroll (mỗi màn chỉ ~6–10 khối; các
  carousel ngang bên trong giữ nguyên LazyRow). Discover bọc mỗi mục `key(title + style)`.
- Dữ liệu đã nạp ngoài main thread sẵn (ExploreFragment IO, HistoryViewModel, BadgeRepository) → không đổi.
- Đo release ký debug key, speed-profile, S8+, 3 vòng cuộn mỗi bản (vòng 1 / vòng 2–3):
  Discover: Lazy jank 20%/4–11%, p99 53/30–42 ms → Column 9%/0%, p99 20/10–11 ms.
  Report: Lazy 5.4%/0.6%, p99 34/13 ms → Column 0.8%/0–0.2%, p99 14/8–10 ms.
- Test S8+: cuộn hết Discover (Hot, Random, Goal, carousel, See more) và Report (streak, tuần, cân nặng, BMI, huy hiệu, gợi ý) hiển thị đúng.

## 2026-10-11 – Cải thiện hiệu năng màn Home (theo cách Tai Chi) ✅ (yêu cầu Telegram, chưa commit)
- `JsonHelper`: danh sách chương trình giải mã + parse 1 lần mỗi ngôn ngữ (`loadProgramList`, có khoá). Trước: `getAllCategory()`
  giải mã lại cả file mỗi lần gọi (Home gọi mỗi lần LiveData chuỗi ngày đổi); `getAllCategoryHashMap()` gán map rỗng trước rồi
  mới đổ, không khoá → luồng nền (Home) + main gọi cùng lúc giải mã 2 lần, main có thể nhận map rỗng (mục "tuần này" trống).
  Map giờ dựng xong mới gán, theo cùng danh sách (đổi ngôn ngữ thì dựng lại). Log `program catalog decoded in N ms`.
- `FitnessApplication`: `prewarmPrograms()` trên `startupExecutor` lúc mở app (S8+: 77–306 ms, chạy trên luồng `deferred-startup`).
- `HomeFragment`: tra map cho "bài tập tuần này" chuyển sang Dispatchers.IO.
- `HomeScreen`: LazyColumn → Column + verticalScroll (Home chỉ ~10 khối; Tai Chi đo LazyColumn giật hơn).
- `SplashActivity`: `MobileAds.initialize` chạy luồng phụ, xong mới `requestSplashAds()` (giữ thứ tự cũ).
- Đo bản release (ký debug key, speed-profile) S8+, cuộn Home 8 lần × 3 lượt, xen kẽ 2–3 lần mỗi bản:
  LazyColumn lượt 2–3 giật 1,8–4,1%, p99 19–29 ms; Column 0–0,2%, p99 9–10 ms. Lượt đầu (nạp ảnh lần đầu) cả hai 12–28%,
  p99 Column 32–36 ms vs LazyColumn 61–93 ms.
- Test: mở rộng "See more" (lưới 2 cột), cuộn tới đáy không hụt, mở bài → ChallengeActivity, Back về Home. Không crash.

## 2026-10-11 – DoWorkoutActivity: giảm nguy cơ ANR ✅ (yêu cầu Telegram, chưa commit)
- **Video động tác**: RestFragment giải mã cả file mp4 trên main thread mỗi lần sang bài (Decoder.decryptToFile), Nghỉ và Tập
  dùng chung `cache/temp.mp4` (PlayFragment có thể đọc file đang ghi dở). Mới `utils/WorkoutVideoCache`: giải mã ở luồng nền,
  mỗi động tác một file `cache/workout_video/<discover>_<version>_<actionId>.mp4` (ghi `.part` rồi đổi tên), trả về main;
  PlayFragment dùng file đó, chưa có (bỏ qua Nghỉ quá nhanh) thì chờ giải mã rồi mới phát, không setVideoURI file chưa có
  (tránh hộp thoại "Can't play this video"). Thoát màn tập thì xoá thư mục (xếp hàng sau các lần giải mã).
- **Âm thanh**: tick/beep/chuông trước tạo + release MediaPlayer trên main mỗi giây → SoundPool nạp sẵn 1 lần.
  Nhạc nền trước `MediaPlayer.create` (prepare đồng bộ) tới 4 lần khi mở màn (3 observer + onResume) → `startMusic()` chỉ tạo
  khi đổi bài / bật, `prepareAsync`, release ở luồng nền (`stopMusic()`); ringPlayer trước không bao giờ release.
- AudioAppManager (TTS / giọng tải về) đã chạy nền sẵn, không đổi.
- Test S8+: Ease Yoga 26 bài – Nghỉ/Tập có video, bấm Skip liên tục 6 vòng (tới 7/26) video đúng bài, không hộp thoại lỗi;
  Previous, ra Home rồi vào lại, Keep going, Quit → không crash/ANR, thư mục video đã xoá.
- Lỗi có sẵn chưa sửa: bấm Previous thì nhãn "x/26" trên cùng không giảm (previousExercise không cập nhật txtProgress).

## 2026-10-08 – Thư viện quảng cáo 23.2.0 → 25.5.0 (sửa nút "Đóng" bị thanh trạng thái che trên Android 15/16) ✅ (yêu cầu Telegram, chưa commit)
- `app/build.gradle`: play-services-ads 25.5.0 (minSdk đã 26); AndroidManifest bỏ `<property AD_SERVICES_CONFIG ... @xml/gma_ad_services_config>` (25.x không còn tài nguyên này).
- Test S8+: mở app có mạng / không mạng → rời splash, quảng cáo hiện bình thường, không crash.

## 2026-10-08 – Splash: UMP quá 10s không trả lời thì tải quảng cáo luôn ✅ (yêu cầu Telegram, chưa commit)
- `SplashActivity.java` đã có sẵn chống kẹt (hasStartedMain, chờ onResume, show lỗi → vào màn chính); thêm `CONSENT_TIMEOUT_MS` 10s.
- Test S8+: có mạng / không mạng → vào Home sau ~7s, không crash.

## 2026-10-08 – Kiểm tra user cũ (onboard cũ) lên bản mới + ProgramRecommenderV3 ✅ (yêu cầu Telegram, chưa commit)
- Splash chỉ xét `IS_FIRST_OPEN`: user cũ = false → vào thẳng YogaActivity, không qua onboard mới. Pref mới (USER_GOAL,
  USER_FOCUS_AREAS, USER_EXPERIENCE, TARGET_WEIGHT) đều có mặc định; `PersonalPlan.defaultsFromOnboarding` → giảm cân / beginner / full body.
- **Sửa**: onboard mới chỉ lưu `USER_FOCUS_AREAS`, không lưu `USER_TARGET_FOCUS` mà ProgramRecommenderV3 (Home, WeekGoalNewFragment)
  đọc → user mới luôn dùng mặc định "Relaxation", không được cộng điểm theo vùng tập. `OnboardState.persist` giờ ghi
  `USER_TARGET_FOCUS` theo khoá cũ (belly→abs, arm, butt, leg→thigh, full_body→body; chọn nhiều thì ưu tiên vùng cụ thể đầu tiên).
  Unit test `OnboardTargetFocusTest` (3) + `OnboardingStepFactoryTest` (6) pass.
- Test S8+: build 1.1.12 (commit 7d9b88d, onboard cũ FirstSetupActivity) → làm onboard cũ → cài đè bản mới → mở thẳng Home, không crash,
  gợi ý hôm nay hiện ("Sun salutations flow"), các tab Training / Report / Recipes / Settings / My training / mở bài gợi ý không crash.
  Data máy test đã khôi phục.

## 2026-10-08 – Màn chào: "Already have an account? SIGN IN" đăng nhập Google thật (phương án A) ✅ (yêu cầu Telegram, chưa commit)
- `OnboardFlowActivity`: bấm SIGN IN → FirebaseUI Google (giống `YogaActivity.onContinueSync`). Thành công → `setOnboarded(true)`,
  `setFirstOpen(false)`, log `onboarding_signed_in`, mở YogaActivity (tự `retrieveUserProgress`: tiến độ, diet, lịch sử) + paywall
  như khi xong onboarding (`openMain()` dùng chung với `finishFlow`). Huỷ → ở lại màn chào; lỗi → toast `oops_sync_failed`.
  Hồ sơ onboarding (cao, cân, mục tiêu) chưa sao lưu → giữ mặc định.
- Dòng SIGN IN chỉ hiện khi có Google Play Services và Remote Config `enable_sync` = true (giống mục Back up & Restore).
- Test S8+: bấm SIGN IN → hiện chọn tài khoản → chọn tài khoản thì lỗi `Code: 10` (DEVELOPER_ERROR) vì SHA-1 debug
  `1F:4C:FE:37:7F:57:E7:EB:0C:11:72:BC:0D:EA:CC:AF:6B:4C:A2:21` chưa có trong Firebase `female-workout-78582` (chỉ có 2 SHA-1
  release/Play) → app không crash, ở lại màn chào. Bản Play ký release sẽ chạy như mục Cài đặt. Muốn test đủ trên bản debug: thêm SHA-1 trên.
- Data máy test đã khôi phục.

## 2026-10-08 – Quay video so sánh 3 nhánh onboarding ✅ (yêu cầu Telegram, không sửa code)
- Build debug OK. Quay trên S8+ (đã tắt mạng): quick_7 ~32s, standard_26 ~83s, full_4x ~136s (47 màn), đã gửi Telegram.
- Màn chào: "Already have an account? SIGN IN" chỉ gọi `onNext`, chưa đăng nhập thật. App đã có đăng nhập Google
  (FirebaseUI, `YogaActivity.onContinueSync`) + sao lưu lên Realtime DB (`syncData` / `retrieveUserProgress`: tiến độ, diet, lịch sử;
  chưa có hồ sơ onboarding). Đã hỏi chủ app: A làm SIGN IN thật (khuyên) / B bỏ dòng SIGN IN / C bỏ màn chào. **Chờ trả lời.**
- Data máy test đã khôi phục, bật lại mạng.

## 2026-10-08 – Ảnh màn Onboard tạo bằng Google Flow (Nano Banana Pro) theo video task/onboard.mp4 ✅ (chưa commit)
- `assets/onboards/v2/`: thêm 64 ảnh `.jpg` (3.9MB), đúng tên trong README → màn tự nhận, không sửa code.
  Phong cách ảnh chụp như video: áo bra + legging đen, nền xám nhạt; bài tập plan_w* mặc đồ navy, nền trắng.
  Khung: 4:3 (welcome_hero, coach_kelly – vùng hiển thị rộng), 9:16 (full_*), 1:1 (plan_w1_0..w4_3), còn lại 3:4.
- Logo ✦ Gemini ở góc dưới phải được xoá bằng ffmpeg `delogo` (máy không có encoder WebP nên giữ JPG q3).
- Không làm `focus_body` (màn dùng drawable `bg_problem_area` + toạ độ hotspot) và `generating_users` (code không dùng).
- **Tách nền** (Vision `VNGenerateForegroundInstanceMaskRequest` của macOS, chạy trên máy) cho 46 ảnh đặt trên thẻ / nền hồng:
  goal_*, shape_*, belly_*, hip_*, leg_*, desired_body_*, change_*, plan_body, plan_w* → `.png` trong suốt (thu nhỏ: thẻ 560px,
  desired/change/plan_body 720px, plan_w 360px; JPG cùng tên đã xoá). 18 ảnh cảnh thật (welcome, coach, testimonial, statement,
  info_*, full_*) giữ JPG có nền. Thư mục còn 7.6MB.
- Ảnh gốc nằm trong project Google Flow "Tháng 10 08 - 19:31" của tài khoản PRO và ở `~/Downloads/ob_*.jpg`.
- Test S8+ (Android 10): xoá data → onboard từ đầu đến paywall; ảnh mục tiêu / dáng mong muốn / mông / chân / testimonial /
  statement / cộng đồng / full screen hiển thị đúng, không crash. Đã khôi phục data máy test.

## 2026-10-08 – Onboarding: màn "Building your plan" dùng Lottie như Tai Chi Qigong ✅ (yêu cầu Telegram, chưa commit)
- `onboarding/onboard/ObGeneratingLottie.kt` (mới): vòng `guide_plan_generate.json` lặp sau con số % (thay vòng Canvas);
  3 pha dùng `lt/part1` (cộng đồng), `lt/part2` (dải bài tập, speed 0.6), `lt/part3` (cúp + 4.9 + sao) như Tai Chi.
  `setMaintainOriginalImageBounds(true)` → thay ảnh độ phân giải khác vẫn đúng khung.
- `OnboardScreens.kt` `GeneratingScreen`: giữ nhịp % (~8s), câu chữ và tiêu đề hồng từng pha; xoá `GeneratingCommunity` / `Exercises` /
  `Rating` + `GENERATING_PHOTOS`.
- **Ảnh trong `assets/lt/part*/images` vẫn là ảnh Tai Chi (tạm theo yêu cầu chủ app)**: người mặc đồ Tai Chi, tên động tác, cúp
  "TAI CHI QIGONG". Thay file cùng tên + cùng tỉ lệ là xong, không cần sửa code.
- Test S8+: chạy onboarding tới tạo kế hoạch → đủ 3 pha → màn kết quả. Đã khôi phục prefs máy test.
- **Pha 2 thay ảnh mới** từ `task/onboard_part.jpg` (lưới 3 cột × 4 bài, có tên bài): cắt 12 ô, mỗi dải ghép 8 ô (cột i + cột i+1,
  xoay vòng) để đủ dài khi trôi → `lt/part2/images/img_{0,1,2}.jpg` (320×2376). `lt/part2/data.json`: `p` → .jpg, `h` theo
  tỉ lệ mới (950 / 965 / 936), giữ `w` → không méo, không lộ khoảng trống. Bỏ 3 file png Tai Chi. Test S8+ pha 2 OK.
- **Pha 1 thay avatar** từ `task/onboard_part1.jpg` (lưới 4×4): cắt 7 ô vuông quanh mặt (vẽ mặt, ca sĩ, làm bánh, chơi game,
  ôm chó, kính, leo núi) → `lt/part1/images/img_0..6.jpg` (376×376), `data.json` `p` → .jpg. Bỏ 7 file webp Tai Chi.
  Test S8+ pha 1 OK. **Part3 (cúp "TAI CHI QIGONG") vẫn ảnh Tai Chi.**
- Pha 1 sắp lại vị trí 7 vòng avatar (`lt/part1/data.json`, `ks.p` của layer "1".."7", khung 854×602) cho khác bản Tai Chi:
  vòng lớn nhất (5) ở giữa, 2 / 6 hai góc trên, 4 / 3 hai góc dưới, 7 giữa trên, 1 (nhỏ) dưới giữa-trái. Giữ kích thước + nhịp bật.
- Part3: chủ app tự thay `lt/part3/images` (sao 64×64, img_3 "4.9" vòng nguyệt quế 325×181, img_4 cúp "WOMEN WORKOUT" 256×256).
  img_3 / img_4 lệch tỉ lệ khung asset (490×190, 276×300) → pad trong suốt thành 467×181 / 256×278 để không bị kéo méo
  (bản gốc chủ app lưu ở scratchpad phiên). Build lại + quay video onboarding gửi Telegram.
  Lưu ý: data.json part3 vẫn tự vẽ bông lúa 2 bên cúp + 5 sao vàng dưới img_3 → trùng với nguyệt quế + sao trong img_3 mới
  (đã hỏi chủ app: ẩn trong json hay đổi img_3, chưa trả lời).
- **Việc chờ (2026-10-08):** chủ app (có Gemini Pro) muốn tạo ảnh cho các màn onboarding qua Gemini / Flow trên Chrome, theo
  video demo `task/onboard.mp4` + `task/step*.jpg`, bắt đầu từ step 1, thử 1 ảnh gửi duyệt trước. Phiên cũ không có công cụ
  Claude in Chrome dù đã chạy /chrome → cần chạy lại `claude --chrome`.
  Phiên 12:10 (2026-10-08) vẫn không có công cụ Chrome → đã nhắn chủ app chạy `claude --chrome` hoặc tự tạo bằng prompt em gửi rồi gửi ảnh qua Telegram.
  Chỗ gắn ảnh: `WelcomeScreen` (OnboardScreens.kt) qua `ObArt(step.art)`.

## 2026-10-08 – Onboarding theo video task/onboard.mp4 ✅ (yêu cầu Telegram, chưa commit)
- **3 nhánh thử nghiệm theo tổng số màn** (`OnboardingVariant`): `quick_7` (chào, mục tiêu, chiều cao, cân nặng, cân mục tiêu,
  tạo kế hoạch, kết quả), `standard_26`, `full_4x` (47 màn). Remote Config `onboarding_experiment_variant` (mặc định full_4x;
  giá trị cũ control_46/short_28/adaptive vẫn map được). `EXPERIMENT_ID` → `onboarding_flow_v2` để chia nhánh lại. Bỏ nhánh adaptive.
  Bản debug: chip "TEST: … · N màn ↻" ở màn chào, bấm để đổi nhánh và chạy lại.
- **Màn cân mục tiêu mới** (`TARGET_WEIGHT`, step10b): thước + mốc cân hiện tại + dải tô; thẻ nhận xét (hợp lý / tham vọng /
  dài hạn / giữ cân / tăng cân / cảnh báo BMI < 18.5). `targetWeightKg` dùng giá trị người chọn, đổi kg/lbs quy đổi cả 2.
- **Thước kéo viết lại** (`ObRuler.kt`, Canvas): vạch chỉ vẽ cùng toạ độ với vạch đang chọn, kéo/fling xong bắt đúng vạch,
  rung nhẹ mỗi nấc → số hiển thị luôn khớp (bản cũ vạch hồng lệch 44dp, không bắt vạch). Xoá `RulerSliderView.java` + 2 thước cũ.
- **Biểu đồ**: dự đoán 1 có màn "trước / sau" (~2.3s, chạm bỏ qua) rồi biểu đồ; dự đoán 2 mốc đích trượt từ ngày cũ về ngày mới,
  "sớm hơn N ngày" đếm lên; biểu đồ tiềm năng vẽ dần + emoji Ngày 1/7/30 bật lên, câu chữ theo tuổi; màn tạo kế hoạch ~8s, 3 pha
  (cộng đồng → lưới ảnh bài tập → đánh giá); lịch tập ở màn kết quả theo số buổi/tuần đã chọn + ngày thật (bản cũ cố định 2/3 ngày, sai ngày cuối tháng).
- **Câu chữ**: viết lại các tiêu đề / gợi ý / lựa chọn còn trùng nguyên văn app gốc (khoá đáp án giữ nguyên).
- Unit test `OnboardingStepFactoryTest` viết lại (7 / 26 / 4x, giữ thứ tự, map giá trị cũ). Test S8+: thước cao (cm/ft), cân,
  cân mục tiêu, dự đoán 1 + 2, tiềm năng, tạo kế hoạch, kết quả, chạy hết nhánh quick_7 → paywall. Đã khôi phục prefs máy test.

## 2026-10-08 – Home: "Thêm" mở rộng ngay trên màn ✅ (yêu cầu Telegram, chưa commit)
- `home/HomeScreen.kt`: `HomeSectionBlock` (port ý từ Home Workout). Bấm "Thêm ›" → thử thách thành danh sách dọc (thẻ full ngang),
  bài tập thường thành lưới 2 cột; "Thu gọn ˄" trở lại hàng cuộn ngang. Trạng thái nhớ qua `rememberSaveable(section.title)`.
  Mục ít thẻ (thử thách ≤1, thường ≤2) không hiện nút. `ChallengeCard` / `ProgramCard` thêm tham số `modifier` (mặc định kích thước cũ).
  `actions.onSeeMore` không còn dùng ở Home (giữ interface).
- `compose/MinUi.kt`: `MinSectionHeader` thêm `actionSuffix` (mặc định " ›", các màn khác không đổi).
- Chuỗi mới `home_show_less` (26 ngôn ngữ, `values*/strings_home_more.xml`).
- Test S8+ (chế độ tối): mở rộng Thử thách, Tập luyện ngực, Cơ bụng; thu gọn; bấm thẻ trong lưới mở Preview.

## 2026-10-08 – Màn Kết thúc + hộp huy hiệu ở chế độ tối; kiểm tra ProGuard Badges ✅ (yêu cầu Telegram, chưa commit)
- `compose/FinishedScreen.kt`: 3 ô chỉ số nền `MinColor.Surface` (trước nền trắng cố định + chữ theo theme → chữ sáng trên trắng);
  hero bản tối gradient hồng trầm; chip streak bản tối nền nâu sẫm.
- `dialog_badge_unlocked.xml` + `values{,-night}/colors_badge_dialog.xml`: hộp "Achievement earned" có bản tối (vẫn nút xanh + chữ Anh từ Wing Chun).
- ProGuard Badges: các class JSON đều có @SerializedName, Gson 2.11 tự giữ Signature → không cần thêm rule. Test release minify (ký debug key):
  thẻ Achievements ở Báo cáo + màn Achievements đủ 29 huy hiệu, tiến độ đúng, không crash.
- Còn lại: màn Achievements (BadgeActivity) chưa hợp chế độ tối (chữ tối trên nền tối, thẻ đầu trang xanh).

## 2026-10-08 – Làm lại màn nghỉ (RestFragment) và màn tập (PlayFragment) ✅ (yêu cầu Telegram, chưa commit)
- Bố cục lấy từ Muay Thai (cùng id, Java PlayFragment không đổi): `layout{,-land}/fragment_rest.xml`, `layout{,-land}/fragment_play.xml`,
  màu `values{,-night}/colors_workout.xml` (wk_*, bảng Rose của MinUi), drawable `wk_*`.
- Màn nghỉ theo ý chủ app "nhẹ nhàng, chuyên nghiệp": nền tối mờ #A6070809 đè ảnh bài tập như bản cũ, vòng đếm mảnh hồng,
  +15s viền mảnh, Bỏ qua hồng, thẻ "Kế tiếp" kính mờ. (Bản gradient tím-hồng và bản nền hồng nhạt bị chê lòe loẹt → bỏ.)
- Màn tập: video vuông, nút tròn mờ, thanh tiến độ hồng, giờ lớn, hàng Trước – Tạm dừng (chạy tiến độ) – Bỏ qua; ngang: video trái.
- `RestFragment.java`: `bindContent()` + `updateRing()`; bọc FrameLayout để xoay màn hình nạp lại layout và binding đúng
  (trước đây inflate vào getView() nhưng binding vẫn trỏ view cũ); sau xoay phát lại video.
- `activity_do_workout.xml`: số bài / giờ trên cùng dùng `wk_accent` (colorPrimary bản tối là #000000 → chữ đen trên nền tối).
- Test S8+ (chế độ tối): Ready → Bỏ qua → màn tập → xoay ngang → màn nghỉ ngang. Không crash.

## 2026-10-07 – Sửa nút Back màn tập trên SDK 36 (Android 16) ✅ (yêu cầu Telegram)
- Target 36 trên Android 16: predictive back không gọi `onBackPressed()` → Back ở màn tập thoát luôn, không hỏi Quit.
- DoWorkoutActivity.java, RestTimeActivity.java: bỏ override `onBackPressed()`, chuyển logic vào `handleBack()` gọi từ `OnBackPressedCallback` đăng ký ngay sau
  `super.onCreate` (`getOnBackPressedDispatcher().addCallback`). `super.onBackPressed()` cũ → `finishBack()` (tạm tắt callback,
  `getOnBackPressedDispatcher().onBackPressed()`, bật lại). Code gọi `onBackPressed()` (nút ←) vẫn đi vào callback.
  Chạy đúng cả khi Manifest bật / tắt predictive back và trên Android cũ.
- Build OK; cùng cách sửa đã thử trên Muay Thai + Kickboxing (Android 16 & 8).


## 2026-10-10 – Chiều cao + vòng eo nhập bằng thước kéo (như Muay Thai) ✅ (yêu cầu Telegram)
- `custom/weight/WeightEntrySheet.kt`, `WeightEntryState.kt`, `WeightEntryViewModel.kt`, `custom/DialogWeight.kt`: hai dòng Chiều cao và
  Vòng eo vẫn thu gọn, bấm mở **thước kéo + ±** thay ô gõ số. kg → cm (chiều cao bước 1, eo bước 0.5); lb → inch bước 0.5
  (chiều cao nhãn 5'6", eo nhãn số inch). Thước dùng chung `ValueRuler`/`RulerRow` với cân nặng; bỏ `NumberField`.
- Giữ cm gốc (`heightCmExact`, `waistCmExact`) khi kéo / đổi kg↔lb → không lệch số do làm tròn.
- Build OK.

## 2026-10-14 – Chọn giọng huấn luyện viên từ Preview như Tai Chi Qigong
- `PreviewScreen.kt`, `PreviewActivity.java`: thêm dòng Coach voice theo Remote Config/ngôn ngữ, mở màn chọn và nghe thử, cập nhật tên khi quay lại; tải bổ sung khi đổi giọng. Dòng nằm trong header nên không ảnh hưởng chỉ số kéo sắp xếp bài tập.
- `CoachOptionActivity.java`: sửa điều kiện Adam/Bella bị đảo, xử lý cấu hình thiếu key/JSON lỗi; giữ trạng thái tải tới khi worker kết thúc, giải phóng MediaPlayer.
- Build `./gradlew assembleDebug`: BUILD SUCCESSFUL. Đã cài APK lên máy thật; kiểm tra Preview → Coach voice, danh sách Default/Jennifer/Adam/Sophia/Alexander, chọn Default → quay lại hiển thị Default. Chưa xác minh tải/phát trọn gói giọng từ máy chủ. Đã tắt màn hình máy test.

## 2026-10-18 — Nâng version 1.1.31 (131) và push
- Build assembleDebug + bundleRelease OK.

## 2026-10-18 — Thẻ ưu đãi năm (base plan yearly-special-34-off) cho mọi người, giống Tai Chi Qigong
- `PremiumActivity.kt`: thẻ ưu đãi hiện cho mọi người trừ paywall ngay sau onboarding / lần setup đầu (trước đây chỉ người
  mở nhiều lần bốc trúng 34%). Đợt 50% vẫn ưu tiên; bốc trúng Trọn đời thì hiện thêm thẻ Trọn đời. Chọn sẵn thẻ ưu đãi.
  Analytics `paywall_offer` thêm giá trị `special34` cho người thấy thẻ mà không qua bốc thăm.
- Build OK; S8+ (giả lập người mới: xoá PAYWALL_ROTATE_PICK / PROMO_END_TIME, đếm mở = 0): thẻ hiện, mức giảm theo giá Play
  hiện tại là 25% (752.000 ₫ / 1.000.000 ₫).
