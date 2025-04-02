package com.example.cgpa

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.Normalizer.Form
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionKeyboard(private val view: View,
                          private val context: Context,
                          private val fragmentManager:FragmentManager,
                          private val viewModel: SharedViewModel):KeyboardManager(view) {

    private var prevValue:String = ""
    private var curValue:String = "0"
    private var itemInfo:ItemInfo = ItemInfo("","",0L,true,1,1,1,"","","")
    private var cur_day = 1
    private var cur_month = 1
    private var cur_year = 1

    private var cur_monthName = ""
    private var cur_dayName = ""


    init{
        inputOperation()
        deleteOperation()
        plusOperation()
        dateOperation()
        okOperation()
        cur_day = Utility.getDay()
        cur_month = Utility.getMonth()
        cur_year = Utility.getYear()
        cur_monthName = Utility.getMonthName()
        cur_dayName= Utility.getDateName()
    }

    fun setItemInfo(itemInfo:ItemInfo){
        this.itemInfo = itemInfo
    }


    override fun inputOperation() {
        for ((buttonId, value) in numberButtons) {
            view.findViewById<Button>(buttonId).setOnClickListener {
                if(inputField.length() <= 11)
                    appendText(value)
            }
        }
    }

    override fun deleteOperation() {
        delete.setOnClickListener {
            if(inputField.text!="0") {
                if(inputField.text.toString().last()==' ') {
                    inputField.text = inputField.text.toString().dropLast(1)
                    inputField.text = inputField.text.toString().dropLast(1)
                }
                inputField.text = inputField.text.toString().dropLast(1)
            }
            if(inputField.text.toString().isEmpty()) {
                inputField.text = "0"
                textColor(R.color.white, done);
                backgroundTint(R.color.lessGray, done);
            }
            noteField.clearFocus()
            curValue = curValue.dropLast(1);
            Log.i(Helper.TAG,curValue)
            if(curValue.isEmpty()) {
                curValue = "0"
            }
            if(!inputField.text.toString().contains("+"))
            {
                done.text = "✓"
                inputField.text = inputField.text.toString().trim();
                curValue = inputField.text.toString();
            }
        }
    }


    override fun okOperation() {
        done.setOnClickListener {
            if(inputField.text.toString().contains("+"))
            {
                inputField.text = "${(prevValue.toLong()+curValue.toLong())}"
                curValue =inputField.text.toString();
            }
            else {
                if (inputField.text.toString() != "0") {
                    addItem();
                    closeFragment()
                }
            }
            done.text = "✓"
        }
    }

    private fun addItem(){
        val note = noteField.text.toString().trim()
        itemInfo.note = note.ifEmpty { null }

        itemInfo.amount = inputField.text.toString().trim().toLong()
        if(itemInfo.isExpense) itemInfo.amount *= -1L

        itemInfo.date = cur_day
        itemInfo.month = cur_month
        itemInfo.year = cur_year
        itemInfo.dateName = cur_dayName
        itemInfo.monthName = cur_monthName

        Utility.log("Added Item: $itemInfo")

        viewModel.setData(itemInfo)

        Helper.saveItemInfoList(viewModel.userData,context)
    }


    private fun closeFragment() {
        val bottomSheetFragment = fragmentManager.findFragmentByTag("AddItemFragment")
        if (bottomSheetFragment is BottomSheetDialogFragment) {
            bottomSheetFragment.dismiss()
            fragmentManager.beginTransaction().remove(bottomSheetFragment).commitAllowingStateLoss()
        }
    }


    fun plusOperation(){
        plus.setOnClickListener {
            Log.i(Helper.TAG,"$prevValue $curValue")
            if(inputField.text.toString().contains("+")) {
                inputField.text = "${(prevValue.toLong()+curValue.toLong())}"
                curValue =inputField.text.toString();
            }

            if(inputField.text.toString()!="0") {
                prevValue = curValue
                curValue = "0"
                inputField.text = inputField.text.toString().trim() + " + "
                textColor(R.color.black, done);
                backgroundTint(R.color.yellow, done);
                done.text = "="
            }
        }
    }


    fun dateOperation(){
        date.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog
            val datePickerDialog = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                cur_day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                cur_month = selectedCalendar.get(Calendar.MONTH) + 1;
                cur_monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(selectedCalendar.time)
                cur_dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedCalendar.time)
                cur_year = selectedCalendar.get(Calendar.YEAR)

                // Display selected date in TextView
                val isToday = selectedCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                if(!isToday) {
                    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(selectedCalendar.time)
                    date.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    date.text = "$monthName $selectedDay\n$selectedYear"
                }
                else
                {
                    date.setCompoundDrawablesWithIntrinsicBounds(
                        Helper.getIcon(context,R.drawable.ic_calendar), null, null, null)
                    date.text = "Today"
                }
            }, year, month, day)

            datePickerDialog.show()
        }
    }


    fun textColor(color:Int,button: Button)
    {
        button.setTextColor(ContextCompat.getColor(context, color))
    }
    fun backgroundTint(color:Int,button: Button)
    {
        val colorStateList =
            ColorStateList.valueOf(ContextCompat.getColor(context, color))
        button.backgroundTintList = colorStateList
    }


    override fun appendText(value: String) {
        if(inputField.text.toString()=="0")
            inputField.text = value
        else {
            inputField.text = inputField.text.toString() + value
        }
        if(inputField.text.toString()!="0" && !inputField.text.toString().contains("+")) {
            textColor(R.color.black, done);
            backgroundTint(R.color.yellow, done);
        }
        curValue +=value;
        noteField.clearFocus();
    }

}
