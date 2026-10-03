package mint.woman.female.workout.activity.ui.diet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.utils.MyPreference

/** Tab Recipes: kế hoạch ăn 30 ngày, giao diện Jetpack Compose. */
class DietFragment : Fragment(), DietActions {

    private val state = DietUiState()
    private lateinit var pref: MyPreference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MinTheme { DietScreen(state, this@DietFragment) } }
        }

    override fun onResume() {
        super.onResume()
        pref = MyPreference(requireContext())
        refresh()
        if (state.progress == DIET_DAYS) showFullDiet()
    }

    private fun refresh() {
        state.progress = pref.progressDiet
        state.doneDays = (1..DIET_DAYS).filter { pref.isDietDone(it) }.toSet()
    }

    private fun showFullDiet() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.txt_finish))
            .setMessage(getString(R.string.txt_are_you_want_to_reset))
            .setPositiveButton(getString(R.string.OK)) { _, _ ->
                pref.resetDietProgress()
                refresh()
            }
            .setNegativeButton(getString(R.string.txt_cancel), null)
            .show()
    }

    override fun onOpenDay(day: Int) {
        startActivity(
            Intent(requireContext(), MealDetailActivity::class.java)
                .putExtras(Bundle().apply { putInt("DAY", day) })
        )
    }

    override fun onShoppingList() {
        startActivity(Intent(requireContext(), ShoppingListActivity::class.java))
    }
}
