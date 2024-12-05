package mint.woman.female.workout.activity.ui.boarding;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.FragmentBoardingBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BoardingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoardingFragment extends Fragment {
    FragmentBoardingBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    private static final String ARG_IMAGE = "image";

    // TODO: Rename and change types of parameters
    private String title;
    private String content;
    private String imageIntro;



    public BoardingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param content Parameter 2.
     * @param imgIntro Parameter 3.
     * @return A new instance of fragment BoardingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BoardingFragment newInstance(String title, String content, String imgIntro) {
        BoardingFragment fragment = new BoardingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putString(ARG_IMAGE,imgIntro);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            content = getArguments().getString(ARG_CONTENT);
            imageIntro = getArguments().getString(ARG_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBoardingBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.txtTitleOnboard.setText(title);
        binding.txtContentOnboard.setText(content);
        Glide.with(requireContext()).load(Uri.parse("file:///android_asset/onboards/" + imageIntro)).into(binding.imgOnboard);
    }
}