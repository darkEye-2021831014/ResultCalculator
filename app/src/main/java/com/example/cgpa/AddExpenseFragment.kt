package com.example.cgpa

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddExpenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddExpenseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var note:EditText;
    private lateinit var value:EditText;

    var name:String="N/A"
    var icon: Drawable? = null

    private val viewModel by activityViewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)
        note = view.findViewById(R.id.note)
        value = view.findViewById(R.id.value)

        val foodButton: Button = view.findViewById(R.id.food);
        val drinksButton: Button = view.findViewById(R.id.drinks);
        val clothButton: Button = view.findViewById(R.id.cloth);
        val educationButton: Button = view.findViewById(R.id.education);
        val healthButton: Button = view.findViewById(R.id.health);
        val transportButton: Button = view.findViewById(R.id.transport);
        val phoneButton: Button = view.findViewById(R.id.phone);

        val otherButton: Button = view.findViewById(R.id.other);

        val buttons = listOf(foodButton,drinksButton,clothButton,educationButton,healthButton,transportButton,
            phoneButton,otherButton);
        //item info


        foodButton.setOnClickListener {
            selectButton(buttons,foodButton,R.drawable.food_record);
        }
        drinksButton.setOnClickListener {
            selectButton(buttons,drinksButton,R.drawable.drinks_records);
        }
        clothButton.setOnClickListener {
            selectButton(buttons,clothButton,R.drawable.cloth_records);
        }
        educationButton.setOnClickListener {
            selectButton(buttons,educationButton,R.drawable.education_records);
        }
        healthButton.setOnClickListener {
            selectButton(buttons,healthButton,R.drawable.healthcare_records);
        }
        transportButton.setOnClickListener {
            selectButton(buttons,transportButton,R.drawable.transportation_records);
        }
        phoneButton.setOnClickListener {
            selectButton(buttons,phoneButton,R.drawable.phone_records);
        }

        otherButton.setOnClickListener {
            selectButton(buttons,otherButton,R.drawable.other_records);
        }





        value.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val enteredText = value.text.toString().trim()
                val enteredNote = note.text.toString().trim();
                if(enteredNote.isNotEmpty())
                    name = enteredNote;

                if (enteredText.isNotEmpty()) {
                    val value = enteredText.toLong() * -1L;
                    val valueText = "-$enteredText"

                    val calendar = Calendar.getInstance()

                    val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
                    val monthName =
                        LocalDate.now().month.name.take(3).lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() } // Get first 3 letters of the month name
//                    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time) // Get month nam
                    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time) // Get day name

                    val item = ItemInfo(name,icon,value,valueText,day,"$monthName $day  $dayOfWeek");
                    viewModel.setUserData(item);
                    Toast.makeText(requireContext(),"Expense Added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Please enter a value", Toast.LENGTH_SHORT).show()
                }
                closeFragments();

                true // Return true to indicate we handled the event
            } else {
                false
            }
        }



        return view;
    }



    private fun closeFragments() {
        val fragmentManager = requireActivity().supportFragmentManager
        val bottomSheetFragment = fragmentManager.findFragmentByTag("AddItemFragment")
        if (bottomSheetFragment is BottomSheetDialogFragment) {
            bottomSheetFragment.dismiss()
            fragmentManager.beginTransaction().remove(bottomSheetFragment).commitAllowingStateLoss()
        }
    }

    private fun selectButton(buttons:List<Button>, selectedButton:Button,drawable:Int)
    {
        note.visibility = View.VISIBLE;
        value.visibility = View.VISIBLE;
        showKeyboard(value);

        for(button in buttons)
            button.isSelected=false;
        selectedButton.isSelected=true;
        name = selectedButton.text.toString()
        icon = Helper.getIcon(requireContext(),drawable);
    }


    private fun showKeyboard(editTextInput:EditText) {
        // Request focus for the EditText
        editTextInput.requestFocus()

        // Show the keyboard
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextInput, InputMethodManager.SHOW_IMPLICIT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddExpenseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddExpenseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
