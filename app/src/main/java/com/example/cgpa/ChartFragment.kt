package com.example.cgpa

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var expenseButton: Button
    private lateinit var incomeButton: Button

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
        val view =  inflater.inflate(R.layout.fragment_chart, container, false)

        viewModel.selectedChart.value = ChartInfo(Format.THIS_MONTH,true)


        //sliding income expense
        viewPager = view.findViewById(R.id.viewPager)
        incomeButton = view.findViewById(R.id.chartIncome)
        expenseButton = view.findViewById(R.id.chartExpense)

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

                // Log every time the user switches pages
                when (position) {
                    0 -> {
                        Utility.log("Expense")
                        val chart = ChartInfo(Format.THIS_MONTH,true)
                        viewModel.selectedChart.value = chart
                    }
                    1 -> {
                        Utility.log("Income")
                        val chart = ChartInfo(Format.THIS_MONTH,false)
                        viewModel.selectedChart.value = chart
                    }
                }
            }
        })











        return view;
    }



    private fun updateButtonStates(position: Int) {
        expenseButton.isSelected = position == 0
        incomeButton.isSelected = position == 1
    }

    private inner class SlidingWindowPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val chart = ChartInfo(Format.THIS_MONTH,true)
                    viewModel.selectedChart.value = chart
                    ExpenseCharts()
                }
                1 -> {
                    val chart = ChartInfo(Format.THIS_MONTH,false)
                    viewModel.selectedChart.value = chart
                    ExpenseCharts()
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
         * @return A new instance of fragment chartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
