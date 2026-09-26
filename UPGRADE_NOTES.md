# Nâng cấp SDK 36 + Onboarding v2 + Paywall v2 (v1.1.13)

## 1. Bước bắt buộc sau khi nhận code
Code Java mới nằm trong thư mục bản sao `workout\` ở gốc dự án. Chép nó đè vào mã nguồn thật, rồi xoá bản sao:

```
robocopy "D:\A-project\Min-Female Fitness\workout" "D:\A-project\Min-Female Fitness\app\src\main\java\mint\woman\female\workout" /E
rmdir /S /Q "D:\A-project\Min-Female Fitness\workout"
```

Sau đó mở Android Studio → Sync Gradle → Build (cần JDK 17+ và Android SDK Platform 36).

## 2. Build / SDK
| Mục | Cũ | Mới |
|---|---|---|
| compileSdk / targetSdk | 35 | **36** |
| Android Gradle Plugin | 8.7.2 | 8.13.0 |
| Gradle | 8.9 | 8.13 |
| Kotlin | 2.1.0 | 2.2.20 |
| google-services / crashlytics plugin | 4.3.14 / 2.9.2 | 4.4.2 / 3.0.2 |
| core-ktx / activity-ktx | 1.15.0 / 1.9.3 | 1.17.0 / 1.10.1 |
| versionCode / versionName | 112 / 1.1.12 | 113 / 1.1.13 |

## 3. Thay đổi hành vi Android 16
- **Edge-to-edge bắt buộc** (bỏ `windowOptOutEdgeToEdgeEnforcement`): `EdgeToEdgeHelper` tự thêm padding cho thanh trạng thái / điều hướng / bàn phím cho mọi màn hình cũ, tô màu thanh theo theme. Màn tự xử lý insets thì implement `EdgeToEdgeHelper.HandlesInsets`.
- **Predictive back**: `android:enableOnBackInvokedCallback="false"` để các `onBackPressed()` cũ vẫn chạy. Nên chuyển dần sang `OnBackPressedDispatcher`.
- **Màn hình lớn** (≥600dp) bỏ qua khoá dọc: đã thêm opt-out tạm thời `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` (sẽ không còn hiệu lực ở SDK 37).
- Bỏ thuộc tính `package` trong manifest (đã dùng `namespace`).

## 4. Onboarding v2 (`OnboardingActivity`)
Language → Mục tiêu → Vùng tập trung (chọn nhiều) → Trình độ → Tuổi/chiều cao/cân nặng/mục tiêu (+BMI) → Thời gian/ngày → Giờ nhắc + xin quyền thông báo khi người dùng bấm → Tạo lộ trình → Kết quả (dự kiến ngày đạt mục tiêu) → Paywall.
- Có nút Bỏ qua, nút quay lại, thanh tiến trình; câu chọn một đáp án tự chuyển bước.
- Lưu: goal, focus areas, level, avg duration, age, weight, height, target weight, giờ nhắc (dùng cho `ProgramRecommenderV3`).
- Sự kiện Analytics: `ob_start`, `ob_step_N`, `ob_skip`, `ob_notification_*`, `ob_complete`.
- Sửa lỗi ở luồng cũ: không còn xin `SCHEDULE_EXACT_ALARM` như quyền runtime; cân nặng/chiều cao nay được lưu đúng.

## 5. Paywall v2 (`PremiumActivity`)
Lợi ích, 3 gói (năm/tháng/trọn đời) với badge Tiết kiệm %, giá/tháng, timeline dùng thử, dòng công bố giá, Khôi phục, Quản lý gói, Quyền riêng tư, nút đóng hiện sau vài giây, ưu đãi khi thoát.
- Chọn offer theo nội dung (base plan / offer có giai đoạn miễn phí), không còn phụ thuộc thứ tự danh sách.
- Không còn đặt lại Premium = false khi Play Store không kết nối được.
- Sự kiện: `paywall_view`, `paywall_purchase_click`, `paywall_purchase_success`, `paywall_close`, `paywall_restore`, `paywall_exit_offer_view/claim`.

### Remote Config mới
| Key | Mặc định | Ý nghĩa |
|---|---|---|
| `onboarding_v2` | true | false = quay về FirstSetupActivity cũ |
| `paywall_after_onboarding` | true | Hiện paywall sau onboarding |
| `paywall_close_delay_sec` | 3 | Giây trước khi hiện nút đóng (tối đa 10) |
| `paywall_default_plan` | yearly | yearly / monthly / lifetime |
| `paywall_show_monthly` / `paywall_show_lifetime` | true | Ẩn/hiện gói |
| `paywall_show_timeline` | true | Timeline dùng thử |
| `paywall_exit_offer_enabled` | true | Ưu đãi khi thoát |
| `paywall_exit_offer_id` | exit-offer | Offer ID (hoặc tag) trên Play Console |
| `enable_trial` (đã có) | false | Dùng offer dùng thử nếu người dùng đủ điều kiện |

### Cần làm trên Play Console
- Ưu đãi khi thoát: tạo offer cho `premium_yearly` với ID `exit-offer` (giai đoạn giá giới thiệu thấp hơn giá gốc). Không có offer này thì paywall đóng bình thường.
- Dùng thử: offer có giai đoạn miễn phí cho gói năm/tháng, rồi bật `enable_trial`.

## 6. Việc còn lại / lưu ý
- Chuỗi mới chỉ có tiếng Anh + tiếng Việt; các ngôn ngữ khác tạm hiển thị tiếng Anh (`strings_onboarding.xml`).
- Kiểm thử trên emulator Android 16: bật/tắt điều hướng cử chỉ, chế độ tối, tablet, bàn phím ở các màn có ô nhập.
- Lỗi có sẵn chưa sửa: `RemoteConfigKeys.APP_OPEN_RESUME = "app_resume"` nhưng file defaults dùng `app_open_resume`; `SplashActivity` luôn gán `DEBUG_GEOGRAPHY_EEA` cho UMP (chỉ nên dùng khi debug).
