package com.example.cgpa

import android.app.DatePickerDialog
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

open class KeyboardManager(private val view: View) {

    protected var inputField: TextView = view.findViewById<TextView>(R.id.inputField)
    protected val delete: Button = view.findViewById(R.id.btn_del)
    protected val done: Button = view.findViewById(R.id.btn_done)

    protected val plus: Button = view.findViewById(R.id.plus)
    protected val minus: Button = view.findViewById(R.id.minus)

    protected val date: Button = view.findViewById(R.id.date)
    protected val keyboard: LinearLayout = view.findViewById(R.id.keyboard)

    protected val noteField: EditText = view.findViewById(R.id.noteField)
    protected var noteText: TextView = view.findViewById(R.id.noteText)

    protected var input:String = "0"

    protected val numberButtons = listOf(
        R.id.btn_0 to "0",
        R.id.btn_1 to "1",
        R.id.btn_2 to "2",
        R.id.btn_3 to "3",
        R.id.btn_4 to "4",
        R.id.btn_5 to "5",
        R.id.btn_6 to "6",
        R.id.btn_7 to "7",
        R.id.btn_8 to "8",
        R.id.btn_9 to "9"
    )




    fun setInput(value:Long){
        inputField.text = value.toString()
        input = inputField.text.toString()
    }

    fun plusButtonText(text:String){
        plus.text = text
    }
    fun minusButtonText(text:String){
        minus.text = text
    }

    open fun showKeyboard(){
        keyboard.visibility = View.VISIBLE
    }

    open fun hideKeyboard(){
        keyboard.visibility = View.GONE
    }


    open fun inputOperation() {
        for ((buttonId, value) in numberButtons) {
            view.findViewById<Button>(buttonId).setOnClickListener {
                if(inputField.length() <= 11)
                    appendText(value)
            }
        }
    }

    open fun deleteOperation(){
        delete.setOnClickListener {
            if(inputField.text!="0")
                inputField.text = inputField.text.toString().dropLast(1)

            if(inputField.text.toString().isEmpty())
                inputField.text = "0"
        }
    }

    open fun okOperation(){
        done.setOnClickListener {
            input = inputField.text.toString()
            keyboard.visibility = View.GONE
        }
    }


    open fun appendText(value: String) {
        if(inputField.text.toString()=="0")
            inputField.text = value
        else
            inputField.text = inputField.text.toString() + value
    }



    fun getInput() = input.toLong()

}
