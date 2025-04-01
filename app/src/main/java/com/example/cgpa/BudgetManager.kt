package com.example.cgpa

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs

class BudgetManager(private val viewModel: SharedViewModel,
                    private val context: Context,
                    private val recyclerView:RecyclerView,
                    view:View) {

    private var day:Int=1
    private var month:Int=1
    private var year:Int = 2000
    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private var expense = 0L
    private val keyboard = BudgetKeyboard(view,context)


    fun setDateRange(datePicker:NumberPicker,min:Int, max:Int) {
        val currentDay = Calendar.getInstance().get(Calendar.DATE)
        datePicker.minValue = min
        datePicker.maxValue = max
        datePicker.value = currentDay
        day=currentDay
    }

    fun setMonthRange(monthPicker: NumberPicker) {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)+1
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = months
        monthPicker.value = currentMonth
        month=currentMonth
        monthlyExpense();
    }


    fun setYearRange(yearPicker:NumberPicker,min:Int, max:Int) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = min
        yearPicker.maxValue = max
        yearPicker.value = currentYear
        year=currentYear
        monthlyExpense();
    }


    fun update(datePicker: NumberPicker,monthPicker: NumberPicker,yearPicker: NumberPicker)
    {
        day = datePicker.value
        month = monthPicker.value
        year = yearPicker.value
        monthlyExpense()
    }

    fun update(monthPicker: NumberPicker,yearPicker: NumberPicker)
    {
        month = monthPicker.value
        year = yearPicker.value
        monthlyExpense()
    }

    private fun monthlyExpense(){
        expense = 0L
        viewModel.userData.value?.let {
            it.forEach{ item ->
                if(item.isExpense && item.month == month && item.year==year)
                    expense+=abs(item.amount)
            }
        }
        setMonthlyBudget()
    }

    private fun setMonthlyBudget(){
        val budget=0L
        val expense = this.expense
        val isCategory=false
        var isExist=false

        viewModel.budgetData.value?.let {
            it.forEach { item->
                if(!item.isCategory) {
                    item.expense = expense
                    isExist = true
                }
            }
        }

        if(!isExist) {
            viewModel.setData(
                BudgetItem(
                    "Monthly Budget", null,
                    Helper.getIcon(context, R.drawable.ic_right),
                    budget,
                    expense,
                    isCategory
                )
            )
            Utility.log("New")
        }


        setUpRecyclerView()
    }

    private fun setUpRecyclerView(){
        initializeKeyboard()
        viewModel.budgetData.value?.let{data->
            //assuming the monthly budget will always be the first item as it will be added first
            val adapter = BudgetAdapter(
                data,
                onClick = {
                    item,position->
                    keyboard.setInput(item.budget)
                    keyboard.setNoteText(
                        if (item.isCategory) "Monthly Budget - ${item.heading}" else "Monthly Budget"
                    )
                    keyboard.showKeyboard()
                    keyboard.okOperation(item,position)
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









    fun getDate() = day.toString();
    fun getMonth() = months[month-1];
    fun getYear() = year.toString();
    fun getExpense() = expense;






















}
