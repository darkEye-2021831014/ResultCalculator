package com.example.cgpa

import android.content.Context
import android.view.View
import android.widget.NumberPicker
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import kotlin.math.abs

class BudgetManager(private val viewModel: SharedViewModel,
                    private val context: Context) {

    private var day:Int=1
    private var month:Int=1
    private var year:Int = 2000
    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private var expense = 0L


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
    }


    fun setYearRange(yearPicker:NumberPicker,min:Int, max:Int) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = min
        yearPicker.maxValue = max
        yearPicker.value = currentYear
        year=currentYear
    }


    fun update(datePicker: NumberPicker,monthPicker: NumberPicker,yearPicker: NumberPicker)
    {
        day = datePicker.value
        month = monthPicker.value
        year = yearPicker.value
        monthlyBudget()
    }

    fun update(monthPicker: NumberPicker,yearPicker: NumberPicker)
    {
        month = monthPicker.value
        year = yearPicker.value
        monthlyBudget()
    }


    fun monthlyBudget(){
        val budget=0L
        val expense = viewModel.monthlyData.value?.get(month to year)?.expense?:0L
        this.expense = expense
        val isCategory=false
        var isExist=false

        viewModel.budgetData.value?.let {
            for(item in it){
                if(!item.isCategory) {
                    if(item.expense!=expense) {
                        viewModel.removeData(item)
                        item.expense = expense
                        viewModel.setData(item)
                    }
                    isExist = true
                    break
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
        }
    }











    fun getDate() = day.toString();
    fun getMonth() = months[month-1];
    fun getYear() = year.toString();
    fun getExpense() = expense;






















}
