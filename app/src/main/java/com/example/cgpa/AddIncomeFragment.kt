package com.example.cgpa

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs


class AddIncomeFragment : Fragment() {
    private val viewModel by activityViewModels<SharedViewModel>()

    private lateinit var keyboard:TransactionKeyboard

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_income, container, false)

        keyboard = TransactionKeyboard(view,requireContext(),requireActivity().supportFragmentManager,viewModel)

        val buttons = listOf(
            view.findViewById<Button>(R.id.salary) to R.drawable.salary_records,
            view.findViewById<Button>(R.id.investment) to R.drawable.investment_records,
            view.findViewById<Button>(R.id.otherIncome) to R.drawable.other_records,
            view.findViewById<Button>(R.id.tution) to R.drawable.tution_records,
        )

        buttons.forEach { (button, drawable) ->
            button.setOnClickListener { selectButton(buttons.map { it.first }, button, drawable) }
        }


        return view;
    }



    private fun selectButton(buttons: List<Button>, selectedButton: Button, drawableId: Int) {

        buttons.forEach { it.isSelected = false }
        selectedButton.isSelected = true

        val name = selectedButton.text.toString()
        val icon = Helper.saveIcon(drawableId,requireContext())
        val isExpense = false

        val itemInfo = ItemInfo(name,
            icon,
            0L,
            isExpense,
            1,
            1,
            2000,
            "",
            "",
            null)

        keyboard.setItemInfo(itemInfo)
        keyboard.showKeyboard()
    }
}
