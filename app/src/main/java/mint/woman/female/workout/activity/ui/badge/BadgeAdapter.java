package mint.woman.female.workout.activity.ui.badge;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.badge.BadgeBoard;
import mint.woman.female.workout.badge.BadgeFormatter;
import mint.woman.female.workout.badge.BadgeIconBinder;
import mint.woman.female.workout.badge.BadgeProgress;
import mint.woman.female.workout.badge.BadgeSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sach huy hieu theo nhom: tieu de nhom chiem het chieu ngang, huy hieu xep thanh luoi.
 */
public class BadgeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_BADGE = 1;

    public interface OnBadgeClickListener {
        void onBadgeClick(BadgeProgress badge);
    }

    /** Mot dong trong danh sach: hoac tieu de nhom, hoac mot huy hieu. */
    private static class Row {
        final int type;
        final BadgeSection section;
        final BadgeProgress badge;

        Row(BadgeSection section) {
            this.type = TYPE_SECTION;
            this.section = section;
            this.badge = null;
        }

        Row(BadgeProgress badge) {
            this.type = TYPE_BADGE;
            this.section = null;
            this.badge = badge;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnBadgeClickListener listener;

    public BadgeAdapter(OnBadgeClickListener listener) {
        this.listener = listener;
    }

    public void submit(BadgeBoard board) {
        rows.clear();
        for (BadgeSection section : board.getSections()) {
            rows.add(new Row(section));
            for (BadgeProgress badge : section.getBadges()) {
                rows.add(new Row(badge));
            }
        }
        notifyDataSetChanged();
    }

    /** Gan vao GridLayoutManager de tieu de nhom chiem tron mot hang. */
    public GridLayoutManager.SpanSizeLookup spanSizeLookup(int spanCount) {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getItemViewType(position) == TYPE_SECTION ? spanCount : 1;
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SECTION) {
            return new SectionHolder(inflater.inflate(R.layout.item_badge_section, parent, false));
        }
        return new BadgeHolder(inflater.inflate(R.layout.item_badge, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof SectionHolder) {
            ((SectionHolder) holder).bind(row.section);
        } else {
            ((BadgeHolder) holder).bind(row.badge, listener);
        }
    }

    static class SectionHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final TextView progress;

        SectionHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_section_title);
            subtitle = itemView.findViewById(R.id.tv_section_subtitle);
            progress = itemView.findViewById(R.id.tv_section_progress);
        }

        void bind(BadgeSection section) {
            title.setText(section.getTitle());
            progress.setText(itemView.getContext().getString(
                    R.string.badge_section_progress,
                    section.getUnlockedCount(),
                    section.getBadges().size()
            ));

            if (TextUtils.isEmpty(section.getSubtitle())) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(section.getSubtitle());
            }
        }
    }

    static class BadgeHolder extends RecyclerView.ViewHolder {
        private final ImageView medallion;
        private final ImageView lock;
        private final TextView title;
        private final TextView progressText;
        private final ProgressBar progressBar;

        BadgeHolder(@NonNull View itemView) {
            super(itemView);
            medallion = itemView.findViewById(R.id.img_badge);
            lock = itemView.findViewById(R.id.img_badge_lock);
            title = itemView.findViewById(R.id.tv_badge_title);
            progressText = itemView.findViewById(R.id.tv_badge_progress);
            progressBar = itemView.findViewById(R.id.progress_badge);
        }

        void bind(BadgeProgress badge, OnBadgeClickListener listener) {
            BadgeIconBinder.bind(badge, medallion, lock);
            title.setText(badge.getTitle());

            if (badge.isUnlocked()) {
                progressText.setText(R.string.badge_earned);
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                progressText.setText(BadgeFormatter.progress(itemView.getContext(), badge));
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(badge.getPercent());
            }

            progressBar.setProgressTintList(ContextCompat.getColorStateList(
                    itemView.getContext(), badge.getTier().colorRes));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBadgeClick(badge);
            });
        }
    }
}
