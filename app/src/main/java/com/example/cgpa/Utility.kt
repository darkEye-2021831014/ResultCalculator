package com.example.cgpa

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

class Utility {
    companion object {

        fun deleteItem(itemInfo: ItemInfo, viewModel: SharedViewModel, context: Context) {
            viewModel.removeData(itemInfo)
            Helper.saveItemInfoList(viewModel.userData, context)
        }

        fun getMonth(): Int {
            return Calendar.getInstance().get(Calendar.MONTH)+1
        }
        fun getYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR)
        }

        //comma separated value
        fun formatedValue(value:Long):String {
//            return String.format("%,d", value)
            return NumberFormat.getNumberInstance(Locale.US).format(value)
        }

        // double to 2 decimal digit
        fun formatDouble(value:Double):Double {
            return Math.round(value * 100)/100.0
        }

        fun showToast(context:Context,message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun log(message: String)
        {
            Log.i(Helper.TAG,message);
        }

        fun bottomSheet(activity:FragmentActivity, bottomSheet: BottomSheetDialogFragment, name:String)
        {
            bottomSheet.show(activity.supportFragmentManager, name)
        }



    }
}
