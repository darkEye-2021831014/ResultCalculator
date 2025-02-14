package com.example.cgpa

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ItemDetailsFregment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemDetailsFregment : BottomSheetDialogFragment() {
    private val viewModel by activityViewModels<SharedViewModel>()

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
        val view = inflater.inflate(R.layout.fragment_item_details_fregment, container, false)

        val delete = view.findViewById<Button>(R.id.delete);

        val name = view.findViewById<TextView>(R.id.itemName);
        val type = view.findViewById<TextView>(R.id.itemType);
        val amount = view.findViewById<TextView>(R.id.itemAmount);
        val date = view.findViewById<TextView>(R.id.itemDate);
        val note = view.findViewById<TextView>(R.id.itemNote);

        val noteView = view.findViewById<LinearLayout>(R.id.noteView);


        viewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            delete.setOnClickListener{
                Helper.showConfirmationDialog(requireContext(),"Are you sure you want to delete?"){
                    confirm->
                    if(confirm)
                    {
                        viewModel.removeData(item)
                        closeFragment()
                    }
                }
            }

            name.text = item.name
            name.setCompoundDrawablesWithIntrinsicBounds(Helper.loadIcon(item.icon,requireContext()), null, null, null)
            type.text = if(item.isExpense) "Expense" else "Income"
            amount.text = abs(item.amount).toString();
            date.text = "${item.monthName} ${item.date}, ${item.year}"
            if(item.note == null)
                noteView.visibility = View.GONE;
            else {
                noteView.visibility = View.VISIBLE
                note.text = item.note;
            }


        }



        return view
    }


    private fun closeFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val bottomSheetFragment = fragmentManager.findFragmentByTag("ItemDetailsFregment")
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
