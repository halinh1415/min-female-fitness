package mint.woman.female.workout.common.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.custom.ItemTouchHelperViewHolder;



public class ExerciseViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
    ImageView mDemoExercise;
    ImageView mDragHandle;
    ImageView mDelete;
    View imgReplace;
    TextView mExerciseName;
    TextView mReps;
    View container;

    ExerciseViewHolder(View itemView) {
        super(itemView);
       mDemoExercise = itemView.findViewById(R.id.img_exercise);
       mDragHandle = itemView.findViewById(R.id.drag_handle);
       mDelete = itemView.findViewById(R.id.img_delete);
       imgReplace = itemView.findViewById(R.id.img_replace);
       mExerciseName = itemView.findViewById(R.id.tv_exercise_name);
       mReps = itemView.findViewById(R.id.txt_exercise_time);
       container = itemView.findViewById(R.id.container);
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
