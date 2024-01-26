package mint.woman.female.workout.common.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.ItemTouchHelperViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExerciseViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
    @BindView(R.id.img_exercise)
    ImageView mDemoExercise;
    @BindView(R.id.drag_handle)
    ImageView mDragHandle;
    @BindView(R.id.img_delete)
    ImageView mDelete;
    @BindView(R.id.img_replace)
    View imgReplace;
    @BindView(R.id.tv_exercise_name)
    TextView mExerciseName;
    @BindView(R.id.txt_exercise_time)
    TextView mReps;
    @BindView(R.id.container)
    View container;

    ExerciseViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onItemSelected() {
        itemView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onItemClear() {
        itemView.setBackgroundColor(0);
    }

}
