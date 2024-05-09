package mint.woman.female.workout.custom;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import mint.woman.female.workout.R;


public class StarCheckedImageView extends AppCompatImageView {
    private boolean isChecked = false;
    private ObjectAnimator scaleXAnimator;
    private ObjectAnimator scaleYAnimator;

    public StarCheckedImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public StarCheckedImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarCheckedImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Create scale-up animators
        scaleXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1f);
        scaleYAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1f);
        scaleXAnimator.setDuration(500);
        scaleYAnimator.setDuration(500);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Start the scale-up animations when attached to window
        scaleXAnimator.start();
        scaleYAnimator.start();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        if (isChecked){
            setImageResource(R.drawable.ic_ratingbar_star_on);
        }else{
            setImageResource(R.drawable.ic_ratingbar_star_off);
        }
    }
}