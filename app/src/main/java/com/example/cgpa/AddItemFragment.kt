package com.example.cgpa

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.viewpager2.adapter.FragmentStateAdapter

class AddItemFragment : BottomSheetDialogFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var expenseButton: Button
    private lateinit var incomeButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            // We prevent the BottomSheetDialog from altering the system UI colors
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setCanceledOnTouchOutside(false) // Prevent dismiss by outside touch
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_item, container, false)

        //sliding income expense
        viewPager = view.findViewById(R.id.viewPager)
        incomeButton = view.findViewById(R.id.addIncome)
        expenseButton = view.findViewById(R.id.addExpense)

        // Set up the ViewPager with the fragments
        val pagerAdapter = SlidingWindowPagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        // Set the default position to 0 (Window 1)
        viewPager.setCurrentItem(0, false)

        // Update button states based on the default position
        updateButtonStates(0)

        // Set up button click listeners
        expenseButton.setOnClickListener {
            viewPager.currentItem = 0
        }

        incomeButton.setOnClickListener {
            viewPager.currentItem = 1
        }

        // Update button states based on ViewPager position
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonStates(position)
            }
        })

        //handle back button press
        val close:Button = view.findViewById(R.id.close);
        return view
    }




    private fun updateButtonStates(position: Int) {
        expenseButton.isSelected = position == 0
        incomeButton.isSelected = position == 1
    }

    private inner class SlidingWindowPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AddExpenseFragment()
                1 -> AddIncomeFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.close).setOnClickListener {
            dismiss() // Only dismiss when cancel button is clicked
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
