package com.example.cgpa

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs



class AddExpenseFragment : Fragment() {

    private val viewModel by activityViewModels<SharedViewModel>()

    private lateinit var keyboard:TransactionKeyboard


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        //initialize transaction keyboard
        keyboard = TransactionKeyboard(view,requireContext(),requireActivity().supportFragmentManager,viewModel)


        val buttons = listOf(
            view.findViewById<Button>(R.id.food) to R.drawable.food_record,
            view.findViewById<Button>(R.id.drinks) to R.drawable.drinks_records,
            view.findViewById<Button>(R.id.cloth) to R.drawable.cloth_records,
            view.findViewById<Button>(R.id.education) to R.drawable.education_records,
            view.findViewById<Button>(R.id.health) to R.drawable.healthcare_records,
            view.findViewById<Button>(R.id.transport) to R.drawable.transportation_records,
            view.findViewById<Button>(R.id.phone) to R.drawable.phone_records,
            view.findViewById<Button>(R.id.other) to R.drawable.other_records
        )

        buttons.forEach { (button, drawable) ->
            button.setOnClickListener { selectButton(buttons.map { it.first }, button, drawable) }
        }


        return view
    }



    private fun selectButton(buttons: List<Button>, selectedButton: Button, drawableId: Int) {

        buttons.forEach { it.isSelected = false }
        selectedButton.isSelected = true

        val name = selectedButton.text.toString()
        val icon = Helper.saveIcon(drawableId,requireContext())

        val isExpense = true

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
