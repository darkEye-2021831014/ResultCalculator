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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale


class BudgetCategoriesFragment : BottomSheetDialogFragment() {
    private val viewModel by activityViewModels<SharedViewModel>()
    private lateinit var keyboard:BudgetKeyboard


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            // We prevent the BottomSheetDialog from altering the system UI colors
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setCanceledOnTouchOutside(false) // Prevent dismiss by outside touch
        }
//        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme).apply {
//            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            setCanceledOnTouchOutside(false)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_budget_categories, container, false)

        keyboard = BudgetKeyboard(view,requireContext())
        initializeKeyboard()

        val items = arrayOf(
            Triple(R.id.food , R.id.foodBudget , R.id.foodView),
            Triple(R.id.drinks, R.id.drinksBudget,  R.id.drinksView),
            Triple(R.id.cloth , R.id.clothBudget , R.id.clothView),
            Triple(R.id.education , R.id.educationBudget , R.id.educationView),
            Triple(R.id.health , R.id.healthBudget  ,R.id.healthView),
            Triple(R.id.transport , R.id.transportBudget , R.id.transportView),
            Triple(R.id.phone , R.id.phoneBudget , R.id.phoneView),
            Triple(R.id.other , R.id.otherBudget , R.id.otherView)
        )

        val iconId = mapOf(
            (R.id.food) to R.drawable.food_record,
            (R.id.drinks) to R.drawable.drinks_records,
            (R.id.cloth) to R.drawable.cloth_records,
            (R.id.education) to R.drawable.education_records,
            (R.id.health) to R.drawable.healthcare_records,
            (R.id.transport) to R.drawable.transportation_records,
            (R.id.phone) to R.drawable.phone_records,
            (R.id.other) to R.drawable.other_records
        )



        for((layoutId,budgetId,itemId) in items){
            val itemView = view.findViewById<TextView>(itemId)
            val heading = itemView.text.toString()
            viewModel.budgetData.value?.let{
                it.forEach { have->
                    if(have.heading == heading){
                        view.findViewById<TextView>(budgetId).text = have.budget.toString()
                    }
                }
            }


            view.findViewById<LinearLayout>(layoutId).setOnClickListener {
                val expense = viewModel.monthlyCategoryData.value?.let{it[Triple(Utility.getMonth(), Utility.getYear(),heading)]?.expense?:0L}?:0L
                var item = BudgetItem(heading,
                    iconId[layoutId]?.let { it1 -> Helper.saveIcon(it1,requireContext()) },Helper.saveIcon(R.drawable.ic_right,requireContext()),0,expense,true)

                viewModel.budgetData.value?.let{
                    it.forEach { have->
                        if(have.heading == heading){
                            item = have
                        }
                    }
                }

                val curBudget = item.budget
                view.findViewById<TextView>(budgetId).text = if (curBudget!=0L) curBudget.toString() else "Edit"

                keyboard.setInput(curBudget)

                keyboard.setNoteText(
                    "Monthly Budget - $heading"
                )

                keyboard.showKeyboard()
                keyboard.okOperation(view.findViewById<TextView>(budgetId),viewModel,item)
            }
        }




        return view
    }







    private fun initializeKeyboard(){
        keyboard.hideKeyboard()
        keyboard.dateButtonText("CLEAR")
        keyboard.minusButtonText("")
        keyboard.plusButtonText("")
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
