# Review và kế hoạch phát triển Min-Female Fitness

> Ngày review: 2026-09-18
>
> Phạm vi: source code và cấu hình hiện có trong working tree; chưa bao gồm kiểm thử thủ công trên thiết bị, Firebase Console, Play Console hoặc dữ liệu production.

## 1. Kết luận điều hành

Ứng dụng có phạm vi sản phẩm tương đối đầy đủ: chương trình tập, buổi tập có âm thanh/video, lịch sử và báo cáo, chế độ ăn, nhắc lịch, badge, Health Connect, đồng bộ Firebase, quảng cáo, billing, onboarding và paywall. Project cũng đã có nền tảng hiện đại hóa gồm target SDK 36, ViewBinding, ViewModel, Room, WorkManager và Jetpack Compose cho onboarding mới.

Rủi ro chính hiện nay không nằm ở thiếu tính năng mà ở khả năng thay đổi an toàn:

- Khoảng 45.423 dòng Java/Kotlin nhưng chỉ có hai test mẫu mặc định.
- Nhiều Activity/Fragment quá lớn và trộn UI, dữ liệu, quảng cáo, permission, navigation và nghiệp vụ.
- Dữ liệu được quản lý đồng thời bởi SQLite thủ công, ba Room database, SharedPreferences, asset JSON và Firebase.
- Một số luồng lifecycle bất đồng bộ có thể tạo listener/observer tồn tại lâu hoặc chạy lặp.
- Có các vấn đề release/compliance cần xử lý trước khi phát hành tiếp theo.

Khuyến nghị tổng quát: **ổn định và tạo safety net trước, tách kiến trúc sau, rồi mới mở rộng Compose**. Không nên viết lại toàn bộ ứng dụng trong một đợt.

## 2. Ảnh chụp hiện trạng

| Hạng mục | Hiện trạng quan sát được |
|---|---|
| Module | Một module Android `:app` |
| Ngôn ngữ | 281 file Java, 12 file Kotlin |
| Quy mô source | Khoảng 45.423 dòng Java/Kotlin |
| Resource | Khoảng 748 file |
| SDK | minSdk 26, compileSdk/targetSdk 36 |
| UI | XML/ViewBinding là chính; Compose đã bật và đang dùng cho onboarding |
| Dữ liệu | SQLite thủ công + 3 Room DB + SharedPreferences + Firebase + asset JSON |
| Test | Một unit test `2 + 2 = 4`, một instrumentation test kiểm tra package name |
| CI | Không thấy workflow CI trong repository |
| Release build | `minifyEnabled true`; ProGuard rules còn tối thiểu |

Các file lớn nhất đáng chú ý:

- `OnboardScreens.kt`: khoảng 1.757 dòng.
- `OnboardUi.kt`: khoảng 1.247 dòng.
- `YogaActivity.kt`: 947 dòng.
- `PreviewActivity.java`: 902 dòng.
- `ReportFragment.java`: 863 dòng.
- `MyPreference.java`: 806 dòng.
- `DoWorkoutActivity.java`: 764 dòng.
- `CustomVideoView.java`: 744 dòng.
- `AudioAppManager.java`: 656 dòng.

Kích thước file không tự động là lỗi, nhưng các file trên đang là vùng có rủi ro regression cao và khó review.

## 3. Điểm mạnh nên giữ lại

- Phân nhóm package theo tính năng đã hình thành: workout, report, diet, onboarding, premium, notification, badge.
- Có ViewBinding và một số ViewModel/repository thay vì hoàn toàn thao tác trực tiếp trên View.
- Room migration hiện có cho User và History; chưa dùng destructive migration cho hai nguồn dữ liệu quan trọng này.
- Onboarding Compose mới tách state khỏi Activity tốt hơn nhiều màn hình legacy.
- Có Crashlytics, Analytics và Firebase Performance để theo dõi production.
- Có Remote Config cho rollout onboarding, paywall và quảng cáo.
- Notification mới đã bắt đầu dùng WorkManager/scheduler và có cấu trúc riêng.
- Release đã bật R8/minification.

## 4. Các phát hiện ưu tiên

### P0 – Cần xử lý trước release tiếp theo

| Vấn đề | Bằng chứng | Rủi ro | Hướng xử lý |
|---|---|---|---|
| UMP luôn ép debug geography EEA | `SplashActivity.requestTCM()` luôn gọi `setDebugGeography(DEBUG_GEOGRAPHY_EEA)` | Consent production có thể chạy sai môi trường kiểm thử | Chỉ cấu hình debug geography khi `BuildConfig.DEBUG`; release dùng `ConsentRequestParameters` bình thường |
| Xin `SCHEDULE_EXACT_ALARM` như runtime permission | `YogaActivity.initView()` gọi `ActivityCompat.requestPermissions()` cho quyền này | Luồng không hoạt động đúng; quyền special access không được cấp theo runtime dialog | Dùng `AlarmManager.canScheduleExactAlarms()` và mở `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` khi thật sự cần; có fallback inexact alarm |
| Remote Config key không khớp | Code dùng `app_resume`, defaults dùng `app_open_resume` | Giá trị mặc định/remote có thể không điều khiển đúng App Open Ad | Chọn một key chuẩn, migrate console và thêm test kiểm tra toàn bộ key code/defaults |
| Không có test bảo vệ nghiệp vụ | Hai test hiện tại chỉ là template | Refactor/payment/sync có thể regression mà CI không phát hiện | Bổ sung test cho billing entitlement, progress/history migration, sync, notification policy và onboarding state trước refactor lớn |
| Chưa có cổng chất lượng tự động | Không thấy CI; lint/test chưa được bắt buộc | Lỗi chỉ xuất hiện khi build tay hoặc sau release | CI tối thiểu: assembleDebug, unit test, lint, kiểm tra manifest và dependency |
| Main Activity được export trực tiếp | `YogaActivity` có `exported=true` và intent filter không có permission; alias Health Connect đã có permission riêng | Tăng bề mặt component có thể bị app khác gọi | Xác minh yêu cầu Health Connect; ưu tiên để YogaActivity không export và chỉ export alias được bảo vệ |

### P1 – Ổn định kiến trúc và dữ liệu

1. **Làm mỏng `YogaActivity`.** Activity hiện quản lý navigation, Firebase, Health Connect, permission, review, notification, TTS, preference migration và callback Fragment. Tách thành `YogaViewModel`, `UserSyncRepository`, `HealthConnectCoordinator`, `ReminderScheduler`, `ReviewPrompter` và `VoicePreviewController`.

2. **Sửa lifecycle của Firebase sync.** `retrieveUserProgress()` đăng ký ba `addValueEventListener()` nhưng không giữ reference để remove. Nếu chỉ tải một lần, dùng `get()`; nếu cần realtime, repository phải expose Flow và tháo listener trong `awaitClose`.

3. **Không đăng ký observer bên trong lệnh sync.** `syncData()` gọi `allHistory.observe(this)` mỗi lần chạy, có thể tích lũy observer và upload lặp. Repository/ViewModel nên lấy snapshot một lần hoặc collect Flow có lifecycle rõ ràng.

4. **Chỉ đánh dấu migration thành công sau khi hoàn tất.** `isMigrateHistory = true` đang được set trước khi Firebase migration trả kết quả; khi request thất bại ứng dụng có thể không retry.

5. **Hợp nhất chiến lược persistence.** Hiện có:

   - `Database.java` quản lý SQLite asset thủ công.
   - `UserDatabase`, `HistoryDatabase`, `RecipeDatabase` dùng Room.
   - `MyPreference.java` chứa hơn 800 dòng và nhiều schema key dạng string.
   - Firebase giữ một phần progress/diet/history.

   Cần lập data ownership map, xác định nguồn chuẩn cho từng loại dữ liệu và dần đưa SQLite thủ công về Room hoặc repository có interface ổn định.

6. **Bật export Room schema và migration tests.** `exportSchema=false` làm mất lịch sử schema có thể review. Bật schema export, lưu schema trong repository và kiểm thử migration 1→latest bằng `room-testing`.

7. **Giảm global mutable state.** `FitnessApplication`, `MyPreference` static singleton, `MaticAd` và các manager toàn cục làm dependency khó kiểm soát. Trước mắt dùng constructor injection thủ công; sau khi ổn định có thể cân nhắc DI framework.

8. **Sửa `MyPreference` singleton giữ Context.** Luôn chuyển sang `context.applicationContext`, chia preference theo domain và gom key thành typed contract. Về dài hạn có thể chuyển state phù hợp sang DataStore.

9. **Rà soát database migration ngày/giờ.** Các migration chuyển epoch-day sang local midnight bằng `Calendar`. Cần test với timezone dương/âm, DST và đổi timezone để tránh lệch ngày lịch sử.

10. **Thống nhất navigation Report.** `AppBarConfiguration` dùng `nav_report2` trong khi bottom menu và `onNewIntent()` dùng `nav_report`. Xác định một destination chuẩn và thêm navigation test.

### P2 – Chất lượng, UI và khả năng bảo trì

- Chuyển dần `onBackPressed()` sang `OnBackPressedDispatcher`; bỏ `enableOnBackInvokedCallback=false` sau khi các flow quan trọng đã được migrate.
- Thay `onActivityResult()` cũ bằng Activity Result API.
- Chia các màn hình Compose onboarding thành file theo screen/component; tránh hai file UI hơn 1.000 dòng.
- Migrate Compose theo từng feature, không thay cả app cùng lúc. Thứ tự đề xuất: Diet → Explore → Home → Report → Settings.
- Giữ MPAndroidChart trong `AndroidView` ở bước đầu thay vì viết lại chart ngay.
- Tách `PreferenceFragmentCompat` sau cùng vì Settings đang phụ thuộc nhiều callback và custom preference.
- Chuẩn hóa theme giữa Material Components và Material 3 để tránh khác màu, typography và kích thước.
- Bổ sung accessibility: content description, touch target, dynamic font, contrast và TalkBack.
- Loại hardcoded text: hiện có nhiều text trong layout/source thay vì string resource; onboarding mới mới chỉ có đầy đủ chuỗi mới cho English/Vietnamese.
- Rà soát tablet/adaptive UI trước khi bỏ opt-out resizability của Android 16/17.

## 5. Vấn đề build và dependency

- Dependency đang khai báo trực tiếp trong một `build.gradle`; nên chuyển sang Version Catalog để quản lý và nâng cấp nhất quán.
- `com.intuit.sdp:sdp-android:1.1.0` bị khai báo hai lần.
- Glide runtime là `4.16.0` nhưng annotation processor là `4.14.2`; cần dùng cùng version hoặc bỏ processor nếu không có generated Glide module.
- `android.enableJetifier=true` cho thấy còn phụ thuộc legacy; kiểm tra dependency tree rồi thử tắt trong branch riêng.
- Health Connect đang dùng bản alpha; cần kiểm tra API compatibility và kế hoạch nâng cấp có kiểm thử.
- `UPGRADE_NOTES.md` ghi AGP/Gradle 8.13 nhưng source hiện tại là AGP 8.10.1 và Gradle Wrapper 8.11.1. Tài liệu phải lấy version thực tế làm nguồn chuẩn.
- ProGuard chưa giữ `SourceFile,LineNumberTable`, làm stack trace release khó đọc hơn. Crashlytics mapping vẫn cần được upload và xác minh trong pipeline.

Không nên nâng đồng loạt toàn bộ dependency cùng một PR. Mỗi nhóm nâng cấp cần có build, test và smoke test riêng.

## 6. Bảo mật, quyền riêng tư và compliance

1. `google-services.json` đang được track. File này thường chứa identifier/API key không phải server secret, nhưng vẫn phải giới hạn API key theo package name + signing certificate và audit Firebase rules.
2. Không đặt secret thật trong APK, Java hoặc native code. `FitnessApplication.getKey()` và thuật toán XOR/JNI chỉ là obfuscation, không phải bảo mật dữ liệu.
3. Native code chứa certificate/string debug cũ, kiểm tra chữ ký hiện bị comment và có chuỗi lỗi không phù hợp. Xóa dead code hoặc thay bằng cơ chế integrity có threat model rõ ràng.
4. Link Play Store fallback đang dùng HTTP; đổi sang HTTPS.
5. Rà soát Play policy cho `USE_FULL_SCREEN_INTENT`, `SCHEDULE_EXACT_ALARM`, notification lock screen, AD_ID và Health Connect.
6. Xác minh Firebase Database Rules: người dùng chỉ được đọc/ghi node của chính UID; validate kiểu và giới hạn dữ liệu.
7. Chuẩn hóa logging: còn nhiều log tag `HAHA`, `printStackTrace()` và có thể log dữ liệu người dùng. Dùng logger có redaction và tắt verbose log ở release.
8. Thêm quy trình xóa tài khoản/dữ liệu và mô tả rõ phạm vi đồng bộ trong privacy policy.

## 7. Kiến trúc đích đề xuất

Không cần tách multi-module ngay. Trước hết tổ chức trong `:app` theo feature và layer:

```text
app/src/main/java/.../
├── core/
│   ├── analytics/
│   ├── billing/
│   ├── health/
│   ├── notification/
│   └── common-ui/
├── data/
│   ├── local/
│   ├── remote/
│   └── repository/
├── domain/
│   ├── model/
│   └── usecase/
└── feature/
    ├── main/
    ├── workout/
    ├── report/
    ├── diet/
    ├── onboarding/
    ├── premium/
    └── settings/
```

Luồng dependency mong muốn:

```text
Activity/Fragment/Composable
          ↓ event / UI state
       ViewModel
          ↓
        Use case
          ↓
 Repository interface
       ↙       ↘
 local data   remote data
```

Nguyên tắc:

- ViewModel không giữ Activity/View/Fragment/Context UI.
- Composable không trực tiếp gọi Firebase, billing, Health Connect hoặc SharedPreferences.
- Repository không điều hướng và không hiện Toast/Dialog.
- Tác vụ một lần phát qua UI effect; state lâu dài dùng immutable `StateFlow`.
- Mỗi loại dữ liệu có một source of truth được ghi rõ.

## 8. Kế hoạch làm mỏng YogaActivity

### Trạng thái mục tiêu

`YogaActivity` chỉ còn:

- Inflate/set Compose content.
- Thiết lập navigation.
- Đăng ký Activity Result contracts.
- Collect `YogaUiState` và `YogaEffect`.
- Thực hiện side effect cần Activity: mở màn hình, permission launcher, Toast/Dialog.

### Thành phần cần tách

| Logic hiện tại | Đích mới |
|---|---|
| `retrieveUserProgress`, `syncData`, history migration | `UserSyncRepository` + `SyncUserDataUseCase` |
| SDK status, permission, revoke Health Connect | `HealthConnectRepository` + `HealthConnectCoordinator` |
| Open count, progress ID migration | `AppLaunchCoordinator` / `ProgressMigration` |
| In-app review và exit rating | `ReviewPrompter` + `RatingPolicy` |
| Reminder và notification channel | `ReminderScheduler` |
| TextToSpeech preview | `VoicePreviewController` |
| Callback Home/Settings | Shared `YogaViewModel` + event/effect |
| Mở Preview/Challenge/Premium | Activity xử lý effect trong giai đoạn đầu |

Tiêu chí hoàn thành:

- `YogaActivity` còn khoảng 100–200 dòng.
- Không còn truy cập/cast trực tiếp `SettingFragment` từ Activity.
- Không còn Firebase listener hoặc LiveData observer được đăng ký trong một nút bấm/lệnh sync mà không có teardown.
- Các policy thuần logic có unit test.
- Navigation/deep link từ notification tới Report vẫn hoạt động với `singleTask`.

## 9. Chiến lược Compose

Compose đã được cấu hình, vì vậy chi phí khởi tạo đã thấp. Tuy nhiên chuyển riêng toolbar/bottom navigation trong khi vẫn giữ `NavHostFragment` chỉ mang lại lợi ích nhỏ và tạo thêm interoperability.

Thứ tự khuyến nghị:

1. Tách business logic khỏi View trước.
2. Migrate `DietFragment` vì nhỏ và ít dependency.
3. Migrate `ExploreFragment`.
4. Migrate `HomeFragment`, thay RecyclerView section bằng LazyColumn khi data contract đã ổn định.
5. Migrate `ReportFragment`; giữ chart cũ qua `AndroidView` trước.
6. Migrate Settings cuối cùng.
7. Khi các destination đã là Compose, thay Fragment NavHost bằng `navigation-compose` và `Scaffold`.

Không đổi `YogaActivity` từ `AppCompatActivity` sang `ComponentActivity` trước khi đã loại các phụ thuộc `supportFragmentManager` và DialogFragment.

## 10. Chiến lược test

### Unit test ưu tiên đầu tiên

- `RatingPolicy`: open count, premium, rated state.
- Progress ID migration: 20→879, 21→881, 22→880, 23→882.
- History date migration qua nhiều timezone.
- Program recommendation/onboarding state.
- Remote Config key mapping và default values.
- Billing plan/offer selection, trial và entitlement persistence.
- User sync mapping, malformed Firebase key và retry migration.
- Notification scheduling policy và exact/inexact fallback.

### Instrumentation/UI test tối thiểu

- First launch: consent → language → onboarding → paywall → home.
- Returning user: splash → home, cả khi ad thành công/thất bại/timeout.
- Bottom navigation và back stack.
- Start/finish workout, pause/resume, xoay/cấu hình nếu được hỗ trợ.
- Health Connect: unavailable, update required, deny, grant, revoke.
- Billing: offline, cancelled, pending, purchased, restored.
- Notification/deep link mở đúng Report.
- Light/dark, English/Vietnamese, font scale 200%, TalkBack.
- Android 8, Android 13, Android 16; phone và tablet.

### CI tối thiểu

```text
checkout
→ setup JDK/Android SDK
→ ./gradlew testDebugUnitTest
→ ./gradlew lintDebug
→ ./gradlew assembleDebug
→ lưu test/lint/APK artifacts
```

Sau khi ổn định, thêm release assemble, screenshot tests, dependency vulnerability scan và baseline profile/macrobenchmark.

## 11. Hiệu năng và độ tin cậy

- `FitnessApplication.onCreate()` đang khởi tạo notification, ads, JSON helper và database. Đo cold start và trì hoãn phần không cần cho first frame.
- `SplashActivity` tải đồng thời ba interstitial và giữ nhiều Handler/timeout. Chuyển thành state machine idempotent để `startMain()` chỉ chạy một lần.
- `largeHeap=true` ở Application/Yoga/DoWorkout che giấu áp lực bộ nhớ; đo ảnh/video/cache rồi loại bỏ nếu không có lý do định lượng.
- Hủy Handler, timer, TTS, media player, ad object và listener đúng lifecycle.
- Tối ưu asset ảnh/animation và tránh decode file lớn trên main thread.
- Thêm performance trace cho cold start, load home, open workout và finish workout.

Chỉ tối ưu sau khi đo; tránh rewrite vì cảm tính.

## 12. Roadmap đề xuất

### Milestone 0 – Baseline có thể tin cậy

- Thiết lập CI build/test/lint.
- Tạo release checklist và test matrix.
- Ghi nhận baseline Crashlytics: crash-free users, ANR, top crashes.
- Chuẩn hóa version thực tế trong tài liệu.
- Không thêm feature lớn trước khi P0 được đóng.

### Milestone 1 – Release safety

- Sửa UMP debug geography.
- Sửa exact alarm flow.
- Sửa Remote Config key mismatch.
- Thu hẹp exported component.
- Audit Firebase Rules, Play permissions và billing entitlement.
- Thêm nhóm unit test nghiệp vụ đầu tiên.

### Milestone 2 – Data và YogaActivity

- Tạo repository cho sync và Health Connect.
- Tách startup/review/reminder/TTS khỏi Activity.
- Thêm Room schema + migration test.
- Định nghĩa source of truth cho progress, history, diet và user profile.
- Loại callback Activity–Fragment bằng shared ViewModel.

### Milestone 3 – UI modernization

- Chia nhỏ onboarding Compose.
- Migrate Diet/Explore/Home.
- Chuẩn hóa Material 3 theme, insets và accessibility.
- Migrate Report/Settings sau khi data contract ổn định.

### Milestone 4 – Scale và tối ưu

- Cân nhắc multi-module theo feature nếu team/build time đã cần.
- Baseline Profile và macrobenchmark.
- Tắt Jetifier khi dependency cho phép.
- Loại opt-out predictive back/resizability.
- Rà soát minification, startup và memory trên thiết bị thấp.

## 13. Definition of Done chung

Một thay đổi được xem là hoàn thành khi:

- Có acceptance criteria và analytics requirement rõ ràng.
- Unit test cho logic mới hoặc lý do cụ thể nếu không thể test.
- Build debug, unit test và lint pass trên CI.
- Không thêm Context/View vào ViewModel hoặc business layer.
- Không thêm hardcoded user-facing string.
- Đã kiểm tra loading, empty, offline, error và retry state.
- Đã kiểm tra accessibility và dark mode nếu có UI.
- Data/schema/Remote Config change có migration hoặc rollback plan.
- Feature flag có default an toàn và owner/ngày xóa.
- Crashlytics/Analytics không ghi PII hoặc health data nhạy cảm.
- Tài liệu liên quan được cập nhật theo source thực tế.

## 14. Kết quả xác minh trong lần review này

- Đã đọc cấu hình Gradle, Manifest, navigation, Activity/Fragment chính, data layer, Room migration, notification/ads và Compose onboarding.
- Đã thống kê source/resource/test và rà soát các pattern lifecycle/deprecated API phổ biến.
- Có APK debug tồn tại tại `app/build/outputs/apk/debug/app-debug.apk`, timestamp 2026-09-18 19:40:25; đây là artifact có sẵn, không phải build được tạo bởi lần review này.
- Đã thử chạy `testDebugUnitTest lintDebug`, nhưng môi trường sandbox chặn Gradle daemon mở local socket (`java.net.SocketException: Operation not permitted`). Vì vậy tài liệu không khẳng định test/lint hiện tại đang pass.

## 15. Ba việc nên bắt đầu ngay

1. Đóng bốn lỗi release: UMP debug geography, exact alarm, Remote Config key và exported YogaActivity.
2. Dựng CI và viết test cho progress/history migration, billing entitlement và sync.
3. Tách Firebase sync + Health Connect khỏi `YogaActivity`; chưa cần chuyển Activity sang Compose ở bước này.
