package com.example.cgpa

import android.content.Context
import java.util.Calendar

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



    }
}
