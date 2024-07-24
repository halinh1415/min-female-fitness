package mint.woman.female.workout.common.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.R;
import mint.woman.female.workout.model.HistoryItem;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.retrofit.ApiUtils;
import mint.woman.female.workout.utils.FitnessConstUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class HistorySection extends Section {
    private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    List<HistoryItem> mListWorkout;
    String title;
    ClickListener clickListener;
    Context mContext;

    public HistorySection(Context context,String title, List<HistoryItem> list,ClickListener clickListener) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.history_item_layout)
                .headerResourceId(R.layout.history_day_item)
                .build());
        mListWorkout = list;
        this.title = title;
        mContext =context;
        this.clickListener = clickListener;
    }

    @Override
    public int getContentItemsTotal() {
        return mListWorkout.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new HistoryViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HistoryTitleViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        final HistoryTitleViewHolder historyTitleViewHolder = (HistoryTitleViewHolder) holder;
        historyTitleViewHolder.mDayName.setText(title);
        historyTitleViewHolder.mTotalWorkouts.setText(""+mListWorkout.size()+" workouts");
        int calories = 0, time = 0;
        List<CalendarDay> calendarDayList = new ArrayList<>();
        for (HistoryItem h : mListWorkout) {
            calories += h.getCalories();
            time += h.getRealDuration();
            Calendar today = Calendar.getInstance();
            today.setTimeInMillis(h.getStartTime());
            calendarDayList.add(FitnessConstUtils.convertFromCalendar(today));
        }

        int minutes = (time) / 60;
        int seconds = time % 60;

        historyTitleViewHolder.mTotalCalories.setText(""+calories+" kcal");
        historyTitleViewHolder.mTotalTime.setText(String.format("%02d:%02d", minutes, seconds));

    }


    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        HistoryViewHolder holder = (HistoryViewHolder) viewHolder;
        HistoryItem historyItem = mListWorkout.get(position);
        if (historyItem != null) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.transform(new CenterCrop(), new RoundedCorners(16));
            holder.mTimes.setText(mFormat.format(historyItem.getStartTime()));
            int duration = historyItem.getRealDuration();
            int minutes = (duration) / 60;
            int seconds = (duration) % 60;
            holder.mPlanName.setText(historyItem.getName());
            String timeString = String.format("%02d:%02d", minutes, seconds);
            holder.mDurations.setText(timeString);
            holder.mCalories.setText(String.format("%d kcal", historyItem.getCalories()));
            if (historyItem.isPassed()) {
                holder.mStatus.setColorFilter(Color.parseColor("#0790FD"));
            } else {
                holder.mStatus.setColorFilter(Color.GRAY);
            }
            String baseUrl = "file:///android_asset/images/";
            ProgramObject programObject = FitnessApplication.getApplication().getProgram(historyItem.getProgramId());
            if (programObject!= null && programObject.getDiscover() >0){
                baseUrl = ApiUtils.BASE_URL_IMAGES;
            }
            Glide.with(mContext).load(Uri.parse(baseUrl + historyItem.getImage())).apply(requestOptions).into(holder.mIcon);
        }
        holder.itemView.setOnClickListener(v -> {
            if (clickListener!= null) clickListener.onItemRootViewClicked(historyItem);
        });

    }
    public interface ClickListener {
        void onItemRootViewClicked(HistoryItem historyItem);
    }
}
