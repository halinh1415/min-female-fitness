package mint.woman.female.workout.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Section;

import java.util.List;


public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SECTION_HEADER = 0;
    private static final int VIEW_TYPE_HORIZONTAL_LIST = 1;

    private List<Section> itemList;
    ProgramClickListener clickListener;

    // Constructor and other necessary methods

    public SectionAdapter(List<Section> itemList,ProgramClickListener clickListener) {
        this.clickListener = clickListener;
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        Section item = itemList.get(position);

       return VIEW_TYPE_SECTION_HEADER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_SECTION_HEADER:
                // Inflate layout for section header and return SectionHeaderViewHolder
                View sectionHeaderView = inflater.inflate(R.layout.section_header_layout, parent, false);
                return new SectionHeaderViewHolder(sectionHeaderView,clickListener);

            default:
                return null; // Return an appropriate ViewHolder based on the viewType
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Section item = itemList.get(position);

        ((SectionHeaderViewHolder) holder).bind( item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView horizontalRecyclerView;
        private TextView sectionTextView;
        private ProgramAdapter programAdapter;

        public SectionHeaderViewHolder(@NonNull View itemView,ProgramClickListener clickListener) {
            super(itemView);
            // Initialize views for the section header
            horizontalRecyclerView = itemView.findViewById(R.id.horizontalRecyclerView);
            sectionTextView = itemView.findViewById(R.id.tv_section_title);

            // Initialize and set up the adapter for the horizontal list
            programAdapter = new ProgramAdapter(R.layout.item_child_recent_challenge,clickListener);
            horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            horizontalRecyclerView.setAdapter(programAdapter);
        }

        public void bind(Section section) {
            sectionTextView.setText(section.sectionName);
            programAdapter.setData(section.programObjectList,section.layout);
            programAdapter.notifyDataSetChanged();
        }
    }
}