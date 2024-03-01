package mint.woman.female.workout.activity.ui.diet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.MyPreference;

public class DietAdapter extends RecyclerView.Adapter<DietAdapter.DietViewHolder> {
    MyPreference myPreference;
    Context mContext;
    IDietClick dietClick;

    public DietAdapter(IDietClick dietClick) {
        this.dietClick = dietClick;
    }

    @NonNull
    @Override
    public DietAdapter.DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diet_item_layout, null);
        return new DietViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DietAdapter.DietViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mDay.setText(mContext.getString(R.string.txt_day) + " " + (position + 1));
        myPreference = new MyPreference(mContext);
        if (myPreference.isDietDone(position + 1)) {
            holder.mIconDone.setVisibility(View.VISIBLE);
          //  holder.bg.setBackgroundResource(R.drawable.background_diet_done);
        } else {
            holder.mIconDone.setVisibility(View.INVISIBLE);
         //   holder.bg.setBackgroundResource(R.drawable.background_diet_none);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dietClick!= null){
                    dietClick.goDay(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    class DietViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_diet_day)
        TextView mDay;
        @BindView(R.id.rl_diet)
        View bg;
        @BindView(R.id.img_diet_done)
        ImageView mIconDone;

        DietViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
    public interface IDietClick{
        void goDay(int day);
    }
}