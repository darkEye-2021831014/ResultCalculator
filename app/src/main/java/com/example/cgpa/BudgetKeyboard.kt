package com.example.cgpa

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
                if(item.isCategory && getInput() == 0L){
                    viewModel.removeItem(item,context)
                    val topItem = viewModel.budgetData.value?.first()
                    topItem?.let {
                        viewModel.removeItem(it,context)
                        it.budget-=item.budget
                        viewModel.addItem(it,context)
                    }
                }
                else {
                    val extra = getInput()-item.budget
                    if(item.isCategory) {
                        val topItem = viewModel.budgetData.value?.first()
                        topItem?.let {
                            viewModel.removeItem(it, context)
                            it.budget += extra
                            viewModel.addItem(it, context)
                        }

                        viewModel.removeItem(item, context)
                        item.budget = getInput()
                        viewModel.addItem(item, context)
                    }
                    else{
                        var total =0L
                        viewModel.budgetData.value?.let {
                            for(i in 1 until it.size){
                                total+=it[i].budget
                            }
                        }
                        if(getInput() >= total) {
                            viewModel.removeItem(item, context)
                            item.budget = getInput()
                            viewModel.addItem(item, context)
                        }
                        else
                            Utility.showToast(context,"Monthly Budget Can not be Lower Than The Sum Of Category Budget")
                    }

                }
            }
            hideKeyboard()
        }
    }

    fun okOperation(budgetView: TextView, viewModel: SharedViewModel, item:BudgetItem){
        done.setOnClickListener {
            input = inputField.text.toString()
            if(item.budget !=getInput()) {
                val extra = getInput()-item.budget

                viewModel.removeItem(item,context)
                item.budget = getInput()
                viewModel.addItem(item,context)
                budgetView.text = item.budget.toString()

                val topItem = viewModel.budgetData.value?.first()
                topItem?.let {
                    viewModel.removeItem(it,context)
                    it.budget+=extra
                    viewModel.addItem(it,context)
                }
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
