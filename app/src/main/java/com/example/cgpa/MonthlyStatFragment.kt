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
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MonthlyStatFragment : BottomSheetDialogFragment() {
    private val viewModel by activityViewModels<SharedViewModel>()
    private lateinit var keyboard:BudgetKeyboard
    private lateinit var recyclerView: RecyclerView


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
        val view = inflater.inflate(R.layout.fragment_monthly_stat, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setUpRecyclerView(view)
        return view
    }

    private fun setUpRecyclerView(view:View){
        val items:MutableList<Any> = mutableListOf()
        var curYear = Utility.getYear()
        val minYear = 2000
        var totalExpense=0L
        var totalIncome=0L

        while (curYear >= minYear){
            items.add(curYear)
            var isAdded = false
            for (month in 1..12){
                var expense=0L
                var income = 0L
                viewModel.monthlyData.value?.let {
                    expense = it[month to curYear]?.expense?:0L
                    income = it[month to curYear]?.income?:0L
                }
                if(expense!=0L || income !=0L) {
                    items.add(Triple(Utility.getMonthNameOf(month), expense, income))
                    isAdded = true
                }
                totalIncome+=income
                totalExpense+=expense
            }
            if(!isAdded)
                items.removeLastOrNull()
            curYear--
        }

        recyclerView.adapter = MonthlyStatAdapter(
            items
        )

        view.findViewById<TextView>(R.id.totalExpense).text = "Expenses : ${Utility.formatedValue(totalExpense)}"
        view.findViewById<TextView>(R.id.totalIncome).text = "Income : ${Utility.formatedValue(totalIncome)}"
        view.findViewById<TextView>(R.id.totalBalance).text = Utility.formatedValue(totalIncome-totalExpense)
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
