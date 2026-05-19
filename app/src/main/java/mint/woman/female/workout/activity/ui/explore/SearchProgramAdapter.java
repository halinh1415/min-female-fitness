package mint.woman.female.workout.activity.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ProgramObject;

import java.util.List;

public class SearchProgramAdapter extends RecyclerView.Adapter<SearchProgramAdapter.ProgramViewHolder> {

    public interface OnProgramClickListener {
        void onProgramClick(ProgramObject program);
    }

    private final List<ProgramObject> items;
    private final OnProgramClickListener listener;

    public SearchProgramAdapter(List<ProgramObject> items, OnProgramClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        ProgramObject program = items.get(position);

        holder.tvProgramName.setText(program.getName());
        holder.tvProgramFocus.setText(program.getFocus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProgramClick(program);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProgramViewHolder extends RecyclerView.ViewHolder {
        TextView tvProgramName;
        TextView tvProgramFocus;

        ProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProgramName = itemView.findViewById(R.id.tvProgramName);
            tvProgramFocus = itemView.findViewById(R.id.tvProgramFocus);
        }
    }
}