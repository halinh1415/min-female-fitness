package mint.woman.female.workout.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.FitnessConstUtils;
import mint.woman.female.workout.utils.MyPreference;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private int[] mDayStatus = new int[]{R.drawable.ic_medal_1, R.drawable.ic_medal_2, R.drawable.ic_tea_cup};
    List<PlanObject> mPlanObjectList;
    Context mContext;
    ClickListener clickListener;
    private int progress;
//    MyPreference myPreference;

    public DayAdapter(List<PlanObject> planObjects, int progress, ClickListener clickListener) {
        this.mPlanObjectList = planObjects;
        this.clickListener = clickListener;
        this.progress = progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_item_layout, parent, false);
        return new DayViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        if (position == RecyclerView.NO_POSITION) return;
        PlanObject planObject = mPlanObjectList.get(position);
        holder.mDayName.setText(mContext.getString(R.string.txt_day) + " " + (position + 1));
        if (planObject.mActionList.size() > 0) {
            holder.mDayDuration.setVisibility(View.VISIBLE);
            holder.mDayCalories.setVisibility(View.VISIBLE);
            float cal = (planObject.getTotalTime() / 3600f) * FitnessConstUtils.CALORIES_CONST ;
            holder.mDayFocus.setText(""+planObject.mActionList.size()+" "+mContext.getString(R.string.txt_exercises));
            holder.mDayDuration.setText(""+(planObject.getTotalTime()/60)+" "+mContext.getString(R.string.minutes));
            holder.mDayCalories.setText("" + (int) cal + " cal");
            if (position < progress) {
                holder.mDayStatus.setImageResource(mDayStatus[1]);
            } else {
                holder.mDayStatus.setImageResource(mDayStatus[0]);
            }
        } else {
            holder.mDayStatus.setImageResource(mDayStatus[2]);
            holder.mDayDuration.setVisibility(View.GONE);
            holder.mDayCalories.setVisibility(View.GONE);
        }
        Drawable img = mContext.getResources().getDrawable(R.drawable.ic_baseline_done_24);
        img.setBounds(0, 0, 60, 60);
        if (position < progress) {
            holder.mDayName.setCompoundDrawables(null, null, img, null);
            holder.mDayName.setCompoundDrawablePadding(5);
        }
    }

    @Override
    public int getItemCount() {
        return mPlanObjectList.size();
    }

    public interface ClickListener {
        void onDayViewClicked(@NonNull PlanObject object, int position);
    }

    class DayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mDayName, mDayFocus, mDayDuration, mDayCalories;
        ImageView mDayStatus, mDayDetail;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            mDayName = itemView.findViewById(R.id.txt_day_name);
            mDayFocus = itemView.findViewById(R.id.txt_day_focus);
            mDayCalories = itemView.findViewById(R.id.txt_day_calories);
            mDayDuration = itemView.findViewById(R.id.txt_day_time);
            mDayDetail = itemView.findViewById(R.id.img_detail);
            mDayStatus = itemView.findViewById(R.id.img_day_status);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int rootPosition = getAdapterPosition();
            if (rootPosition == RecyclerView.NO_POSITION) {
                return;
            }
            PlanObject planObject = mPlanObjectList.get(rootPosition);
            if (clickListener != null) {
                clickListener.onDayViewClicked(planObject, rootPosition);
            }
        }
    }

}
