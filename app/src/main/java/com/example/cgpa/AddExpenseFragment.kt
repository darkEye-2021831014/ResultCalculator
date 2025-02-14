package com.example.cgpa

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    private lateinit var note: EditText
    private lateinit var value: EditText
    private val viewModel by activityViewModels<SharedViewModel>()

    private var name: String = "N/A"
    private var icon: Drawable? = null
    private var iconId:Int=R.drawable.other_records;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        note = view.findViewById(R.id.note)
        value = view.findViewById(R.id.value)

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

        value.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                addExpense()
                true
            } else {
                false
            }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addExpense() {
        val enteredText = value.text.toString().trim()
        val enteredNote = note.text.toString().trim()
        var note:String? = null

        if (enteredNote.isNotEmpty()) note = enteredNote;

        if (enteredText.isNotEmpty()) {
            val cash = -enteredText.toLong()

            val calendar = Calendar.getInstance()
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val month = LocalDate.now().monthValue
            val monthName = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM"))
            val dateName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            val year = Year.now().value

            val item = ItemInfo(name, Helper.saveIcon(iconId,requireContext()), cash, true, date, month, year, monthName, dateName,note)
            viewModel.setData(item)
            val dateExchange = "${item.year}-${item.month}-${item.date}"
            viewModel.updateExchange(dateExchange,abs(cash),true)
            Helper.saveItemInfoList(viewModel.userData,requireContext())

            closeFragment()
        } else {
            Toast.makeText(requireContext(), "Please enter a value", Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val bottomSheetFragment = fragmentManager.findFragmentByTag("AddItemFragment")
        if (bottomSheetFragment is BottomSheetDialogFragment) {
            bottomSheetFragment.dismiss()
            fragmentManager.beginTransaction().remove(bottomSheetFragment).commitAllowingStateLoss()
        }
    }

    private fun selectButton(buttons: List<Button>, selectedButton: Button, drawableId: Int) {
        note.visibility = View.VISIBLE
        value.visibility = View.VISIBLE
        showKeyboard(value)

        buttons.forEach { it.isSelected = false }
        selectedButton.isSelected = true

        name = selectedButton.text.toString()
//        icon = Helper.getIcon(requireContext(), drawable)
        iconId = drawableId;
    }

    private fun showKeyboard(editTextInput: EditText) {
        editTextInput.requestFocus()
        val inputMethodManager = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextInput, InputMethodManager.SHOW_IMPLICIT)
    }
}
