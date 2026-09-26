package mint.woman.female.workout.common.adapter;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;


public class HistoryViewHolder extends RecyclerView.ViewHolder {
    TextView mTimes, mDurations, mPlanName, mCalories;
    ImageView mStatus, mIcon;

    HistoryViewHolder(View itemView) {
        super(itemView);
        mTimes = itemView.findViewById(R.id.txt_time);
        mDurations = itemView.findViewById(R.id.txt_duration);
        mPlanName = itemView.findViewById(R.id.txt_program_name);
        mCalories = itemView.findViewById(R.id.txt_calories);
        mStatus = itemView.findViewById(R.id.img_history_status);
        mIcon = itemView.findViewById(R.id.img_history_icon);
    }
}