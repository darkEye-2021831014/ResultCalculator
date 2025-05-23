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
 * Use the [BudgetModificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BudgetModificationFragment : BottomSheetDialogFragment() {
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
        val view = inflater.inflate(R.layout.fragment_set_budget, container, false)

        keyboard = BudgetKeyboard(view,requireContext())

        val addBudget = view.findViewById<Button>(R.id.addBudget)
        recyclerView = view.findViewById<RecyclerView>(R.id.budgetContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)

//        val outsideKeyboard = view.findViewById<LinearLayout>(R.id.outsideKeyboard)

        val budgetManager = BudgetManager(viewModel, requireContext())
        budgetManager.setMonthRange(monthPicker)
        budgetManager.setYearRange(yearPicker, 2000, 2050)
        budgetManager.monthlyBudget()

        monthPicker.setOnValueChangedListener { _, _, _ ->
            budgetManager.update(
                monthPicker,
                yearPicker
            )
        }
        yearPicker.setOnValueChangedListener { _, _, _ ->
            budgetManager.update(
                monthPicker,
                yearPicker
            )
        }



        setUpRecyclerView()


        //addBudget operation
        addBudget.setOnClickListener {
            Utility.bottomSheet(requireActivity(),BudgetCategoriesFragment(),"BudgetCategoriesFragment")
        }
        return view
    }




    private fun setUpRecyclerView(){
        initializeKeyboard()
        viewModel.budgetData.observe(viewLifecycleOwner){data->
            //assuming the monthly budget will always be the first item as it will be added first
            val adapter = BudgetAdapter(
                requireContext(),
                data,
                onClick = {
                    item->
                    keyboard.setInput(item.budget)
                    keyboard.setNoteText(
                        if (item.isCategory) "Monthly Budget - ${item.heading}" else "Monthly Budget"
                    )
                    keyboard.showKeyboard()
                    keyboard.okOperation(item,viewModel)
                },
                onLongClick = {
                    item->
                    if(item.isCategory){
//                        val list = viewModel.budgetData.value?: mutableListOf()
//                        val firstItem = viewModel.budgetData.value?.first()
//                        firstItem?.let{
//                            it.budget-=item.budget
//                            list[0] = it
//                        }
//                        viewModel.budgetData.value = list
                        Helper.showConfirmationDialog(requireContext(),"Are You Sure You Want TO Delete?"){
                            confirm->
                            if(confirm){
                                viewModel.budgetData.value?.first()?.let{
                                    it.budget-=item.budget
                                }
                                viewModel.removeItem(item,requireContext())
                            }
                        }

                    }
                }
            )
            keyboard.setAdapter(adapter)
            recyclerView.adapter = adapter
        }
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

            behavior.isDraggable = false
//            behavior.skipCollapsed = true
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

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
