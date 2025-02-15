package com.example.cgpa

import android.content.Context

class Utility {
    companion object {

        fun deleteItem(itemInfo:ItemInfo, viewModel:SharedViewModel, context: Context)
        {
            viewModel.removeData(itemInfo)
            Helper.saveItemInfoList(viewModel.userData,context)
        }
    }
}
