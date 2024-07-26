package mint.woman.female.workout.common.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    ImageView mBackground,mWorkout;
    View mNew;
    AppCompatTextView mProgramName;
    TextView mSubTitle;
    TextView mProgressTxt;
    View mContainer;

    CategoryViewHolder(View itemView) {
        super(itemView);
        mBackground = itemView.findViewById(R.id.iv_workout_bg);
        mWorkout = itemView.findViewById(R.id.iv_workout);
        mNew = itemView.findViewById(R.id.ll_workout_new);
        mProgramName = itemView.findViewById(R.id.tv_title);
        mSubTitle = itemView.findViewById(R.id.tv_sub_title);
        mContainer = itemView.findViewById(R.id.container);
        mProgressTxt = itemView.findViewById(R.id.tv_day_left);
    }

}