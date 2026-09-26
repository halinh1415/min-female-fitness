# Kế hoạch phát triển OnboardFlowActivity

> Ngày lập: 2026-09-19
>
> Phạm vi: onboarding Compose hiện tại, cá nhân hóa kế hoạch, analytics và thử nghiệm doanh thu In-App. Màn xin quyền notification không nằm trong thử nghiệm này.

## Trạng thái triển khai

Cập nhật 2026-09-19:

- Đã backup `OnboardFlowActivity` trước khi thay đổi.
- Đã thêm `OnboardingVariant`, assignment ổn định và Remote Config key.
- Đã chuyển Activity/State từ `ONBOARD_STEPS` cố định sang danh sách bước động.
- Đã có ba definition ban đầu: `control_46`, `short_28` và `adaptive`.
- Đã thêm analytics chung chứa experiment, variant, step và visible step count.
- Đã thêm unit test cho fallback, số bước bắt buộc và rule adaptive ban đầu.
- `control_46` vẫn là default; chưa bật experiment production.
- Chưa triển khai safety branch mới, persistence typed, plan mapper và purchase attribution.

## 1. Mục tiêu

Phát triển `OnboardFlowActivity` thành một onboarding có thể chạy nhiều biến thể mà không nhân bản Activity hoặc UI, đồng thời trả lời được bằng dữ liệu production:

- Onboarding 46 màn hiện tại có tạo ra doanh thu trên mỗi lượt cài đặt tốt hơn flow ngắn hay không.
- Những bước nào làm người dùng rời onboarding.
- Câu trả lời nào thực sự cải thiện chương trình tập và nội dung paywall.
- Flow cố định hay flow phân nhánh tạo ra trial, purchase và retention tốt hơn.

Chỉ số thắng cuối cùng là **doanh thu trên mỗi lượt cài đặt**, không phải riêng tỷ lệ mua tại paywall.

## 2. Hiện trạng code

Các file chính:

- `app/src/main/java/mint/woman/female/workout/activity/ui/onboarding/onboard/OnboardFlowActivity.kt`
- `app/src/main/java/mint/woman/female/workout/activity/ui/onboarding/onboard/OnboardModel.kt`
- `app/src/main/java/mint/woman/female/workout/activity/ui/onboarding/onboard/OnboardState.kt`
- `app/src/main/java/mint/woman/female/workout/activity/ui/onboarding/onboard/OnboardScreens.kt`
- `app/src/main/java/mint/woman/female/workout/activity/ui/onboarding/onboard/OnboardUi.kt`

Hiện tại:

- Toàn bộ người dùng đi qua một danh sách `ONBOARD_STEPS` cố định.
- Activity đã ghi nhận start, step, complete và skip bằng Firebase Analytics.
- `OnboardState` giữ câu trả lời khi Activity còn sống.
- Khi hoàn thành, chỉ age, height, weight, target weight, goal, focus và workout level được lưu.
- Frequency, discomfort, workout place/type, activity, fitness, flexibility, aerobic và nhóm câu hỏi động lực chưa được lưu đầy đủ để tạo chương trình.
- `OnboardState`, progress, back/skip và UI đọc trực tiếp biến toàn cục `ONBOARD_STEPS`, nên chưa thể hỗ trợ flow phân nhánh an toàn.
- Paywall được mở sau khi hoàn thành onboarding nếu người dùng chưa Premium.
- Project đã có Firebase Analytics và Remote Config, vì vậy không cần thêm một hệ thống experiment mới ở giai đoạn đầu.

## 3. Quyết định sản phẩm

Không tạo ba Activity. Giữ duy nhất `OnboardFlowActivity` và chạy ba biến thể bằng cấu hình:

| Variant | Mục đích | Số màn dự kiến |
|---|---|---:|
| `control_46` | Giữ trải nghiệm hiện tại làm control | 46 |
| `short_28` | Loại câu trùng và rút thời gian tới paywall | 26–30 |
| `adaptive` | Phân nhánh theo câu trả lời và chỉ hỏi nội dung liên quan | 20–32 mỗi người |

Không thay đổi đồng thời giá, trial, thiết kế paywall hoặc thời điểm xin notification trong cùng experiment. Nếu thay nhiều yếu tố một lúc sẽ không xác định được onboarding hay paywall tạo ra khác biệt doanh thu.

## 4. Kiến trúc mục tiêu

```text
OnboardFlowActivity
        │
        ├── OnboardingViewModel
        │       ├── OnboardingState
        │       ├── OnboardingAnswerStore
        │       └── OnboardingExperimentRepository
        │
        ├── OnboardingStepFactory
        │       ├── Control46Definition
        │       ├── Short28Definition
        │       └── AdaptiveDefinition
        │
        ├── OnboardingAnalytics
        └── OnboardingResultMapper
                ├── WorkoutPlanInput
                └── PaywallPersonalization
```

Nguyên tắc:

- Activity chỉ thiết lập Compose, collect state và thực hiện navigation/side effect.
- ViewModel sở hữu variant, danh sách bước hiện tại, câu trả lời và điều hướng.
- UI chỉ render `currentStep`; không đọc biến toàn cục `ONBOARD_STEPS`.
- Step factory quyết định thứ tự màn theo variant và câu trả lời.
- Analytics dùng chung schema cho cả ba variant.
- Variant được gán một lần và lưu ổn định đến khi experiment kết thúc.
- Các câu trả lời dùng cho plan phải đi qua model typed; không truyền string rời rạc xuyên suốt ứng dụng.

## 5. Cấu trúc code đề xuất

```text
onboarding/onboard/
├── OnboardFlowActivity.kt
├── OnboardingViewModel.kt
├── model/
│   ├── OnboardingVariant.kt
│   ├── OnboardingStep.kt
│   ├── OnboardingAnswers.kt
│   └── OnboardingUiState.kt
├── experiment/
│   ├── OnboardingExperimentRepository.kt
│   └── OnboardingVariantResolver.kt
├── flow/
│   ├── OnboardingStepFactory.kt
│   ├── Control46Definition.kt
│   ├── Short28Definition.kt
│   └── AdaptiveDefinition.kt
├── analytics/
│   └── OnboardingAnalytics.kt
├── result/
│   ├── OnboardingResultMapper.kt
│   └── PaywallPersonalization.kt
├── OnboardScreens.kt
└── OnboardUi.kt
```

Không bắt buộc di chuyển toàn bộ file ngay trong một commit. Có thể tách lần lượt để hạn chế regression.

## 6. Variant và cách gán nhóm

### 6.1 Model

```kotlin
enum class OnboardingVariant(val remoteValue: String) {
    CONTROL_46("control_46"),
    SHORT_28("short_28"),
    ADAPTIVE("adaptive")
}
```

### 6.2 Remote Config

Thêm key:

```text
onboarding_experiment_variant = control_46
```

Giá trị mặc định trong app luôn là `control_46`. Giá trị Remote Config không hợp lệ cũng phải fallback về control.

Nếu dùng Firebase A/B Testing, cấu hình ba nhóm trực tiếp trên key này. Nếu chưa dùng A/B Testing, có thể rollout phần trăm qua Remote Config condition, nhưng phải bảo đảm cùng một installation không đổi variant giữa chừng.

### 6.3 Lưu assignment

Lưu tối thiểu:

```text
onboarding_experiment_id
onboarding_variant
onboarding_variant_assigned_at
onboarding_flow_version
```

Quy tắc:

1. Đọc assignment đã lưu.
2. Nếu experiment ID vẫn giống nhau, dùng lại assignment cũ.
3. Chỉ đọc/gán variant mới khi chưa có assignment hoặc experiment đã kết thúc.
4. Không đổi variant khi người dùng đang onboarding dở.
5. Gửi variant vào user property hoặc event parameter Analytics.

## 7. Thiết kế ba flow

### 7.1 Variant A – `control_46`

- Giữ nguyên thứ tự và nội dung hiện tại để làm baseline.
- Chỉ sửa lỗi kỹ thuật, analytics và persistence cần thiết.
- Không thay copy hoặc số lượng màn trong thời gian chạy experiment.

### 7.2 Variant B – `short_28`

Giữ:

- Welcome và coach.
- Motivation, goal và focus.
- Height, weight và age.
- Workout place/type/level.
- Frequency.
- Sàng lọc discomfort/giới hạn vận động.
- Typical day hoặc activity level.
- Một màn social proof.
- Một màn goal visualization.
- Generating, plan result và trial intro.

Gộp hoặc loại:

- Gộp current body shape và desired body shape nếu chúng không đổi plan.
- Loại belly/hip/leg type khỏi variant ngắn.
- Gộp activity, fitness và aerobic thành một màn năng lực hiện tại.
- Chỉ giữ một trong ba statement về body/choice/give-up.
- Gộp celebration và feeling thành một màn motivation cuối.
- Chỉ giữ một trong ba màn full yes/no.
- Giữ một prediction thay vì hai prediction gần giống nhau.

### 7.3 Variant C – `adaptive`

Mọi người dùng đi qua các bước lõi:

```text
Welcome → Goal → Focus → Safety → Experience
→ Available time/frequency → Equipment/location
→ Plan summary → Generating → Result → Trial intro
```

Nhánh bổ sung:

- Có đau/chấn thương: hỏi vùng đau và giới hạn động tác.
- Có tình trạng sức khỏe cần thận trọng: không tự chẩn đoán; hiển thị hướng dẫn an toàn và giới hạn plan.
- Mang thai/sau sinh: chuyển sang nhánh phù hợp nếu app có nội dung đã được duyệt; nếu chưa có thì không đưa ra plan cường độ thông thường.
- Beginner hoặc ít vận động: hỏi thêm khả năng chịu cường độ và ưu tiên low-impact.
- Advanced: bỏ các màn giải thích dành cho người mới và cho phép mức thử thách cao hơn.
- Chọn no jumping/đau gối: loại nội dung nhảy khỏi plan preview.
- Không có dụng cụ: không quảng bá bài cần thiết bị.

Flow adaptive phải có số lượng bước tối đa để tránh vòng lặp hoặc phát sinh onboarding dài hơn control.

## 8. Dữ liệu cần lưu và sử dụng

Tạo model typed thay cho việc chỉ dùng `Map<String, String>`:

```kotlin
data class OnboardingAnswers(
    val motivation: Motivation? = null,
    val goal: Goal? = null,
    val focusAreas: Set<FocusArea> = emptySet(),
    val age: Int? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val targetWeightKg: Float? = null,
    val experience: Experience? = null,
    val sessionsPerWeek: Int? = null,
    val minutesPerSession: Int? = null,
    val equipment: Set<Equipment> = emptySet(),
    val workoutPreferences: Set<WorkoutPreference> = emptySet(),
    val movementRestrictions: Set<MovementRestriction> = emptySet(),
    val recentActivity: ActivityLevel? = null
)
```

Mỗi dữ liệu được hỏi phải có ít nhất một consumer:

| Câu trả lời | Ảnh hưởng bắt buộc |
|---|---|
| Goal | Chọn nhóm chương trình, copy result và paywall |
| Focus area | Chọn/chấm điểm chương trình phù hợp |
| Experience | Mức độ và thời gian nghỉ |
| Frequency | Lịch tuần và reminder suggestion |
| Minutes/session | Độ dài buổi tập |
| Equipment | Lọc bài theo dụng cụ |
| No jumping/restriction | Loại hoặc thay động tác |
| Recent activity | Cường độ khởi đầu |
| Height/weight/age | Chỉ dùng cho tính toán đã được kiểm tra; không coi BMI là chẩn đoán |

Nếu một câu trả lời không có consumer trong plan, result, paywall hoặc analytics đã xác định rõ, câu hỏi đó không được gọi là “personalization”.

## 9. Cá nhân hóa màn kết quả và paywall

Màn kết quả phải phản chiếu dữ liệu thật, ví dụ:

```text
Kế hoạch giảm mỡ toàn thân dành cho bạn
15 phút mỗi buổi · 4 ngày mỗi tuần
Beginner · No jumping · Không cần dụng cụ
```

Paywall nhận một model giới hạn, không đọc trực tiếp toàn bộ dữ liệu sức khỏe:

```kotlin
data class PaywallPersonalization(
    val goalLabel: String,
    val focusLabel: String,
    val scheduleLabel: String,
    val workoutStyleLabel: String
)
```

Nội dung có thể thay đổi theo goal/focus, nhưng sản phẩm, giá, trial và quyền lợi phải giữ giống nhau trong experiment onboarding để kết quả có thể so sánh.

Không sử dụng claim như “đảm bảo giảm X kg”, “đạt kết quả vào ngày Y” hoặc tỷ lệ thành công nếu chưa có bằng chứng và điều kiện diễn giải phù hợp.

## 10. Analytics

### 10.1 Event bắt buộc

| Event | Thời điểm |
|---|---|
| `onboarding_started` | Activity bắt đầu flow |
| `onboarding_step_viewed` | Một step được hiển thị |
| `onboarding_step_answered` | Người dùng xác nhận câu trả lời |
| `onboarding_step_skipped` | Skip step/section |
| `onboarding_back_clicked` | Quay về bước trước |
| `onboarding_abandoned` | Flow bị thoát trước complete, khi xác định được |
| `onboarding_completed` | Hoàn thành flow |
| `onboarding_plan_viewed` | Xem kết quả cá nhân hóa |
| `onboarding_paywall_viewed` | Paywall xuất hiện sau onboarding |
| `onboarding_trial_started` | Bắt đầu trial từ paywall này |
| `onboarding_purchase_completed` | Purchase hoàn tất |

### 10.2 Parameter chung

```text
experiment_id
variant
flow_version
step_id
step_kind
step_index
visible_step_count
section
elapsed_ms
is_skippable
```

Không gửi chiều cao, cân nặng, bệnh/chấn thương hoặc câu trả lời sức khỏe thô vào Firebase Analytics. Chỉ gửi segment không nhạy cảm, đã được duyệt và thật sự cần đo lường.

### 10.3 Funnel

```text
install
  → onboarding_started
  → onboarding_completed
  → onboarding_paywall_viewed
  → onboarding_trial_started
  → onboarding_purchase_completed
  → retained / renewed / refunded
```

Dashboard phải lọc được theo variant, country, app version và acquisition channel khi dữ liệu cho phép.

## 11. Chỉ số đánh giá

### Primary metric

- Revenue D7 trên mỗi install.
- Khi đủ thời gian, bổ sung Revenue D30/D35 trên mỗi install.

### Secondary metrics

- Onboarding completion/install.
- Paywall view/install.
- Trial start/install.
- Paid/install.
- Trial-to-paid.
- Thời gian trung vị từ start tới paywall.
- D1, D7 và D30 retention.

### Guardrail metrics

- Refund và chargeback.
- Trial cancellation sớm.
- Crash/ANR trong onboarding.
- Tỷ lệ abandon tại từng step.
- Đánh giá tiêu cực liên quan tới onboarding, paywall hoặc claim sức khỏe.

Không chọn variant thắng chỉ vì `purchase/paywall_view` cao hơn. Variant phải thắng hoặc ít nhất không giảm đáng kể ở cấp `install` và doanh thu sau hoàn tiền.

## 12. Kế hoạch triển khai

### Giai đoạn 0 – Đóng baseline

- Giữ `control_46` giống production hiện tại.
- Chốt tên step ổn định; không dùng index làm định danh duy nhất.
- Ghi nhận funnel hiện tại ít nhất một chu kỳ traffic đủ đại diện.
- Kiểm tra purchase attribution từ paywall về onboarding variant.

### Giai đoạn 1 – Tách hạ tầng

- Tạo `OnboardingVariant` và resolver.
- Tạo `OnboardingStepFactory` nhưng ban đầu chỉ trả về control.
- Chuyển `OnboardState` sang dùng `steps` được inject/cung cấp bởi ViewModel.
- Thay mọi tham chiếu trực tiếp `ONBOARD_STEPS` trong state/UI.
- Tạo `OnboardingAnalytics` và thống nhất event schema.
- Lưu assignment và progress để process recreation không đổi flow.

### Giai đoạn 2 – Persistence và cá nhân hóa thật

- Tạo `OnboardingAnswers` typed.
- Lưu các input thật sự cần cho plan.
- Tạo `OnboardingResultMapper`.
- Dùng restrictions/equipment/time/frequency để lọc và sắp xếp chương trình.
- Hiển thị plan summary dựa trên dữ liệu thật.
- Truyền model cá nhân hóa tối thiểu sang paywall.

### Giai đoạn 3 – Thêm variant

- Triển khai `short_28` từ cùng component/step definition.
- Triển khai `adaptive` với rule rõ ràng và unit test.
- Kiểm tra số bước min/max của từng nhánh.
- Không thay paywall, giá hoặc notification permission trong giai đoạn này.

### Giai đoạn 4 – Rollout experiment

- Internal QA: ép variant qua debug menu hoặc intent extra chỉ trong debug build.
- Rollout 5% để kiểm tra crash, analytics và assignment.
- Tăng tỷ lệ khi dữ liệu ổn định.
- Không dừng experiment ngay khi một ngày có kết quả tốt; chờ đủ chu kỳ tuần và đủ conversion theo kế hoạch phân tích.
- Giữ control holdout trong toàn bộ thời gian thử nghiệm.

### Giai đoạn 5 – Kết luận và dọn code

- Chọn variant theo primary metric và guardrails.
- Ghi lại ngày, version, cohort và kết quả experiment.
- Đưa variant thắng thành default.
- Giữ control thêm một thời gian ngắn nếu cần xác nhận.
- Sau khi quyết định ổn định, xóa definition không còn sử dụng và migrate assignment cũ.

## 13. Test plan

### Unit test

- Remote value hợp lệ/không hợp lệ trả đúng variant/fallback.
- Assignment được giữ nguyên qua nhiều lần mở app.
- Mỗi variant có step ID duy nhất.
- Back, next và skip không vượt giới hạn danh sách động.
- Progress tính đúng khi số bước thay đổi.
- Adaptive rule tạo đúng nhánh cho injury, no-jumping, beginner và advanced.
- Mọi nhánh adaptive đều đi tới plan result/paywall.
- Plan mapper dùng đúng goal, frequency, duration, equipment và restrictions.
- `keep_fit` không tự động ép giảm cân.
- Dữ liệu metric/imperial chuyển đổi chính xác.

### Compose/UI test

- Hoàn thành từng variant từ đầu tới cuối.
- Process recreation không mất variant hoặc câu trả lời.
- Back tại step đầu thoát đúng hành vi mong muốn.
- Skip section không bỏ qua safety gate bắt buộc.
- TalkBack đọc được title, lựa chọn và CTA.
- Font scale lớn không che CTA.
- Paywall chỉ mở một lần sau complete.
- Premium user không bị mở paywall.

### Manual QA

- English và Vietnamese.
- Light/dark mode nếu hỗ trợ.
- Thiết bị nhỏ, tablet và gesture navigation.
- Offline khi Remote Config không fetch được.
- App bị kill ở giữa onboarding.
- Upgrade từ bản đã onboard và bản đang onboard dở.

## 14. Tiêu chí hoàn thành

- Chỉ có một `OnboardFlowActivity` cho cả ba variant.
- Variant assignment ổn định và có fallback an toàn.
- Control giữ nguyên trải nghiệm production trong thời gian experiment.
- Analytics nối được install → onboarding → paywall → trial/purchase.
- Không gửi dữ liệu sức khỏe thô vào Analytics.
- Mọi câu được gọi là cá nhân hóa đều thay đổi plan, result hoặc paywall theo cách đã định nghĩa.
- Flow adaptive không bỏ qua câu hỏi an toàn bắt buộc.
- Có unit test cho step factory, branching, result mapper và assignment.
- Có dashboard so sánh primary/secondary/guardrail metrics theo variant.
- Có tài liệu kết quả trước khi xóa bất kỳ variant nào.

## 15. Rủi ro cần kiểm soát

| Rủi ro | Cách kiểm soát |
|---|---|
| Variant dài tăng conversion tại paywall nhưng giảm số người tới paywall | Đánh giá theo install và revenue/install |
| Remote Config đổi nhóm giữa flow | Persist assignment theo experiment ID |
| Danh sách bước động làm sai index/progress | Dùng stable step ID và test min/max branch |
| Câu trả lời chỉ tạo cảm giác cá nhân hóa | Lập bảng consumer và result mapper |
| Thu thập dữ liệu sức khỏe quá mức | Data minimization, không gửi raw answer vào Analytics |
| Nhiều thay đổi làm sai kết luận A/B test | Giữ giá, paywall và notification flow cố định |
| Claim giảm cân hoặc social proof không có bằng chứng | Gỡ hoặc cung cấp nguồn/điều kiện hợp lệ trước release |
| Người dùng gặp safety risk | Safety gate bắt buộc, không chẩn đoán, giới hạn plan hoặc khuyên hỏi chuyên gia |

## 16. Thứ tự ưu tiên đề xuất

1. Hoàn thiện analytics và purchase attribution cho flow 46 màn hiện tại.
2. Tách `OnboardingStepFactory` mà chưa đổi UI production.
3. Lưu variant/progress ổn định.
4. Lưu và sử dụng thật các câu trả lời quan trọng.
5. Tạo variant `short_28`.
6. Tạo variant `adaptive`.
7. QA, rollout nhỏ và xác minh dashboard.
8. Chạy experiment, đánh giá revenue/install và retention.

Không nên xây cả ba flow trước khi baseline analytics được xác minh. Nếu attribution hoặc event thiếu, experiment sẽ tiêu tốn traffic nhưng không trả lời được biến thể nào thực sự tạo thêm doanh thu.

## 17. Tài liệu tham khảo sản phẩm

- RevenueCat – Paywall testing and placement: <https://www.revenuecat.com/blog/growth/paywall-tests-grow-app-revenue>
- RevenueCat – Goal-oriented onboarding case study: <https://www.revenuecat.com/blog/growth/asl-bloom-applica-case-study>
- WHO – Guidelines on physical activity and sedentary behaviour: <https://www.who.int/publications/i/item/9789240014886>
- ACOG – Exercise during pregnancy and postpartum: <https://www.acog.org/clinical/clinical-guidance/committee-opinion/articles/2020/04/physical-activity-and-exercise-during-pregnancy-and-the-postpartum-period>
