package mint.woman.female.workout.common.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;

public class HistoryTitleViewHolder extends RecyclerView.ViewHolder {
    TextView mDayName,mTotalTime,mTotalCalories,mTotalWorkouts;
    public HistoryTitleViewHolder(@NonNull View itemView) {
        super(itemView);
        mDayName  = itemView.findViewById(R.id.txt_day_name);
        mTotalTime = itemView.findViewById(R.id.txt_time);
        mTotalCalories = itemView.findViewById(R.id.txt_calories);
        mTotalWorkouts = itemView.findViewById(R.id.txt_total_workout);
    }
}
