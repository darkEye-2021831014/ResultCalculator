package com.example.cgpa

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

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

        fun formatedValue(value:Long):String {
//            return String.format("%,d", value)
            return NumberFormat.getNumberInstance(Locale.US).format(value)
        }

        fun showToast(context:Context,message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun log(message: String)
        {
            Log.i(Helper.TAG,message);
        }

    }
}
