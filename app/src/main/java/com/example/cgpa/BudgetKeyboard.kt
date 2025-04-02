package com.example.cgpa

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat


class BudgetKeyboard(private val view: View,
                     private val context: Context
): KeyboardManager(view) {

    private lateinit var adapter:BudgetAdapter
    private val outsideKeyboard = view.findViewById<LinearLayout>(R.id.outsideKeyboard)

    init{
        //swap inputField and noteField
        val temp = inputField
        inputField = noteText
        noteText = temp

        inputField.gravity = Gravity.END or Gravity.CENTER
        noteText.gravity = Gravity.START or Gravity.CENTER
        inputField.textSize = 20F
        noteText.textSize = 16F
        inputField.setTextColor(Color.WHITE)
        done.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context,R.color.yellow))
        done.setTextColor(Color.BLACK)

        inputOperation()
        deleteOperation()
        onOutsideClick()
        inputField.setBackgroundResource(R.drawable.background_stroke)
        noteField.visibility=View.GONE
    }


    fun dateButtonText(text:String){
        date.text = text
        Utility.setDrawable(date,null,null,null,null)
        clearOperation()
    }

    fun setNoteText(text:String)
    {
        noteText.setTextColor(Color.WHITE)
        noteText.text = text
    }


    fun okOperation(item:BudgetItem, viewModel: SharedViewModel){
        done.setOnClickListener {
            input = inputField.text.toString()
            if(item.budget !=getInput()) {
                viewModel.removeData(item)
                item.budget = getInput()
                viewModel.setData(item)
            }
            hideKeyboard()
        }
    }

    fun clearOperation(){
        date.setOnClickListener {
            inputField.text = "0"
        }
    }

    fun setAdapter(adapter:BudgetAdapter){
        this.adapter = adapter
    }

    override fun showKeyboard() {
        super.showKeyboard()
        outsideKeyboard.visibility =View.VISIBLE
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        outsideKeyboard.visibility =View.GONE
    }


    private fun onOutsideClick(){
        outsideKeyboard.setOnClickListener {
            hideKeyboard()
        }
    }
}
