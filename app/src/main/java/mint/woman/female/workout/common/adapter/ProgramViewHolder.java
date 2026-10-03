package mint.woman.female.workout.common.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;

final class ProgramViewHolder extends RecyclerView.ViewHolder {
    ImageView mBackground,mWorkout;
    View mNew;
    AppCompatTextView mProgramName;
    TextView mSubTitle;
    TextView mStartBtn;
    View mContainer;

    ProgramViewHolder(View itemView) {
        super(itemView);
        mBackground = itemView.findViewById(R.id.iv_workout_bg);
        mStartBtn = itemView.findViewById(R.id.btn_start);
        mWorkout = itemView.findViewById(R.id.iv_workout);
        mNew = itemView.findViewById(R.id.ll_workout_new);
        mProgramName = itemView.findViewById(R.id.tv_title);
        mSubTitle = itemView.findViewById(R.id.tv_sub_title);
        mContainer = itemView.findViewById(R.id.container);
    }

}