package mint.woman.female.workout.activity.ui.weekgoal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import mint.woman.female.workout.R;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    List<String> dayNames;
    List<String> dayOfMonths;
    boolean[] status ;

    Context mContext;
    int now = -1;

    IDayClickListener dayClick;
    public DayAdapter(List<String> dayNames, List<String> dayOfMonths, IDayClickListener iDayClick) {
        this.dayNames = dayNames;
        this.dayOfMonths = dayOfMonths;
        this.status = new boolean[10];
        this.dayClick = iDayClick;
    }

    public void setStatus(boolean[] status) {
        this.status = status;
        notifyDataSetChanged();
    }

    public void setNow(int position) {
        this.now = position;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_goal_item_layout, parent, false);
        mContext = parent.getContext();
        return new DayViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.mDayName.setText(dayNames.get(position));
        holder.mDayOfMonth.setText(dayOfMonths.get(position));
        if (position == now){
            holder.mDayOfMonth.setBackground(mContext.getDrawable(R.drawable.background_text_circle_now));
        }else{
            holder.mDayOfMonth.setBackground(mContext.getDrawable(R.drawable.background_text_circle));
        }
        if ( status[position]) {
            holder.mDayOfMonth.setText("");
            holder.mDayOfMonth.setEnabled(false);
        } else {
            holder.mDayOfMonth.setEnabled(true);
        }
        holder.itemView.setOnClickListener(v->dayClick.goDay());

//        if (position == 5) {
//            holder.mDayOfMonth.setEnabled(true);
//            holder.mDayOfMonth.setBackground(mContext.getDrawable(R.drawable.background_text_circle_now));
//        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    class DayViewHolder extends RecyclerView.ViewHolder {

        TextView mDayName, mDayOfMonth;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            mDayName = itemView.findViewById(R.id.txt_day_name);
            mDayOfMonth = itemView.findViewById(R.id.txt_day_of_month);
        }
    }
}
