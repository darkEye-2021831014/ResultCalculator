package com.example.cgpa

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SetBudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetBudgetFragment : BottomSheetDialogFragment() {
    private val viewModel by activityViewModels<SharedViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), theme).apply {
//            // We prevent the BottomSheetDialog from altering the system UI colors
//            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            setCanceledOnTouchOutside(false) // Prevent dismiss by outside touch
//        }
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_set_budget, container, false)

        val addBudget = view.findViewById<Button>(R.id.addBudget)
        val recyclerView = view.findViewById<RecyclerView>(R.id.budgetContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

//        val outsideKeyboard = view.findViewById<LinearLayout>(R.id.outsideKeyboard)

        val dateManager = BudgetManager(viewModel, requireContext(),recyclerView,view)
        dateManager.setMonthRange(monthPicker)
        dateManager.setYearRange(yearPicker, 2000, 2050)

        monthPicker.setOnValueChangedListener { _, _, _ ->
            dateManager.update(
                monthPicker,
                yearPicker
            )
        }
        yearPicker.setOnValueChangedListener { _, _, _ ->
            dateManager.update(
                monthPicker,
                yearPicker
            )
        }




//        viewModel.clearBudgetData()
        return view
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.back).setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        // Set system bar colors to your desired color (it will not be overwritten now)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)

        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            // Ensure full-screen height
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.peekHeight = 0 // Start at the very bottom
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED // Ensure it starts collapsed (not hidden)

            // Smooth animation from bottom to full screen
            val animator = ValueAnimator.ofInt(0, resources.displayMetrics.heightPixels)
            animator.duration = 1000 // Slow animation (1 second)
            animator.addUpdateListener { animation ->
                val height = animation.animatedValue as Int
                behavior.peekHeight = height
            }
            animator.start()

            // Expand fully after animation
            it.postDelayed({
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }, 0)
        }
    }

    override fun onStop() {
        super.onStop()

        // Ensure system bar colors remain as you originally set them in MainActivity
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
    }

    override fun getTheme(): Int {
        return com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog
    }
}
