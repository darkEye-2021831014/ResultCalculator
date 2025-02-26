package com.example.cgpa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExpenseCharts.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpenseCharts : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private  val viewModel: SharedViewModel by activityViewModels()

    private lateinit var viewPager: ViewPager2
    private lateinit var lastMonth: Button
    private lateinit var thisMonth: Button
    private lateinit var lastYear: Button
    private lateinit var thisYear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_expense_charts, container, false)

        //sliding income expense
        viewPager = view.findViewById(R.id.viewPager)
        thisMonth = view.findViewById(R.id.thisMonth)
        lastMonth = view.findViewById(R.id.lastMonth)
        thisYear = view.findViewById(R.id.thisYear)
        lastYear = view.findViewById(R.id.lastYear)

        // Set up the ViewPager with the fragments
        val pagerAdapter = SlidingWindowPagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        // Set the default position to 0 (Window 1)
        viewPager.setCurrentItem(0, false)

        // Update button states based on the default position
        updateButtonStates(0)

        // Set up button click listeners
        thisMonth.setOnClickListener {
            viewPager.currentItem = 0
        }

        lastMonth.setOnClickListener {
            viewPager.currentItem = 1
        }
        thisYear.setOnClickListener {
            viewPager.currentItem = 2
        }
        lastYear.setOnClickListener {
            viewPager.currentItem = 3
        }

        // Update button states based on ViewPager position
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonStates(position)
            }
        })

        return view;
    }



    private fun updateButtonStates(position: Int) {
        thisMonth.isSelected = position == 0
        lastMonth.isSelected = position == 1
        thisYear.isSelected = position == 2
        lastYear.isSelected = position == 3
    }

    private inner class SlidingWindowPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    viewModel.selectedChart.value = ChartInfo(Format.THIS_MONTH,viewModel.selectedChart.value?.isExpense?:true)
                    ChartThisMonth()
                }
                1 -> {
                    viewModel.selectedChart.value = ChartInfo(Format.LAST_MONTH,viewModel.selectedChart.value?.isExpense?:true)
                    ChartThisMonth()
                }
                2 -> {
                    viewModel.selectedChart.value = ChartInfo(Format.THIS_YEAR,viewModel.selectedChart.value?.isExpense?:true)
                    ChartThisMonth()
                }
                3 -> {
                    viewModel.selectedChart.value = ChartInfo(Format.LAST_YEAR,viewModel.selectedChart.value?.isExpense?:true)
                    ChartThisMonth()
                }
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExpenseCharts.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExpenseCharts().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
