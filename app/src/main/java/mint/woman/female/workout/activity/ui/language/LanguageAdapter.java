package mint.woman.female.workout.activity.ui.language;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    private Context context;
    private List<Language> languages;
    private int choicePosition = -1;
    IChangeLanguage iChangeLanguage;


    public LanguageAdapter(Context context, List<Language> languages, IChangeLanguage iChangeLanguage) {
        this.context = context;
        this.languages = languages;
        this.iChangeLanguage = iChangeLanguage;
    }

    public void setDefault(String lang) {
        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getCode().contains(lang)) {
                choicePosition = i;
            }
        }
        notifyDataSetChanged();
    }

    public String getLanguageCode() {
        if (choicePosition != -1 && choicePosition < languages.size()) {
            return languages.get(choicePosition).getCode();
        }
        return "";
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Language language = languages.get(position);

        holder.tvNameLanguage.setText(language.getName());
        holder.imgIconLanguage.setImageResource(language.getIdIcon());
        if (position == choicePosition) {
            holder.imgChooseLanguage.setImageResource(R.drawable.ic_selected);
        } else {
            holder.imgChooseLanguage.setImageResource(R.drawable.ic_unselected);
        }
        holder.itemView.setOnClickListener(v -> {
            if (position != choicePosition) {
                choicePosition = position;
                iChangeLanguage.onChangeLanguage(language.getCode());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameLanguage;
        private ImageView imgIconLanguage;
        private ImageView imgChooseLanguage;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameLanguage = itemView.findViewById(R.id.tvNameLanguage);
            imgIconLanguage = itemView.findViewById(R.id.imgIconLanguage);
            imgChooseLanguage = itemView.findViewById(R.id.imgChooseLanguage);
        }
    }

    public interface IChangeLanguage {
        void onChangeLanguage(String code);
    }
}
