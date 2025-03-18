package com.example.cgpa

import android.animation.ValueAnimator
import android.app.AlertDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itextpdf.kernel.colors.Color
import java.util.Calendar
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GenerateReport.newInstance] factory method to
 * create an instance of this fragment.
 */



class GenerateReport : BottomSheetDialogFragment() {
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
        val view = inflater.inflate(R.layout.fragment_generate_report, container, false)


        val datePicker = view.findViewById<NumberPicker>(R.id.datePicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

        //date Picker(1 - 31)
        val currentDay = Calendar.getInstance().get(Calendar.DATE)
        datePicker.minValue = 1
        datePicker.maxValue = 31
        datePicker.value = currentDay

        // Month Picker (1 - 12)
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = months

        // Year Picker (Range: 2000 - 2030)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 2000
        yearPicker.maxValue = 2030
        yearPicker.value = currentYear

        // Update text when selection changes
        val updateDateText = {
            val selectedDate = datePicker.value
            val selectedMonth = months[monthPicker.value - 1] // Adjust 1-based index
            val selectedYear = yearPicker.value
            Utility.showToast(requireContext(),"Selected: $selectedDate $selectedMonth $selectedYear")
//            selectedDateText.text = "Selected: $selectedMonth $selectedYear"
        }

        datePicker.setOnValueChangedListener { _, _, _ -> updateDateText() }
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDateText() }
        yearPicker.setOnValueChangedListener { _, _, _ -> updateDateText() }


        return view
    }













    private fun closeFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val bottomSheetFragment = fragmentManager.findFragmentByTag("GenerateFragment")
        if (bottomSheetFragment is BottomSheetDialogFragment) {
            bottomSheetFragment.dismiss()
            fragmentManager.beginTransaction().remove(bottomSheetFragment).commitAllowingStateLoss()
        }
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

