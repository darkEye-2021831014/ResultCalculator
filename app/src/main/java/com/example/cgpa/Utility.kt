package com.example.cgpa

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat

class Utility {
    companion object {


        fun deleteItem(itemInfo: Any, viewModel: SharedViewModel, context: Context) {
            viewModel.removeData(itemInfo)
            Helper.saveItemInfoList(viewModel.userData, context)
        }

        fun getDay():Int{
            return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        }

        fun getMonth(): Int {
            return Calendar.getInstance().get(Calendar.MONTH)+1
        }
        fun getYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR)
        }

        fun getMonthName():String{
            return SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().time)
        }

        fun getDateName():String{
            return SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time)
        }

        //1 index based months
        fun getMonthNameOf(month:Int):String{
            val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            return months[month-1]
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

        fun getColor(context:Context,colorId:Int):Int
        {
            return ContextCompat.getColor(context,colorId)
        }

        //color a text part by part. one text will be colored in multiple color
        fun textColor(spannable: Spannable, text:String, color:Int, fullText:String)
        {
            spannable.setSpan(ForegroundColorSpan(color), fullText.indexOf(text), fullText.indexOf(text) + text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        //item can be a Button or textView...
        fun setDrawable(item:Any, start:Drawable?,end:Drawable?,top:Drawable?,bottom:Drawable?){
            if(item is TextView)
                item.setCompoundDrawablesWithIntrinsicBounds(start,top,end,bottom)
        }



    }
}
