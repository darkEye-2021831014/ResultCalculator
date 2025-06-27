package com.example.cgpa

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class RecordsFragment : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()

    private lateinit var incomeText:TextView
    private lateinit var expenseText:TextView
    private lateinit var balanceText:TextView

    private lateinit var currentYear:TextView
    private lateinit var currentMonth:Button

    private lateinit var recyclerView:RecyclerView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        currentYear = view.findViewById(R.id.currentYear)
        currentMonth = view.findViewById(R.id.monthSelector)

        incomeText = view.findViewById(R.id.incomeTextValue)
        expenseText = view.findViewById(R.id.expenseTextValue)
        balanceText = view.findViewById(R.id.balanceTextValue)


        currentYear.text = LocalDate.now().year.toString()
        currentMonth.text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"))

        var Ddate = Calendar.getInstance().get(Calendar.DATE)
        var Dmonth =Calendar.getInstance().get(Calendar.MONTH)+1;
        var DmonthName = SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().time)
        var Dyear = Calendar.getInstance().get(Calendar.YEAR)

        currentMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            // Create the DatePickerDialog with default day set to 1 (first day of the month)
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, _ ->
                // Here, we ensure the first day is selected every time the user switches months
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, 1)  // Force the day to 1, no matter the previous selection
                }

                // Update the UI
                currentMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(selectedCalendar.time)
                currentYear.text = selectedYear.toString()

                Dmonth = selectedMonth+1
                Dyear = selectedYear
                DmonthName = currentMonth.text.toString()

                // Trigger UI update based on the new selected month
                viewModel.userData.value?.let { updateUI(it) }

            }, year, month, 1) // Default day is set to 1 initially

            // Show the dialog
            datePickerDialog.show()
        }



        // Handle menu button click
        val menu: ImageButton = view.findViewById(R.id.menu)
        val drawer: DrawerLayout = view.findViewById(R.id.drawer_Layout_Records)
        menu.setOnClickListener { drawer.openDrawer(GravityCompat.START) }

        // Navigate to report generator
        view.findViewById<Button>(R.id.generateReport).setOnClickListener {
            Utility.bottomSheet(requireActivity(),GenerateReport(),"GenerateReport")
        }

        // Navigate to Result Calculator
        view.findViewById<Button>(R.id.resultCalculator).setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        // Exit App
        view.findViewById<Button>(R.id.exit).setOnClickListener {
            requireActivity().finishAffinity()
        }

        //handle header button monthly report click
        view.findViewById<ImageButton>(R.id.monthlyReport).setOnClickListener {
            val reportType = Format.MONTHLY_REPORT
            val date = CalenderDate(Ddate,Dmonth,DmonthName,Dyear)

            Utility.showToast(requireContext(),"Generating Monthly Report For \"${DmonthName.uppercase()} $Dyear\"")
            CreateReport(requireContext(),requireActivity(),reportType,date,viewModel).generateReport(reportType,date)

        }

        //handle header button mode change
        view.findViewById<TextView>(R.id.recordName).text = "Personal Records"
        viewModel.mode.value?.let {it->
            if(it =="Personal")
                view.findViewById<TextView>(R.id.recordName).text = "Personal Records"
            else {
                view.findViewById<TextView>(R.id.recordName).text = it
            }
        }

        view.findViewById<ImageButton>(R.id.mode).setOnClickListener{
            val options: MutableList<String> = mutableListOf()
            options.add("Personal")
            viewModel.sharedData.value?.let {
                it.forEach { item ->
                    options.add(item.name ?: "N/A")
                }
            }

            var curOption=0;
            val selectedMode = viewModel.mode.value;
            for( i in 0 until options.size){
                if(options[i]==selectedMode){
                    curOption=i;
                    break;
                }
            }

            showSingleChoiceDialog(requireContext(), options,"Select Record Type",curOption) { selected ->
                val name = options[selected]
                if(name == "Personal") {
                    view.findViewById<TextView>(R.id.recordName).text = "Personal Records"
                    viewModel.switchMode(null, requireContext())
                }
                else {
                    view.findViewById<TextView>(R.id.recordName).text = name
                    viewModel.switchMode(name, requireContext())
                }
            }
        }

        //list of items
        setupRecyclerView(view)

        return view
    }




    fun showSingleChoiceDialog(
        context: Context,
        options: MutableList<String>,  // Changed to immutable List
        title: String = "Choose an option",  // Added customizable title
        curOption:Int=0,
        selected: (Int) -> Unit
    ) {
        var selectedOption = curOption // default selection

        AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(options.toTypedArray(), selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("OK") { dialog, _ ->
                selected(selectedOption)  // Call the callback with selected position
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener {
                // Handle if user dismisses dialog without pressing any button
            }
            .create()
            .show()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recordContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            updateUI(data)
        }
    }

    private fun updateUI(data:MutableList<ItemInfo>)
    {
        var expense:Long=0
        var income:Long=0
        for(itemInfo in data)
        {
            if(itemInfo.monthName == currentMonth.text && itemInfo.year == currentYear.text.toString().toInt())
            {
                if(itemInfo.isExpense)expense+=abs(itemInfo.amount)
                else income += abs(itemInfo.amount);
            }
        }

        incomeText.text = income.toString()
        expenseText.text = expense.toString()
        balanceText.text = (income-expense).toString()


        val items = processUserData(data)

        recyclerView.adapter = ItemAdapter(
            items,
            true,
            onEditClick = { item -> showToast("Editing: $item") },
            onDeleteClick = { item -> deleteItem(item as Item) },
            onDetailsClick = { item -> itemDetails(item as Item) },
            onDateClick = { item ->
                val date = item as Date
                Utility.showToast(requireContext(),"Generating Daily Report For \"${date.day} ${date.monthName.uppercase()}\"")
                CreateReport(requireContext(),requireActivity(),Format.DAILY_REPORT,
                    CalenderDate(date.day,date.month,date.monthName,date.year),viewModel).generateReport(
                        Format.DAILY_REPORT, CalenderDate(date.day,date.month,date.monthName,date.year)
                    )
            }
        )
    }


    private fun processUserData(data: List<ItemInfo>): MutableList<Any> {
        val items = mutableListOf<Any>()
        var date = -1
        val tmp = mutableListOf<Item>()
        var expense = 0L
        var income = 0L
        var dateText = ""
        var calenderDate:CalenderDate = CalenderDate(1,1,"Jan",2000);

        for (item in data) {
            if(item.monthName != currentMonth.text || item.year != currentYear.text.toString().toInt())
                continue
            //show only current months data
            if (item.date != date) {
                if (date != -1) {
                    addDateHeader(items, dateText, expense, income,calenderDate)
                    items.addAll(tmp)
                    tmp.clear()
                }
                dateText = getDateText(item)
                calenderDate = CalenderDate(item.date,item.month,item.monthName,item.year)
                date = item.date
                expense = 0L
                income = 0L
            }

            tmp.add(Item(item.note ?: item.name, Helper.loadIcon(item.icon,requireContext()),
                (if(!item.isExpense)"+" else "") + item.amount.toString(),item))
            if (item.isExpense) expense += abs(item.amount)
            else income += abs(item.amount)
        }

        if (date != -1) {
            addDateHeader(items, dateText, expense, income,calenderDate)
            items.addAll(tmp)
        }

        return items
    }

    private fun addDateHeader(items: MutableList<Any>, dateText: String, expense: Long, income: Long,date:CalenderDate) {
        val summary = when {
            expense == 0L -> "Income: $income"
            income == 0L -> "Expense: $expense"
            else -> "Expense: $expense  Income: $income"
        }
        items.add(Date(dateText, summary,date.day,date.month,date.monthName,date.year))
    }

    private fun getDateText(itemInfo: ItemInfo): String {
        return "${itemInfo.monthName} ${itemInfo.date}  ${itemInfo.dateName}"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun deleteItem(item:Item)
    {
        val itemInfo = item.info
        Helper.showConfirmationDialog(requireContext(),"Are you sure you want to delete?") {
            confirm ->
            if(confirm)
                Utility.deleteItem(itemInfo,viewModel,requireContext())
        }
    }

    fun itemDetails(item:Item)
    {
        viewModel.selectedItem.value = item.info;
        val bottomSheet = ItemDetailsFregment()
        bottomSheet.show(requireActivity().supportFragmentManager, "ItemDetailsFregment")
    }


}
