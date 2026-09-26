package mint.woman.female.workout.custom;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.RecyclerView;

public class CustomLayoutParams extends RecyclerView.LayoutParams {
    public CustomLayoutParams(Context context) {
        super(getItemWidth(context), RecyclerView.LayoutParams.MATCH_PARENT);
    }

    private static int getItemWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Calculate 80% of the screen width
        return (int) (screenWidth * 0.8);
    }
}