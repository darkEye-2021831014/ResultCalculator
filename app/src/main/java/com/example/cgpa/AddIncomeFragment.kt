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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddIncomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddIncomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel by activityViewModels<SharedViewModel>()

    private var name: String = "N/A"
    private var iconId:Int = R.drawable.other_records;
    private lateinit var keyboard: LinearLayout;
    private lateinit var inputField: TextView;
    private lateinit var inputNote: EditText;
    private lateinit var done:Button;
    private var prevValue:String = ""
    private var curValue:String = "0"

    private lateinit var itemInfo: ItemInfo;




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_income, container, false)


        keyboard = view.findViewById(R.id.keyboard);
        inputField = view.findViewById(R.id.inputField);
        inputNote = view.findViewById(R.id.noteField);
        keyboardOperation(view);


        //current date
        val calender = Calendar.getInstance()
        val date = calender.get(Calendar.DAY_OF_MONTH)
        val month = calender.get(Calendar.MONTH) + 1;
        val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calender.time)
        val dateName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calender.time)
        val year = calender.get(Calendar.YEAR)

        itemInfo = ItemInfo(name,"",0,true,date,month,year,monthName,dateName,null)



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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddIncomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddIncomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun keyboardOperation(view:View) {
        // Handle number clicks
        val numberButtons = listOf(
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

        for ((buttonId, value) in numberButtons) {
            view.findViewById<Button>(buttonId).setOnClickListener { appendText(value) }
        }

        val delete:Button = view.findViewById(R.id.btn_del)
        done = view.findViewById(R.id.btn_done)

        val plus:Button = view.findViewById(R.id.plus)
        val minus:Button = view.findViewById(R.id.minus)

        val date:Button = view.findViewById(R.id.date)


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
            inputNote.clearFocus()
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

        done.setOnClickListener {
            if(inputField.text.toString().contains("+"))
            {
                inputField.text = "${(prevValue.toLong()+curValue.toLong())}"
                curValue =inputField.text.toString();
            }
            else {
                if (inputField.text.toString() != "0") {
                    addExpense();
                    closeFragment()
                }
            }
            done.text = "✓"
        }

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


        date.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog
            val datePickerDialog =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }

                    itemInfo.date = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                    itemInfo.month = selectedCalendar.get(Calendar.MONTH) + 1;
                    itemInfo.monthName =
                        SimpleDateFormat("MMM", Locale.getDefault()).format(selectedCalendar.time)
                    itemInfo.dateName =
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedCalendar.time)
                    itemInfo.year = selectedCalendar.get(Calendar.YEAR)

                    // Display selected date in TextView
                    val isToday =
                        selectedCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            selectedCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                    if (!isToday) {
                        val monthName = SimpleDateFormat(
                            "MMM",
                            Locale.getDefault()
                        ).format(selectedCalendar.time)
                        date.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                        date.text = "$monthName $selectedDay\n$selectedYear"
                    } else {
                        date.setCompoundDrawablesWithIntrinsicBounds(
                            Helper.getIcon(requireContext(), R.drawable.ic_calendar),
                            null,
                            null,
                            null
                        )
                        date.text = "Today"
                    }
                }, year, month, day)

            datePickerDialog.show()
        }

    }


    fun textColor(color:Int,button:Button)
    {
        button.setTextColor(ContextCompat.getColor(requireContext(), color))
    }
    fun backgroundTint(color:Int,button:Button)
    {
        val colorStateList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        button.backgroundTintList = colorStateList
    }


    private fun appendText(value: String) {
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
        inputNote.clearFocus();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addExpense() {
        val enteredText = inputField.text.toString().trim()
        val enteredNote = inputNote.text.toString().trim()
        var note:String? = null

        if (enteredNote.isNotEmpty()) note = enteredNote;

        if (enteredText.isNotEmpty()) {
            val amount = enteredText.toLong()

            itemInfo.name = name;
            itemInfo.icon = Helper.saveIcon(iconId,requireContext())
            itemInfo.amount = amount
            itemInfo.isExpense = false
            itemInfo.note = note;

            val item = itemInfo;
            viewModel.setData(item)

            //save item info in file
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

        buttons.forEach { it.isSelected = false }
        selectedButton.isSelected = true

        name = selectedButton.text.toString()
        keyboard.visibility=View.VISIBLE
        iconId = drawableId;
    }
}
