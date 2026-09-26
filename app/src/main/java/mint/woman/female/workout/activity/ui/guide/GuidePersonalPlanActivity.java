package mint.woman.female.workout.activity.ui.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.premium.PremiumActivity;
import mint.woman.female.workout.activity.ui.main.YogaActivity;
import mint.woman.female.workout.databinding.ActivityGuidePersonalPlanBinding;

public class GuidePersonalPlanActivity extends AppCompatActivity {
    private ValueAnimator animator;
    ActivityGuidePersonalPlanBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuidePersonalPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startLoadingProcess();


    }
    private void startLoadingProcess() {
        // Tạo Animation chạy từ 0 đến 100
        // Thời gian chạy tổng cộng: 4000ms (4 giây)
        animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(4000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();

                // 1. Cập nhật thanh Progress Bar
                //binding..setProgress(progress);

                // 2. Cập nhật số %
                binding.tvPercent.setText(progress + "%");

                // 3. (Mẹo hay) Cập nhật dòng chữ trạng thái để trông "thật" hơn
                if (progress < 30) {
                    binding.tvStatus.setText(R.string.ob_status_bmi);
                } else if (progress < 60) {
                    binding.tvStatus.setText(R.string.ob_status_habits);
                } else if (progress < 90) {
                    binding.tvStatus.setText(R.string.ob_status_plan);
                } else {
                    binding.tvStatus.setText(R.string.ob_status_done);
                }
            }
        });

        // Lắng nghe sự kiện khi chạy xong 100%
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Chuyển sang trang Premium
                nextAction();
            }
        });

        // Bắt đầu chạy
        animator.start();
    }
    void nextAction(){
        Intent intentMain = new Intent(GuidePersonalPlanActivity.this, YogaActivity.class);
        startActivity(intentMain);
        Intent intent = new Intent(GuidePersonalPlanActivity.this, PremiumActivity.class);

        intent.putExtra(PremiumActivity.EXTRA_IS_FIRST, true);
        startActivity(intent);
        finish();
    }
}