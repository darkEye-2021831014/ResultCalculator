package com.example.cgpa

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
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

        val dailyReport = view.findViewById<Button>(R.id.dailyReport)
        val monthlyReport = view.findViewById<Button>(R.id.monthlyReport)
        val yearlyReport = view.findViewById<Button>(R.id.yearlyReport)


        val datePicker = view.findViewById<NumberPicker>(R.id.datePicker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)
        val selectedDateText = view.findViewById<TextView>(R.id.selectDate)

        //date Picker(1 - 31)
        var currentDay = Calendar.getInstance().get(Calendar.DATE)
        datePicker.minValue = 1
        datePicker.maxValue = 31
        datePicker.value = currentDay

        // Month Picker (1 - 12)
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        var currentMonth = Calendar.getInstance().get(Calendar.MONTH)+1
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = months
        monthPicker.value = currentMonth

        // Year Picker (Range: 2000 - 2030)
        var currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 2000
        yearPicker.maxValue = 2030
        yearPicker.value = currentYear


        selectedDateText.text = "$currentDay / ${months[currentMonth-1]} / $currentYear"

        var buttonDailyText= "DAILY REPORT\n$currentDay ${months[currentMonth-1]}"
        var buttonMonthlyText= "MONTHLY REPORT\n${months[currentMonth-1]} $currentYear"
        var buttonYearlyText= "YEARLY REPORT\n$currentYear"
        var spannableDaily = SpannableString(buttonDailyText)
        var spannableMonthly = SpannableString(buttonMonthlyText)
        var spannableYearly = SpannableString(buttonYearlyText)


        spanText(spannableDaily,"DAILY REPORT","$currentDay ${months[currentMonth-1]}",buttonDailyText,dailyReport)
        spanText(spannableMonthly,"MONTHLY REPORT","${months[currentMonth-1]} $currentYear",buttonMonthlyText,monthlyReport)
        spanText(spannableYearly,"YEARLY REPORT","$currentYear",buttonYearlyText,yearlyReport)

        var date = CalenderDate(currentDay,currentMonth,months[currentMonth-1],currentYear)



        // Update text when selection changes
        val updateDateText = {
            currentDay = datePicker.value
            currentMonth = monthPicker.value
            val selectedMonth = months[currentMonth - 1] // Adjust 1-based index
            currentYear = yearPicker.value
            selectedDateText.text = "$currentDay / $selectedMonth / $currentYear"

            date = CalenderDate(currentDay,currentMonth,months[currentMonth-1],currentYear)

            //button text
            buttonDailyText= "DAILY REPORT\n$currentDay $selectedMonth"
            buttonMonthlyText= "MONTHLY REPORT\n$selectedMonth $currentYear"
            buttonYearlyText= "YEARLY REPORT\n$currentYear"
            spannableDaily = SpannableString(buttonDailyText)
            spannableMonthly = SpannableString(buttonMonthlyText)
            spannableYearly = SpannableString(buttonYearlyText)

            spanText(spannableDaily,"DAILY REPORT","$currentDay $selectedMonth",buttonDailyText,dailyReport)
            spanText(spannableMonthly,"MONTHLY REPORT","$selectedMonth $currentYear",buttonMonthlyText,monthlyReport)
            spanText(spannableYearly,"YEARLY REPORT","$currentYear",buttonYearlyText,yearlyReport)
        }

        datePicker.setOnValueChangedListener { _, _, _ -> updateDateText() }
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDateText() }
        yearPicker.setOnValueChangedListener { _, _, _ -> updateDateText() }



        //generate report buttons
        val report = CreateReport(requireContext(),requireActivity(),Format.DAILY_REPORT,date,viewModel)

        dailyReport.setOnClickListener{
            report.generateReport(Format.DAILY_REPORT,date)
        }
        monthlyReport.setOnClickListener {
            report.generateReport(Format.MONTHLY_REPORT,date)
        }
        yearlyReport.setOnClickListener {
            report.generateReport(Format.YEARLY_REPORT,date)
        }




        return view
    }



    fun spanText(spannable: Spannable,textReport:String,textDate:String,fullText:String,button:Button)
    {
        val cyan = Utility.getColor(requireContext(),R.color.cyan)
        val orange = Utility.getColor(requireContext(),R.color.orange)

        Utility.textColor(spannable,textReport,cyan,fullText)
        Utility.textColor(spannable,textDate,orange,fullText)
        button.text=spannable
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

