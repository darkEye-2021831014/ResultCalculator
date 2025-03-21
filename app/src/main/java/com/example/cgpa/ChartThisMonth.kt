package com.example.cgpa

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChartThisMonth.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChartThisMonth : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: SharedViewModel by activityViewModels()

    private var currentMonth:Int = 1;
    private var currentYear:Int = 2000;
    private var currentMode:Boolean = true;
    private var isMonth:Boolean = false;
    private lateinit var pieChart:PieChart

    private lateinit var recyclerView: RecyclerView

    private val itemValue: MutableMap<String, Long> = mutableMapOf()
    private val itemIcon: MutableMap<String, String?> = mutableMapOf()



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
        val view = inflater.inflate(R.layout.fragment_chart_this_month, container, false)

        pieChart = view.findViewById(R.id.pieChart)





        Log.i(Helper.TAG,"${viewModel.selectedChart.value}")
        viewModel.userData.observe(viewLifecycleOwner){
            data->
            updateChart(data)
        }

        //list of items
        setupRecyclerView(view)

        return view;
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.pieChartDetails)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }




    private fun updateUI()
    {
        val sortedItems = itemValue.entries.sortedByDescending { it.value };
        val sum = itemValue.entries.sumOf { it.value }

        val items:MutableList<Any> = mutableListOf()
        for((itemName,value) in sortedItems)
        {
            val percentage = (abs(value) * 100.0 / sum)
            val formated = String.format("%.2f",percentage);
            items.add(Item("$itemName   ${formated}%",Helper.loadIcon(itemIcon[itemName],requireContext()),value.toString(),
                ItemInfo("","",0L,false,0,0,0,"","","")))
        }

        recyclerView.adapter = ItemAdapter(
            items,
            false,
            onEditClick = {},
            onDeleteClick = {},
            onDetailsClick = {},
            onDateClick = {}
        )
    }





    fun primaryData(){
        val calendar = Calendar.getInstance()
        val curYear =  calendar.get(Calendar.YEAR);
        val curMonth = calendar.get(Calendar.MONTH) + 1;
        currentMonth = curMonth
        currentYear = curYear
        currentMode = viewModel.selectedChart.value?.isExpense?:true

        when(viewModel.selectedChart.value?.chart)
        {
            Format.THIS_MONTH -> isMonth = true;
            Format.LAST_MONTH ->
            {
                if(curMonth!=1)currentMonth = curMonth-1;
                else {
                    currentMonth = 12
                    currentYear = curYear-1;
                }
                isMonth = true;
            }
            Format.LAST_YEAR -> currentYear=curYear-1;
            else -> {}
        }
    }




    fun updateChart(items: MutableList<ItemInfo>) {
        primaryData();
        if (items.isEmpty()) return  // Prevent empty data issues

        val pieEntries: MutableList<PieEntry> = mutableListOf()
        var maxWidth = 0
        var total = 0L

        for (itemInfo in items) {
            if (itemInfo.isExpense == currentMode) {
                if (isMonth) {
                    if (itemInfo.month == currentMonth && itemInfo.year == currentYear) {
                        itemValue[itemInfo.name] = itemValue.getOrDefault(itemInfo.name, 0L) + abs(itemInfo.amount)
                        total += abs(itemInfo.amount)
                    }
                } else {
                    if (itemInfo.year == currentYear) {
                        itemValue[itemInfo.name] = itemValue.getOrDefault(itemInfo.name, 0L) + abs(itemInfo.amount)
                        total += abs(itemInfo.amount)
                    }
                }
            }
            itemIcon[itemInfo.name] = itemInfo.icon;
        }

        // Sort the map by value in descending order and take the top 4 items
        val topItems = itemValue.entries.sortedByDescending { it.value }.take(4)

        // Add the top 4 items as PieEntries
        for ((name, value) in topItems) {
            maxWidth = maxWidth.coerceAtLeast(name.length)
            pieEntries.add(PieEntry(value.toFloat(), name))
        }

        // Calculate the sum of the remaining items and add them as "Other"
        val otherSum = itemValue.entries
            .filterNot { it.key in topItems.map { it.key } } // Exclude top 4 items
            .sumOf { it.value } // Sum the remaining values

        // If there are remaining values, add them as "Other"
        if (otherSum > 0) {
            pieEntries.add(PieEntry(otherSum.toFloat(), "Other"))
            maxWidth = maxWidth.coerceAtLeast(5)
        }

        val totalSum = pieEntries.sumOf { it.value.toDouble() } // Calculate total sum
        if (totalSum == 0.0) return // Prevent division by zero

        // Create a list of PieEntries with percentage in the label
        val formattedEntries = pieEntries.map { entry ->
            val percentage = (abs(entry.value) * 100 / totalSum)
            val extraSpace = maxWidth - entry.label.length
            val formattedText = String.format("""${entry.label}%${extraSpace + 1}s %.2f%%""", "", percentage)
            PieEntry(entry.value, formattedText) // Set the formatted label
        }

        val customColors = listOf(
            Color.parseColor("#3E4A59"),
            Color.parseColor("#A9A9A9"),
            Color.parseColor("#8A4F7D"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#4CAF50")
        )

        // Create a PieDataSet
        val dataSet = PieDataSet(formattedEntries, "")
        dataSet.colors = customColors.take(formattedEntries.size).toMutableList().apply {
            while (size < formattedEntries.size) {
                add(customColors[size % customColors.size])
            }
        }
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(false)

        // Create a PieData object
        val pieData = PieData(dataSet)

        // Set data to the chart
        pieChart.data = pieData
        pieChart.setDrawEntryLabels(false)

        // Customize the chart
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(700)
        pieChart.centerText = Utility.formatedValue(total)
        pieChart.holeRadius = 65f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.isHighlightPerTapEnabled = false

        // Customize the legend
        val legend = pieChart.legend
        legend.typeface = Typeface.MONOSPACE
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 5f
        legend.textSize = 12f
        legend.textColor = Color.WHITE
        legend.form = Legend.LegendForm.CIRCLE

        // Refresh the chart
        pieChart.invalidate()

        //update the list items
        updateUI()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChartThisMonth.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChartThisMonth().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
