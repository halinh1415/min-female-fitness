package mint.woman.female.workout.custom

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.custom.weight.WeightEntryActions
import mint.woman.female.workout.custom.weight.WeightEntrySheet
import mint.woman.female.workout.custom.weight.WeightEntryTheme
import mint.woman.female.workout.custom.weight.WeightEntryViewModel
import mint.woman.female.workout.fragment.WeightViewModel
import mint.woman.female.workout.utils.MyPreference
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Bottom sheet nhập số đo mới. Tên lớp được giữ nguyên để các màn BMI và Report không phải đổi
 * cách mở dialog.
 */
class DialogWeight : BottomSheetDialogFragment() {

    private val historyViewModel: HistoryViewModel by lazy {
        ViewModelProvider(requireActivity())[HistoryViewModel::class.java]
    }
    private val sharedWeightViewModel: WeightViewModel by lazy {
        ViewModelProvider(requireActivity())[WeightViewModel::class.java]
    }
    private val viewModel: WeightEntryViewModel by lazy {
        ViewModelProvider(this)[WeightEntryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WeightEntryTheme {
                val state by viewModel.state.collectAsState()
                WeightEntrySheet(
                    state = state,
                    gender = MyPreference.newInstance(requireContext()).getGender(),
                    actions = WeightEntryActions(
                        onWeightText = viewModel::setWeightText,
                        onWeightValue = viewModel::setWeightValue,
                        onStep = viewModel::stepWeight,
                        onUnitKg = viewModel::setUnitKg,
                        onToggleHeight = viewModel::toggleHeightExpanded,
                        onHeightCm = viewModel::setHeightCmText,
                        onHeightFeet = viewModel::setHeightFeetText,
                        onHeightInches = viewModel::setHeightInchesText,
                        onToggleWaist = viewModel::toggleWaistExpanded,
                        onWaistCm = viewModel::setWaistCmText,
                        onWaistFeet = viewModel::setWaistFeetText,
                        onWaistInches = viewModel::setWaistInchesText,
                        onDate = viewModel::setDate,
                        onSave = { viewModel.save(::onSaved) },
                        onCancel = ::dismiss,
                    ),
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

    override fun getTheme(): Int = R.style.WeightSheetDialogTheme

    private fun onSaved(result: WeightEntryViewModel.SaveResult) {
        if (!isAdded) return
        sharedWeightViewModel.setIsKg(result.isKg)
        if (result.isNewest) sharedWeightViewModel.setUser(result.user)
        historyViewModel.notifyWeightChanged()
        Toast.makeText(requireContext(), R.string.weight_saved, Toast.LENGTH_SHORT).show()
        dismiss()
    }
}
