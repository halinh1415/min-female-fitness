package mint.woman.female.workout.activity.ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.minfitness.decode.Decoder
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ExploreDetailActivity
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.workout.CategoryDetailActivity
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.model.CategoryObject
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.MyPreference

/** Màn Discover (tab Training) viết bằng Jetpack Compose, cùng dữ liệu với bản cũ. */
class ExploreFragment : Fragment(), DiscoverActions {

    private var sections by mutableStateOf<List<DiscoverSection>>(emptyList())
    private var isPremium by mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MinTheme { DiscoverScreen(sections, isPremium, this@ExploreFragment) } }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sections = buildSections()
    }

    override fun onResume() {
        super.onResume()
        isPremium = MyPreference(requireContext()).isPremiumMember
    }

    private fun buildSections(): List<DiscoverSection> {
        // Giống bản cũ: dữ liệu discover chỉ có khi giải mã thành công.
        if (!Decoder.isSuccess) return emptyList()
        val all = FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap
        fun programs(vararg ids: Int) = ids.toList().mapNotNull { all[it] }
        fun category(vararg ids: Int) = programs(*ids).takeIf { it.isNotEmpty() }?.let { CategoryObject(it) }

        return listOfNotNull(
            DiscoverSection(
                getString(R.string.explore_tag_hot),
                style = DiscoverStyle.HERO,
                categories = listOfNotNull(category(345, 346, 347))
            ),
            DiscoverSection(
                getString(R.string.random_workout),
                style = DiscoverStyle.CATEGORY_GRID,
                categories = listOfNotNull(category(351, 352, 353), category(348, 349, 350))
            ),
            DiscoverSection(
                getString(R.string.training_goal),
                style = DiscoverStyle.CATEGORY_GRID,
                categories = listOfNotNull(
                    category(354, 355, 356),
                    category(357, 358, 359),
                    category(360, 361, 362),
                    category(363, 365, 366)
                )
            ),
            DiscoverSection(
                getString(R.string.reach_goal_fast),
                getString(R.string.seven_min_get_fit_des),
                DiscoverStyle.CAROUSEL,
                programs(24, 41, 37, 32, 45, 969)
            ),
            DiscoverSection(
                getString(R.string.at_home),
                getString(R.string.upper_body_stretching_short_des),
                DiscoverStyle.CAROUSEL,
                programs(18, 19, 971, 16, 17, 959)
            ),
            DiscoverSection(
                getString(R.string.for_summer),
                style = DiscoverStyle.CAROUSEL,
                programs = programs(1110, 1140, 47, 39, 46, 30, 34, 38)
            ),
            DiscoverSection(
                getString(R.string.tip_pro_instruction_3),
                style = DiscoverStyle.LIST,
                programs = programs(10, 11)
            ),
            DiscoverSection(
                getString(R.string.pain_relief),
                style = DiscoverStyle.LIST,
                programs = programs(883, 200, 201, 1151, 1150)
            )
        ).filter { it.programs.isNotEmpty() || it.categories.isNotEmpty() }
    }

    override fun onSearch() {
        startActivity(Intent(requireContext(), SearchActivity::class.java))
    }

    override fun onProgramClick(program: ProgramObject) {
        val intent = when (program.type) {
            1 -> Intent(requireActivity(), ChallengeActivity::class.java)
                .putExtras(Bundle().apply { putInt("PROGRAM_ID", program.id) })
            2 -> Intent(requireActivity(), PreviewActivity::class.java)
                .putExtras(Bundle().apply {
                    putInt("PROGRAM_ID", program.id)
                    putInt("DAY", 0)
                })
            else -> null
        }
        intent?.let(::startActivity)
    }

    override fun onCategoryClick(category: CategoryObject) {
        FitnessApplication.getApplication().storageCommon.category.postValue(category)
        startActivity(Intent(requireContext(), CategoryDetailActivity::class.java))
    }

    override fun onSeeMore(section: DiscoverSection) {
        startActivity(ExploreDetailActivity.intent(requireContext(), section.title, section.programs))
    }
}
