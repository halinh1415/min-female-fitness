package mint.woman.female.workout.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {
    private int[] days = new int[]{R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6, R.id.day_7};
    private int[] links = new int[]{R.id.link_1, R.id.link_2, R.id.link_3, R.id.link_4, R.id.link_5, R.id.link_6, R.id.link_7};
    IDayClickListener dayClickListener;
    Context mContext;
    int progress =5,totalDay = 28;

    public ChallengeAdapter(IDayClickListener dayClickListener,int totalDay) {
        this.dayClickListener = dayClickListener;
        this.totalDay = totalDay;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_item_layout,parent,false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        holder.mWeekName.setText(mContext.getString(R.string.txt_week_count)+" "+(position+1));
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        if (position * 7 <= progress + 1) {
            holder.mIconWeek.setImageResource(R.drawable.ic_week_doing);
            holder.wLink.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            holder.mIconWeek.setImageResource(R.drawable.ic_week_none);
            holder.wLink.setBackgroundColor(mContext.getResources().getColor(R.color.Gray));
        }
        //if ((position+1) == sizePlan/7) holder.wLink.setVisibility(View.INVISIBLE);
        for (int i = 0; i < days.length && i < links.length; i++) {
            int indexDay = position * 7 + (i + 1);
            TextView day = holder.itemView.findViewById(days[i]);
            View link = holder.itemView.findViewById(links[i]);
            if (indexDay < progress + 1) {
                day.setText("");
                day.setBackgroundResource(R.drawable.img_day_done);
                link.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            } else if (indexDay == progress + 1) {
                day.setBackgroundResource(R.drawable.img_day_doing);
                link.setBackgroundColor(mContext.getResources().getColor(R.color.Gray));
            } else {
                day.setBackgroundResource(R.drawable.img_day_none);
                link.setBackgroundColor(mContext.getResources().getColor(R.color.Gray));
            }
            if (indexDay > totalDay) {
                day.setVisibility(View.INVISIBLE);
                link.setVisibility(View.INVISIBLE);
            }
            if (indexDay == totalDay) {
                if (totalDay % 7 != 0) {
                    link.setVisibility(View.INVISIBLE);
                    holder.mCup.setVisibility(View.INVISIBLE);
                } else {
                }
                holder.wLink.setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return totalDay/7;
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder{
        TextView mWeekName;
        ImageView mCup;
        ImageView mIconWeek;
        View wLink;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);

            wLink = itemView.findViewById(R.id.w_link);
            mIconWeek = itemView.findViewById(R.id.img_icon_week);
            mCup = itemView.findViewById(R.id.img_cup);
            mWeekName = itemView.findViewById(R.id.txt_week_name);
            itemView.findViewById(R.id.day_1).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_2).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_3).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_4).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_5).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_6).setOnClickListener(this::onClick);
            itemView.findViewById(R.id.day_7).setOnClickListener(this::onClick);
        }


        void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            int indexDay = 0;
            switch (view.getId()) {
                case R.id.day_1:
                    indexDay = getAdapterPosition() * 7;
                    break;
                case R.id.day_2:
                    indexDay = getAdapterPosition() * 7 + 1;
                    break;
                case R.id.day_3:
                    indexDay = getAdapterPosition() * 7 + 2;
                    break;
                case R.id.day_4:
                    indexDay = getAdapterPosition() * 7 + 3;
                    break;
                case R.id.day_5:
                    indexDay = getAdapterPosition() * 7 + 4;
                    break;
                case R.id.day_6:
                    indexDay = getAdapterPosition() * 7 + 5;
                    break;
                case R.id.day_7:
                    indexDay = getAdapterPosition() * 7 + 6;
                    break;
            }
            if (dayClickListener != null) {
                dayClickListener.goDay(indexDay);
            }
        }
    }
}
