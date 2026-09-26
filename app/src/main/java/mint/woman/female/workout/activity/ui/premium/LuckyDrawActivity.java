package mint.woman.female.workout.activity.ui.premium;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import mint.woman.female.workout.R;
import mint.woman.female.workout.admodule.MaticAd;
import mint.woman.female.workout.admodule.MaticAdCallBack;
import mint.woman.female.workout.databinding.ActivityLuckyDrawBinding;
import mint.woman.female.workout.utils.EdgeToEdgeHelper;
import mint.woman.female.workout.utils.MyPreference;

public class LuckyDrawActivity extends AppCompatActivity
        implements DiscountDialogFragment.DismissListener, EdgeToEdgeHelper.HandlesInsets {

    ActivityLuckyDrawBinding binding;
    private ObjectAnimator buttonPulseAnimator;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLuckyDrawBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
    }

    private void initView() {
        playEntranceAnimation();
        binding.lottieView.setAnimation("slot_machine.json");
        binding.lottieView.setProgress(0f);
        binding.btnLuckyDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chan bam lien tuc: khong thi listener animator cong don ben duoi bi xep chong.
                binding.btnLuckyDraw.setEnabled(false);
                if (buttonPulseAnimator != null) {
                    buttonPulseAnimator.cancel();
                    binding.btnLuckyDraw.setScaleX(1f);
                    binding.btnLuckyDraw.setScaleY(1f);
                }
                binding.lottieView.playAnimation();
                binding.lottieLight.setImageAssetsFolder("images_light");
                binding.lottieLight.setAnimation("lucky_draw_light.json");
                binding.lottieView.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        binding.lottieLight.playAnimation();


                        binding.lottieLight.addAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationCancel(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(@NonNull Animator animator) {
                                binding.lottieLight.setAnimation("lucky_draw_ribbons.json");
                                binding.lottieLight.playAnimation();
                                if (getSupportFragmentManager().findFragmentByTag("discount") == null) {
                                    DiscountDialogFragment.newInstance()
                                            .show(getSupportFragmentManager(), "discount");
                                }
                            }

                            @Override
                            public void onAnimationRepeat(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationStart(@NonNull Animator animator) {

                            }
                        });


                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }
                });


            }
        });
        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /** Hieu ung xuat hien khi vao man: fade + truot vao, so le tung view. */
    private void playEntranceAnimation() {
        fadeSlideIn(binding.ivClose, 0);
        fadeSlideIn(binding.tvTitle, 100);
        fadeSlideIn(binding.lottieView, 150);
        fadeSlideIn(binding.btnLuckyDraw, 300);
        binding.btnLuckyDraw.postDelayed(this::startButtonPulse, 700);
    }

    private void fadeSlideIn(View view, long startDelay) {
        view.setTranslationY(view.getResources().getDisplayMetrics().density * 24);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelay)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Nhap nhay nhe nut Lucky Draw de thu hut chu y, no luon co dinh o phia duoi man hinh. */
    private void startButtonPulse() {
        buttonPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.btnLuckyDraw,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.06f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.06f, 1f));
        buttonPulseAnimator.setDuration(1200);
        buttonPulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        buttonPulseAnimator.setInterpolator(new OvershootInterpolator());
        buttonPulseAnimator.start();
    }

    /** Dong dialog uu dai ma chua mua inapp thi cho xem quang cao roi moi dong man. */
    @Override
    public void onDiscountDialogDismissed() {
        if (new MyPreference(this).isPremiumMember() || !MaticAd.getInstance().isAvailableInter()) {
            finish();
            return;
        }
        MaticAd.getInstance().showPriorityInterstitialAd(this, new MaticAdCallBack() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                LuckyDrawActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        if (buttonPulseAnimator != null) {
            buttonPulseAnimator.cancel();
        }
        super.onDestroy();
    }
}
